package com.petitur.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.petitur.R;
import com.petitur.adapters.ImagesRecycleViewAdapter;
import com.petitur.data.Family;
import com.petitur.data.FirebaseDao;
import com.petitur.data.Foundation;
import com.petitur.data.MapMarker;
import com.petitur.data.Pet;
import com.petitur.data.User;
import com.petitur.resources.ImageSyncAsyncTaskLoader;
import com.petitur.resources.Utilities;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ShowPetProfileActivity extends BaseActivity implements
        ImagesRecycleViewAdapter.ImageClickHandler,
        ImageSyncAsyncTaskLoader.OnImageSyncOperationsHandler,
        LoaderManager.LoaderCallbacks<List<Pet>>,
        FirebaseDao.FirebaseOperationsHandler {


    //region Parameters
    @BindView(R.id.show_pet_profile_recyclerview_images) RecyclerView mRecyclerViewImages;
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
    private static final int SINGLE_OBJECT_IMAGES_SYNC_LOADER = 8520;
    Pet mPet;
    private Unbinder mBinding;
    private ImagesRecycleViewAdapter mImagesRecycleViewAdapter;
    private int mImagesRecyclerViewPosition;
    private List<Uri> mDisplayedImageList;
    private int mScrollPosition;
    private ImageSyncAsyncTaskLoader mImageSyncAsyncTaskLoader;
    private String mClickedImageUriString;
    private FirebaseDao mFirebaseDao;
    private Family mFamily;
    //endregion


    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pet_profile);

        getExtras();
        initializeParameters();
        updateProfileFieldsOnScreen();
        setupImagesRecyclerView();
        startImageSyncThread();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        if (mImageSyncAsyncTaskLoader!=null) mImageSyncAsyncTaskLoader.stopUpdatingImagesForObjects();
        mBinding.unbind();
    }
    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getString(R.string.pet_profile_parcelable), mPet);
        mScrollPosition = mScrollContainer.getScrollY();
        outState.putInt(getString(R.string.container_scroll_position), mScrollPosition);
    }
    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPet = savedInstanceState.getParcelable(getString(R.string.pet_profile_parcelable));
        mScrollPosition = savedInstanceState.getInt(getString(R.string.container_scroll_position), 0);
    }
    @Override public void onBackPressed() {
        super.onBackPressed();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_pet_menu, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        switch (itemThatWasClickedId) {
            case android.R.id.home:
                Intent data = new Intent();
                data.putExtra(getString(R.string.pet_profile_parcelable), mPet);
                data.putExtra(getString(R.string.family_profile_parcelable), mFamily);
                setResult(RESULT_OK, data);
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //Functionality methods
    private void getExtras() {
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.pet_profile_parcelable))) {
            mPet = intent.getParcelableExtra(getString(R.string.pet_profile_parcelable));
        }
        if (intent.hasExtra(getString(R.string.family_profile_parcelable))) {
            mFamily = intent.getParcelableExtra(getString(R.string.family_profile_parcelable));
        }
    }
    private void initializeParameters() {

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.find_a_pet);
        }

        mBinding =  ButterKnife.bind(this);
        Utilities.hideSoftKeyboard(this);
        mFirebaseDao = new FirebaseDao(this, this);

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
                mFirebaseDao.updateObjectKeyValuePair(mFamily, "fpi", favoriteIds);
            }
        });
    }
    private void setupImagesRecyclerView() {
        mRecyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerViewImages.setNestedScrollingEnabled(true);
        mImagesRecycleViewAdapter = new ImagesRecycleViewAdapter(this, this, null);
        mRecyclerViewImages.setAdapter(mImagesRecycleViewAdapter);
        mRecyclerViewImages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mImagesRecyclerViewPosition = Utilities.getLinearRecyclerViewPosition(mRecyclerViewImages);
            }
        });
    }
    private void updateProfileFieldsOnScreen() {

        if (mPet==null) return;

        String gender = mPet.getGn();
        if (gender.equals("Male")) Picasso.with(this).load(R.drawable.ic_pet_gender_male_24dp).into(mTextViewPetGender);
        else Picasso.with(this).load(R.drawable.ic_pet_gender_female_24dp).into(mTextViewPetGender);

        mTextViewPetName.setText(mPet.getNm());
        mTextViewCity.setText(mPet.getCtL());

        mTextViewPetRace.setText(Utilities.getLocalizedPetBreed(getApplicationContext(), mPet));
        mTextViewPetAge.setText(Utilities.getLocalizedPetAge(getApplicationContext(), mPet));

        String displayableDistance = Utilities.convertDistanceToDisplayableValue(mPet.getDt()) + getString(R.string.unit_km);
        mTextViewDistance.setText(displayableDistance);

        String foundation = mPet.getFN();
        if (!TextUtils.isEmpty(foundation)) {
            //Make the foundation name a hyperlink
            SpannableString foundationSpan = new SpannableString(foundation);
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
        if(mPet==null) return;

        //Updating the images with the video links to display to the user
        mDisplayedImageList = Utilities.getExistingImageUriListForObject(this, mPet, false);
        if (mDisplayedImageList.size()==0) mDisplayedImageList.add(Utilities.getGenericImageUri(mPet));

        List<String> videoUrls = mPet.getVU();
        for (String videoUrl : videoUrls) {
            mDisplayedImageList.add(Uri.parse(videoUrl));
        }
        mImagesRecycleViewAdapter.setContents(mDisplayedImageList);

        if (mRecyclerViewImages!=null) {
            mRecyclerViewImages.scrollToPosition(mImagesRecyclerViewPosition);
        }

        mClickedImageUriString = mDisplayedImageList.get(0).toString();
    }
    private void openFoundationProfile() {
        Intent intent = new Intent(this, ShowFoundationProfileActivity.class);
        intent.putExtra(getString(R.string.foundation_profile_id), mPet.getOI());
        startActivity(intent);
    }
    private void playVideoInBrowser(int clickedItemIndex) {
        Utilities.goToWebLink(this, mDisplayedImageList.get(clickedItemIndex).toString());
    }
    private void startImageSyncThread() {

        LoaderManager loaderManager = getSupportLoaderManager();
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
    private void sharePetProfile() {

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);

        StringBuilder builder = new StringBuilder("");
        builder.append(mPet.getNm());
        builder.append("\n\n");
        builder.append("Address:\n");
        builder.append(Utilities.getAddressStringFromComponents(mPet.getStN(), mPet.getSt(), mPet.getCt(), mPet.getSe(), null));
        builder.append("\n\n");
        builder.append("Foundation info:\n");
        if (!TextUtils.isEmpty(mPet.getFN())) builder.append(mPet.getFN());
        if (!TextUtils.isEmpty(mPet.getAFCP())) { builder.append("\ntel. "); builder.append(mPet.getAFCP()); }
        shareIntent.putExtra(Intent.EXTRA_TEXT, builder.toString());

        Uri imageUri = Utilities.getImageUriForObjectWithFileProvider(this, mPet, Utilities.getImageNameFromUri(mClickedImageUriString));
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share images..."));

    }


    //View click listeners
    @OnClick(R.id.show_pet_profile_share_fab) public void onShareFabClick() {
        sharePetProfile();
    }


    //Communication with other classes:

    //Communication with RecyclerView adapters
    @Override public void onImageClick(int clickedItemIndex) {
        String clickedImageUriString = mDisplayedImageList.get(clickedItemIndex).toString();
        if (URLUtil.isNetworkUrl(clickedImageUriString)) {
            playVideoInBrowser(clickedItemIndex);
        }
        else {
            mClickedImageUriString = clickedImageUriString;
        }
    }

    //Communication with Loader
    @NonNull @Override public Loader<List<Pet>> onCreateLoader(int id, @Nullable Bundle args) {

        if (id== SINGLE_OBJECT_IMAGES_SYNC_LOADER) {
            List<Pet> pets = new ArrayList<>();
            pets.add(mPet);
            mImageSyncAsyncTaskLoader =  new ImageSyncAsyncTaskLoader(this, getString(R.string.task_sync_single_object_images),
                    getString(R.string.pet_profile), pets, null, null, this);
            return mImageSyncAsyncTaskLoader;
        }
        return new ImageSyncAsyncTaskLoader(this, "", null, null, null, null, this);
    }
    @Override public void onLoadFinished(@NonNull Loader<List<Pet>> loader, List<Pet> data) {
        if (loader.getId() == SINGLE_OBJECT_IMAGES_SYNC_LOADER) {
            displayImages();
        }
    }
    @Override public void onLoaderReset(@NonNull Loader<List<Pet>> loader) {

    }

    //Communication with ImageSyncAsyncTaskLoader
    @Override public void onDisplayRefreshRequested() {
        displayImages();
    }

    //Communication with Firebase Dao handler
    @Override public void onPetListFound(List<Pet> pets) {
    }
    @Override public void onFamilyListFound(List<Family> families) {
    }
    @Override public void onFoundationListFound(List<Foundation> foundations) {
    }
    @Override public void onUserListFound(List<User> users) {
    }
    @Override public void onMapMarkerListFound(List<MapMarker> mapMarkers) {

    }
    @Override public void onImageAvailable(boolean imageWasDownloaded, Uri imageUri, String imageName) {

    }
    @Override public void onImageUploaded(List<String> uploadTimes) {

    }
}
