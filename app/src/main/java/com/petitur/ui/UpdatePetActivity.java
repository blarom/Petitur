package com.petitur.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.NestedScrollView;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;
import com.petitur.R;
import com.petitur.adapters.FosteringFamiliesRecycleViewAdapter;
import com.petitur.adapters.ImagesRecycleViewAdapter;
import com.petitur.adapters.SimpleTextRecycleViewAdapter;
import com.petitur.adapters.VetEventRecycleViewAdapter;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class UpdatePetActivity extends BaseActivity implements
        FirebaseDao.FirebaseOperationsHandler,
        AdapterView.OnItemSelectedListener,
        SimpleTextRecycleViewAdapter.TextClickHandler,
        ImagesRecycleViewAdapter.ImageClickHandler, VetEventRecycleViewAdapter.VetEventClickHandler, FosteringFamiliesRecycleViewAdapter.FosteringFamilyClickHandler {


    //region Parameters
    private static final String DEBUG_TAG = "Petitur Update Pet";
    @BindView(R.id.update_pet_button_choose_main_pic) Button mButtonChooseMainPic;
    @BindView(R.id.update_pet_button_upload_pics) Button mButtonUploadPics;
    @BindView(R.id.update_pet_button_add_video_link) Button mButtonAddVideoLink;
    @BindView(R.id.update_pet_value_name) TextInputEditText mEditTextName;
    @BindView(R.id.update_pet_value_name_local) TextInputEditText mEditTextNameLocal;
    @BindView(R.id.update_pet_value_foundation) TextInputEditText mEditTextFoundation;
    @BindView(R.id.update_pet_value_country) TextInputEditText mEditTextCountry;
    @BindView(R.id.update_pet_value_state) TextInputEditText mEditTextState;
    @BindView(R.id.update_pet_value_city) TextInputEditText mEditTextCity;
    @BindView(R.id.update_pet_value_street) TextInputEditText mEditTextStreet;
    @BindView(R.id.update_pet_value_street_number) TextInputEditText mEditTextStreetNumber;
    @BindView(R.id.update_pet_value_history) TextInputEditText mEditTextHistory;
    @BindView(R.id.update_pet_image_main) ImageView mImageViewMain;
    @BindView(R.id.update_pet_recyclerview_video_links) RecyclerView mRecyclerViewVideoLinks;
    @BindView(R.id.update_pet_recyclerview_vet_events) RecyclerView mRecyclerViewVetEvents;
    @BindView(R.id.update_pet_recyclerview_fostering_families) RecyclerView mRecyclerViewFosteringFamilies;
    @BindView(R.id.update_pet_recyclerview_images) RecyclerView mRecyclerViewDogImages;
    @BindView(R.id.update_pet_age_years_edittext) EditText mAgeYearsEditText;
    @BindView(R.id.update_pet_age_months_edittext) EditText mAgeMonthsEditText;
    @BindView(R.id.update_pet_type_spinner) Spinner mSpinnerType;
    @BindView(R.id.update_pet_size_spinner) Spinner mSpinnerSize;
    @BindView(R.id.update_pet_gender_spinner) Spinner mSpinnerGender;
    @BindView(R.id.update_pet_race_autocompletetextview) AutoCompleteTextView mAutoCompleteTextViewBreed;
    @BindView(R.id.update_pet_coat_length_spinner) Spinner mSpinnerCoatLengths;
    @BindView(R.id.update_pet_checkbox_good_with_kids) CheckBox mCheckBoxGoodWithKids;
    @BindView(R.id.update_pet_checkbox_good_with_cats) CheckBox mCheckBoxGoodWithCats;
    @BindView(R.id.update_pet_checkbox_good_with_dogs) CheckBox mCheckBoxGoodWithDogs;
    @BindView(R.id.update_pet_checkbox_castrated) CheckBox mCheckBoxCastrated;
    @BindView(R.id.update_pet_checkbox_house_trained) CheckBox mCheckBoxHouseTrained;
    @BindView(R.id.update_pet_checkbox_special_needs) CheckBox mCheckBoxSpecialNeeds;
    @BindView(R.id.update_pet_scroll_container) NestedScrollView mScrollViewContainer;
    @BindView(R.id.update_pet_arrow_breed) ImageView mImageViewArrowBreed;
    private ArrayAdapter<String> mSpinnerAdapterType;
    private ArrayAdapter<String> mSpinnerAdapterSize;
    private ArrayAdapter<String> mSpinnerAdapterGender;
    private ArrayAdapter<String> mSpinnerAdapterBreed;
    private ArrayAdapter<String> mSpinnerAdapterCoatLengths;
    private int mTypeSpinnerPosition;
    private int mSizeSpinnerPosition;
    private int mGenderSpinnerPosition;
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
    private Unbinder mBinding;
    private SimpleTextRecycleViewAdapter mVideoLinksRecycleViewAdapter;
    private VetEventRecycleViewAdapter mVetEventsRecycleViewAdapter;
    private FosteringFamiliesRecycleViewAdapter mFosteringFamiliesRecycleViewAdapter;
    private List<String> mVideoLinks;
    private boolean[] mImagesReady;
    private Bundle mSavedInstanceState;
    private int mScrollPosition;
    private Uri[] mTempImageUris;
    private boolean mCurrentlySyncingImages;
    private boolean mRequireOnlineSync;
    private String mVetEventDate;
    private String mVetEventDescription;
    private String mFosteringFamilyStartDate;
    private String mFosteringFamilyEndDate;
    private String mDateRangeFromButtons;
    private String mFosteringFamilyDescription;
    private Foundation mFoundation;
    private List<String> mPetTypesList;
    private List<String> mPetGendersList;
    private List<String> mPetSizesList;
    private List<String> mAvailableDogBreeds;
    private List<String> mAvailableCatBreeds;
    private List<String> mAvailableParrotBreeds;
    private List<String> mPetCoatLengths;
    private List<String> mPetAgesList;
    private List<String> mDisplayedPetTypesList;
    private List<String> mDisplayedPetGendersList;
    private List<String> mDisplayedPetSizesList;
    private List<String> mDisplayedAvailableDogBreeds;
    private List<String> mDisplayedAvailableCatBreeds;
    private List<String> mDisplayedAvailableParrotBreeds;
    private List<String> mDisplayedPetCoatLengths;
    private List<String> mDisplayedPetAgesList;
    //endregion


    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pet);

        mSavedInstanceState = savedInstanceState;
        getExtras();
        initializeParameters();
        setupVetEventsRecyclerView();
        setupFosteringFamiliesRecyclerView();
        setupVideoLinksRecyclerView();
        setupPetImagesRecyclerView();
        if (savedInstanceState==null) updateLayoutWithFoundationAndPetProfiles();
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
                    if (TextUtils.isEmpty(mPet.getUI())) {
                        mPet = (Pet) mFirebaseDao.createObjectWithUIAndReturnIt(mPet);
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
                    if (TextUtils.isEmpty(mPet.getUI())) {
                        mPet = (Pet) mFirebaseDao.createObjectWithUIAndReturnIt(mPet);
                    }
                    else mFirebaseDao.updateObject(mPet);

                    Intent data = new Intent();
                    data.putExtra(getString(R.string.pet_profile_parcelable), mPet);
                    setResult(RESULT_OK, data);
                    onBackPressed();
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
        outState.putParcelable(getString(R.string.foundation_profile_parcelable), mFoundation);
        outState.putBoolean(getString(R.string.critical_parameters_set), mPetCriticalParametersSet);
        updatePetWithUserInput();
        outState.putParcelable(getString(R.string.pet_profile_parcelable), mPet);
        super.onSaveInstanceState(outState);

    }
    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mStoredDogImagesRecyclerViewPosition = savedInstanceState.getInt(getString(R.string.profile_update_pet_images_rv_position));
            mRecyclerViewDogImages.scrollToPosition(mStoredDogImagesRecyclerViewPosition);
            mImageName = savedInstanceState.getString(getString(R.string.profile_update_image_name));
            mPet = savedInstanceState.getParcelable(getString(R.string.pet_profile_parcelable));
            mFoundation = savedInstanceState.getParcelable(getString(R.string.foundation_profile_parcelable));
            mScrollPosition = savedInstanceState.getInt(getString(R.string.saved_scroll_position));
            mPetCriticalParametersSet = savedInstanceState.getBoolean(getString(R.string.critical_parameters_set));

            mScrollViewContainer.setScrollY(mScrollPosition);
            updateLayoutWithFoundationData();
            updateLayoutWithPetData();
            setupPetImagesRecyclerView();
            Utilities.displayObjectImageInImageView(getApplicationContext(), mPet, "mainImage", mImageViewMain);
        }
    }


    //Functional methods
    private void getExtras() {
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.pet_profile_parcelable))) {
            mPet = intent.getParcelableExtra(getString(R.string.pet_profile_parcelable));
        }
        if (intent.hasExtra(getString(R.string.foundation_profile_parcelable))) {
            mFoundation = intent.getParcelableExtra(getString(R.string.foundation_profile_parcelable));
        }
    }
    private void initializeParameters() {
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.pet_profile);
        }

        mBinding =  ButterKnife.bind(this);
        mPetCriticalParametersSet = false;
        mImagesReady = new boolean[]{false, false, false, false, false, false};
        mTempImageUris = new Uri[]{null, null, null, null, null, null, null};
        mEditTextFoundation.setEnabled(false);
        mVideoLinks = new ArrayList<>();
        mCurrentlySyncingImages = false;

        mFirebaseDao = new FirebaseDao(getBaseContext(), this);
        mCurrentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseAuth = FirebaseAuth.getInstance();


        mDisplayedPetAgesList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.pet_ages)));
        mPetAgesList = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.pet_ages)));

        mDisplayedPetTypesList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.pet_types)));
        mPetTypesList = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.pet_types)));
        mSpinnerAdapterType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mDisplayedPetTypesList);
        mSpinnerAdapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerType.setAdapter(mSpinnerAdapterType);
        mSpinnerType.setOnItemSelectedListener(this);

        mDisplayedPetSizesList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.pet_sizes)));
        mPetSizesList = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.pet_sizes)));
        mSpinnerAdapterSize = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mDisplayedPetSizesList);
        mSpinnerAdapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSize.setAdapter(mSpinnerAdapterSize);
        mSpinnerSize.setOnItemSelectedListener(this);

        mDisplayedPetGendersList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.pet_genders)));
        mPetGendersList = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.pet_genders)));
        mSpinnerAdapterGender = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mDisplayedPetGendersList);
        mSpinnerAdapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerGender.setAdapter(mSpinnerAdapterGender);
        mSpinnerGender.setOnItemSelectedListener(this);

        mDisplayedAvailableDogBreeds = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.dog_breeds)));
        mAvailableDogBreeds = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.dog_breeds)));
        mDisplayedAvailableCatBreeds = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.cat_breeds)));
        mAvailableCatBreeds = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.cat_breeds)));
        mDisplayedAvailableParrotBreeds = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.parrot_breeds)));
        mAvailableParrotBreeds = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.parrot_breeds)));
        mSpinnerAdapterBreed = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mDisplayedAvailableDogBreeds);
        mAutoCompleteTextViewBreed.setAdapter(mSpinnerAdapterBreed);
        mAutoCompleteTextViewBreed.setText(mDisplayedAvailableDogBreeds.get(0));
        mAutoCompleteTextViewBreed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                mAutoCompleteTextViewBreed.showDropDown();
            }
        });
        mImageViewArrowBreed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAutoCompleteTextViewBreed.showDropDown();
            }
        });

        mDisplayedPetCoatLengths = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.coat_lengths)));
        mPetCoatLengths = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.coat_lengths)));
        mSpinnerAdapterCoatLengths = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mDisplayedPetCoatLengths);
        mSpinnerAdapterCoatLengths.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCoatLengths.setAdapter(mSpinnerAdapterCoatLengths);
        mSpinnerCoatLengths.setOnItemSelectedListener(this);

    }
    private void updateLayoutWithFoundationAndPetProfiles() {
        if (mCurrentFirebaseUser != null) {
            // Name, email address, and profile photo Url
            mNameFromFirebase = mCurrentFirebaseUser.getDisplayName();
            mEmailFromFirebase = mCurrentFirebaseUser.getEmail();
            mPhotoUriFromFirebase = mCurrentFirebaseUser.getPhotoUrl();
            mFirebaseUid = mCurrentFirebaseUser.getUid();

            //Initializing the local parameters that depend on this pet, used in the rest of the activity
            mImageName = "mainImage";

            //Getting the foundation details
            if (mFoundation==null || TextUtils.isEmpty(mFoundation.getUI())) {
                mFoundation = new Foundation(mFirebaseUid);
                mFirebaseDao.requestObjectsWithConditions(mFoundation, Utilities.getQueryConditionsForSingleObjectSearchByOwnerId(this, mFoundation));
            }
            else {
                updateLayoutWithFoundationData();
            }

            //Getting the pet details
            if (mPet==null || TextUtils.isEmpty(mPet.getUI())) {
                mPet = new Pet(mChosenPetId);
                mFirebaseDao.requestObjectWithId(mPet);
            }
            else {
                updateLayoutWithPetData();
                updatePetWithUserInput();
                mFirebaseDao.syncAllObjectImages(mPet);
                displayPetImages();
            }
        }
    }
    private void updateLayoutWithPetData() {

        if (mEditTextName==null) return;
        //mEditTextFoundation.setText(mDog.getFN());
        mEditTextName.setText(mPet.getNm());
        mEditTextNameLocal.setText(mPet.getNmL());
        mEditTextCountry.setText(mPet.getCn());
        mEditTextState.setText(mPet.getSe());
        mEditTextCity.setText(mPet.getCt());
        mEditTextStreet.setText(mPet.getSt());
        mEditTextStreetNumber.setText(mPet.getStN());
        mEditTextHistory.setText(mPet.getHs());

        mAgeYearsEditText.setText(Integer.toString(Utilities.getYearsFromAge(mPet.getAg())));
        mAgeMonthsEditText.setText(Integer.toString(Utilities.getMonthsFromAge(mPet.getAg())));

        mTypeSpinnerPosition = Utilities.getSpinnerPositionFromText(mSpinnerType,
                Utilities.getDisplayedTextFromFlagText(mDisplayedPetTypesList, mPetTypesList, mPet.getTp()));
        mSizeSpinnerPosition = Utilities.getSpinnerPositionFromText(mSpinnerSize,
                Utilities.getDisplayedTextFromFlagText(mDisplayedPetSizesList, mPetSizesList, mPet.getSz()));
        mGenderSpinnerPosition = Utilities.getSpinnerPositionFromText(mSpinnerGender,
                Utilities.getDisplayedTextFromFlagText(mDisplayedPetGendersList, mPetGendersList, mPet.getGn()));

        mSpinnerType.setSelection(mTypeSpinnerPosition);
        mSpinnerSize.setSelection(mSizeSpinnerPosition);
        mSpinnerGender.setSelection(mGenderSpinnerPosition);

        if (mPet.getTp().equals(getString(R.string.dog))) {
            mAutoCompleteTextViewBreed.setText(Utilities.getDisplayedTextFromFlagText(mDisplayedAvailableDogBreeds, mAvailableDogBreeds, mPet.getRc()));
        }
        else if (mPet.getTp().equals(getString(R.string.cat))) {
            mAutoCompleteTextViewBreed.setText(Utilities.getDisplayedTextFromFlagText(mDisplayedAvailableCatBreeds, mAvailableCatBreeds, mPet.getRc()));
        }
        else if (mPet.getTp().equals(getString(R.string.parrot))) {
            mAutoCompleteTextViewBreed.setText(Utilities.getDisplayedTextFromFlagText(mDisplayedAvailableParrotBreeds, mAvailableParrotBreeds, mPet.getRc()));
        }

        mCheckBoxGoodWithKids.setChecked(mPet.getGK());
        mCheckBoxGoodWithCats.setChecked(mPet.getGC());
        mCheckBoxGoodWithDogs.setChecked(mPet.getGD());
        mCheckBoxCastrated.setChecked(mPet.getCs());
        mCheckBoxHouseTrained.setChecked(mPet.getHT());
        mCheckBoxSpecialNeeds.setChecked(mPet.getSN());

        mVetEventsRecycleViewAdapter.setContents(mPet.getVet());
        mFosteringFamiliesRecycleViewAdapter.setContents(mPet.getFam());

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
    private void setupVetEventsRecyclerView() {
        mRecyclerViewVetEvents.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        mRecyclerViewVetEvents.setNestedScrollingEnabled(false);
        mVetEventsRecycleViewAdapter = new VetEventRecycleViewAdapter(this, this, null);
        mRecyclerViewVetEvents.setAdapter(mVetEventsRecycleViewAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                Map<String, String> vetEvents = mPet.getVet();
                vetEvents.remove(mVetEventDate);
                mPet.setVet(vetEvents);

                mVetEventsRecycleViewAdapter.setContents(vetEvents);
                //mVideoLinksRecycleViewAdapter.notifyItemRemoved(viewHolder.getLayoutPosition());
                mPet.setVU(mVideoLinks);
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerViewVetEvents);
    }
    private void setupFosteringFamiliesRecyclerView() {
        mRecyclerViewFosteringFamilies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        mRecyclerViewFosteringFamilies.setNestedScrollingEnabled(false);
        mFosteringFamiliesRecycleViewAdapter = new FosteringFamiliesRecycleViewAdapter(this, this, null);
        mRecyclerViewFosteringFamilies.setAdapter(mFosteringFamiliesRecycleViewAdapter);

    }
    private void setupPetImagesRecyclerView() {
        mRecyclerViewDogImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerViewDogImages.setNestedScrollingEnabled(true);
        mPetImagesRecycleViewAdapter = new ImagesRecycleViewAdapter(this, this, null);
        mRecyclerViewDogImages.setAdapter(mPetImagesRecycleViewAdapter);
    }
    private void displayPetImages() {
        List<Uri> uris = Utilities.getExistingImageUriListForObject(getApplicationContext(), mPet, true);
        mPetImagesRecycleViewAdapter.setContents(uris);

        Utilities.displayObjectImageInImageView(getApplicationContext(), mPet, "mainImage", mImageViewMain);
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

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) builder.setView(dialogView);
        //else builder.setMessage(R.string.device_version_too_low);

        builder.setView(dialogView);

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
        String nameLocal = mEditTextNameLocal.getText().toString();
        String country = mEditTextCountry.getText().toString();
        String state = mEditTextState.getText().toString();
        String city = mEditTextCity.getText().toString();
        String street = mEditTextStreet.getText().toString();
        String streeNumber = mEditTextStreetNumber.getText().toString();

        mPet.setNm(name);
        mPet.setNmL(nameLocal);

        mPet.setCn(country);
        mPet.setSe(state);
        mPet.setCt(city);
        mPet.setSt(street);
        mPet.setStN(streeNumber);

        String addressString = Utilities.getAddressStringFromComponents(streeNumber, street, city, state, country);
        Address address = Utilities.getAddressObjectFromAddressString(this, addressString);
        if (address!=null) {
            String geoAddressCountry = address.getCountryCode();
            double geoAddressLatitude = address.getLatitude() + Utilities.getCoordinateRandomJitter();
            double geoAddressLongitude = address.getLongitude() + Utilities.getCoordinateRandomJitter();

            mPet.setGaC(geoAddressCountry);
            mPet.setGeo(new GeoPoint(geoAddressLatitude, geoAddressLongitude));
        }

        mPet.setHs(mEditTextHistory.getText().toString());

        mPet.setTp(Utilities.getFlagTextFromDisplayedText(mDisplayedPetTypesList, mPetTypesList, mSpinnerType.getSelectedItem().toString()));
        mPet.setGn(Utilities.getFlagTextFromDisplayedText(mDisplayedPetGendersList, mPetGendersList, mSpinnerGender.getSelectedItem().toString()));

        String yearsString = mAgeYearsEditText.getText().toString();
        String monthsString = mAgeMonthsEditText.getText().toString();
        int years = (yearsString.equals("") || yearsString.length()>2) ? 0 : Integer.parseInt(yearsString);
        int months = (monthsString.equals("") || monthsString.length()>2) ? 0 : Integer.parseInt(monthsString);
        mPet.setAg(Utilities.getMonthsAgeFromYearsMonths(years, months));
        mPet.setAgR(Utilities.getFlagTextFromDisplayedText(mDisplayedPetAgesList, mPetAgesList, Utilities.getAgeRange(this, mPet.getTp(), years, months)));
        mPet.setSz(Utilities.getFlagTextFromDisplayedText(mDisplayedPetSizesList, mPetSizesList, mSpinnerSize.getSelectedItem().toString()));
        mPet.setCL(Utilities.getFlagTextFromDisplayedText(mDisplayedPetCoatLengths, mPetCoatLengths, mSpinnerCoatLengths.getSelectedItem().toString()));

        if (mPet.getTp().equals(getString(R.string.dog))) {
            mPet.setRc(Utilities.getFlagTextFromDisplayedText(mDisplayedAvailableDogBreeds, mAvailableDogBreeds, mAutoCompleteTextViewBreed.getText().toString()));
        }
        else if (mPet.getTp().equals(getString(R.string.cat))) {
            mPet.setRc(Utilities.getFlagTextFromDisplayedText(mDisplayedAvailableCatBreeds, mAvailableCatBreeds, mAutoCompleteTextViewBreed.getText().toString()));
        }
        else if (mPet.getTp().equals(getString(R.string.parrot))) {
            mPet.setRc(Utilities.getFlagTextFromDisplayedText(mDisplayedAvailableParrotBreeds, mAvailableParrotBreeds, mAutoCompleteTextViewBreed.getText().toString()));
        }

        mPet.setGK(mCheckBoxGoodWithKids.isChecked());
        mPet.setGC(mCheckBoxGoodWithCats.isChecked());
        mPet.setGD(mCheckBoxGoodWithDogs.isChecked());
        mPet.setCs(mCheckBoxCastrated.isChecked());
        mPet.setHT(mCheckBoxHouseTrained.isChecked());
        mPet.setSN(mCheckBoxSpecialNeeds.isChecked());

        if ((years==0 && months==0)
                || (nameLocal.length() < 2 && name.length() < 2)
                || country.length() < 2 || city.length() < 1 || street.length() < 2 || streeNumber.length() < 1) {
            mPetCriticalParametersSet = false;
        }
        else {
            mPetCriticalParametersSet = true;
        }

    }
    private void updatePetWithFoundationData() {
        mPet.setFN(mFoundation.getNm());
        mPet.setOI(mFoundation.getUI());
    }
    private void updateLayoutWithFoundationData() {
        mEditTextFoundation.setText(mFoundation.getNm());
        if (mEditTextCity.getText().toString().equals("")) mEditTextCity.setText(mFoundation.getCt());
        if (mEditTextCountry.getText().toString().equals("")) mEditTextCountry.setText(mFoundation.getCn());
        if (mEditTextStreet.getText().toString().equals("")) mEditTextStreet.setText(mFoundation.getSt());
        if (mEditTextStreetNumber.getText().toString().equals("")) mEditTextStreetNumber.setText(mFoundation.getStN());
    }
    private void removeListeners() {
        mFirebaseDao.removeListeners();
        if (mSpinnerSize!=null) mSpinnerSize.setOnItemSelectedListener(null);
        if (mSpinnerGender!=null) mSpinnerGender.setOnItemSelectedListener(null);
        if (mAutoCompleteTextViewBreed !=null) mAutoCompleteTextViewBreed.setOnItemSelectedListener(null);
        if (mSpinnerCoatLengths !=null) mSpinnerCoatLengths.setOnItemSelectedListener(null);
        if (mButtonChooseMainPic!=null) mButtonChooseMainPic.setOnClickListener(null);
        if (mButtonUploadPics!=null) mButtonUploadPics.setOnClickListener(null);
    }
    private void showUpdateVetEventDialog(final String eventDate) {

        //region Get the dialog view
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_set_vet_event, null);
        final Button vetEventDateButton = dialogView.findViewById(R.id.dialog_vet_event_date_button);
        final EditText vetEventDescriptionEditText = dialogView.findViewById(R.id.dialog_vet_event_description_text);
        vetEventDescriptionEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        //endregion

        //region Setting the layout element behaviors
        final Map<String, String> vetEvents = mPet.getVet();
        final Calendar calendar = Calendar.getInstance();
        if (!TextUtils.isEmpty(eventDate) && vetEvents.containsKey(eventDate)) {
            vetEventDateButton.setText(eventDate);
            vetEventDescriptionEditText.setText(vetEvents.get(eventDate));
        }
        else {
            String buttonText = calendar.get(Calendar.DATE) + "-" + (calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.YEAR);
            vetEventDateButton.setText(buttonText);
        }

        vetEventDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //inspired by: https://stackoverflow.com/questions/30987181/date-picker-inside-custom-dialog-in-android

                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String buttonText = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                        vetEventDateButton.setText(buttonText);
                    }
                };

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        UpdatePetActivity.this,
                        listener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DATE)
                );

                datePickerDialog.show();

            }
        });
        //endregion

        //region Building the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.vet_event);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                //Getting the user entries
                mVetEventDate = vetEventDateButton.getText().toString();
                mVetEventDescription = vetEventDescriptionEditText.getText().toString();

                //Updating the pet object - If there already was an event at that date, add the event to the list
                String oldEventDescription;
                if (vetEvents.containsKey(mVetEventDate)) {
                    oldEventDescription = vetEvents.get(mVetEventDate);
                    if (!oldEventDescription.equals(mVetEventDescription)) {
                        if (oldEventDescription.length() > 2 && oldEventDescription.substring(0, 1).equals("-")) {
                            vetEvents.put(mVetEventDate, oldEventDescription + "\n- " + mVetEventDescription);
                        } else {
                            vetEvents.put(mVetEventDate, "- " + oldEventDescription + "\n- " + mVetEventDescription);
                        }
                    }
                }
                else {
                    vetEvents.put(mVetEventDate, mVetEventDescription); //create a new event at the requested date
                }
                mPet.setVet(vetEvents);

                //Removing the old event if the date was changed
                //if (!eventDate.equals(mVetEventDate) && vetEvents.containsKey(eventDate)) vetEvents.remove(eventDate);

                //Updating the events list shown to the user
                mVetEventsRecycleViewAdapter.setContents(vetEvents);

                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (vetEvents.containsKey(eventDate)) vetEvents.remove(eventDate);
                mPet.setVet(vetEvents);
                mVetEventsRecycleViewAdapter.setContents(vetEvents);
            }
        });
        //endregion

        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void showUpdateFosteringFamilyDialog(final String dateRangeFromList) {

        //region Getting the date limits
        String[] dateLimits = dateRangeFromList.split("~");
        final String startDate = dateLimits[0];
        String endDate = (dateLimits.length>1)? dateLimits[1].trim() : "";
        if (endDate.equals("")) endDate = getString(R.string.today);
        //endregion

        //region Get the dialog view
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_set_fostering_family, null);
        final Button fosteringFamilyStartDateButton = dialogView.findViewById(R.id.dialog_fostering_family_start_date_button);
        final Button fosteringFamilyEndDateButton = dialogView.findViewById(R.id.dialog_fostering_family_end_date_button);
        final EditText fosteringFamilyDescriptionEditText = dialogView.findViewById(R.id.dialog_fostering_family_description_text);
        fosteringFamilyDescriptionEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        //endregion

        //region Setting the layout element behaviors
        final Map<String, String> fosterFamilies = mPet.getFam();
        Calendar calendar = Calendar.getInstance();
        if (!TextUtils.isEmpty(dateRangeFromList) && fosterFamilies.containsKey(dateRangeFromList)) {
            fosteringFamilyStartDateButton.setText(startDate);
            fosteringFamilyEndDateButton.setText(endDate);
            fosteringFamilyDescriptionEditText.setText(fosterFamilies.get(dateRangeFromList));
        }
        else {
            String buttonText = calendar.get(Calendar.DATE) + "-" + (calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.YEAR);
            fosteringFamilyStartDateButton.setText(buttonText);
            buttonText = (calendar.get(Calendar.DATE)+1) + "-" + (calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.YEAR);
            fosteringFamilyEndDateButton.setText(buttonText);
        }

        fosteringFamilyStartDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //inspired by: https://stackoverflow.com/questions/30987181/date-picker-inside-custom-dialog-in-android

                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String buttonText = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                        fosteringFamilyStartDateButton.setText(buttonText);
                        buttonText = (dayOfMonth + 1) + "-" + (monthOfYear + 1) + "-" + year;
                        fosteringFamilyEndDateButton.setText(buttonText);
                }
                };

                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        UpdatePetActivity.this,
                        listener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DATE)
                );

                datePickerDialog.show();

            }
        });
        fosteringFamilyEndDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //inspired by: https://stackoverflow.com/questions/30987181/date-picker-inside-custom-dialog-in-android

                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String buttonText = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                        fosteringFamilyEndDateButton.setText(buttonText);
                    }
                };

                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        UpdatePetActivity.this,
                        listener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DATE)
                );

                String[] startDateElements = fosteringFamilyStartDateButton.getText().toString().split("-");
                calendar.set(
                        Integer.parseInt(startDateElements[2].trim()),
                        Integer.parseInt(startDateElements[1].trim())-1,
                        Integer.parseInt(startDateElements[0].trim()));
                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis()- 1000);
                //datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()- 1000);

                datePickerDialog.show();

            }
        });
        //endregion

        //region Building the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.vet_event);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                //Getting the user entries
                mFosteringFamilyStartDate = fosteringFamilyStartDateButton.getText().toString().trim();
                mFosteringFamilyEndDate = fosteringFamilyEndDateButton.getText().toString().trim();
                mDateRangeFromButtons = mFosteringFamilyStartDate + " ~ " + mFosteringFamilyEndDate;
                mFosteringFamilyDescription = fosteringFamilyDescriptionEditText.getText().toString();

                //Updating the pet object - If there already was an foster family at that date range, add the event to the list
                String oldRangeDescription;
                if (fosterFamilies.containsKey(mDateRangeFromButtons)) {
                    oldRangeDescription = fosterFamilies.get(mDateRangeFromButtons);
                    if (!oldRangeDescription.equals(mFosteringFamilyDescription)) {
                        if (oldRangeDescription.length() > 2 && oldRangeDescription.substring(0, 1).equals("-")) {
                            fosterFamilies.put(mDateRangeFromButtons, oldRangeDescription + "\n- " + mFosteringFamilyDescription);
                        } else {
                            fosterFamilies.put(mDateRangeFromButtons, "- " + oldRangeDescription + "\n- " + mFosteringFamilyDescription);
                        }
                    }
                }
                else {
                    fosterFamilies.put(mDateRangeFromButtons, mFosteringFamilyDescription); //create a new family at the requested date range
                }
                mPet.setFam(fosterFamilies);

                //Updating the events list shown to the user
                mFosteringFamiliesRecycleViewAdapter.setContents(fosterFamilies);

                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (fosterFamilies.containsKey(dateRangeFromList)) fosterFamilies.remove(dateRangeFromList);
                mPet.setFam(fosterFamilies);
                mFosteringFamiliesRecycleViewAdapter.setContents(fosterFamilies);
            }
        });
        //endregion

        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();
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
    @OnClick(R.id.update_pet_button_add_vet_event) public void onAddVetEventButtonClick() {
        showUpdateVetEventDialog("");
    }
    @OnClick(R.id.update_pet_button_add_fostering_family) public void onAddFosteringFamilyButtonClick() {
        showUpdateFosteringFamilyDialog("");
    }


    //Communication with other classes:

    //Communication with Firebase Dao handler
    @Override public void onPetListFound(List<Pet> pets) {

        if (pets == null) return;

        if (pets.size() == 0 || pets.get(0)==null) {
            Toast.makeText(getBaseContext(), R.string.no_pet_found_press_done_to_create, Toast.LENGTH_SHORT).show();
        }
        else {
            mPet = pets.get(0);
            if (mSavedInstanceState==null) {
                updateLayoutWithPetData();
                updatePetWithUserInput();
                mFirebaseDao.syncAllObjectImages(mPet);
                displayPetImages();
            }
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

    //Communication with VetEventsRecyclerView adapter
    @Override public void onVetEventClick(String date) {
        showUpdateVetEventDialog(date);
    }

    //Communication with FosteringFamiliesRecyclerView adapter
    @Override public void onFosteringFamilyClick(String dateRange) {
        showUpdateFosteringFamilyDialog(dateRange);
    }

    //Communication with spinner adapters
    @Override public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        switch (adapterView.getId()) {
            case R.id.update_pet_type_spinner:
                mPet.setTp((String) adapterView.getItemAtPosition(pos));
                break;
            case R.id.update_pet_size_spinner:
                mPet.setSz((String) adapterView.getItemAtPosition(pos));
                break;
            case R.id.update_pet_gender_spinner:
                mPet.setGn((String) adapterView.getItemAtPosition(pos));
                break;
            case R.id.update_pet_race_autocompletetextview:
                mPet.setRc((String) adapterView.getItemAtPosition(pos));
                break;
        }
    }
    @Override public void onNothingSelected(AdapterView<?> adapterView) {

    }

}
