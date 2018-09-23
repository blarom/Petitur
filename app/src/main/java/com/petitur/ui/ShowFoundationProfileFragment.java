package com.petitur.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.TextView;

import com.petitur.R;
import com.petitur.adapters.ImagesViewPagerAdapter;
import com.petitur.data.Foundation;
import com.petitur.resources.ImageSyncAsyncTaskLoader;
import com.petitur.resources.Utilities;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

//TODO: Add donations button
public class ShowFoundationProfileFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Object>,
        ImageSyncAsyncTaskLoader.OnImageSyncOperationsHandler,
        ImagesViewPagerAdapter.ImageClickHandler {


    //region Parameters
    private static final int SINGLE_OBJECT_IMAGES_SYNC_LOADER = 8522;
    @BindView(R.id.show_foundation_profile_foundation_name) TextView mTextViewFoundationName;
    @BindView(R.id.show_foundation_profile_address) TextView mTextViewFoundationAddress;
    @BindView(R.id.show_foundation_profile_phone_number) TextView mTextViewFoundationPhoneNumber;
    @BindView(R.id.show_foundation_profile_email) TextView mTextViewFoundationEmail;
    @BindView(R.id.show_foundation_profile_website) TextView mTextViewFoundationWebsite;
    @BindView(R.id.show_foundation_profile_scroll_container) NestedScrollView mScrollContainer;
    @BindView(R.id.show_foundation_profile_image_viewpager) klogi.com.RtlViewPager mImageViewPager;
    @BindView(R.id.show_foundation_profile_image_indicator) CirclePageIndicator mImageIndicator;
    private Unbinder mBinding;
    private Foundation mFoundation;
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
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_show_foundation_profile, container, false);

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
        outState.putParcelable(getString(R.string.foundation_profile_parcelable), mFoundation);
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
            mFoundation = getArguments().getParcelable(getString(R.string.foundation_profile_parcelable));
        }
    }
    private void initializeViews(View rootView) {
        mBinding = ButterKnife.bind(this, rootView);
        if (mFoundation!=null) mSelectedImageUriString = Utilities.getImageUriForObjectWithFileProvider(getContext(), mFoundation, "mainImage").toString();
    }
    private void restoreFragmentParameters(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mFoundation = savedInstanceState.getParcelable(getString(R.string.foundation_profile_parcelable));
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
        if (mFoundation==null) return;

        mTextViewFoundationName.setText(mFoundation.getNm());

        String address = Utilities.getAddressStringFromComponents(mFoundation.getStN(), mFoundation.getSt(), mFoundation.getCt(), mFoundation.getSe(), mFoundation.getCn());
        mTextViewFoundationAddress.setText(address);
        mTextViewFoundationPhoneNumber.setText(mFoundation.getCP());

        String foundationContactPhone = mFoundation.getCP();
        if (!TextUtils.isEmpty(foundationContactPhone)) {
            SpannableString foundationSpan = new SpannableString(foundationContactPhone);
            foundationSpan.setSpan(new URLSpan(""), 0, foundationSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTextViewFoundationPhoneNumber.setText(foundationSpan);
            mTextViewFoundationPhoneNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openPhoneDialer();
                }
            });
        }

        String foundationEmail = mFoundation.getCE();
        if (!TextUtils.isEmpty(foundationEmail)) {
            SpannableString foundationSpan = new SpannableString(foundationEmail);
            foundationSpan.setSpan(new URLSpan(""), 0, foundationSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTextViewFoundationEmail.setText(foundationSpan);
            mTextViewFoundationEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendAnEmail();
                }
            });
        }

        String foundationWebsite = mFoundation.getWb();
        if (!TextUtils.isEmpty(foundationWebsite)) {
            SpannableString foundationSpan = new SpannableString(foundationWebsite);
            foundationSpan.setSpan(new URLSpan(""), 0, foundationSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTextViewFoundationWebsite.setText(foundationSpan);
            mTextViewFoundationWebsite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openWebsite();
                }
            });
        }

        mScrollContainer.scrollTo(0, mScrollPosition);
    }
    private void displayImages() {
        if (getContext()==null) return;

        mDisplayedImageList = Utilities.getExistingImageUriListForObject(getContext(), mFoundation, false);
        if (mDisplayedImageList.size()==0) mDisplayedImageList.add(Utilities.getGenericImageUri(mFoundation));

        setupImagesViewPager();

        if (mDisplayedImageList.get(0)!=null) mSelectedImageUriString = mDisplayedImageList.get(0).toString();
    }
    private void openPhoneDialer() {
        //inspired by: https://stackoverflow.com/questions/36309049/how-to-open-dialer-on-phone-with-a-selected-number-in-android
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+mFoundation.getCP()));
        startActivity(intent);
    }
    private void sendAnEmail() {
        //inspired by: https://stackoverflow.com/questions/8701634/send-email-intent
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + mFoundation.getCE()));
        //emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        //emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(emailIntent, ""));
    }
    private void openWebsite() {
        String url = mFoundation.getWb();
        Utilities.goToWebLink(getContext(), url);
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
    private void shareFoundationProfile() {

        StringBuilder builder = new StringBuilder("");
        builder.append(mFoundation.getNm());
        builder.append("\n\n");
        builder.append("Address:\n");
        builder.append(Utilities.getAddressStringFromComponents(mFoundation.getStN(), mFoundation.getSt(), mFoundation.getCt(), mFoundation.getSe(), null));
        if (!TextUtils.isEmpty(mFoundation.getCP())) { builder.append("\ntel. "); builder.append(mFoundation.getCP()); }
        if (!TextUtils.isEmpty(mFoundation.getWb())) { builder.append("\n"); builder.append(mFoundation.getWb()); }
        if (!TextUtils.isEmpty(mFoundation.getCE())) { builder.append("\n"); builder.append(mFoundation.getCE()); }

        mSelectedImagePosition = mImageViewPager.getCurrentItem();
        mSelectedImageUriString = mDisplayedImageList.get(mSelectedImagePosition).toString();
        Uri imageUri = Utilities.getImageUriForObjectWithFileProvider(getContext(), mFoundation, Utilities.getImageNameFromUri(mSelectedImageUriString));
        if (getActivity()!=null) Utilities.shareProfile(getActivity(), builder.toString(), imageUri);
    }


    //View click listeners
    @OnClick(R.id.foundation_profile_share_fab) public void onShareFabClick() {
        shareFoundationProfile();
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
    @NonNull @Override public Loader<Object> onCreateLoader(int id, @Nullable Bundle args) {

        if (id== SINGLE_OBJECT_IMAGES_SYNC_LOADER) {
            List<Foundation> foundationList = new ArrayList<>();
            foundationList.add(mFoundation);
            mImageSyncAsyncTaskLoader =  new ImageSyncAsyncTaskLoader(getContext(), getString(R.string.task_sync_single_object_images),
                    getString(R.string.foundation_profile), null, null, foundationList, this);
            return mImageSyncAsyncTaskLoader;
        }
        return new ImageSyncAsyncTaskLoader(getContext(), "", null, null, null, null, this);
    }
    @Override public void onLoadFinished(@NonNull Loader<Object> loader, Object data) {
        if (loader.getId() == SINGLE_OBJECT_IMAGES_SYNC_LOADER && !mAlreadyLoadedImages) {
            mAlreadyLoadedImages = true;
            if (getContext()!=null) displayImages();
        }
    }
    @Override public void onLoaderReset(@NonNull Loader<Object> loader) {

    }

    //Communication with ImageSyncAsyncTaskLoader
    @Override public void onDisplayRefreshRequested() {
        if (getContext()!=null) displayImages();
    }

    //Communication with parent
    public void updateProfile(Foundation foundation) {
        mFoundation = foundation;
    }
}
