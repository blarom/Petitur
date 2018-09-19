package com.petitur.ui;

import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;
import com.petitur.R;
import com.petitur.adapters.ImagesRecycleViewAdapter;
import com.petitur.data.*;
import com.petitur.resources.GeoAdressLookupAsyncTaskLoader;
import com.petitur.resources.Utilities;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class UpdateFoundationActivity extends BaseActivity  implements
        FirebaseDao.FirebaseOperationsHandler,
        ImagesRecycleViewAdapter.ImageClickHandler,
        LoaderManager.LoaderCallbacks<Address> {


    //region Parameters
    private static final String DEBUG_TAG = "TinDog Update";
    @BindView(R.id.update_my_foundation_value_name)
    TextInputEditText mEditTextName;
    @BindView(R.id.update_my_foundation_value_contact_phone) TextInputEditText mEditTextContactPhone;
    @BindView(R.id.update_my_foundation_value_contact_email) TextInputEditText mEditTextContactEmail;
    @BindView(R.id.update_my_foundation_value_website) TextInputEditText mEditTextWebsite;
    @BindView(R.id.update_my_foundation_value_country) TextInputEditText mEditTextCountry;
    @BindView(R.id.update_my_foundation_value_state) TextInputEditText mEditTextState;
    @BindView(R.id.update_my_foundation_value_city) TextInputEditText mEditTextCity;
    @BindView(R.id.update_my_foundation_value_street) TextInputEditText mEditTextStreet;
    @BindView(R.id.update_my_foundation_value_street_number) TextInputEditText mEditTextStreetNumber;
    @BindView(R.id.update_my_foundation_image_main)
    ImageView mImageViewMain;
    @BindView(R.id.update_my_foundation_recyclerview_images)
    RecyclerView mRecyclerViewFoundationImages;
    @BindView(R.id.update_my_foundation_scroll_container)
    NestedScrollView mScrollViewContainer;
    private Unbinder mBinding;
    private Foundation mFoundation;
    private FirebaseDao mFirebaseDao;
    private ImagesRecycleViewAdapter mFoundationImagesRecycleViewAdapter;
    private String mImageName = "mainImage";
    private int mStoredFoundationImagesRecyclerViewPosition;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mCurrentFirebaseUser;
    private String mNameFromFirebase;
    private String mEmailFromFirebase;
    private Uri mPhotoUriFromFirebase;
    private String mFirebaseUid;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean[] mImagesReady;
    private int mScrollPosition;
    private Bundle mSavedInstanceState;
    private boolean mFoundationFound;
    private boolean mFoundationCriticalParametersSet;
    private Uri[] mTempImageUris;
    private boolean mCurrentlySyncingImages;
    private boolean mRequireOnlineSync;
    private boolean mAlreadyGotGeoAddress;
    //endregion


    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_foundation);

        mSavedInstanceState = savedInstanceState;
        getExtras();
        initializeParameters();
        if (mFoundation==null || TextUtils.isEmpty(mFoundation.getUI())) getFoundationProfileFromFirebase();
        updateLayoutWithFoundationData();
        updateFoundationWithUserInput();
        setupFoundationImagesRecyclerView();
        Utilities.displayObjectImageInImageView(getApplicationContext(), mFoundation, "mainImage", mImageViewMain);
    }
    @Override public void onStart() {
        super.onStart();
        setupFirebaseAuthentication();
    }
    @Override protected void onStop() {
        super.onStop();
        cleanUpListeners();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        mBinding.unbind();
        mFirebaseDao.removeListeners();
    }
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri croppedImageTempUri = result.getUri();
                boolean succeeded = Utilities.shrinkImageWithUri(getApplicationContext(), croppedImageTempUri, 300, 300);

                if (succeeded) {
                    Uri tempImageUri = Utilities.updateTempObjectImage(getApplicationContext(), croppedImageTempUri, mImageName);
                    if (tempImageUri==null) return;

                    mTempImageUris = Utilities.registerAndDisplayTempImage(
                            getApplicationContext(), tempImageUri, mTempImageUris, mImageName, mFoundation,
                            mImageViewMain, mFoundationImagesRecycleViewAdapter);
                }
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.update_foundation_menu, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        switch (itemThatWasClickedId) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_save:
                updateFoundationWithUserInput();
                mRequireOnlineSync = Utilities.overwriteLocalImagesWithTempImages(getApplicationContext(), mTempImageUris, mFoundation);
                mTempImageUris = new Uri[]{null, null, null, null, null, null, null};
                if (mRequireOnlineSync) mCurrentlySyncingImages = Utilities.startSyncingImagesIfNotAlreadySyncing(getApplicationContext(), mCurrentlySyncingImages, mFoundation, mFirebaseDao);

                if (mFoundationCriticalParametersSet) {
                    mFirebaseDao.updateObject(mFoundation);
                }
                else Toast.makeText(getApplicationContext(), R.string.family_not_saved, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_done:
                updateFoundationWithUserInput();
                mRequireOnlineSync = Utilities.overwriteLocalImagesWithTempImages(getApplicationContext(), mTempImageUris, mFoundation);
                mTempImageUris = new Uri[]{null, null, null, null, null, null, null};
                if (mRequireOnlineSync) mCurrentlySyncingImages = Utilities.startSyncingImagesIfNotAlreadySyncing(getApplicationContext(), mCurrentlySyncingImages, mFoundation, mFirebaseDao);

                if (mCurrentlySyncingImages) {
                    Toast.makeText(getApplicationContext(), R.string.please_wait_syncing_images, Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (mFoundationCriticalParametersSet) {
                    mFirebaseDao.updateObject(mFoundation);

                    //Return the updated Foundation to the TaskSelectionActivity
                    Intent data = new Intent();
                    data.putExtra(getString(R.string.foundation_profile_parcelable), mFoundation);
                    setResult(RESULT_OK, data);
                    finish();
                }
                else Toast.makeText(getApplicationContext(), R.string.foundation_not_saved, Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public void onSaveInstanceState(Bundle outState) {
        mStoredFoundationImagesRecyclerViewPosition = Utilities.getLinearRecyclerViewPosition(mRecyclerViewFoundationImages);
        outState.putInt(getString(R.string.profile_update_pet_images_rv_position), mStoredFoundationImagesRecyclerViewPosition);
        outState.putString(getString(R.string.profile_update_image_name), mImageName);
        mScrollPosition = mScrollViewContainer.getScrollY();
        outState.putInt(getString(R.string.scroll_position),mScrollPosition);
        outState.putString(getString(R.string.saved_firebase_email), mEmailFromFirebase);
        outState.putString(getString(R.string.saved_firebase_name), mNameFromFirebase);
        outState.putString(getString(R.string.saved_firebase_id), mFirebaseUid);
        outState.putBoolean(getString(R.string.critical_parameters_set), mFoundationCriticalParametersSet);
        updateFoundationWithUserInput();
        outState.putParcelable(getString(R.string.saved_profile), mFoundation);
        super.onSaveInstanceState(outState);

    }
    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mStoredFoundationImagesRecyclerViewPosition = savedInstanceState.getInt(getString(R.string.profile_update_pet_images_rv_position));
            mRecyclerViewFoundationImages.scrollToPosition(mStoredFoundationImagesRecyclerViewPosition);
            mImageName = savedInstanceState.getString(getString(R.string.profile_update_image_name));
            mFoundation = savedInstanceState.getParcelable(getString(R.string.saved_profile));
            mScrollPosition = savedInstanceState.getInt(getString(R.string.scroll_position));
            mEmailFromFirebase = savedInstanceState.getString(getString(R.string.saved_firebase_email));
            mNameFromFirebase = savedInstanceState.getString(getString(R.string.saved_firebase_name));
            mFirebaseUid = savedInstanceState.getString(getString(R.string.saved_firebase_id));
            mFoundationCriticalParametersSet = savedInstanceState.getBoolean(getString(R.string.critical_parameters_set));

            mScrollViewContainer.setScrollY(mScrollPosition);
            updateLayoutWithFoundationData();
            setupFoundationImagesRecyclerView();
            Utilities.displayObjectImageInImageView(getApplicationContext(), mFoundation, "mainImage", mImageViewMain);
        }
    }


    //Functional methods
    private void getExtras() {

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.foundation_profile_parcelable))) {
            mFoundation = intent.getParcelableExtra(getString(R.string.foundation_profile_parcelable));
        }
    }
    private void initializeParameters() {
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.foundation_profile);
        }
        mBinding =  ButterKnife.bind(this);
        mImagesReady = new boolean[]{false, false, false, false, false, false};
        mTempImageUris = new Uri[]{null, null, null, null, null, null, null};
        mFoundationFound = false;
        mFoundationCriticalParametersSet = false;
        if (mFoundation==null) mFoundation = new Foundation();
        mFirebaseDao = new FirebaseDao(getBaseContext(), this);
        mCurrentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentlySyncingImages = false;
        mAlreadyGotGeoAddress = false;

        if (mCurrentFirebaseUser != null) {
            mNameFromFirebase = mCurrentFirebaseUser.getDisplayName();
            mEmailFromFirebase = mCurrentFirebaseUser.getEmail();
            mPhotoUriFromFirebase = mCurrentFirebaseUser.getPhotoUrl();
            mFirebaseUid = mCurrentFirebaseUser.getUid();
            boolean emailVerified = mCurrentFirebaseUser.isEmailVerified();
        }
    }
    private void getFoundationProfileFromFirebase() {
        if (mCurrentFirebaseUser != null) {
            //Setting the requested Foundation's id
            mFoundation.setOI(mFirebaseUid);

            //Initializing the local parameters that depend on this family, used in the rest of the activity
            mImageName = "mainImage";

            //Getting the rest of the family's parameters
            if (!mFoundationFound) mFirebaseDao.requestObjectsWithConditions(mFoundation, Utilities.getQueryConditionsForSingleObjectSearchByOwnerId(this, mFoundation));
        }
    }
    private void updateLayoutWithFoundationData() {
        if (mEditTextName==null || mFoundation==null) return;

        mEditTextName.setText(mFoundation.getNm());
        mEditTextContactPhone.setText(mFoundation.getCP());
        mEditTextContactEmail.setText(mFoundation.getCE());
        mEditTextWebsite.setText(mFoundation.getWb());
        mEditTextCountry.setText(mFoundation.getCn());
        mEditTextState.setText(mFoundation.getSe());
        mEditTextCity.setText(mFoundation.getCt());
        mEditTextStreet.setText(mFoundation.getSt());
        mEditTextStreetNumber.setText(mFoundation.getStN());
        Utilities.hideSoftKeyboard(this);
    }
    private void setupFoundationImagesRecyclerView() {
        mRecyclerViewFoundationImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerViewFoundationImages.setNestedScrollingEnabled(true);
        List<Uri> uris = Utilities.getExistingImageUriListForObject(getApplicationContext(), mFoundation, true);
        mFoundationImagesRecycleViewAdapter = new ImagesRecycleViewAdapter(this, this, uris);
        mRecyclerViewFoundationImages.setAdapter(mFoundationImagesRecycleViewAdapter);
    }
    private void performImageCaptureAndCrop() {
        // start source picker (camera, gallery, etc..) to get image for cropping and then use the image in cropping activity
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this);
    }
    private void setupFirebaseAuthentication() {
        // Check if user is signed in (non-null) and update UI accordingly.
        mCurrentFirebaseUser = mFirebaseAuth.getCurrentUser();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mCurrentFirebaseUser = firebaseAuth.getCurrentUser();
                if (mCurrentFirebaseUser != null) {
                    // TinDogUser is signed in
                    Utilities.setAppPreferenceUserHasNotRefusedSignIn(getApplicationContext(), true);
                    Log.d(DEBUG_TAG, "onAuthStateChanged:signed_in:" + mCurrentFirebaseUser.getUid());
                    //getFoundationProfileFromFirebase();
                } else {
                    // TinDogUser is signed out
                    Log.d(DEBUG_TAG, "onAuthStateChanged:signed_out");
                    //Showing the sign-in screen
                    if (Utilities.getAppPreferenceUserHasNotRefusedSignIn(getApplicationContext())) {
                        mSavedInstanceState = null;
                        Utilities.showSignInScreen(UpdateFoundationActivity.this);
                    }
                }
            }
        };
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
    private void cleanUpListeners() {
        if (mFirebaseAuth!=null) mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }
    private void updateFoundationWithUserInput() {
        if (mFoundation==null) return;

        mFoundation.setOI(mFirebaseUid);
        mFoundation.setCP(mEditTextContactPhone.getText().toString());
        mFoundation.setCE(mEditTextContactEmail.getText().toString());
        mFoundation.setWb(mEditTextWebsite.getText().toString());
        mFoundation.setCn(mEditTextCountry.getText().toString());

        String name = mEditTextName.getText().toString();
        String country = mEditTextCountry.getText().toString();
        String state = mEditTextState.getText().toString();
        String city = mEditTextCity.getText().toString();
        String street = mEditTextStreet.getText().toString();
        String streeNumber = mEditTextStreetNumber.getText().toString();

        mFoundation.setNm(name);
        mFoundation.setCn(country);
        mFoundation.setCt(state);
        mFoundation.setCt(city);
        mFoundation.setSt(street);
        mFoundation.setStN(streeNumber);


        String addressString = Utilities.getAddressStringFromComponents(streeNumber, street, city, state, country);
        if (!mAlreadyGotGeoAddress) startGeoAddressLookupThread(addressString);

        mFoundation.setSt(mEditTextStreet.getText().toString());
        mFoundation.setStN(mEditTextStreetNumber.getText().toString());

        mFoundation.setUniqueIdentifierFromDetails();

        if (name.length() < 2 || country.length() < 2 || city.length() < 1) {
            mFoundationCriticalParametersSet = false;
        }
        else {
            mFoundationCriticalParametersSet = true;
        }

        if (TextUtils.isEmpty(mFoundation.getUI())) Log.i(DEBUG_TAG, "Error: TinDog Foundation has empty unique ID!");
    }
    private void startGeoAddressLookupThread(String addressString) {

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<Address> imageSyncAsyncTaskLoader = loaderManager.getLoader(Utilities.GEO_ADDRESS_LOOKUP_LOADER);
        Bundle args = new Bundle();
        args.putString(getString(R.string.bundled_address_string), addressString);
        if (!mAlreadyGotGeoAddress && imageSyncAsyncTaskLoader==null) {
            loaderManager.initLoader(Utilities.GEO_ADDRESS_LOOKUP_LOADER, args, this);
        }

    }


    //View click listeners
    @OnClick(R.id.update_my_foundation_button_choose_main_pic) public void onChooseMainPicButtonClick() {
        if (mFoundationCriticalParametersSet && !TextUtils.isEmpty(mFoundation.getUI())) {
            mImageName = "mainImage";
            performImageCaptureAndCrop();
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.must_save_profile_first, Toast.LENGTH_SHORT).show();
        }
    }
    @OnClick(R.id.update_my_foundation_button_upload_pics) public void onUploadPicsButtonClick() {
        if (mFoundationCriticalParametersSet && !TextUtils.isEmpty(mFoundation.getUI())) {

            List<Uri> uris = Utilities.getExistingImageUriListForObject(getApplicationContext(), mFoundation, true);
            if (uris.size() == 5) {
                Toast.makeText(getApplicationContext(), R.string.reached_max_images, Toast.LENGTH_SHORT).show();
            }
            else {
                mImageName = Utilities.getNameOfFirstAvailableImageInImagesList(getApplicationContext(), mFoundation);
                if (!TextUtils.isEmpty(mImageName)) performImageCaptureAndCrop();
                else Toast.makeText(getApplicationContext(), R.string.error_processing_request, Toast.LENGTH_SHORT).show();
            }

        }
        else {
            Toast.makeText(getApplicationContext(), R.string.must_save_profile_first, Toast.LENGTH_SHORT).show();
        }
    }


    //Communication with other activities/fragments:

    //Communication with RecyclerView adapters
    @Override public void onImageClick(int clickedItemIndex) {
        switch (clickedItemIndex) {
            case 0: mImageName = "image1"; break;
            case 1: mImageName = "image2"; break;
            case 2: mImageName = "image3"; break;
            case 3: mImageName = "image4"; break;
            case 4: mImageName = "image5"; break;
        }
        performImageCaptureAndCrop();
    }

    //Communication with Firebase Dao handler
    @Override public void onPetListFound(List<Pet> pets) {

    }
    @Override public void onFamilyListFound(List<Family> families) {
    }
    @Override public void onFoundationListFound(List<Foundation> foundations) {
        if (foundations == null) return;

        if (foundations.size() == 0 || foundations.get(0)==null) {
            mFoundationFound = false;
            mFoundation = (Foundation) mFirebaseDao.createObjectWithUIAndReturnIt(mFoundation);
            Toast.makeText(getBaseContext(), R.string.foundation_not_found_press_done_to_create, Toast.LENGTH_SHORT).show();
        }
        else {
            mFoundation = foundations.get(0);
            mFoundationFound = true;
            if (mSavedInstanceState==null) {
                updateLayoutWithFoundationData();
                updateFoundationWithUserInput();
            }
            mFirebaseDao.syncAllObjectImages(mFoundation);
        }

    }
    @Override public void onUserListFound(List<User> users) {

    }
    @Override public void onMapMarkerListFound(List<MapMarker> mapMarkers) {

    }
    @Override public void onImageAvailable(boolean imageWasDownloaded, Uri downloadedImageUri, String imageName) {

        if (mImageViewMain==null || mFoundationImagesRecycleViewAdapter==null || mFoundation==null) return;

        Utilities.synchronizeImageOnAllDevices(getApplicationContext(), mFoundation, mFirebaseDao, imageName, downloadedImageUri, imageWasDownloaded);

        //Displaying the images (Only showing the images if all images are ready (prevents image flickering))
        boolean allImagesFinishedSyncing = Utilities.checkIfImagesReadyForDisplay(mImagesReady, imageName);
        if (allImagesFinishedSyncing) {
            Utilities.displayAllAvailableImages(getApplicationContext(), mFoundation, mImageViewMain, mFoundationImagesRecycleViewAdapter);
            mCurrentlySyncingImages = false;
        }
        else {
            mCurrentlySyncingImages = true;
        }

    }
    @Override public void onImageUploaded(List<String> uploadTimes) {
        mFoundation.setIUT(uploadTimes);
    }

    //Communication with GeoAddressLookupLoader
    @NonNull @Override public Loader<Address> onCreateLoader(int id, @Nullable Bundle args) {
        String addressString = (args==null)? "" : args.getString(getString(R.string.bundled_address_string), "");
        return new GeoAdressLookupAsyncTaskLoader(this, addressString);
    }
    @Override public void onLoadFinished(@NonNull Loader<Address> loader, Address data) {
        if (data!=null ) {
            String geoAddressCountry = data.getCountryCode();
            double geoAddressLatitude = data.getLatitude();
            double geoAddressLongitude = data.getLongitude();

            mFoundation.setGaC(geoAddressCountry);
            mFoundation.setGeo(new GeoPoint(geoAddressLatitude, geoAddressLongitude));
        }
        getLoaderManager().destroyLoader(Utilities.GEO_ADDRESS_LOOKUP_LOADER);
    }
    @Override public void onLoaderReset(@NonNull Loader<Address> loader) {

    }
}
