package com.petitur.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.TextView;

import com.petitur.R;
import com.petitur.adapters.ImagesViewPagerAdapter;
import com.petitur.data.Family;
import com.petitur.resources.ImageSyncAsyncTaskLoader;
import com.petitur.resources.Utilities;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ShowFamilyProfileFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<Object>>,
        ImageSyncAsyncTaskLoader.OnImageSyncOperationsHandler,
        ImagesViewPagerAdapter.ImageClickHandler {


    //region Parameters
    private static final int SINGLE_OBJECT_IMAGES_SYNC_LOADER = 8521;
    @BindView(R.id.show_family_profile_pseudonym) TextView mTextViewFamilyPseudonym;
    @BindView(R.id.show_family_profile_address) TextView mTextViewFamilyAddress;
    @BindView(R.id.show_family_profile_value_experience) TextView mTextViewFamilyExperience;
    @BindView(R.id.show_family_profile_checkbox_foster) CheckBox mCheckBoxFoster;
    @BindView(R.id.show_family_profile_checkbox_adopt) CheckBox mCheckBoxAdopt;
    @BindView(R.id.show_family_profile_checkbox_foster_and_adopt) CheckBox mCheckBoxFosterAndAdopt;
    @BindView(R.id.show_family_profile_checkbox_want_to_help) CheckBox mCheckBoxWantToHelp;
    @BindView(R.id.show_family_profile_scroll_container) NestedScrollView mScrollContainer;
    @BindView(R.id.show_family_profile_image_viewpager) klogi.com.RtlViewPager mImageViewPager;
    @BindView(R.id.show_family_profile_image_indicator) CirclePageIndicator mImageIndicator;
    private Unbinder mBinding;
    private Family mFamily;
    private List<Uri> mDisplayedImageList;
    private String mSelectedImageUriString;
    private ImageSyncAsyncTaskLoader mImageSyncAsyncTaskLoader;
    private boolean mAlreadyLoadedImages;
    private int mScrollPosition;
    private int mSelectedImagePosition;
    //endregion


    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getExtras();
    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_show_family_profile, container, false);

        initializeViews(rootView);
        if (savedInstanceState!=null) restoreFragmentParameters(savedInstanceState);
        updateProfileFieldsOnScreen();
        displayImages();
        if (savedInstanceState==null) startImageSyncThread();

        return rootView;
    }
    @Override public void onAttach(Context context) {
        super.onAttach(context);
    }
    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mScrollPosition = mScrollContainer.getScrollY();
        outState.putInt(getString(R.string.saved_profile_scroll_position), mScrollPosition);
        outState.putParcelable(getString(R.string.family_profile_parcelable), mFamily);
        outState.putBoolean(getString(R.string.saved_profile_images_loaded_state), mAlreadyLoadedImages);
        mSelectedImagePosition = mImageViewPager.getCurrentItem();
        outState.putInt(getString(R.string.selected_profile_image_position), mSelectedImagePosition);
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        mBinding.unbind();
        if (mImageSyncAsyncTaskLoader!=null) mImageSyncAsyncTaskLoader.stopUpdatingImagesForObjects();
    }
    @Override public void onDetach() {
        super.onDetach();
        if (mImageSyncAsyncTaskLoader!=null) mImageSyncAsyncTaskLoader.stopUpdatingImagesForObjects();
    }


    //Structural methods
    private void getExtras() {
        if (getArguments() != null) {
            mFamily = getArguments().getParcelable(getString(R.string.family_profile_parcelable));
        }
    }
    private void initializeViews(View rootView) {
        mBinding = ButterKnife.bind(this, rootView);
        mSelectedImageUriString = Utilities.getImageUriForObjectWithFileProvider(getContext(), mFamily, "mainImage").toString();
    }
    private void restoreFragmentParameters(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
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

        mTextViewFamilyPseudonym.setText(mFamily.getPn());

        String address = Utilities.getAddressStringFromComponents(null, null, mFamily.getCt(), mFamily.getSe(), mFamily.getCn());
        mTextViewFamilyAddress.setText(address);
        mTextViewFamilyExperience.setText(mFamily.getXp());

        if (mFamily.getXp().equals("")) mTextViewFamilyExperience.setText(R.string.no_exp_shared);
        else mTextViewFamilyExperience.setText(mFamily.getXp());

        mCheckBoxFoster.setChecked(mFamily.getFD());
        mCheckBoxAdopt.setChecked(mFamily.getAD());
        mCheckBoxFosterAndAdopt.setChecked(mFamily.getFAD());
        mCheckBoxWantToHelp.setChecked(mFamily.getWTH());

        mScrollContainer.scrollTo(0, mScrollPosition);
    }
    private void displayImages() {
        if (getContext()==null) return;

        mDisplayedImageList = Utilities.getExistingImageUriListForObject(getContext(), mFamily, false);
        if (mDisplayedImageList.size()==0) mDisplayedImageList.add(Utilities.getGenericImageUri(mFamily));

        setupImagesViewPager();

        mSelectedImageUriString = mDisplayedImageList.get(0).toString();
    }
    private void startImageSyncThread() {

        mAlreadyLoadedImages = false;
        if (getActivity()!=null) {
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

    }
    private void shareFamilyProfile() {

        StringBuilder builder = new StringBuilder("");
        builder.append(mFamily.getPn());
        builder.append("\n");
        builder.append(Utilities.getAddressStringFromComponents(null, null, mFamily.getCt(), mFamily.getSe(), null));

        mSelectedImagePosition = mImageViewPager.getCurrentItem();
        mSelectedImageUriString = mDisplayedImageList.get(mSelectedImagePosition).toString();
        Uri imageUri = Utilities.getImageUriForObjectWithFileProvider(getContext(), mFamily, Utilities.getImageNameFromUri(mSelectedImageUriString));
        if (getActivity()!=null) Utilities.shareProfile(getActivity(), builder.toString(), imageUri);
    }


    //View click listeners
    @OnClick(R.id.family_profile_share_fab) public void shareProfile() {
        shareFamilyProfile();
    }


    //Communication with other activities/fragments:

    //Communication with RecyclerView adapters
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
            List<Family> familyList = new ArrayList<>();
            familyList.add(mFamily);
            mImageSyncAsyncTaskLoader =  new ImageSyncAsyncTaskLoader(getContext(), getString(R.string.task_sync_single_object_images),
                    getString(R.string.family_profile), null, familyList, null, this);
            return mImageSyncAsyncTaskLoader;
        }
        return new ImageSyncAsyncTaskLoader(getContext(), "", null, null, null, null, this);
    }
    @Override public void onLoadFinished(@NonNull Loader<List<Object>> loader, List<Object> data) {
        if (loader.getId() == SINGLE_OBJECT_IMAGES_SYNC_LOADER && !mAlreadyLoadedImages) {
            mAlreadyLoadedImages = true;
            if (getContext()!=null) displayImages();
        }
    }
    @Override public void onLoaderReset(@NonNull Loader<List<Object>> loader) {

    }

    //Communication with ImageSyncAsyncTaskLoader
    @Override public void onDisplayRefreshRequested() {
        if (getContext()!=null) displayImages();
    }

    //Communication with parent
    public void updateProfile(Family family) {
        mFamily = family;
    }
}
