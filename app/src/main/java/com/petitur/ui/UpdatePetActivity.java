package com.petitur.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.petitur.R;
import com.petitur.adapters.ImagesRecycleViewAdapter;
import com.petitur.adapters.SimpleTextRecycleViewAdapter;
import com.petitur.data.Family;
import com.petitur.data.FirebaseDao;
import com.petitur.data.Foundation;
import com.petitur.data.MapMarker;
import com.petitur.data.Pet;
import com.petitur.data.User;
import com.petitur.resources.Utilities;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class UpdatePetActivity extends AppCompatActivity implements
        FirebaseDao.FirebaseOperationsHandler,
        AdapterView.OnItemSelectedListener,
        SimpleTextRecycleViewAdapter.TextClickHandler,
        ImagesRecycleViewAdapter.ImageClickHandler {


    //region Parameters
    private static final String DEBUG_TAG = "TinDog Update";
    @BindView(R.id.update_pet_button_choose_main_pic) Button mButtonChooseMainPic;
    @BindView(R.id.update_pet_button_upload_pics) Button mButtonUploadPics;
    @BindView(R.id.update_pet_button_add_video_link) Button mButtonAddVideoLink;
    @BindView(R.id.update_pet_value_name) TextInputEditText mEditTextName;
    @BindView(R.id.update_pet_value_foundation) TextInputEditText mEditTextFoundation;
    @BindView(R.id.update_pet_value_country) TextInputEditText mEditTextCountry;
    @BindView(R.id.update_pet_value_state) TextInputEditText mEditTextState;
    @BindView(R.id.update_pet_value_city) TextInputEditText mEditTextCity;
    @BindView(R.id.update_pet_value_street) TextInputEditText mEditTextStreet;
    @BindView(R.id.update_pet_value_street_number) TextInputEditText mEditTextStreetNumber;
    @BindView(R.id.update_pet_value_history) TextInputEditText mEditTextHistory;
    @BindView(R.id.update_pet_image_main) ImageView mImageViewMain;
    @BindView(R.id.update_pet_recyclerview_video_links) RecyclerView mRecyclerViewVideoLinks;
    @BindView(R.id.update_pet_recyclerview_images) RecyclerView mRecyclerViewDogImages;
    @BindView(R.id.update_pet_age_spinner) Spinner mSpinnerAge;
    @BindView(R.id.update_pet_size_spinner) Spinner mSpinnerSize;
    @BindView(R.id.update_pet_gender_spinner) Spinner mSpinnerGender;
    @BindView(R.id.update_pet_race_spinner) Spinner mSpinnerRace;
    @BindView(R.id.update_pet_behavior_spinner) Spinner mSpinnerBehavior;
    @BindView(R.id.update_pet_interactions_spinner) Spinner mSpinnerInteractions;
    @BindView(R.id.update_pet_scroll_container) NestedScrollView mScrollViewContainer;
    private ArrayAdapter<CharSequence> mSpinnerAdapterAge;
    private ArrayAdapter<CharSequence> mSpinnerAdapterSize;
    private ArrayAdapter<CharSequence> mSpinnerAdapterGender;
    private ArrayAdapter<CharSequence> mSpinnerAdapterRace;
    private ArrayAdapter<CharSequence> mSpinnerAdapterBehavior;
    private ArrayAdapter<CharSequence> mSpinnerAdapterInteractions;
    private int mAgeSpinnerPosition;
    private int mSizeSpinnerPosition;
    private int mGenderSpinnerPosition;
    private int mRaceSpinnerPosition;
    private int mBehaviorSpinnerPosition;
    private int mInteractionsSpinnerPosition;
    private Pet mPet;
    private FirebaseDao mFirebaseDao;
    private ImagesRecycleViewAdapter mPetImagesRecycleViewAdapter;
    private String mImageName = "mainImage";
    private int mStoredDogImagesRecyclerViewPosition;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mCurrentFirebaseUser;
    private String mNameFromFirebase;
    private String mEmailFromFirebase;
    private Uri mPhotoUriFromFirebase;
    private String mChosenPetId;
    private String mFirebaseUid;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean mPetCriticalParametersSet;
    private boolean mPetAlreadyExistsInFirebaseDb;
    private Unbinder mBinding;
    private SimpleTextRecycleViewAdapter mVideoLinksRecycleViewAdapter;
    private List<String> mVideoLinks;
    private boolean[] mImagesReady;
    private Bundle mSavedInstanceState;
    private String mFoundationName;
    private String mFoundationCity;
    private String mFoundationCountry;
    private String mFoundationStreet;
    private String mFoundationStreetNumber;
    private int mScrollPosition;
    private String mFoundationId;
    private Uri[] mTempImageUris;
    private boolean mCurrentlySyncingImages;
    private boolean mRequireOnlineSync;
    //endregion


    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pet);

        mSavedInstanceState = savedInstanceState;
        getExtras();
        initializeParameters();
        if (savedInstanceState==null) getFoundationAndPetProfilesFromFirebase();
        setupVideoLinksRecyclerView();
        setupDogImagesRecyclerView();
        Utilities.displayObjectImageInImageView(getApplicationContext(), mPet, "mainImage", mImageViewMain);
    }
    @Override public void onStart() {
        super.onStart();
        setupFirebaseAuthentication();
    }
    @Override protected void onStop() {
        super.onStop();
        if (mFirebaseAuth!=null) mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        mBinding.unbind();
        removeListeners();
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
                            getApplicationContext(), tempImageUri, mTempImageUris, mImageName, mPet,
                            mImageViewMain, mPetImagesRecycleViewAdapter);
                }
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        if (requestCode == Utilities.FIREBASE_SIGN_IN_KEY) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                mCurrentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (!mPetAlreadyExistsInFirebaseDb) getFoundationAndPetProfilesFromFirebase();
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.update_pet_menu, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        switch (itemThatWasClickedId) {
            case android.R.id.home:
                setResult(Activity.RESULT_OK, new Intent());
                this.finish();
                return true;
            case R.id.action_save:
                updatePetWithFoundationData();
                updatePetWithUserInput();
                mRequireOnlineSync = Utilities.overwriteLocalImagesWithTempImages(getApplicationContext(), mTempImageUris, mPet);
                mTempImageUris = new Uri[]{null, null, null, null, null, null, null};
                if (mRequireOnlineSync) mCurrentlySyncingImages = Utilities.startSyncingImagesIfNotAlreadySyncing(getApplicationContext(), mCurrentlySyncingImages, mPet, mFirebaseDao);

                if (mPetCriticalParametersSet) {
                    if (!mPetAlreadyExistsInFirebaseDb) {
                        mPet = (Pet) mFirebaseDao.createObjectWithUIAndReturnIt(mPet);
                        mPetAlreadyExistsInFirebaseDb = true;
                    }
                    else mFirebaseDao.updateObject(mPet);
                }
                else Toast.makeText(getApplicationContext(), R.string.pet_not_saved, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_done:
                updatePetWithFoundationData();
                updatePetWithUserInput();
                mRequireOnlineSync = Utilities.overwriteLocalImagesWithTempImages(getApplicationContext(), mTempImageUris, mPet);
                mTempImageUris = new Uri[]{null, null, null, null, null, null, null};
                if (mRequireOnlineSync) mCurrentlySyncingImages = Utilities.startSyncingImagesIfNotAlreadySyncing(getApplicationContext(), mCurrentlySyncingImages, mPet, mFirebaseDao);

                if (mCurrentlySyncingImages) {
                    Toast.makeText(getApplicationContext(), R.string.please_wait_syncing_images, Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (mPetCriticalParametersSet) {
                    if (!mPetAlreadyExistsInFirebaseDb) {
                        mPet = (Pet) mFirebaseDao.createObjectWithUIAndReturnIt(mPet);
                    }
                    else mFirebaseDao.updateObject(mPet);
                    setResult(Activity.RESULT_OK, new Intent());
                    finish();
                }
                else Toast.makeText(getApplicationContext(), R.string.pet_not_saved, Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public void onSaveInstanceState(Bundle outState) {
        mStoredDogImagesRecyclerViewPosition = Utilities.getLinearRecyclerViewPosition(mRecyclerViewDogImages);
        mScrollPosition = mScrollViewContainer.getScrollY();
        outState.putInt(getString(R.string.saved_scroll_position),mScrollPosition);
        outState.putInt(getString(R.string.profile_update_pet_images_rv_position), mStoredDogImagesRecyclerViewPosition);
        outState.putString(getString(R.string.profile_update_image_name), mImageName);
        outState.putString(getString(R.string.saved_foundation_name), mFoundationName);
        outState.putString(getString(R.string.saved_foundation_city), mFoundationCity);
        outState.putString(getString(R.string.saved_foundation_id), mFoundationId);
        outState.putString(getString(R.string.saved_foundation_country), mFoundationCountry);
        outState.putString(getString(R.string.saved_foundation_street), mFoundationStreet);
        outState.putString(getString(R.string.saved_foundation_street_number), mFoundationStreetNumber);
        outState.putBoolean(getString(R.string.critical_parameters_set), mPetCriticalParametersSet);
        updatePetWithUserInput();
        outState.putParcelable(getString(R.string.saved_profile), mPet);
        super.onSaveInstanceState(outState);

    }
    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mStoredDogImagesRecyclerViewPosition = savedInstanceState.getInt(getString(R.string.profile_update_pet_images_rv_position));
            mRecyclerViewDogImages.scrollToPosition(mStoredDogImagesRecyclerViewPosition);
            mImageName = savedInstanceState.getString(getString(R.string.profile_update_image_name));
            mFoundationName = savedInstanceState.getString(getString(R.string.saved_foundation_name));
            mFoundationCity = savedInstanceState.getString(getString(R.string.saved_foundation_city));
            mFoundationId = savedInstanceState.getString(getString(R.string.saved_foundation_id));
            mFoundationCountry = savedInstanceState.getString(getString(R.string.saved_foundation_country));
            mFoundationStreet = savedInstanceState.getString(getString(R.string.saved_foundation_street));
            mFoundationStreetNumber = savedInstanceState.getString(getString(R.string.saved_foundation_street_number));
            mPet = savedInstanceState.getParcelable(getString(R.string.saved_profile));
            mScrollPosition = savedInstanceState.getInt(getString(R.string.saved_scroll_position));
            mPetCriticalParametersSet = savedInstanceState.getBoolean(getString(R.string.critical_parameters_set));

            mScrollViewContainer.setScrollY(mScrollPosition);
            updateLayoutWithFoundationData();
            updateLayoutWithPetData();
            setupDogImagesRecyclerView();
            Utilities.displayObjectImageInImageView(getApplicationContext(), mPet, "mainImage", mImageViewMain);
        }
    }


    //Functional methods
    private void getExtras() {
        Intent intent = getIntent();
        if (getIntent().hasExtra(getString(R.string.selected_pet_id))) {
            mChosenPetId = intent.getStringExtra(getString(R.string.selected_pet_id));
        }
    }
    private void initializeParameters() {
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.pet_profile);
        }

        mBinding =  ButterKnife.bind(this);
        mPetAlreadyExistsInFirebaseDb = false;
        mPetCriticalParametersSet = false;
        mImagesReady = new boolean[]{false, false, false, false, false, false};
        mTempImageUris = new Uri[]{null, null, null, null, null, null, null};
        mEditTextFoundation.setEnabled(false);
        mPet = new Pet();
        mVideoLinks = new ArrayList<>();
        mCurrentlySyncingImages = false;

        mFirebaseDao = new FirebaseDao(getBaseContext(), this);
        mCurrentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mSpinnerAdapterAge = ArrayAdapter.createFromResource(this, R.array.pet_age_array_simple, android.R.layout.simple_spinner_item);
        mSpinnerAdapterAge.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerAge.setAdapter(mSpinnerAdapterAge);
        mSpinnerAge.setOnItemSelectedListener(this);

        mSpinnerAdapterSize = ArrayAdapter.createFromResource(this, R.array.pet_size_array_simple, android.R.layout.simple_spinner_item);
        mSpinnerAdapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSize.setAdapter(mSpinnerAdapterSize);
        mSpinnerSize.setOnItemSelectedListener(this);

        mSpinnerAdapterGender = ArrayAdapter.createFromResource(this, R.array.pet_gender_array_simple, android.R.layout.simple_spinner_item);
        mSpinnerAdapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerGender.setAdapter(mSpinnerAdapterGender);
        mSpinnerGender.setOnItemSelectedListener(this);

        mSpinnerAdapterRace = ArrayAdapter.createFromResource(this, R.array.pet_race_array_simple, android.R.layout.simple_spinner_item);
        mSpinnerAdapterRace.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerRace.setAdapter(mSpinnerAdapterRace);
        mSpinnerRace.setOnItemSelectedListener(this);

        mSpinnerAdapterBehavior = ArrayAdapter.createFromResource(this, R.array.pet_behavior_array_simple, android.R.layout.simple_spinner_item);
        mSpinnerAdapterBehavior.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerBehavior.setAdapter(mSpinnerAdapterBehavior);
        mSpinnerBehavior.setOnItemSelectedListener(this);

        mSpinnerAdapterInteractions = ArrayAdapter.createFromResource(this, R.array.pet_interactions_array_simple, android.R.layout.simple_spinner_item);
        mSpinnerAdapterInteractions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerInteractions.setAdapter(mSpinnerAdapterInteractions);
        mSpinnerInteractions.setOnItemSelectedListener(this);

    }
    private void getFoundationAndPetProfilesFromFirebase() {
        if (mCurrentFirebaseUser != null && !mPetAlreadyExistsInFirebaseDb) {
            // Name, email address, and profile photo Url
            mNameFromFirebase = mCurrentFirebaseUser.getDisplayName();
            mEmailFromFirebase = mCurrentFirebaseUser.getEmail();
            mPhotoUriFromFirebase = mCurrentFirebaseUser.getPhotoUrl();
            mFirebaseUid = mCurrentFirebaseUser.getUid();

            //Initializing the local parameters that depend on this pet, used in the rest of the activity
            mImageName = "mainImage";

            //Getting the foundation details
            Foundation foundation = new Foundation(mFirebaseUid);
            mFirebaseDao.requestObjectsWithConditions(foundation, Utilities.getQueryConditionsForSingleObjectSearchByOwnerId(foundation));

            //Getting the pet details
            if (!TextUtils.isEmpty(mChosenPetId)) {
                mPet.setUI(mChosenPetId);
                mFirebaseDao.requestObjectWithId(mPet);
            }
        }
    }
    private void updateLayoutWithPetData() {
        //mEditTextFoundation.setText(mDog.getFN());
        mEditTextName.setText(mPet.getNm());
        mEditTextCountry.setText(mPet.getCn());
        mEditTextState.setText(mPet.getSe());
        mEditTextCity.setText(mPet.getCt());
        mEditTextStreet.setText(mPet.getSt());
        mEditTextStreetNumber.setText(mPet.getStN());
        mEditTextHistory.setText(mPet.getHs());

        mAgeSpinnerPosition = Utilities.getSpinnerPositionFromText(mSpinnerAge, mPet.getAg());
        mSizeSpinnerPosition = Utilities.getSpinnerPositionFromText(mSpinnerSize, mPet.getSz());
        mGenderSpinnerPosition = Utilities.getSpinnerPositionFromText(mSpinnerGender, mPet.getGn());
        mRaceSpinnerPosition = Utilities.getSpinnerPositionFromText(mSpinnerRace, mPet.getRc());
        mBehaviorSpinnerPosition = Utilities.getSpinnerPositionFromText(mSpinnerBehavior, mPet.getBh());
        mInteractionsSpinnerPosition = Utilities.getSpinnerPositionFromText(mSpinnerInteractions, mPet.getIt());

        mSpinnerAge.setSelection(mAgeSpinnerPosition);
        mSpinnerSize.setSelection(mSizeSpinnerPosition);
        mSpinnerGender.setSelection(mGenderSpinnerPosition);
        mSpinnerRace.setSelection(mRaceSpinnerPosition);
        mSpinnerBehavior.setSelection(mBehaviorSpinnerPosition);
        mSpinnerInteractions.setSelection(mInteractionsSpinnerPosition);

        mVideoLinksRecycleViewAdapter.setContents(mPet.getVU());

        Utilities.hideSoftKeyboard(this);
    }
    private void setupVideoLinksRecyclerView() {
        mRecyclerViewVideoLinks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        mRecyclerViewVideoLinks.setNestedScrollingEnabled(false);
        mVideoLinksRecycleViewAdapter = new SimpleTextRecycleViewAdapter(this, this, null);
        mRecyclerViewVideoLinks.setAdapter(mVideoLinksRecycleViewAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                mVideoLinks = mPet.getVU();
                mVideoLinks.remove(viewHolder.getLayoutPosition());
                mVideoLinksRecycleViewAdapter.setContents(mVideoLinks);
                //mVideoLinksRecycleViewAdapter.notifyItemRemoved(viewHolder.getLayoutPosition());
                mPet.setVU(mVideoLinks);
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerViewVideoLinks);
    }
    private void setupDogImagesRecyclerView() {
        mRecyclerViewDogImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerViewDogImages.setNestedScrollingEnabled(true);
        List<Uri> uris = Utilities.getExistingImageUriListForObject(getApplicationContext(), mPet, true);
        mPetImagesRecycleViewAdapter = new ImagesRecycleViewAdapter(this, this, uris);
        mRecyclerViewDogImages.setAdapter(mPetImagesRecycleViewAdapter);
    }
    private void showVideoLinkDialog() {

        //Get the dialog view
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_enter_video_link, null);
        final EditText inputText = dialogView.findViewById(R.id.input_text_video_link);

        //Building the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_video_link);
        inputText.setText("");

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                List<String> videoUrls = mPet.getVU();
                videoUrls.add(inputText.getText().toString());
                mVideoLinksRecycleViewAdapter.setContents(videoUrls);
                mPet.setVU(videoUrls);

                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) builder.setView(dialogView);
        else builder.setMessage(R.string.device_version_too_low);

        AlertDialog dialog = builder.create();
        dialog.show();
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
                } else {
                    // TinDogUser is signed out
                    Log.d(DEBUG_TAG, "onAuthStateChanged:signed_out");
                    //Showing the sign-in screen
                    if (Utilities.getAppPreferenceUserHasNotRefusedSignIn(getApplicationContext())) {
                        mSavedInstanceState = null;
                        Utilities.showSignInScreen(UpdatePetActivity.this);
                    }
                }
            }
        };
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
    private void updatePetWithUserInput() {

        String name = mEditTextName.getText().toString();
        String country = mEditTextCountry.getText().toString();
        String state = mEditTextState.getText().toString();
        String city = mEditTextCity.getText().toString();
        String street = mEditTextStreet.getText().toString();
        String streeNumber = mEditTextStreetNumber.getText().toString();

        mPet.setNm(name);
        mPet.setCn(country);
        mPet.setSe(state);
        mPet.setCt(city);
        mPet.setSt(street);
        mPet.setStN(streeNumber);

        String addressString = Utilities.getAddressStringFromComponents(streeNumber, street, city, state, country);
        Address address = Utilities.getAddressObjectFromAddressString(this, addressString);
        if (address!=null) {
            String geoAddressCountry = address.getCountryCode();
            double geoAddressLatitude = address.getLatitude();
            double geoAddressLongitude = address.getLongitude();

            mPet.setGaC(geoAddressCountry);
            mPet.setGaLt(Double.toString(geoAddressLatitude));
            mPet.setGaLg(Double.toString(geoAddressLongitude));
        }

        mPet.setHs(mEditTextHistory.getText().toString());

        mPet.setAg(mSpinnerAge.getSelectedItem().toString());
        mPet.setSz(mSpinnerSize.getSelectedItem().toString());
        mPet.setGn(mSpinnerGender.getSelectedItem().toString());
        mPet.setRc(mSpinnerRace.getSelectedItem().toString());
        mPet.setBh(mSpinnerBehavior.getSelectedItem().toString());
        mPet.setIt(mSpinnerInteractions.getSelectedItem().toString());


        if (name.length() < 2 || country.length() < 2 || city.length() < 1 || street.length() < 2 || streeNumber.length() < 1) {
            mPetCriticalParametersSet = false;
        }
        else {
            mPetCriticalParametersSet = true;
        }

    }
    private void updatePetWithFoundationData() {
        mPet.setFN(mFoundationName);
        mPet.setOI(mFoundationId);
    }
    private void updateLayoutWithFoundationData() {
        if (!TextUtils.isEmpty(mFoundationName)) mEditTextFoundation.setText(mFoundationName);
        if (!TextUtils.isEmpty(mFoundationCity) && mEditTextCity.getText().toString().equals("")) mEditTextCity.setText(mFoundationCity);
        if (!TextUtils.isEmpty(mFoundationCountry) && mEditTextCountry.getText().toString().equals("")) mEditTextCountry.setText(mFoundationCountry);
        if (!TextUtils.isEmpty(mFoundationStreet) && mEditTextStreet.getText().toString().equals("")) mEditTextStreet.setText(mFoundationStreet);
        if (!TextUtils.isEmpty(mFoundationStreetNumber) && mEditTextStreetNumber.getText().toString().equals("")) mEditTextStreetNumber.setText(mFoundationStreetNumber);
    }
    private void removeListeners() {
        mFirebaseDao.removeListeners();
        if (mSpinnerAge!=null) mSpinnerAge.setOnItemSelectedListener(null);
        if (mSpinnerSize!=null) mSpinnerSize.setOnItemSelectedListener(null);
        if (mSpinnerGender!=null) mSpinnerGender.setOnItemSelectedListener(null);
        if (mSpinnerRace!=null) mSpinnerRace.setOnItemSelectedListener(null);
        if (mSpinnerBehavior!=null) mSpinnerBehavior.setOnItemSelectedListener(null);
        if (mSpinnerInteractions!=null) mSpinnerInteractions.setOnItemSelectedListener(null);
        if (mButtonChooseMainPic!=null) mButtonChooseMainPic.setOnClickListener(null);
        if (mButtonUploadPics!=null) mButtonUploadPics.setOnClickListener(null);
    }


    //View click listeners
    @OnClick(R.id.update_pet_button_choose_main_pic) public void onChooseMainPicButtonClick() {
        if (mPetCriticalParametersSet && !TextUtils.isEmpty(mPet.getUI())) {
            mImageName = "mainImage";
            performImageCaptureAndCrop();
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.must_save_profile_first, Toast.LENGTH_SHORT).show();
        }
    }
    @OnClick(R.id.update_pet_button_upload_pics) public void onUploadPicsButtonClick() {
        if (mPetCriticalParametersSet && !TextUtils.isEmpty(mPet.getUI())) {

            List<Uri> uris = Utilities.getExistingImageUriListForObject(getApplicationContext(), mPet, true);
            if (uris.size() == 5) {
                Toast.makeText(getApplicationContext(), R.string.reached_max_images, Toast.LENGTH_SHORT).show();
            }
            else {
                mImageName = Utilities.getNameOfFirstAvailableImageInImagesList(getApplicationContext(), mPet);
                if (!TextUtils.isEmpty(mImageName)) performImageCaptureAndCrop();
                else Toast.makeText(getApplicationContext(), R.string.error_processing_request, Toast.LENGTH_SHORT).show();
            }

        }
        else {
            Toast.makeText(getApplicationContext(), R.string.must_save_profile_first, Toast.LENGTH_SHORT).show();
        }
    }
    @OnClick(R.id.update_pet_button_add_video_link) public void onAddVideosButtonClick() {
        showVideoLinkDialog();
    }


    //Communication with other classes:

    //Communication with Firebase Dao handler
    @Override public void onPetListFound(List<Pet> pets) {

        if (pets == null) return;

        //If pet is not in database then create it, otherwise update mPet
        if (pets.size() == 0 || pets.get(0)==null) {
            mPetAlreadyExistsInFirebaseDb = false;
            Toast.makeText(getBaseContext(), R.string.no_pet_found_press_done_to_create, Toast.LENGTH_SHORT).show();
        }
        else {
            mPetAlreadyExistsInFirebaseDb = true;
            mPet = pets.get(0);
            if (mSavedInstanceState==null) {
                updateLayoutWithPetData();
                updatePetWithUserInput();
            }
            mFirebaseDao.getAllObjectImages(mPet);
        }

    }
    @Override public void onFamilyListFound(List<Family> families) {

    }
    @Override public void onFoundationListFound(List<Foundation> foundations) {

        if (foundations.size() == 0) {
            Log.i(DEBUG_TAG, "Warning! No foundation found with the required id.");
        }
        else {
            if (foundations.get(0) != null) {
                mFoundationId = foundations.get(0).getUI();
                mFoundationName = foundations.get(0).getNm();
                mFoundationCity = foundations.get(0).getCt();
                mFoundationCountry = foundations.get(0).getCn();
                mFoundationStreet = foundations.get(0).getSt();
                mFoundationStreetNumber = foundations.get(0).getStN();
                updateLayoutWithFoundationData();
            }
            if (foundations.size()>1) Log.i(DEBUG_TAG, "Warning! Multiple foundations found with the same id.");
        }
    }
    @Override public void onUserListFound(List<User> users) {

    }
    @Override public void onMapMarkerListFound(List<MapMarker> mapMarkers) {

    }
    @Override public void onImageAvailable(boolean imageWasDownloaded, Uri downloadedImageUri, String imageName) {

        if (mImageViewMain==null || mPetImagesRecycleViewAdapter==null || mPet==null) return;

        Utilities.synchronizeImageOnAllDevices(getApplicationContext(), mPet, mFirebaseDao, imageName, downloadedImageUri, imageWasDownloaded);

        //Displaying the images (Only showing the images if all images are ready (prevents image flickering))
        boolean allImagesFinishedSyncing = Utilities.checkIfImagesReadyForDisplay(mImagesReady, imageName);
        if (allImagesFinishedSyncing) {
            Utilities.displayAllAvailableImages(getApplicationContext(), mPet, mImageViewMain, mPetImagesRecycleViewAdapter);
            mCurrentlySyncingImages = false;
        }
        else {
            mCurrentlySyncingImages = true;
        }

    }
    @Override public void onImageUploaded(List<String> uploadTimes) {
        mPet.setIUT(uploadTimes);
    }

    //Communication with ImagesRecyclerView adapter
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

    //Communication with VideoLinkRecyclerView adapter
    @Override public void onTextClick(int clickedItemIndex) {
        mVideoLinks = mPet.getVU();
        if (mVideoLinks==null || mVideoLinks.size()==0) return;
        String url = mVideoLinks.get(clickedItemIndex);
        Utilities.goToWebLink(this, url);
    }

    //Communication with spinner adapters
    @Override public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        switch (adapterView.getId()) {
            case R.id.update_pet_age_spinner:
                mPet.setAg((String) adapterView.getItemAtPosition(pos));
                break;
            case R.id.update_pet_size_spinner:
                mPet.setSz((String) adapterView.getItemAtPosition(pos));
                break;
            case R.id.update_pet_gender_spinner:
                mPet.setGn((String) adapterView.getItemAtPosition(pos));
                break;
            case R.id.update_pet_race_spinner:
                mPet.setRc((String) adapterView.getItemAtPosition(pos));
                break;
            case R.id.update_pet_behavior_spinner:
                mPet.setBh((String) adapterView.getItemAtPosition(pos));
                break;
            case R.id.update_pet_interactions_spinner:
                mPet.setIt((String) adapterView.getItemAtPosition(pos));
                break;
        }
    }
    @Override public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
