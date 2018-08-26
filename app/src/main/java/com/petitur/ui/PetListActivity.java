package com.petitur.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.petitur.R;
import com.petitur.adapters.PetListRecycleViewAdapter;
import com.petitur.adapters.SortOptionsRecycleViewAdapter;
import com.petitur.data.Family;
import com.petitur.data.FirebaseDao;
import com.petitur.data.Foundation;
import com.petitur.data.MapMarker;
import com.petitur.data.Pet;
import com.petitur.data.QueryCondition;
import com.petitur.data.User;
import com.petitur.resources.CustomLocationListener;
import com.petitur.resources.ImageSyncAsyncTaskLoader;
import com.petitur.resources.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class PetListActivity extends AppCompatActivity implements
        PetListRecycleViewAdapter.PetListItemClickHandler,
        CustomLocationListener.LocationListenerHandler,
        LoaderManager.LoaderCallbacks<String>,
        ImageSyncAsyncTaskLoader.OnImageSyncOperationsHandler,
        FirebaseDao.FirebaseOperationsHandler {


    //regionParameters
    private static final String DEBUG_TAG = "Petitur Pet List";
    private static final int LIST_MAIN_IMAGES_SYNC_LOADER = 3698;
    @BindView(R.id.pet_list_loading_indicator) ProgressBar mProgressBarLoadingIndicator;
    @BindView(R.id.pet_list_pets_recyclerview) RecyclerView mPetsRecyclerView;
    private Unbinder mBinding;
    private User mUser;
    private PetListRecycleViewAdapter mPetsRecyclerViewAdapter;
    private double mUserLongitude;
    private double mUserLatitude;
    private List<Pet> mPetList;
    private boolean hasLocationPermissions;
    private LocationManager mLocationManager;
    private CustomLocationListener mLocationListener;
    private List<Pet> mPetsAtDistance;
    private List<Pet> mSortedPetsAtDistance;
    private String mProfileType;
    private String mRequestedDogProfileUI;
    private String mRequestedFamilyProfileUI;
    private String mRequestedFoundationProfileUI;
    private boolean mFoundResults;
    private ImageSyncAsyncTaskLoader mImageSyncAsyncTaskLoader;
    private int mSelectedProfileIndex;
    private int mPetsRecyclerViewPosition;
    private CountDownTimer mTimer;
    private boolean mUpdatedRecyclerView;
    private FirebaseDao mFirebaseDao;
    private int mPetDistance;
    private List<String> mSelectedBreedsList;
    private List<String> mSelectedCoatLengthsList;
    private String mSelectedGender;
    private String mSelectedAgeRange;
    private String mSelectedSize;
    private String mTempSelectedGender;
    private String mTempSelectedAgeRange;
    private String mTempSelectedSize;
    private List<String> mPetGendersList;
    private List<String> mPetAgesList;
    private List<String> mPetSizesList;
    private List<String> mSelectedAges;
    private List<String> mSelectedSizes;
    private List<String> mTempSelectedAges;
    private List<String> mTempSelectedSizes;
    private boolean[] mTempSelectedAgesArray;
    private boolean mTempSelectedGoodWithKids;
    private boolean mTempSelectedGoodWithCats;
    private boolean mTempSelectedGoodWithDogs;
    private boolean mTempSelectedCastrated;
    private boolean mTempSelectedHouseTrained;
    private boolean mTempSelectedSpecialNeeds;
    private boolean mSelectedGoodWithKids;
    private boolean mSelectedGoodWithCats;
    private boolean mSelectedGoodWithDogs;
    private boolean mSelectedCastrated;
    private boolean mSelectedHouseTrained;
    private boolean mSelectedSpecialNeeds;
    private String mSelectedPetType;
    private List<String> mAvailableDogBreeds;
    private List<String> mPetCoatLengths;
    private String mSelectedCoatLength;
    private String mSelectedBreed;
    private List<String> mPetTypesList;
    private String mTempSelectedType;
    private Family mFamily;
    private FirebaseUser mCurrentFirebaseUser;
    private String mTempSelectedDogBreed;
    private String mTempSelectedCoatLength;
    private String mTempSelectedCatBreed;
    private String mTempSelectedParrotBreed;
    private List<String> mAvailableCatBreeds;
    private ArrayList<String> mAvailableParrotBreeds;
    private double[] mCoordinateLimits;
    private int mTempPetDistance;
    private List<Integer> mPetDistances;
    private String mTempSortField;
    private SortOptionsRecycleViewAdapter mListAdapter;
    private String mSortField;
    private boolean mSortAscending;
    private boolean mTempSortAscending;
    private List<String> mSortOptions;
    private String mTempSelectedBreed;
    //endregion


    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_list);

        getExtras();
        initializeParameters();
        getFamilyProfileFromFirebase();
        if (!Utilities.internetIsAvailable(this)) {
            Toast.makeText(this, R.string.no_internet_bad_results_warning, Toast.LENGTH_SHORT).show();
        }

        setupRecyclerView();
        startListeningForUserLocation();
    }
    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }
    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        mBinding.unbind();
    }


    //Functionality methods
    private void getExtras() {
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.bundled_user))) {
            mUser = intent.getParcelableExtra(getString(R.string.bundled_user));
        }
    }
    private void initializeParameters() {

        mFirebaseDao = new FirebaseDao(this, this);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.find_a_pet);
        }

        mBinding =  ButterKnife.bind(this);
        Utilities.hideSoftKeyboard(this);

        mCurrentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        hasLocationPermissions = Utilities.checkLocationPermission(this);
        mUserLongitude = Utilities.getAppPreferenceUserLongitude(this);
        mUserLatitude = Utilities.getAppPreferenceUserLatitude(this);

        //Getting the string lists
        mPetTypesList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.pet_types)));
        mPetTypesList.add(0,getString(R.string.filter_option_any));
        mTempSelectedType = mPetTypesList.get(0);
        mSelectedPetType = mPetTypesList.get(0);

        mPetGendersList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.pet_genders)));
        mPetGendersList.add(0,getString(R.string.filter_option_any));
        mTempSelectedGender = mPetGendersList.get(0);
        mSelectedGender = mPetGendersList.get(0);

        mPetAgesList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.pet_ages)));
        mPetAgesList.add(0,getString(R.string.filter_option_any));
        mTempSelectedAgeRange = mPetAgesList.get(0);
        mSelectedAgeRange = mPetAgesList.get(0);

        mPetSizesList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.pet_sizes)));
        mPetSizesList.add(0,getString(R.string.filter_option_any));
        mTempSelectedSize = mPetSizesList.get(0);
        mSelectedSize = mPetSizesList.get(0);

        mAvailableDogBreeds = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.dog_breeds)));
        mAvailableDogBreeds.add(0,getString(R.string.mixed));
        mAvailableDogBreeds.add(0,getString(R.string.filter_option_any));
        mAvailableCatBreeds = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.cat_breeds)));
        mAvailableDogBreeds.add(0,getString(R.string.mixed));
        mAvailableDogBreeds.add(0,getString(R.string.filter_option_any));
        mAvailableParrotBreeds = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.parrot_breeds)));
        mAvailableDogBreeds.add(0,getString(R.string.mixed));
        mAvailableDogBreeds.add(0,getString(R.string.filter_option_any));
        mTempSelectedDogBreed = mAvailableDogBreeds.get(0);
        mTempSelectedCatBreed = mAvailableCatBreeds.get(0);
        mTempSelectedParrotBreed = mAvailableParrotBreeds.get(0);
        mSelectedBreed = mAvailableDogBreeds.get(0);

        mPetCoatLengths = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.coat_lengths)));
        mPetCoatLengths.add(0,getString(R.string.filter_option_any));
        mTempSelectedCoatLength = mPetCoatLengths.get(0);
        mSelectedCoatLength = mPetCoatLengths.get(0);

        mTempSelectedGoodWithKids = false;
        mTempSelectedGoodWithCats = false;
        mTempSelectedGoodWithDogs = false;
        mTempSelectedCastrated = false;
        mTempSelectedHouseTrained = false;
        mTempSelectedSpecialNeeds = false;

        mPetDistance = getResources().getInteger(R.integer.default_pet_distance);
        mTempPetDistance = mPetDistance;
    }
    private void getFamilyProfileFromFirebase() {
        if (mCurrentFirebaseUser != null) {

            //Setting the requested Family's id
            mFamily = new Family();
            mFamily.setOI(mCurrentFirebaseUser.getUid());

            //Getting the rest of the family's parameters
            mFirebaseDao.requestObjectsWithConditions(mFamily, Utilities.getQueryConditionsForSingleObjectSearchByOwnerId(this, mFamily));
        }
    }
    private void showLoadingIndicator() {
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.VISIBLE);
    }
    private void hideLoadingIndicator() {
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.INVISIBLE);
    }
    private void startListeningForUserLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new CustomLocationListener(this, this);
        if (mLocationManager!=null && Utilities.checkLocationPermission(this)) {
            if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1.0f, mLocationListener);
            }
            else if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1.0f, mLocationListener);
            }
        }
    }
    private int getRequestedDistanceFromUserInput(String editTextString) {
        if (hasLocationPermissions) {
            if (TextUtils.isEmpty(editTextString)) return 0;
            else return Integer.parseInt(editTextString)*1000;
        }
        else return 40000000;
    }
    private void setupRecyclerView() {

        //Setting up the RecyclerView adapters
        mPetsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (mPetsRecyclerViewAdapter ==null) mPetsRecyclerViewAdapter = new PetListRecycleViewAdapter(this, this, null, mUserLatitude, mUserLongitude);
        mPetsRecyclerView.setAdapter(mPetsRecyclerViewAdapter);
        mPetsRecyclerViewAdapter.setSelectedProfile(mSelectedProfileIndex);

        mPetsRecyclerView.scrollToPosition(mPetsRecyclerViewPosition);
        mPetsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mPetsRecyclerViewPosition = Utilities.getLinearRecyclerViewPosition(mPetsRecyclerView);
                //TODO: check if this is the same as dy
            }
        });
    }
    private void startImageSyncThread() {

        Log.i(DEBUG_TAG, "Called startImageSyncThread");
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> imageSyncAsyncTaskLoader = loaderManager.getLoader(LIST_MAIN_IMAGES_SYNC_LOADER);
        if (imageSyncAsyncTaskLoader == null) {
            loaderManager.initLoader(LIST_MAIN_IMAGES_SYNC_LOADER, null, this);
        }
        else {
            if (mImageSyncAsyncTaskLoader!=null) {
                //The asynctask is called twice: once on activity start, and then when the user location is found
                //In order to avoid performing background image syncs twice on the same images, we stop the asynctask operation here if the loader is being restarted
                mImageSyncAsyncTaskLoader.stopUpdatingImagesForObjects();
                mImageSyncAsyncTaskLoader.cancelLoadInBackground();
                mImageSyncAsyncTaskLoader = null;
            }
            loaderManager.restartLoader(LIST_MAIN_IMAGES_SYNC_LOADER, null, this);
        }

    }
    public void stopImageSyncThread() {
        if (mImageSyncAsyncTaskLoader!=null) {
            mImageSyncAsyncTaskLoader.stopUpdatingImagesForObjects();
            if (getLoaderManager()!=null) getLoaderManager().destroyLoader(LIST_MAIN_IMAGES_SYNC_LOADER);
        }
    }
    private void stopListeningForLocation() {
        if (mLocationManager!=null) {
            mLocationManager.removeUpdates(mLocationListener);
            mLocationListener = null;
            mLocationManager = null;
        }
        if (mLocationListener != null) mLocationListener = null;
    }
    private void setSearchParametersEqualToTempParameters() {

        //Filter Buttons
        mSelectedPetType = mTempSelectedType;
        mSelectedGender = mTempSelectedGender;
        mSelectedAgeRange = mTempSelectedAgeRange;
        mSelectedSize = mTempSelectedSize;

        //Filter TextViews
        mPetDistance = mTempPetDistance;
        mSelectedBreed = mTempSelectedBreed;
        mSelectedCoatLength = mTempSelectedCoatLength;

        //Filter Checkboxes
        mSelectedGoodWithKids = mTempSelectedGoodWithKids;
        mSelectedGoodWithCats = mTempSelectedGoodWithCats;
        mSelectedGoodWithDogs = mTempSelectedGoodWithDogs;
        mSelectedCastrated = mTempSelectedCastrated;
        mSelectedHouseTrained = mTempSelectedHouseTrained;
        mSelectedSpecialNeeds = mTempSelectedSpecialNeeds;

        //Sort options
        mSortField = mTempSortField;
        mSortAscending = mTempSortAscending;
    }
    private void showFilterDialog() {

        //Get the dialog view
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_filter, null);

        //region Getting the pet type
        ArrayAdapter<String> spinnerAdapterType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mPetTypesList);
        final Spinner dialogFilterSpinnerType = dialogView.findViewById(R.id.dialog_filter_type_spinner);
        dialogFilterSpinnerType.setSelection(Utilities.getSpinnerPositionFromText(dialogFilterSpinnerType, mTempSelectedType));
        dialogFilterSpinnerType.setAdapter(spinnerAdapterType);
        dialogFilterSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                mTempSelectedType = (String) adapterView.getItemAtPosition(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //endregion

        //region Setting the gender buttons and their behaviors
        final ToggleButton dialogFilterButtonGenderAny = dialogView.findViewById(R.id.dialog_filter_button_gender_any);
        final ToggleButton dialogFilterButtonGenderMale = dialogView.findViewById(R.id.dialog_filter_button_gender_male);
        final ToggleButton dialogFilterButtonGenderFemale = dialogView.findViewById(R.id.dialog_filter_button_gender_female);
        dialogFilterButtonGenderAny.setChecked(mTempSelectedGender.equals(mPetGendersList.get(0)));
        dialogFilterButtonGenderMale.setChecked(mTempSelectedGender.equals(mPetGendersList.get(1)));
        dialogFilterButtonGenderFemale.setChecked(mTempSelectedGender.equals(mPetGendersList.get(2)));
        dialogFilterButtonGenderAny.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dialogFilterButtonGenderMale.setChecked(false);
                    dialogFilterButtonGenderFemale.setChecked(false);
                    mTempSelectedGender = mPetGendersList.get(0);
                }
                else if (!dialogFilterButtonGenderMale.isChecked()
                        && !dialogFilterButtonGenderFemale.isChecked()) {
                    dialogFilterButtonGenderMale.setChecked(true);
                    mTempSelectedGender = mPetGendersList.get(1);
                }
            }
        });
        dialogFilterButtonGenderMale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dialogFilterButtonGenderAny.setChecked(false);
                    dialogFilterButtonGenderFemale.setChecked(false);
                    mTempSelectedGender = mPetGendersList.get(1);
                }
                else if (!dialogFilterButtonGenderAny.isChecked()
                        && !dialogFilterButtonGenderFemale.isChecked()) {
                    dialogFilterButtonGenderAny.setChecked(true);
                    mTempSelectedGender = mPetGendersList.get(0);
                }
            }
        });
        dialogFilterButtonGenderFemale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dialogFilterButtonGenderAny.setChecked(false);
                    dialogFilterButtonGenderMale.setChecked(false);
                    mTempSelectedGender = mPetGendersList.get(2);
                }
                else if (!dialogFilterButtonGenderAny.isChecked()
                        && !dialogFilterButtonGenderMale.isChecked()) {
                    dialogFilterButtonGenderAny.setChecked(true);
                    mTempSelectedGender = mPetGendersList.get(0);
                }
            }
        });
        //endregion

        //region Setting the age buttons and their behaviors
        final ToggleButton dialogFilterButtonAgeAny = dialogView.findViewById(R.id.dialog_filter_button_age_any);
        final ToggleButton dialogFilterButtonAgeToddler = dialogView.findViewById(R.id.dialog_filter_button_age_puppy);
        final ToggleButton dialogFilterButtonAgeYoung = dialogView.findViewById(R.id.dialog_filter_button_age_young);
        final ToggleButton dialogFilterButtonAgeAdult = dialogView.findViewById(R.id.dialog_filter_button_age_adult);
        final ToggleButton dialogFilterButtonAgeSenior = dialogView.findViewById(R.id.dialog_filter_button_age_senior);
        dialogFilterButtonAgeAny.setChecked(mTempSelectedAgeRange.equals(mPetAgesList.get(0)));
        dialogFilterButtonAgeToddler.setChecked(mTempSelectedAgeRange.equals(mPetAgesList.get(1)));
        dialogFilterButtonAgeYoung.setChecked(mTempSelectedAgeRange.equals(mPetAgesList.get(2)));
        dialogFilterButtonAgeAdult.setChecked(mTempSelectedAgeRange.equals(mPetAgesList.get(3)));
        dialogFilterButtonAgeSenior.setChecked(mTempSelectedAgeRange.equals(mPetAgesList.get(4)));
        dialogFilterButtonAgeAny.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (dialogFilterButtonAgeAny.isChecked()) {
                    dialogFilterButtonAgeToddler.setChecked(false);
                    dialogFilterButtonAgeYoung.setChecked(false);
                    dialogFilterButtonAgeAdult.setChecked(false);
                    dialogFilterButtonAgeSenior.setChecked(false);
                    mTempSelectedAgeRange = mPetAgesList.get(0);
                }
                else if (!dialogFilterButtonAgeToddler.isChecked()
                        && !dialogFilterButtonAgeYoung.isChecked()
                        && !dialogFilterButtonAgeAdult.isChecked()
                        && !dialogFilterButtonAgeSenior.isChecked()) {
                    dialogFilterButtonAgeToddler.setChecked(true);
                    mTempSelectedAgeRange = mPetAgesList.get(0);
                }
            }
        });
        dialogFilterButtonAgeToddler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dialogFilterButtonAgeAny.setChecked(false);
                    dialogFilterButtonAgeYoung.setChecked(false);
                    dialogFilterButtonAgeAdult.setChecked(false);
                    dialogFilterButtonAgeSenior.setChecked(false);
                    mTempSelectedAgeRange = mPetAgesList.get(1);
                }
                else if (!dialogFilterButtonAgeAny.isChecked()
                        && !dialogFilterButtonAgeYoung.isChecked()
                        && !dialogFilterButtonAgeAdult.isChecked()
                        && !dialogFilterButtonAgeSenior.isChecked()) {
                    dialogFilterButtonAgeAny.setChecked(true);
                    mTempSelectedAgeRange = mPetAgesList.get(0);
                }
            }
        });
        dialogFilterButtonAgeYoung.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dialogFilterButtonAgeAny.setChecked(false);
                    dialogFilterButtonAgeToddler.setChecked(false);
                    dialogFilterButtonAgeAdult.setChecked(false);
                    dialogFilterButtonAgeSenior.setChecked(false);
                    mTempSelectedAgeRange = mPetAgesList.get(2);
                }
                else if (!dialogFilterButtonAgeAny.isChecked()
                        && !dialogFilterButtonAgeToddler.isChecked()
                        && !dialogFilterButtonAgeAdult.isChecked()
                        && !dialogFilterButtonAgeSenior.isChecked()) {
                    dialogFilterButtonAgeAny.setChecked(true);
                    mTempSelectedAgeRange = mPetAgesList.get(0);
                }
            }
        });
        dialogFilterButtonAgeAdult.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dialogFilterButtonAgeAny.setChecked(false);
                    dialogFilterButtonAgeToddler.setChecked(false);
                    dialogFilterButtonAgeYoung.setChecked(false);
                    dialogFilterButtonAgeSenior.setChecked(false);
                    mTempSelectedAgeRange = mPetAgesList.get(3);
                }
                else if (!dialogFilterButtonAgeAny.isChecked()
                        && !dialogFilterButtonAgeToddler.isChecked()
                        && !dialogFilterButtonAgeYoung.isChecked()
                        && !dialogFilterButtonAgeSenior.isChecked()) {
                    dialogFilterButtonAgeAny.setChecked(true);
                    mTempSelectedAgeRange = mPetAgesList.get(0);
                }
            }
        });
        dialogFilterButtonAgeSenior.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dialogFilterButtonAgeAny.setChecked(false);
                    dialogFilterButtonAgeToddler.setChecked(false);
                    dialogFilterButtonAgeYoung.setChecked(false);
                    dialogFilterButtonAgeAdult.setChecked(false);
                    mTempSelectedAgeRange = mPetAgesList.get(4);
                }
                else if (!dialogFilterButtonAgeAny.isChecked()
                        && !dialogFilterButtonAgeToddler.isChecked()
                        && !dialogFilterButtonAgeYoung.isChecked()
                        && !dialogFilterButtonAgeAdult.isChecked()) {
                    dialogFilterButtonAgeAny.setChecked(true);
                    mTempSelectedAgeRange = mPetAgesList.get(0);
                }
            }
        });
        //endregion

        //region Setting the size buttons and their behaviors
        final ToggleButton dialogFilterButtonSizeAny = dialogView.findViewById(R.id.dialog_filter_button_size_any);
        final ToggleButton dialogFilterButtonSizeSmall = dialogView.findViewById(R.id.dialog_filter_button_size_small);
        final ToggleButton dialogFilterButtonSizeMedium = dialogView.findViewById(R.id.dialog_filter_button_size_medium);
        final ToggleButton dialogFilterButtonSizeLarge = dialogView.findViewById(R.id.dialog_filter_button_size_large);
        final ToggleButton dialogFilterButtonSizeExtraLarge = dialogView.findViewById(R.id.dialog_filter_button_size_extra_large);
        dialogFilterButtonSizeAny.setChecked(mTempSelectedSize.equals(mPetSizesList.get(0)));
        dialogFilterButtonSizeSmall.setChecked(mTempSelectedSize.equals(mPetSizesList.get(1)));
        dialogFilterButtonSizeMedium.setChecked(mTempSelectedSize.equals(mPetSizesList.get(2)));
        dialogFilterButtonSizeLarge.setChecked(mTempSelectedSize.equals(mPetSizesList.get(3)));
        dialogFilterButtonSizeExtraLarge.setChecked(mTempSelectedSize.equals(mPetSizesList.get(4)));
        dialogFilterButtonSizeAny.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (dialogFilterButtonSizeAny.isChecked()) {
                    dialogFilterButtonSizeSmall.setChecked(false);
                    dialogFilterButtonSizeMedium.setChecked(false);
                    dialogFilterButtonSizeLarge.setChecked(false);
                    dialogFilterButtonSizeExtraLarge.setChecked(false);
                    mTempSelectedSize = mPetSizesList.get(0);
                }
                else if (!dialogFilterButtonSizeSmall.isChecked()
                        && !dialogFilterButtonSizeMedium.isChecked()
                        && !dialogFilterButtonSizeLarge.isChecked()
                        && !dialogFilterButtonSizeExtraLarge.isChecked()) {
                    dialogFilterButtonSizeSmall.setChecked(true);
                    mTempSelectedSize = mPetSizesList.get(1);
                }
            }
        });
        dialogFilterButtonSizeSmall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dialogFilterButtonSizeAny.setChecked(false);
                    dialogFilterButtonSizeMedium.setChecked(false);
                    dialogFilterButtonSizeLarge.setChecked(false);
                    dialogFilterButtonSizeExtraLarge.setChecked(false);
                    mTempSelectedSize = mPetSizesList.get(1);
                }
                else if (!dialogFilterButtonSizeAny.isChecked()
                        && !dialogFilterButtonSizeMedium.isChecked()
                        && !dialogFilterButtonSizeLarge.isChecked()
                        && !dialogFilterButtonSizeExtraLarge.isChecked()) {
                    dialogFilterButtonSizeAny.setChecked(true);
                    mTempSelectedSize = mPetSizesList.get(0);
                }
            }
        });
        dialogFilterButtonSizeMedium.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dialogFilterButtonSizeAny.setChecked(false);
                    dialogFilterButtonSizeSmall.setChecked(false);
                    dialogFilterButtonSizeLarge.setChecked(false);
                    dialogFilterButtonSizeExtraLarge.setChecked(false);
                    mTempSelectedSize = mPetSizesList.get(2);
                }
                else if (!dialogFilterButtonSizeAny.isChecked()
                        && !dialogFilterButtonSizeSmall.isChecked()
                        && !dialogFilterButtonSizeLarge.isChecked()
                        && !dialogFilterButtonSizeExtraLarge.isChecked()) {
                    dialogFilterButtonSizeAny.setChecked(true);
                    mTempSelectedSize = mPetSizesList.get(0);
                }
            }
        });
        dialogFilterButtonSizeLarge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dialogFilterButtonSizeAny.setChecked(false);
                    dialogFilterButtonSizeSmall.setChecked(false);
                    dialogFilterButtonSizeMedium.setChecked(false);
                    dialogFilterButtonSizeExtraLarge.setChecked(false);
                    mTempSelectedSize = mPetSizesList.get(3);
                }
                else if (!dialogFilterButtonSizeAny.isChecked()
                        && !dialogFilterButtonSizeSmall.isChecked()
                        && !dialogFilterButtonSizeMedium.isChecked()
                        && !dialogFilterButtonSizeExtraLarge.isChecked()) {
                    dialogFilterButtonSizeAny.setChecked(true);
                    mTempSelectedSize = mPetSizesList.get(0);
                }
            }
        });
        dialogFilterButtonSizeExtraLarge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dialogFilterButtonSizeAny.setChecked(false);
                    dialogFilterButtonSizeSmall.setChecked(false);
                    dialogFilterButtonSizeMedium.setChecked(false);
                    dialogFilterButtonSizeLarge.setChecked(false);
                    mTempSelectedSize = mPetSizesList.get(4);
                }
                else if (!dialogFilterButtonSizeAny.isChecked()
                        && !dialogFilterButtonSizeSmall.isChecked()
                        && !dialogFilterButtonSizeMedium.isChecked()
                        && !dialogFilterButtonSizeLarge.isChecked()) {
                    dialogFilterButtonSizeAny.setChecked(true);
                    mTempSelectedSize = mPetSizesList.get(0);
                }
            }
        });
        //endregion

        //region Getting the breed
        final AutoCompleteTextView dialogFilterAutoCompleteTextViewBreed =  dialogView.findViewById(R.id.dialog_filter_autocompletetextview_breed);
        final ArrayAdapter<String> breedArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mAvailableDogBreeds);
        dialogFilterAutoCompleteTextViewBreed.setText(mTempSelectedDogBreed);
        dialogFilterAutoCompleteTextViewBreed.setAdapter(breedArrayAdapter);
        dialogFilterAutoCompleteTextViewBreed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                dialogFilterAutoCompleteTextViewBreed.showDropDown();
            }
        });
        ImageView dialogFilterImageViewArrowBreed = dialogView.findViewById(R.id.dialog_filter_arrow_breed);
        dialogFilterImageViewArrowBreed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogFilterAutoCompleteTextViewBreed.showDropDown();
            }
        });
        //endregion

        //region Getting the coat length
        final AutoCompleteTextView dialogFilterAutoCompleteTextViewCoatLength =  dialogView.findViewById(R.id.dialog_filter_autocompletetextview_coat_length);
        final ArrayAdapter<String> coatLengthArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mPetCoatLengths);
        dialogFilterAutoCompleteTextViewCoatLength.setText(mTempSelectedCoatLength);
        dialogFilterAutoCompleteTextViewCoatLength.setAdapter(coatLengthArrayAdapter);
        dialogFilterAutoCompleteTextViewCoatLength.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                dialogFilterAutoCompleteTextViewCoatLength.showDropDown();
            }
        });
        ImageView dialogFilterImageViewArrowCoatLength = dialogView.findViewById(R.id.dialog_filter_arrow_coat_length);
        dialogFilterImageViewArrowCoatLength.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogFilterAutoCompleteTextViewCoatLength.showDropDown();
            }
        });
        //endregion

        //region Getting the checked button states
        final CheckBox dialogFilterCheckBoxGoodWithKids = dialogView.findViewById(R.id.dialog_filter_checkbox_good_with_kids);
        dialogFilterCheckBoxGoodWithKids.setChecked(mTempSelectedGoodWithKids);
        dialogFilterCheckBoxGoodWithKids.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mTempSelectedGoodWithKids = isChecked;
            }
        });

        final CheckBox dialogFilterCheckBoxGoodWithCats = dialogView.findViewById(R.id.dialog_filter_checkbox_good_with_cats);
        dialogFilterCheckBoxGoodWithCats.setChecked(mTempSelectedGoodWithCats);
        dialogFilterCheckBoxGoodWithCats.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mTempSelectedGoodWithCats = isChecked;
            }
        });

        final CheckBox dialogFilterCheckBoxGoodWithDogs = dialogView.findViewById(R.id.dialog_filter_checkbox_good_with_dogs);
        dialogFilterCheckBoxGoodWithDogs.setChecked(mTempSelectedGoodWithDogs);
        dialogFilterCheckBoxGoodWithDogs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mTempSelectedGoodWithDogs = isChecked;
            }
        });

        final CheckBox dialogFilterCheckBoxCastrated = dialogView.findViewById(R.id.dialog_filter_checkbox_castrated);
        dialogFilterCheckBoxCastrated.setChecked(mTempSelectedCastrated);
        dialogFilterCheckBoxCastrated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mTempSelectedCastrated = isChecked;
            }
        });

        final CheckBox dialogFilterCheckBoxHouseTrained = dialogView.findViewById(R.id.dialog_filter_checkbox_house_trained);
        dialogFilterCheckBoxHouseTrained.setChecked(mTempSelectedHouseTrained);
        dialogFilterCheckBoxHouseTrained.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mTempSelectedHouseTrained = isChecked;
            }
        });

        final CheckBox dialogFilterCheckBoxSpecialNeeds = dialogView.findViewById(R.id.dialog_filter_checkbox_special_needs);
        dialogFilterCheckBoxSpecialNeeds.setChecked(mTempSelectedSpecialNeeds);
        dialogFilterCheckBoxSpecialNeeds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mTempSelectedSpecialNeeds = isChecked;
            }
        });
        //endregion

        //region Getting the distance
        final EditText dialogFilterEditTextDistance = dialogView.findViewById(R.id.dialog_filter_distance_edittext);
        String distance = ""+mTempPetDistance/1000;
        dialogFilterEditTextDistance.setText(distance);
        //endregion

        //region Reset button functionality
        final Button dialogFilterButtonReset = dialogView.findViewById(R.id.dialog_filter_button_reset);
        dialogFilterButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogFilterSpinnerType.setSelection(0);
                mTempSelectedType = mPetTypesList.get(0);

                dialogFilterButtonGenderAny.setChecked(true);
                dialogFilterButtonGenderMale.setChecked(false);
                dialogFilterButtonGenderFemale.setChecked(false);
                mTempSelectedGender = mPetGendersList.get(0);

                dialogFilterButtonAgeAny.setChecked(true);
                dialogFilterButtonAgeToddler.setChecked(false);
                dialogFilterButtonAgeYoung.setChecked(false);
                dialogFilterButtonAgeAdult.setChecked(false);
                dialogFilterButtonAgeSenior.setChecked(false);
                mTempSelectedAgeRange = mPetAgesList.get(0);

                dialogFilterButtonSizeAny.setChecked(true);
                dialogFilterButtonSizeSmall.setChecked(false);
                dialogFilterButtonSizeMedium.setChecked(false);
                dialogFilterButtonSizeLarge.setChecked(false);
                dialogFilterButtonSizeExtraLarge.setChecked(false);
                mTempSelectedSize = mPetSizesList.get(0);

                dialogFilterAutoCompleteTextViewBreed.setText(mAvailableDogBreeds.get(0));
                dialogFilterAutoCompleteTextViewCoatLength.setText(mPetCoatLengths.get(0));

                dialogFilterCheckBoxGoodWithKids.setChecked(false);
                mTempSelectedGoodWithKids = false;
                dialogFilterCheckBoxGoodWithCats.setChecked(false);
                mTempSelectedGoodWithCats = false;
                dialogFilterCheckBoxGoodWithDogs.setChecked(false);
                mTempSelectedGoodWithDogs = false;
                dialogFilterCheckBoxCastrated.setChecked(false);
                mTempSelectedCastrated = false;
                dialogFilterCheckBoxHouseTrained.setChecked(false);
                mTempSelectedHouseTrained = false;
                dialogFilterCheckBoxSpecialNeeds.setChecked(false);
                mTempSelectedSpecialNeeds = false;

                dialogFilterEditTextDistance.setText("0");
                mTempPetDistance = 0;
            }
        });
        //endregion

        //region Save button functionality
        final Button dialogFilterButtonSave = dialogView.findViewById(R.id.dialog_filter_button_save);
        dialogFilterButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mFamily.getOI())) {
                    Toast.makeText(getBaseContext(), R.string.could_not_save_preferences, Toast.LENGTH_SHORT).show();
                }
                else {

                    mTempPetDistance = getRequestedDistanceFromUserInput(dialogFilterEditTextDistance.getText().toString());
                    mTempSelectedBreed = dialogFilterAutoCompleteTextViewBreed.getText().toString();
                    mTempSelectedCoatLength = dialogFilterAutoCompleteTextViewCoatLength.getText().toString();

                    saveSearchParametersInFamilyProfile();

                    mFirebaseDao.updateObject(mFamily);
                }
            }
        });
        //endregion

        //region Building the dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                mTempPetDistance = getRequestedDistanceFromUserInput(dialogFilterEditTextDistance.getText().toString());
                mTempSelectedBreed = dialogFilterAutoCompleteTextViewBreed.getText().toString();
                mTempSelectedCoatLength = dialogFilterAutoCompleteTextViewCoatLength.getText().toString();

                saveSearchParametersInFamilyProfile();
                setSearchParametersEqualToTempParameters();
                requestFilteredListFromFirebase();

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

        android.app.AlertDialog dialog = builder.create();
        dialog.show();
        //endregion

    }
    private void showSortDialog() {

        //Get the dialog view
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_sort, null);

        //region Setting the list behavior
        final RecyclerView dialogSortList = dialogView.findViewById(R.id.dialog_sort_list);
        dialogSortList.setLayoutManager(new LinearLayoutManager(this));

        mSortOptions = new ArrayList<>();
        mSortOptions.add(getString(R.string.distance));
        mSortOptions.add(getString(R.string.age));
        mSortOptions.add(getString(R.string.breed));

        SortOptionsRecycleViewAdapter.SortOptionClickHandler sortOptionClickHandler = new SortOptionsRecycleViewAdapter.SortOptionClickHandler() {
            @Override
            public void onSortOptionClick(int clickedItemIndex) {
                mTempSortField = mSortOptions.get(clickedItemIndex);
                if (mListAdapter!=null) mListAdapter.setSelectedOption(clickedItemIndex);
            }
        };
        mListAdapter = new SortOptionsRecycleViewAdapter(this, sortOptionClickHandler, mSortOptions);
        dialogSortList.setAdapter(mListAdapter);
        for (int i=0; i< mSortOptions.size(); i++) {
            String sortOption = mSortOptions.get(i);
            if (sortOption.equals(mTempSortField)) {
                mListAdapter.setSelectedOption(i);
                break;
            }
        }
        //endregion

        //region Setting the order button behavior
        final ToggleButton orderButton = dialogView.findViewById(R.id.dialog_sort_order);
        orderButton.setChecked(mTempSortAscending);
        orderButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mTempSortAscending = isChecked;
            }
        });
        //endregion

        //region Building the dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                mSortField = mTempSortField;
                mSortAscending = mTempSortAscending;

                saveSearchParametersInFamilyProfile();

                //Sorting the pets
                if (mPetsAtDistance!=null && mPetsAtDistance.size()>0) {
                    sortPets();
                    mPetsRecyclerViewAdapter.setContents(mPetsAtDistance);
                }

                //TODO: update only the data that was changed to lower bandwidth usage
                mFirebaseDao.updateObject(mFamily);

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

        android.app.AlertDialog dialog = builder.create();
        dialog.show();
        //endregion

    }
    private void saveSearchParametersInFamilyProfile() {
        mFamily.setTP(mTempSelectedType);
        mFamily.setGP(mTempSelectedGender);
        mFamily.setAP(mTempSelectedAgeRange);
        mFamily.setSP(mTempSelectedSize);

        if (mTempSelectedType.equals(getString(R.string.dog))) mFamily.setDRP(mTempSelectedBreed);
        else if (mTempSelectedType.equals(getString(R.string.cat))) mFamily.setCRP(mTempSelectedBreed);
        else if (mTempSelectedType.equals(getString(R.string.parrot))) mFamily.setPRP(mTempSelectedBreed);

        mFamily.setCLP(mTempSelectedCoatLength);

        mFamily.setGKP(mTempSelectedGoodWithKids);
        mFamily.setGCP(mTempSelectedGoodWithCats);
        mFamily.setGDP(mTempSelectedGoodWithDogs);
        mFamily.setCsP(mTempSelectedCastrated);
        mFamily.setHTP(mTempSelectedHouseTrained);
        mFamily.setSNP(mTempSelectedSpecialNeeds);

        mFamily.setDP(mTempPetDistance);

        mFamily.setSrT(mTempSortField);
        mFamily.setSrA(mTempSortAscending);
    }
    private void requestFilteredListFromFirebase() {

        List<QueryCondition> queryConditions = new ArrayList<>();
        QueryCondition queryCondition;

        //Setting the pet type
        if (!TextUtils.isEmpty(mSelectedPetType) && !mSelectedPetType.equals(getString(R.string.filter_option_any))) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "tp", mSelectedPetType, true, 0);
            queryConditions.add(queryCondition);
        }

        //Setting the gender
        if (!TextUtils.isEmpty(mSelectedGender) && !mSelectedGender.equals(getString(R.string.filter_option_any))) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "gn", mSelectedGender, true, 0);
            queryConditions.add(queryCondition);
        }

        //Setting the age range
        if (!TextUtils.isEmpty(mSelectedAgeRange) && !mSelectedAgeRange.equals(getString(R.string.filter_option_any))) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "agR", mSelectedAgeRange, true, 0);
            queryConditions.add(queryCondition);
        }

        //Setting the size
        if (!TextUtils.isEmpty(mSelectedSize) && !mSelectedSize.equals(getString(R.string.filter_option_any))) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "sz", mSelectedSize, true, 0);
            queryConditions.add(queryCondition);
        }

        //Setting the breed/race
        if (!TextUtils.isEmpty(mSelectedBreed) && !mSelectedBreed.equals(getString(R.string.filter_option_any))) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "rc", mSelectedBreed, true, 0);
            queryConditions.add(queryCondition);
        }

        //Setting the coat lengths
        if (!TextUtils.isEmpty(mSelectedCoatLength) && !mSelectedCoatLength.equals(getString(R.string.filter_option_any))) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "rc", mSelectedCoatLength, true, 0);
            queryConditions.add(queryCondition);
        }

        //Setting the checkbox settings
        if (mSelectedGoodWithKids) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsBoolean), "gk", "", mSelectedGoodWithKids, 0);
            queryConditions.add(queryCondition);
        }
        if (mSelectedGoodWithCats) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsBoolean), "gc", "", mSelectedGoodWithCats, 0);
            queryConditions.add(queryCondition);
        }
        if (mSelectedGoodWithDogs) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsBoolean), "gd", "", mSelectedGoodWithDogs, 0);
            queryConditions.add(queryCondition);
        }
        if (mSelectedCastrated) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsBoolean), "cs", "", mSelectedCastrated, 0);
            queryConditions.add(queryCondition);
        }
        if (mSelectedHouseTrained) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsBoolean), "ht", "", mSelectedHouseTrained, 0);
            queryConditions.add(queryCondition);
        }
        if (mSelectedSpecialNeeds) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsBoolean), "sn", "", mSelectedSpecialNeeds, 0);
            queryConditions.add(queryCondition);
        }

        //Limiting the search to the user's state (if it's not null) or country
        //if (!TextUtils.isEmpty(mFamily.getSe())) { //TODO: stub - complete country/state selection when creating a pet
        if (false) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "se", "Israel", mSelectedSpecialNeeds, 0);
            queryConditions.add(queryCondition);
        }
        else {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "cn", "Israel", mSelectedSpecialNeeds, 0);
            queryConditions.add(queryCondition);
        }

        //Setting the latitude limits based on the distance (longitude is ignored since Firestore does not support full GeoPoint comparison, see https://github.com/firebase/firebase-js-sdk/issues/826)
        mCoordinateLimits = Utilities.getCoordinateLimitsAroundLatLong(mUserLatitude, mUserLongitude, mPetDistance);

//        //TODO: improve this when Firestore upgrades its geo capabilities, see: https://stackoverflow.com/questions/46630507/how-to-run-a-geo-nearby-query-with-firestore
//        queryCondition = new QueryCondition(getString(R.string.query_condition_greaterThanOrEqualToNumber), "geo", "", true, mCoordinateLimits[2]);
//        queryConditions.add(queryCondition);
//        queryCondition = new QueryCondition(getString(R.string.query_condition_lessThanNumber), "geo", "", true, mCoordinateLimits[3]);
//        queryConditions.add(queryCondition);
//
//        //Setting the limit to the number of results
//        queryCondition = new QueryCondition(getString(R.string.query_condition_limit), "", "", true, 40);
//        queryConditions.add(queryCondition);

        //Requesting the list
        showLoadingIndicator();
        mFirebaseDao.requestObjectsWithConditions(new Pet(), queryConditions);
    }
    private void sortAndDisplayPetList() {

        getPetsAtDistance();
        sortPets();
        startImageSyncThread();
        hideLoadingIndicator();
        mPetsRecyclerViewAdapter.setContents(mPetsAtDistance);
    }
    private void sortPets() {

        Pet[] pets = new Pet[mPetsAtDistance.size()];
        pets = mPetsAtDistance.toArray(pets);

        if (mSortField.equals(getString(R.string.distance)) && mSortAscending) Arrays.sort(pets, Pet.PetDistanceComparatorAscending);
        else if (mSortField.equals(getString(R.string.distance)) && !mSortAscending) Arrays.sort(pets, Pet.PetDistanceComparatorDescending);
        else if (mSortField.equals(getString(R.string.age)) && mSortAscending) Arrays.sort(pets, Pet.PetAgeComparatorAscending);
        else if (mSortField.equals(getString(R.string.age)) && !mSortAscending) Arrays.sort(pets, Pet.PetAgeComparatorDescending);
        else if (mSortField.equals(getString(R.string.breed)) && mSortAscending) Arrays.sort(pets, Pet.PetBreedComparatorAscending);
        else if (mSortField.equals(getString(R.string.breed)) && !mSortAscending) Arrays.sort(pets, Pet.PetBreedComparatorDescending);
        else Arrays.sort(pets, Pet.PetDistanceComparatorAscending);

        mPetsAtDistance = Arrays.asList(pets);
    }
    private void getPetsAtDistance() {
        mPetsAtDistance = new ArrayList<>();
        mPetDistances = new ArrayList<>();
        for (Pet pet : mPetList) {
            int distance = Utilities.getDistanceFromLatLong(mUserLatitude, mUserLongitude, pet.getGeo().getLatitude(), pet.getGeo().getLongitude());
            if (distance <= mPetDistance) {
                pet.setDt(distance);
                mPetsAtDistance.add(pet);
            }
        }
    }
    private void setTempFiltersAccordingToFamilyPreferences() {

        if (!TextUtils.isEmpty(mFamily.getTP())) mTempSelectedType = mFamily.getTP();
        if (!TextUtils.isEmpty(mFamily.getGP())) mTempSelectedGender = mFamily.getGP();
        if (!TextUtils.isEmpty(mFamily.getAP())) mTempSelectedAgeRange = mFamily.getAP();
        if (!TextUtils.isEmpty(mFamily.getSP())) mTempSelectedSize = mFamily.getSP();
        if (!TextUtils.isEmpty(mFamily.getDRP())) mTempSelectedDogBreed = mFamily.getDRP();
        if (!TextUtils.isEmpty(mFamily.getCRP())) mTempSelectedCatBreed = mFamily.getCRP();
        if (!TextUtils.isEmpty(mFamily.getPRP())) mTempSelectedParrotBreed = mFamily.getPRP();
        if (!TextUtils.isEmpty(mFamily.getCLP())) mTempSelectedCoatLength = mFamily.getCLP();

        mTempSelectedGoodWithKids = mFamily.getGKP();
        mTempSelectedGoodWithCats = mFamily.getGCP();
        mTempSelectedGoodWithDogs = mFamily.getGDP();
        mTempSelectedCastrated = mFamily.getCsP();
        mTempSelectedHouseTrained = mFamily.getHTP();
        mTempSelectedSpecialNeeds = mFamily.getSNP();

        mTempPetDistance = mFamily.getDP();
    }
    private void setTempSortParametersAccordingToFamilyPreferences() {
        if (!TextUtils.isEmpty(mFamily.getSrT())) mTempSortField = mFamily.getSrT();
        else mTempSortField = getString(R.string.distance);

        mTempSortAscending = mFamily.getSrA();
    }


    //View click listeners
    @OnClick(R.id.pet_list_filter_button) public void onFilterButtonClick() {
        setTempFiltersAccordingToFamilyPreferences();
        setSearchParametersEqualToTempParameters();
        showFilterDialog();
    }
    @OnClick(R.id.pet_list_sort_button) public void onSortButtonClick() {
        setTempSortParametersAccordingToFamilyPreferences();
        setSearchParametersEqualToTempParameters();
        showSortDialog();
    }


    //Communication with other classes:

    //Communication with RecyclerView adapters
    @Override public void onPetListItemClick(int clickedItemIndex) {

    }

    //Communication with Location handler
    @Override public void onLocalCoordinatesFound(double longitude, double latitude) {

        mUserLongitude = longitude;
        mUserLatitude = latitude;
        Utilities.setAppPreferenceUserLongitude(this, longitude);
        Utilities.setAppPreferenceUserLatitude(this, latitude);

        if (!(mUserLongitude == 0.0 && mUserLatitude == 0.0) && mLocationManager!=null) {
            stopListeningForLocation();
        }

    }

    //Communication with Loader
    @NonNull @Override public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {

        if (id == LIST_MAIN_IMAGES_SYNC_LOADER && mImageSyncAsyncTaskLoader==null) {
            mImageSyncAsyncTaskLoader =  new ImageSyncAsyncTaskLoader(this, getString(R.string.task_sync_list_main_images),
                    mProfileType, mPetsAtDistance, null, null, this);
            return mImageSyncAsyncTaskLoader;
        }
        return new ImageSyncAsyncTaskLoader(this, "", null, null, null, null, this);
    }
    @Override public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        if (loader.getId() == LIST_MAIN_IMAGES_SYNC_LOADER) {
            mPetsRecyclerViewAdapter.notifyDataSetChanged();
            stopImageSyncThread();
        }
    }
    @Override public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    //Communication with ImageSyncAsyncTaskLoader
    @Override public void onDisplayRefreshRequested() {

    }

    //Communication with Firebase Dao handler
    @Override public void onPetListFound(List<Pet> pets) {
        if (pets == null) return;
        mPetList = pets;
        sortAndDisplayPetList();
    }
    @Override public void onFamilyListFound(List<Family> families) {
        if (families == null) return;

        //If pet is not in database then create it, otherwise update mPet
        if (families.size() == 0 || families.get(0)==null) {
            Toast.makeText(getBaseContext(), R.string.must_create_family_profile, Toast.LENGTH_SHORT).show();
            Utilities.startUpdateFamilyProfileActivity(PetListActivity.this);
        }
        else {
            mFamily = families.get(0);
            setTempSortParametersAccordingToFamilyPreferences();
            setTempFiltersAccordingToFamilyPreferences();
            setSearchParametersEqualToTempParameters();
            requestFilteredListFromFirebase();
        }
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
