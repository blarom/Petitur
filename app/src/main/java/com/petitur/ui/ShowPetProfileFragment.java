package com.petitur.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.petitur.R;
import com.petitur.adapters.ImagesViewPagerAdapter;
import com.petitur.data.Family;
import com.petitur.data.Foundation;
import com.petitur.data.Pet;
import com.petitur.resources.ImageSyncAsyncTaskLoader;
import com.petitur.resources.Utilities;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ShowPetProfileFragment extends Fragment implements
        ImageSyncAsyncTaskLoader.OnImageSyncOperationsHandler,
        LoaderManager.LoaderCallbacks<List<Object>>,
        ImagesViewPagerAdapter.ImageClickHandler {


    //region Parameters
    @BindView(R.id.show_pet_profile_name) TextView mTextViewPetName;
    @BindView(R.id.show_pet_profile_value_foundation) TextView mTextViewFoundation;
    @BindView(R.id.show_pet_profile_age) TextView mTextViewPetAge;
    @BindView(R.id.show_pet_profile_race) TextView mTextViewPetRace;
    @BindView(R.id.show_pet_profile_city) TextView mTextViewCity;
    @BindView(R.id.show_pet_profile_distance) TextView mTextViewDistance;
    @BindView(R.id.show_pet_profile_gender) ImageView mTextViewPetGender;
    @BindView(R.id.show_pet_profile_description) TextView mTextViewPetHistory;
    @BindView(R.id.show_pet_profile_love) ToggleButton mButtonFavorite;
    @BindView(R.id.show_pet_profile_scroll_container) NestedScrollView mScrollContainer;
    @BindView(R.id.show_pet_profile_image_viewpager) klogi.com.RtlViewPager mImageViewPager;
    @BindView(R.id.show_pet_profile_image_indicator) CirclePageIndicator mImageIndicator;
    private static final int SINGLE_OBJECT_IMAGES_SYNC_LOADER = 8520;
    private Unbinder mBinding;
    private List<Uri> mDisplayedImageList;
    private int mScrollPosition;
    private ImageSyncAsyncTaskLoader mImageSyncAsyncTaskLoader;
    private String mSelectedImageUriString;
    private Family mFamily;
    private Pet mPet;
    private int mSelectedImagePosition;
    private String mFullName;
    private Foundation mFoundation;
    private boolean mAlreadyLoadedImages;
    //endregion


    //Lifecycle methods
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getExtras();
        initializeParameters();
    }
    @Override public void onAttach(Context context) {
        super.onAttach(context);
        this.mPetProfileFragmentOperationsHandler = (PetProfileFragmentOperationsHandler) context;
    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_show_pet_profile, container, false);

        initializeViews(rootView);
        updateProfileFieldsOnScreen();
        startImageSyncThread();
        if (savedInstanceState!=null) restoreFragmentParameters(savedInstanceState);
        updateProfileFieldsOnScreen();
        displayImages();
        if (savedInstanceState==null) startImageSyncThread();

        return rootView;
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        mBinding.unbind();
        if (mImageSyncAsyncTaskLoader!=null) mImageSyncAsyncTaskLoader.stopUpdatingImagesForObjects();
    }
    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getString(R.string.pet_profile_parcelable), mPet);
        mScrollPosition = mScrollContainer.getScrollY();
        outState.putInt(getString(R.string.container_scroll_position), mScrollPosition);
        mSelectedImagePosition = mImageViewPager.getCurrentItem();
        outState.putInt(getString(R.string.selected_profile_image_position), mSelectedImagePosition);
        outState.putBoolean(getString(R.string.saved_profile_images_loaded_state), mAlreadyLoadedImages);
    }


    //Functionality methods
    private void getExtras() {
        if (getArguments() != null) {
            mPet = getArguments().getParcelable(getString(R.string.pet_profile_parcelable));
            mFamily = getArguments().getParcelable(getString(R.string.family_profile_parcelable));
            mFoundation = getArguments().getParcelable(getString(R.string.foundation_profile_parcelable));
            mFullName = getArguments().getString(getString(R.string.user_name));
        }
    }
    private void initializeParameters() {

        mDisplayedImageList = new ArrayList<>();

    }
    private void initializeViews(View rootView) {
        mBinding = ButterKnife.bind(this, rootView);

        mButtonFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isFavorited) {
                List<String> favoriteIds = mFamily.getFPI();
                if (favoriteIds==null) favoriteIds = new ArrayList<>();
                String currentPetUI = mPet.getUI();

                boolean isInFavoritesList = false;
                for (String id : favoriteIds) {
                    if (currentPetUI.equals(id)) {
                        mPet.setFv(isFavorited);
                        isInFavoritesList = true;
                        break;
                    }
                }

                if (isFavorited && !isInFavoritesList) favoriteIds.add(currentPetUI);
                else if (!isFavorited&& isInFavoritesList) favoriteIds.remove(currentPetUI);

                mFamily.setFPI(favoriteIds);
                mPetProfileFragmentOperationsHandler.onProfilesModified(mPet, mFamily);
                mPetProfileFragmentOperationsHandler.onFavoriteIdsModified(favoriteIds);
            }
        });
    }
    private void restoreFragmentParameters(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPet = savedInstanceState.getParcelable(getString(R.string.pet_profile_parcelable));
            mFamily = savedInstanceState.getParcelable(getString(R.string.family_profile_parcelable));
            mAlreadyLoadedImages = savedInstanceState.getBoolean(getString(R.string.saved_profile_images_loaded_state));
            mScrollPosition = savedInstanceState.getInt(getString(R.string.container_scroll_position), 0);
            mSelectedImagePosition =savedInstanceState.getInt(getString(R.string.selected_profile_image_position), 0);
        }
    }
    private void setupImagesViewPager() {

        ImagesViewPagerAdapter adapter = new ImagesViewPagerAdapter(getContext(), this, mDisplayedImageList);
        mImageViewPager.setAdapter(adapter);
        mImageIndicator.setViewPager(mImageViewPager);
        mImageViewPager.setCurrentItem(mSelectedImagePosition);

        final float density = getResources().getDisplayMetrics().density;
        mImageIndicator.setRadius(5 * density);
    }
    private void updateProfileFieldsOnScreen() {

        if (mPet==null || getContext()==null) return;

        String gender = mPet.getGn();
        if (gender.equals("Male")) Picasso.with(getContext()).load(R.drawable.ic_pet_gender_male_24dp).into(mTextViewPetGender);
        else Picasso.with(getContext()).load(R.drawable.ic_pet_gender_female_24dp).into(mTextViewPetGender);

        mTextViewPetName.setText(mPet.getNm());
        mTextViewCity.setText(mPet.getCtL());

        mTextViewPetRace.setText(Utilities.getLocalizedPetBreed(getContext(), mPet));
        mTextViewPetAge.setText(Utilities.getLocalizedPetAge(getContext(), mPet));

        String displayableDistance = Utilities.convertDistanceToDisplayableValue(mPet.getDt()) + getString(R.string.unit_km);
        mTextViewDistance.setText(displayableDistance);

        if (mFoundation!=null) {
            //Make the foundation name a hyperlink
            SpannableString foundationSpan = new SpannableString(mFoundation.getNm());
            foundationSpan.setSpan(new URLSpan(""), 0, foundationSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTextViewFoundation.setText(foundationSpan);
            mTextViewFoundation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openFoundationProfile();
                }
            });
        }

        if (mPet.getHs().equals("")) mTextViewPetHistory.setText(R.string.no_history_available);
        else mTextViewPetHistory.setText(mPet.getHs());

        mButtonFavorite.setChecked(mPet.getFv());

        mScrollContainer.scrollTo(0, mScrollPosition);

    }
    private void displayImages() {
        if(mPet==null || getContext()==null) return;

        //Updating the images with the video links to display to the user
        mDisplayedImageList = Utilities.getExistingImageUriListForObject(getContext(), mPet, false);
        if (mDisplayedImageList.size()==0) mDisplayedImageList.add(Utilities.getGenericImageUri(mPet));

        List<String> videoUrls = mPet.getVU();
        for (String videoUrl : videoUrls) {
            mDisplayedImageList.add(Uri.parse(videoUrl));
        }

        setupImagesViewPager();

        mSelectedImageUriString = mDisplayedImageList.get(0).toString();
    }
    private void openFoundationProfile() {
        //TODO fix the dialog pet type display
        Intent intent = new Intent(getContext(), SearchProfileActivity.class);
        if (mFoundation!=null) intent.putExtra(getString(R.string.foundation_profile_parcelable), mFoundation);
        else intent.putExtra(getString(R.string.foundation_profile_id), mPet.getOI());
        startActivity(intent);
    }
    private void startImageSyncThread() {
        if (getActivity()==null) return;

        mAlreadyLoadedImages = false;
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader<String> imageSyncAsyncTaskLoader = loaderManager.getLoader(SINGLE_OBJECT_IMAGES_SYNC_LOADER);
        if (imageSyncAsyncTaskLoader == null) {
            loaderManager.initLoader(SINGLE_OBJECT_IMAGES_SYNC_LOADER, null, this);
        }
        else {
            if (mImageSyncAsyncTaskLoader!=null) {
                mImageSyncAsyncTaskLoader.cancelLoadInBackground();
                mImageSyncAsyncTaskLoader = null;
            }
            loaderManager.restartLoader(SINGLE_OBJECT_IMAGES_SYNC_LOADER, null, this);
        }

    }
    private void showShareOptionsDialog() {
        if (getContext()==null) return;

        //region Get the dialog view
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_choose_share_options, null);
        final CheckBox shareMyFullName = dialogView.findViewById(R.id.dialog_choose_share_options_my_name);
        final CheckBox shareMyPhone = dialogView.findViewById(R.id.dialog_choose_share_options_my_phone);
        final CheckBox shareMyEmail = dialogView.findViewById(R.id.dialog_choose_share_options_my_email);
        final CheckBox shareFamilyDetails = dialogView.findViewById(R.id.dialog_choose_share_options_my_family_profile_details);
        final CheckBox shareFoundationDetails = dialogView.findViewById(R.id.dialog_choose_share_options_pet_foundation_details);
        final EditText additionalDetails = dialogView.findViewById(R.id.dialog_choose_share_options_free_text);
        additionalDetails.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        //endregion

        //region Building the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.vet_event);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                StringBuilder builder = new StringBuilder("");

                builder.append(getString(R.string.pet_s_name_));
                builder.append(mPet.getNm());

                builder.append("\n");
                builder.append(getString(R.string.id_));
                builder.append(mPet.getUI());

                builder.append("\n");
                builder.append(getString(R.string.address_));
                builder.append(Utilities.getAddressStringFromComponents(mPet.getStN(), mPet.getSt(), mPet.getCt(), mPet.getSe(), null));

                if (shareMyFullName.isChecked()) {
                    builder.append("\n\n");
                    builder.append(getString(R.string.my_name_));
                    builder.append((TextUtils.isEmpty(mFullName))? getString(R.string.no_name_available) : mFullName);
                }
                if (shareMyPhone.isChecked()) {
                    builder.append("\n");
                    builder.append(getString(R.string.my_tel_));
                    builder.append(TextUtils.isEmpty(mFamily.getCp())? getString(R.string.no_tel_available) : mFamily.getCp());
                }
                if (shareMyEmail.isChecked()) {
                    builder.append("\n");
                    builder.append(getString(R.string.my_email_));
                    builder.append(TextUtils.isEmpty(mFamily.getEm())? getString(R.string.no_email_registered) : mFamily.getEm());
                }
                if (shareFamilyDetails.isChecked()) {
                    builder.append("\n\n");
                    builder.append(getString(R.string.family_details));
                    builder.append("\n");
                    builder.append(getString(R.string.name_));
                    builder.append(TextUtils.isEmpty(mFamily.getPn())? getString(R.string.no_name_registered) : mFamily.getPn());
                    builder.append("\n");
                    builder.append(getString(R.string.id_));
                    builder.append(mFamily.getUI());
                }
                if (shareFoundationDetails.isChecked()) {
                    builder.append("\n\n");
                    builder.append(getString(R.string.organization_details));
                    builder.append("\n");
                    if (mFoundation==null) {
                        builder.append(getString(R.string.no_details_available));
                    }
                    else {
                        builder.append(getString(R.string.name_));
                        builder.append(TextUtils.isEmpty(mFoundation.getNm()) ? getString(R.string.no_name_registered) : mFoundation.getNm());
                        builder.append("\n");
                        builder.append(getString(R.string.tel_));
                        builder.append(TextUtils.isEmpty(mFoundation.getCP()) ? getString(R.string.no_tel_available) : mFoundation.getCP());
                        builder.append("\n");
                        builder.append(getString(R.string.email_));
                        builder.append(TextUtils.isEmpty(mFoundation.getCE()) ? getString(R.string.no_email_registered) : mFoundation.getCE());
                        builder.append("\n");
                        builder.append(getString(R.string.website_));
                        builder.append(TextUtils.isEmpty(mFoundation.getWb()) ? getString(R.string.website_no_registered) : mFoundation.getWb());
                    }
                }

                String additionalDetailsString = additionalDetails.getText().toString();
                if (!additionalDetailsString.equals("")) {
                    builder.append("\n\n");
                    builder.append(additionalDetailsString);
                }

                mSelectedImagePosition = mImageViewPager.getCurrentItem();
                mSelectedImageUriString = mDisplayedImageList.get(mSelectedImagePosition).toString();
                Uri imageUri = Utilities.getImageUriForObjectWithFileProvider(getContext(), mPet, Utilities.getImageNameFromUri(mSelectedImageUriString));
                if (getActivity()!=null) Utilities.shareProfile(getActivity(), builder.toString(), imageUri);

                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(getString(R.string.reset), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                shareMyFullName.setChecked(false);
                shareMyPhone.setChecked(false);
                shareMyEmail.setChecked(false);
                shareFamilyDetails.setChecked(false);
                shareFoundationDetails.setChecked(true);
                additionalDetails.setText("");
            }
        });
        //endregion

        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    //View click listeners
    @OnClick(R.id.show_pet_profile_share_fab) public void onShareFabClick() {
        showShareOptionsDialog();
    }


    //Communication with other classes:

    //Communication with image adapter
    @Override public void onImageClick(int clickedItemIndex) {
        String clickedImageUriString = mDisplayedImageList.get(clickedItemIndex).toString();
        if (URLUtil.isNetworkUrl(clickedImageUriString)) {
            Utilities.goToWebLink(getContext(), mDisplayedImageList.get(clickedItemIndex).toString());
        }
        else {
            mSelectedImageUriString = clickedImageUriString;
        }
    }

    //Communication with Loader
    @NonNull @Override public Loader<List<Object>> onCreateLoader(int id, @Nullable Bundle args) {

        if (id== SINGLE_OBJECT_IMAGES_SYNC_LOADER) {
            List<Pet> pets = new ArrayList<>();
            pets.add(mPet);
            mImageSyncAsyncTaskLoader =  new ImageSyncAsyncTaskLoader(getContext(), getString(R.string.task_sync_single_object_images),
                    getString(R.string.pet_profile), pets, null, null, this);
            return mImageSyncAsyncTaskLoader;
        }
        return new ImageSyncAsyncTaskLoader(getContext(), "", null, null, null, null, this);
    }
    @Override public void onLoadFinished(@NonNull Loader<List<Object>> loader, List<Object> data) {
        if (loader.getId() == SINGLE_OBJECT_IMAGES_SYNC_LOADER && !mAlreadyLoadedImages) {
            mAlreadyLoadedImages = true;
            displayImages();
        }
    }
    @Override public void onLoaderReset(@NonNull Loader<List<Object>> loader) {

    }

    //Communication with ImageSyncAsyncTaskLoader
    @Override public void onDisplayRefreshRequested() {
        //displayImages();
    }

    //Communication with parent
    public void updateProfile(Pet pet) {
        mPet = pet;
        updateProfileFieldsOnScreen();
    }
    public void updateProfile(Foundation foundation) {
        mFoundation = foundation;
        updateProfileFieldsOnScreen();
    }
    private PetProfileFragmentOperationsHandler mPetProfileFragmentOperationsHandler;
    public interface PetProfileFragmentOperationsHandler {
        void onProfilesModified(Pet pet, Family family);
        void onFavoriteIdsModified(List<String> favoriteIds);
    }
}
