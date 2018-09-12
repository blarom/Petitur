package com.petitur.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.petitur.adapters.VetEventRecycleViewAdapter;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class PetListActivity extends BaseActivity implements
        PetListRecycleViewAdapter.PetListItemClickHandler,
        CustomLocationListener.LocationListenerHandler,
        LoaderManager.LoaderCallbacks<List<Pet>>,
        ImageSyncAsyncTaskLoader.OnImageSyncOperationsHandler,
        FirebaseDao.FirebaseOperationsHandler {


    //regionParameters
    private static final String DEBUG_TAG = "Petitur Pet List";
    private static final int LIST_MAIN_IMAGES_SYNC_LOADER = 3698;
    private static final int LIST_ADDRESS_LANGUAGE_SYNC_LOADER = 3675;
    public static final int SHOW_PET_ACTIVITY_KEY = 1234;
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
    private ImageSyncAsyncTaskLoader mImageSyncAsyncTaskLoader;
    private int mPetsRecyclerViewPosition;
    private FirebaseDao mFirebaseDao;
    private int mPetDistance;
    private String mSelectedGender;
    private String mSelectedAgeRange;
    private String mSelectedSize;
    private String mTempSelectedGender;
    private String mTempSelectedAgeRange;
    private String mTempSelectedSize;
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
    private List<String> mPetTypesList;
    private List<String> mPetGendersList;
    private List<String> mPetAgesList;
    private List<String> mPetSizesList;
    private List<String> mAvailableDogBreeds;
    private List<String> mAvailableCatBreeds;
    private List<String> mAvailableParrotBreeds;
    private List<String> mPetCoatLengths;
    private List<String> mDisplayedPetTypesList;
    private List<String> mDisplayedPetGendersList;
    private List<String> mDisplayedPetAgesList;
    private List<String> mDisplayedPetSizesList;
    private List<String> mDisplayedAvailableDogBreeds;
    private List<String> mDisplayedAvailableCatBreeds;
    private List<String> mDisplayedAvailableParrotBreeds;
    private List<String> mDisplayedPetCoatLengths;
    private String mSelectedCoatLength;
    private String mSelectedBreed;
    private String mTempSelectedPetType;
    private Family mFamily;
    private Foundation mFoundation;
    private FirebaseUser mCurrentFirebaseUser;
    private String mTempSelectedDogBreed;
    private String mTempSelectedCoatLength;
    private String mTempSelectedCatBreed;
    private String mTempSelectedParrotBreed;
    private double[] mCoordinateLimits;
    private int mTempPetDistance;
    private String mTempSortOrder;
    private VetEventRecycleViewAdapter mListAdapter;
    private String mSortOrder;
    private boolean mSortAscending;
    private boolean mTempSortAscending;
    private List<String> mDisplayedSortOptions;
    private List<String> mSortOptions;
    private String mTempSelectedBreed;
    private boolean mRequestedFavorites;
    private String mTempListChoice;
    private int mDocumentCounter;
    private List<String> mFavoritePetIds;
    private AddressLanguageAsyncTaskLoader mAddressLanguageSyncAsyncTaskLoader;
    private EditText mDialogFilterEditTextDistance;
    private AutoCompleteTextView mDialogFilterAutoCompleteTextViewBreed;
    private AutoCompleteTextView mDialogFilterAutoCompleteTextViewCoatLength;
    private Spinner dialogFilterSpinnerSort;
    private ToggleButton dialogFilterButtonListMyPets;
    private ToggleButton dialogFilterButtonListAllPets;
    private Spinner dialogFilterSpinnerType;
    private ToggleButton dialogFilterButtonGenderAny;
    private ToggleButton dialogFilterButtonGenderMale;
    private ToggleButton dialogFilterButtonGenderFemale;
    private ToggleButton dialogFilterButtonAgeAny;
    private ToggleButton dialogFilterButtonAgeToddler;
    private ToggleButton dialogFilterButtonAgeYoung;
    private ToggleButton dialogFilterButtonAgeAdult;
    private ToggleButton dialogFilterButtonAgeSenior;
    private ToggleButton dialogFilterButtonSizeAny;
    private ToggleButton dialogFilterButtonSizeSmall;
    private ToggleButton dialogFilterButtonSizeMedium;
    private ToggleButton dialogFilterButtonSizeLarge;
    private ToggleButton dialogFilterButtonSizeExtraLarge;
    private CheckBox dialogFilterCheckBoxGoodWithKids;
    private CheckBox dialogFilterCheckBoxGoodWithCats;
    private CheckBox dialogFilterCheckBoxGoodWithDogs;
    private CheckBox dialogFilterCheckBoxCastrated;
    private CheckBox dialogFilterCheckBoxHouseTrained;
    private CheckBox dialogFilterCheckBoxSpecialNeeds;
    //endregion


    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_list);

        getExtras();
        initializeParameters();
        getFamilyOrFoundationProfileFromFirebase();
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
    @Override public void onBackPressed() {
        super.onBackPressed();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tips_info_menu, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        switch (itemThatWasClickedId) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOW_PET_ACTIVITY_KEY) {
            if (resultCode == RESULT_OK) {

                //Update the family if it was modified
                if (data.hasExtra(getString(R.string.family_profile_parcelable))) mFamily = data.getParcelableExtra(getString(R.string.family_profile_parcelable));

                //Update the pet in the pets list if it was modified
                Pet modifiedPet = new Pet();
                if (data.hasExtra(getString(R.string.pet_profile_parcelable))) modifiedPet = data.getParcelableExtra(getString(R.string.pet_profile_parcelable));
                for (int i=0; i<mPetsAtDistance.size(); i++) {
                    if (mPetsAtDistance.get(i).getUI().equals(modifiedPet.getUI())) {
                        mPetsAtDistance.set(i, modifiedPet);
                    }
                }
                mPetsRecyclerViewAdapter.setContents(mPetsAtDistance);
            }
        }
    }


    //Functionality methods
    private void getExtras() {
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.bundled_user))) {
            mUser = intent.getParcelableExtra(getString(R.string.bundled_user));
            mRequestedFavorites = intent.getBooleanExtra(getString(R.string.bundled_requested_favorites), false);
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

        mPetsAtDistance = new ArrayList<>();
        mCurrentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        hasLocationPermissions = Utilities.checkLocationPermission(this);
        mUserLongitude = Utilities.getAppPreferenceUserLongitude(this);
        mUserLatitude = Utilities.getAppPreferenceUserLatitude(this);

        //Initilalizing the dialog temp elements
        if (mRequestedFavorites) mTempListChoice = Utilities.getFlag(getApplicationContext()).getString(R.string.dialog_filter_my_favorites);
        else if (mUser!=null && mUser.getIF()) mTempListChoice = Utilities.getFlag(getApplicationContext()).getString(R.string.dialog_filter_my_pets);
        else mTempListChoice = Utilities.getFlag(getApplicationContext()).getString(R.string.dialog_filter_all_pets);

        mDisplayedPetTypesList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.pet_types)));
        mDisplayedPetTypesList.add(0,getString(R.string.filter_option_any));
        mPetTypesList = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.pet_types)));
        mPetTypesList.add(0,Utilities.getFlag(getApplicationContext()).getString(R.string.filter_option_any));
        mTempSelectedPetType = mPetTypesList.get(0);
        mSelectedPetType = mPetTypesList.get(0);

        mDisplayedPetGendersList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.pet_genders)));
        mDisplayedPetGendersList.add(0,getString(R.string.filter_option_any));
        mPetGendersList = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.pet_genders)));
        mPetGendersList.add(0,Utilities.getFlag(getApplicationContext()).getString(R.string.filter_option_any));
        mTempSelectedGender = mPetGendersList.get(0);
        mSelectedGender = mPetGendersList.get(0);

        mDisplayedPetAgesList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.pet_ages)));
        mDisplayedPetAgesList.add(0,getString(R.string.filter_option_any));
        mPetAgesList = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.pet_ages)));
        mPetAgesList.add(0,Utilities.getFlag(getApplicationContext()).getString(R.string.filter_option_any));
        mTempSelectedAgeRange = mPetAgesList.get(0);
        mSelectedAgeRange = mPetAgesList.get(0);

        mDisplayedPetSizesList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.pet_sizes)));
        mDisplayedPetSizesList.add(0,getString(R.string.filter_option_any));
        mPetSizesList = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.pet_sizes)));
        mPetSizesList.add(0,Utilities.getFlag(getApplicationContext()).getString(R.string.filter_option_any));
        mTempSelectedSize = mPetSizesList.get(0);
        mSelectedSize = mPetSizesList.get(0);

        mDisplayedAvailableDogBreeds = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.dog_breeds)));
        mDisplayedAvailableDogBreeds.add(0,getString(R.string.filter_option_any));
        mAvailableDogBreeds = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.dog_breeds)));
        mAvailableDogBreeds.add(0,Utilities.getFlag(getApplicationContext()).getString(R.string.filter_option_any));
        mTempSelectedDogBreed = mAvailableDogBreeds.get(0);

        mDisplayedAvailableCatBreeds = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.cat_breeds)));
        mDisplayedAvailableCatBreeds.add(0,getString(R.string.filter_option_any));
        mAvailableCatBreeds = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.cat_breeds)));
        mAvailableCatBreeds.add(0,Utilities.getFlag(getApplicationContext()).getString(R.string.filter_option_any));
        mTempSelectedCatBreed = mAvailableCatBreeds.get(0);

        mDisplayedAvailableParrotBreeds = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.parrot_breeds)));
        mDisplayedAvailableParrotBreeds.add(0,getString(R.string.filter_option_any));
        mAvailableParrotBreeds = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.parrot_breeds)));
        mAvailableParrotBreeds.add(0,Utilities.getFlag(getApplicationContext()).getString(R.string.filter_option_any));
        mTempSelectedParrotBreed = mAvailableParrotBreeds.get(0);

        mSelectedBreed = mAvailableDogBreeds.get(0);

        mDisplayedPetCoatLengths = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.coat_lengths)));
        mDisplayedPetCoatLengths.add(0,getString(R.string.filter_option_any));
        mPetCoatLengths = new ArrayList<>(Arrays.asList(Utilities.getFlag(getApplicationContext()).getStringArray(R.array.coat_lengths)));
        mPetCoatLengths.add(0,Utilities.getFlag(getApplicationContext()).getString(R.string.filter_option_any));
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
    private void getFamilyOrFoundationProfileFromFirebase() {
        if (mCurrentFirebaseUser != null) {

            if (mUser.getIF()) {
                //Setting the requested Foundation's id
                mFoundation = new Foundation();
                mFoundation.setOI(mCurrentFirebaseUser.getUid());

                //Getting the rest of the family's parameters
                mFirebaseDao.requestObjectsWithConditions(mFoundation, Utilities.getQueryConditionsForSingleObjectSearchByOwnerId(this, mFoundation));
            }
            else {
                //Setting the requested Family's id
                mFamily = new Family();
                mFamily.setOI(mCurrentFirebaseUser.getUid());

                //Getting the rest of the family's parameters
                mFirebaseDao.requestObjectsWithConditions(mFamily, Utilities.getQueryConditionsForSingleObjectSearchByOwnerId(this, mFamily));
            }
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

        if (mPetsRecyclerViewAdapter ==null) mPetsRecyclerViewAdapter = new PetListRecycleViewAdapter(this, this, null, mUser.getIF());
        mPetsRecyclerView.setAdapter(mPetsRecyclerViewAdapter);
        mPetsRecyclerViewAdapter.setSelectedProfile(0);

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
        Loader<List<Pet>> imageSyncAsyncTaskLoader = loaderManager.getLoader(LIST_MAIN_IMAGES_SYNC_LOADER);
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
    private void startAddressLanguageUpdateThread() {

        for (Pet pet : mPetsAtDistance) {
            pet.setCtL(pet.getCt());
        }

        Log.i(DEBUG_TAG, "Called startAddressLanguageUpdateThread");
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> addressLanguageSyncAsyncTaskLoader = loaderManager.getLoader(LIST_ADDRESS_LANGUAGE_SYNC_LOADER);
        if (addressLanguageSyncAsyncTaskLoader == null) {
            loaderManager.initLoader(LIST_ADDRESS_LANGUAGE_SYNC_LOADER, null, this);
        }
        else {
            if (mAddressLanguageSyncAsyncTaskLoader!=null) {
                mAddressLanguageSyncAsyncTaskLoader.cancelLoadInBackground();
                mAddressLanguageSyncAsyncTaskLoader = null;
            }
            loaderManager.restartLoader(LIST_ADDRESS_LANGUAGE_SYNC_LOADER, null, this);
        }

    }
    private void stopImageSyncThread() {
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
    private void setFilterParametersEqualToTempParameters() {

        //Filter Buttons
        mSelectedPetType = mTempSelectedPetType;
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
        mSortOrder = mTempSortOrder;
    }
    private void showFilterDialog() {

        //Get the dialog view
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_filter, null);

        //region Setting the sort order
        mSortOptions = new ArrayList<>();
        mSortOptions.add(Utilities.getFlag(getApplicationContext()).getString(R.string.distance_ascending));
        mSortOptions.add(Utilities.getFlag(getApplicationContext()).getString(R.string.distance_descending));
        mSortOptions.add(Utilities.getFlag(getApplicationContext()).getString(R.string.age_ascending));
        mSortOptions.add(Utilities.getFlag(getApplicationContext()).getString(R.string.age_descending));
        mSortOptions.add(Utilities.getFlag(getApplicationContext()).getString(R.string.breed_ascending));
        mSortOptions.add(Utilities.getFlag(getApplicationContext()).getString(R.string.breed_descending));

        mDisplayedSortOptions = new ArrayList<>();
        mDisplayedSortOptions.add(getString(R.string.distance_ascending));
        mDisplayedSortOptions.add(getString(R.string.distance_descending));
        mDisplayedSortOptions.add(getString(R.string.age_ascending));
        mDisplayedSortOptions.add(getString(R.string.age_descending));
        mDisplayedSortOptions.add(getString(R.string.breed_ascending));
        mDisplayedSortOptions.add(getString(R.string.breed_descending));

        ArrayAdapter<String> spinnerAdapterSort = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mDisplayedSortOptions);
        dialogFilterSpinnerSort = dialogView.findViewById(R.id.dialog_filter_sort_spinner);
        dialogFilterSpinnerSort.setSelection(Utilities.getListPositionFromText(mSortOptions, mTempSortOrder));
        dialogFilterSpinnerSort.setAdapter(spinnerAdapterSort);
        dialogFilterSpinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                mTempSortOrder = mSortOptions.get(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //endregion

        //region Setting the my/all pets buttons behavior
        dialogFilterButtonListMyPets = dialogView.findViewById(R.id.dialog_filter_button_pet_list_my_pets);
        dialogFilterButtonListAllPets = dialogView.findViewById(R.id.dialog_filter_button_pet_list_all_pets);
        if (mUser!=null && mUser.getIF()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialogFilterButtonListMyPets.setChecked(mTempListChoice.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.dialog_filter_my_pets)));
                    dialogFilterButtonListAllPets.setChecked(!mTempListChoice.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.dialog_filter_my_pets)));
                }
            }, 100);
            dialogFilterButtonListMyPets.setTextOn(getString(R.string.dialog_filter_my_pets));
            dialogFilterButtonListMyPets.setTextOff(getString(R.string.dialog_filter_my_pets));
        }
        else if (mUser!=null && !mUser.getIF()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialogFilterButtonListMyPets.setChecked(mTempListChoice.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.dialog_filter_my_favorites)));
                    dialogFilterButtonListAllPets.setChecked(!mTempListChoice.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.dialog_filter_my_favorites)));
                }
            }, 100);
            dialogFilterButtonListMyPets.setTextOn(getString(R.string.dialog_filter_my_favorites));
            dialogFilterButtonListMyPets.setTextOff(getString(R.string.dialog_filter_my_favorites));
        }
        else {
            dialogFilterButtonListMyPets.setChecked(false);
            dialogFilterButtonListAllPets.setChecked(true);
        }
        dialogFilterButtonListMyPets.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dialogFilterButtonListAllPets.setChecked(false);
                    if (mUser!=null && !mUser.getIF()) mTempListChoice = Utilities.getFlag(getApplicationContext()).getString(R.string.dialog_filter_my_favorites);
                    else mTempListChoice = Utilities.getFlag(getApplicationContext()).getString(R.string.dialog_filter_my_pets);
                }
                else {
                    dialogFilterButtonListAllPets.setChecked(true);
                    mTempListChoice = Utilities.getFlag(getApplicationContext()).getString(R.string.dialog_filter_all_pets);
                }
            }
        });
        dialogFilterButtonListAllPets.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dialogFilterButtonListMyPets.setChecked(false);
                    mTempListChoice = Utilities.getFlag(getApplicationContext()).getString(R.string.dialog_filter_all_pets);
                }
                else {
                    dialogFilterButtonListMyPets.setChecked(true);
                }
            }
        });
        //endregion

        //region Getting the pet type
        ArrayAdapter<String> spinnerAdapterType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mDisplayedPetTypesList);
        dialogFilterSpinnerType = dialogView.findViewById(R.id.dialog_filter_type_spinner);
        dialogFilterSpinnerType.setSelection(Utilities.getListPositionFromText(mPetTypesList, mTempSelectedPetType));
        dialogFilterSpinnerType.setAdapter(spinnerAdapterType);
        dialogFilterSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                mTempSelectedPetType = mPetTypesList.get(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //endregion

        //region Setting the gender buttons and their behaviors
        dialogFilterButtonGenderAny = dialogView.findViewById(R.id.dialog_filter_button_gender_any);
        dialogFilterButtonGenderMale = dialogView.findViewById(R.id.dialog_filter_button_gender_male);
        dialogFilterButtonGenderFemale = dialogView.findViewById(R.id.dialog_filter_button_gender_female);
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
        dialogFilterButtonAgeAny = dialogView.findViewById(R.id.dialog_filter_button_age_any);
        dialogFilterButtonAgeToddler = dialogView.findViewById(R.id.dialog_filter_button_age_puppy);
        dialogFilterButtonAgeYoung = dialogView.findViewById(R.id.dialog_filter_button_age_young);
        dialogFilterButtonAgeAdult = dialogView.findViewById(R.id.dialog_filter_button_age_adult);
        dialogFilterButtonAgeSenior = dialogView.findViewById(R.id.dialog_filter_button_age_senior);
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
        dialogFilterButtonSizeAny = dialogView.findViewById(R.id.dialog_filter_button_size_any);
        dialogFilterButtonSizeSmall = dialogView.findViewById(R.id.dialog_filter_button_size_small);
        dialogFilterButtonSizeMedium = dialogView.findViewById(R.id.dialog_filter_button_size_medium);
        dialogFilterButtonSizeLarge = dialogView.findViewById(R.id.dialog_filter_button_size_large);
        dialogFilterButtonSizeExtraLarge = dialogView.findViewById(R.id.dialog_filter_button_size_extra_large);
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
        mDialogFilterAutoCompleteTextViewBreed =  dialogView.findViewById(R.id.dialog_filter_autocompletetextview_breed);
        final ArrayAdapter<String> breedArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mDisplayedAvailableDogBreeds);
        String breedToDisplay = Utilities.getDisplayedTextFromFlagText(mDisplayedAvailableDogBreeds, mAvailableDogBreeds, mTempSelectedDogBreed);
        mDialogFilterAutoCompleteTextViewBreed.setText(breedToDisplay);
        mDialogFilterAutoCompleteTextViewBreed.setAdapter(breedArrayAdapter);
        mDialogFilterAutoCompleteTextViewBreed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                mDialogFilterAutoCompleteTextViewBreed.showDropDown();
            }
        });
        ImageView dialogFilterImageViewArrowBreed = dialogView.findViewById(R.id.dialog_filter_arrow_breed);
        dialogFilterImageViewArrowBreed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialogFilterAutoCompleteTextViewBreed.showDropDown();
            }
        });
        //endregion

        //region Getting the coat length
        mDialogFilterAutoCompleteTextViewCoatLength =  dialogView.findViewById(R.id.dialog_filter_autocompletetextview_coat_length);
        final ArrayAdapter<String> coatLengthArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mDisplayedPetCoatLengths);
        String coatLengthToDisplay = Utilities.getDisplayedTextFromFlagText(mDisplayedPetCoatLengths, mPetCoatLengths, mTempSelectedCoatLength);
        mDialogFilterAutoCompleteTextViewCoatLength.setText(coatLengthToDisplay);
        mDialogFilterAutoCompleteTextViewCoatLength.setAdapter(coatLengthArrayAdapter);
        mDialogFilterAutoCompleteTextViewCoatLength.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                mDialogFilterAutoCompleteTextViewCoatLength.showDropDown();
            }
        });
        ImageView dialogFilterImageViewArrowCoatLength = dialogView.findViewById(R.id.dialog_filter_arrow_coat_length);
        dialogFilterImageViewArrowCoatLength.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialogFilterAutoCompleteTextViewCoatLength.showDropDown();
            }
        });
        //endregion

        //region Getting the checked button states
        dialogFilterCheckBoxGoodWithKids = dialogView.findViewById(R.id.dialog_filter_checkbox_good_with_kids);
        dialogFilterCheckBoxGoodWithKids.setChecked(mTempSelectedGoodWithKids);
        dialogFilterCheckBoxGoodWithKids.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mTempSelectedGoodWithKids = isChecked;
            }
        });

        dialogFilterCheckBoxGoodWithCats = dialogView.findViewById(R.id.dialog_filter_checkbox_good_with_cats);
        dialogFilterCheckBoxGoodWithCats.setChecked(mTempSelectedGoodWithCats);
        dialogFilterCheckBoxGoodWithCats.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mTempSelectedGoodWithCats = isChecked;
            }
        });

        dialogFilterCheckBoxGoodWithDogs = dialogView.findViewById(R.id.dialog_filter_checkbox_good_with_dogs);
        dialogFilterCheckBoxGoodWithDogs.setChecked(mTempSelectedGoodWithDogs);
        dialogFilterCheckBoxGoodWithDogs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mTempSelectedGoodWithDogs = isChecked;
            }
        });

        dialogFilterCheckBoxCastrated = dialogView.findViewById(R.id.dialog_filter_checkbox_castrated);
        dialogFilterCheckBoxCastrated.setChecked(mTempSelectedCastrated);
        dialogFilterCheckBoxCastrated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mTempSelectedCastrated = isChecked;
            }
        });

        dialogFilterCheckBoxHouseTrained = dialogView.findViewById(R.id.dialog_filter_checkbox_house_trained);
        dialogFilterCheckBoxHouseTrained.setChecked(mTempSelectedHouseTrained);
        dialogFilterCheckBoxHouseTrained.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mTempSelectedHouseTrained = isChecked;
            }
        });

        dialogFilterCheckBoxSpecialNeeds = dialogView.findViewById(R.id.dialog_filter_checkbox_special_needs);
        dialogFilterCheckBoxSpecialNeeds.setChecked(mTempSelectedSpecialNeeds);
        dialogFilterCheckBoxSpecialNeeds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mTempSelectedSpecialNeeds = isChecked;
            }
        });
        //endregion

        //region Getting the distance
        mDialogFilterEditTextDistance = dialogView.findViewById(R.id.dialog_filter_distance_edittext);
        String distance = ""+mTempPetDistance/1000;
        mDialogFilterEditTextDistance.setText(distance);
        //endregion

        //region Building the dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.filters);
        builder.setPositiveButton(R.string.save_and_go, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                saveSearchParametersToProfile();
                requestFilteredListFromFirebase();

                dialog.dismiss();
            }
        });
        builder.setNeutralButton(getString(R.string.reset), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                resetButtonsAndTempSearchParameters();

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

        android.app.AlertDialog dialog = builder.create();
        dialog.show();
        //endregion

    }
    private void resetButtonsAndTempSearchParameters() {
        dialogFilterSpinnerSort.setSelection(0);
        mTempSortOrder = mSortOptions.get(0);

        dialogFilterSpinnerType.setSelection(0);
        mTempSelectedPetType = mPetTypesList.get(0);

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

        String breedToDisplay = Utilities.getDisplayedTextFromFlagText(mDisplayedAvailableDogBreeds, mAvailableDogBreeds, mAvailableDogBreeds.get(0));
        mDialogFilterAutoCompleteTextViewBreed.setText(breedToDisplay);
        String coatLengthToDisplay = Utilities.getDisplayedTextFromFlagText(mDisplayedPetCoatLengths, mPetCoatLengths, mPetCoatLengths.get(0));
        mDialogFilterAutoCompleteTextViewCoatLength.setText(coatLengthToDisplay);

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

        mDialogFilterEditTextDistance.setText("0");
        mTempPetDistance = 0;
    }
    private void saveSearchParametersToProfile() {

        if (mUser.getIF() && TextUtils.isEmpty(mFoundation.getOI()) || !mUser.getIF() && TextUtils.isEmpty(mFamily.getOI())) {
            Toast.makeText(getBaseContext(), R.string.could_not_save_preferences, Toast.LENGTH_SHORT).show();
        }
        else {

            mTempPetDistance = getRequestedDistanceFromUserInput(mDialogFilterEditTextDistance.getText().toString());
            mTempSelectedBreed = Utilities.getFlagTextFromDisplayedText(
                    mDisplayedAvailableDogBreeds, mAvailableDogBreeds, mDialogFilterAutoCompleteTextViewBreed.getText().toString());
            mTempSelectedCoatLength = Utilities.getFlagTextFromDisplayedText(
                    mDisplayedPetCoatLengths, mPetCoatLengths, mDialogFilterAutoCompleteTextViewCoatLength.getText().toString());

            setFilterParametersEqualToTempParameters();
            if (mUser.getIF()) {
                saveSearchParametersToLocalFoundationProfile();
                mFirebaseDao.updateObject(mFoundation);
            }
            else {
                saveSearchParametersToLocalFamilyProfile();
                mFirebaseDao.updateObject(mFamily);
            }
        }
    }
    private void saveSearchParametersToLocalFamilyProfile() {
        mFamily.setTP(mSelectedPetType);
        mFamily.setGP(mSelectedGender);
        mFamily.setAP(mSelectedAgeRange);
        mFamily.setSP(mSelectedSize);

        if (mSelectedPetType.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.dog))) mFamily.setDRP(mSelectedBreed);
        else if (mSelectedPetType.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.cat))) mFamily.setCRP(mSelectedBreed);
        else if (mSelectedPetType.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.parrot))) mFamily.setPRP(mSelectedBreed);

        mFamily.setCLP(mSelectedCoatLength);

        mFamily.setGKP(mSelectedGoodWithKids);
        mFamily.setGCP(mSelectedGoodWithCats);
        mFamily.setGDP(mSelectedGoodWithDogs);
        mFamily.setCsP(mSelectedCastrated);
        mFamily.setHTP(mSelectedHouseTrained);
        mFamily.setSNP(mSelectedSpecialNeeds);

        mFamily.setDP(mPetDistance);

        mFamily.setSrT(mSortOrder);
    }
    private void saveSearchParametersToLocalFoundationProfile() {
        mFoundation.setTP(mSelectedPetType);
        mFoundation.setGP(mSelectedGender);
        mFoundation.setAP(mSelectedAgeRange);
        mFoundation.setSP(mSelectedSize);

        if (mSelectedPetType.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.dog))) mFoundation.setDRP(mSelectedBreed);
        else if (mSelectedPetType.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.cat))) mFoundation.setCRP(mSelectedBreed);
        else if (mSelectedPetType.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.parrot))) mFoundation.setPRP(mSelectedBreed);

        mFoundation.setCLP(mSelectedCoatLength);

        mFoundation.setGKP(mSelectedGoodWithKids);
        mFoundation.setGCP(mSelectedGoodWithCats);
        mFoundation.setGDP(mSelectedGoodWithDogs);
        mFoundation.setCsP(mSelectedCastrated);
        mFoundation.setHTP(mSelectedHouseTrained);
        mFoundation.setSNP(mSelectedSpecialNeeds);

        mFoundation.setDP(mPetDistance);

        mFoundation.setSrT(mSortOrder);
    }
    private void requestFilteredListFromFirebase() {

        final List<QueryCondition> queryConditions = new ArrayList<>();
        QueryCondition queryCondition;

        //Setting the pet type
        if (!TextUtils.isEmpty(mSelectedPetType) &&
                !mSelectedPetType.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.filter_option_any))) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "tp", mSelectedPetType, true, 0);
            queryConditions.add(queryCondition);
        }

        //Setting the gender
        if (!TextUtils.isEmpty(mSelectedGender) &&
                !mSelectedGender.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.filter_option_any))) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "gn", mSelectedGender, true, 0);
            queryConditions.add(queryCondition);
        }

        //Setting the age range
        if (!TextUtils.isEmpty(mSelectedAgeRange) &&
                !mSelectedAgeRange.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.filter_option_any))) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "agR", mSelectedAgeRange, true, 0);
            queryConditions.add(queryCondition);
        }

        //Setting the size
        if (!TextUtils.isEmpty(mSelectedSize) &&
                !mSelectedSize.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.filter_option_any))) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "sz", mSelectedSize, true, 0);
            queryConditions.add(queryCondition);
        }

        //Setting the breed/race
        if (!TextUtils.isEmpty(mSelectedBreed) &&
                !mSelectedBreed.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.filter_option_any))) {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "rc", mSelectedBreed, true, 0);
            queryConditions.add(queryCondition);
        }

        //Setting the coat lengths
        if (!TextUtils.isEmpty(mSelectedCoatLength) &&
                !mSelectedCoatLength.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.filter_option_any))) {
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
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "se", "Israel", false, 0);
            queryConditions.add(queryCondition);
        }
        else {
            queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "cn", "Israel", false, 0);
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


        //Requesting the list depending on the type of list
        showLoadingIndicator();
        if (!TextUtils.isEmpty(mTempListChoice) &&
                mTempListChoice.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.dialog_filter_my_pets))) {
            if (mFoundation==null) {
                //If the Foundation is mull, then try waiting for Firebase to fetch the Foundation data before searching for the pets
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        QueryCondition queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "oi", mFoundation.getUI(), true, 0);
                        queryConditions.add(queryCondition);
                        mFirebaseDao.requestObjectsWithConditions(new Pet(), queryConditions);
                    }
                }, 100);
            }
            else {
                //Othersiee, perform a search now
                queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "oi", mFoundation.getUI(), true, 0);
                queryConditions.add(queryCondition);
                mFirebaseDao.requestObjectsWithConditions(new Pet(), queryConditions);
            }
        }
        else if (!TextUtils.isEmpty(mTempListChoice) &&
                mTempListChoice.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.dialog_filter_my_favorites))) {

            mFavoritePetIds = mFamily.getFPI();
            mDocumentCounter = 0;
            mPetList = new ArrayList<>();

            for (String id : mFavoritePetIds) {
                List<QueryCondition> currentQueryConditions = new ArrayList<>(queryConditions);
                queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "ui", id, true, 0);
                currentQueryConditions.add(queryCondition);
                mFirebaseDao.requestObjectsWithConditions(new Pet(), currentQueryConditions);
            }
        }
        else {
            mFirebaseDao.requestObjectsWithConditions(new Pet(), queryConditions);
        }

    }
    private void prepareAndDisplayPetList() {

        getPetsAtDistance();
        handlePetFavorites();
        sortPets();
        startImageSyncThread();
        startAddressLanguageUpdateThread();
        hideLoadingIndicator();
        mPetsRecyclerViewAdapter.setContents(mPetsAtDistance);
    }
    private void handlePetFavorites() {

        if (mFamily!=null) {
            List<String> favoriteIds = new ArrayList<>(mFamily.getFPI());
            for (Pet pet : mPetsAtDistance) {
                for (String id : favoriteIds) {
                    if (pet.getUI().equals(id)) {
                        pet.setFv(true);
                        favoriteIds.remove(id);
                        break;
                    }
                }
            }
        }
    }
    private void getPetsAtDistance() {
        mPetsAtDistance = new ArrayList<>();
        for (Pet pet : mPetList) {
            int distance = Utilities.getDistanceFromLatLong(mUserLatitude, mUserLongitude, pet.getGeo().getLatitude(), pet.getGeo().getLongitude());
            if (distance <= mPetDistance) {
                pet.setDt(distance);
                mPetsAtDistance.add(pet);
            }
        }
    }
    private void sortPets() {

        Pet[] pets = new Pet[mPetsAtDistance.size()];
        pets = mPetsAtDistance.toArray(pets);

        if (mSortOrder.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.distance_ascending))) Arrays.sort(pets, Pet.PetDistanceComparatorAscending);
        else if (mSortOrder.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.distance_descending))) Arrays.sort(pets, Pet.PetDistanceComparatorDescending);
        else if (mSortOrder.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.age_ascending))) Arrays.sort(pets, Pet.PetAgeComparatorAscending);
        else if (mSortOrder.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.age_descending))) Arrays.sort(pets, Pet.PetAgeComparatorDescending);
        else if (mSortOrder.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.breed_ascending))) Arrays.sort(pets, Pet.PetBreedComparatorAscending);
        else if (mSortOrder.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.breed_descending))) Arrays.sort(pets, Pet.PetBreedComparatorDescending);
        else Arrays.sort(pets, Pet.PetDistanceComparatorAscending);

        mPetsAtDistance = Arrays.asList(pets);
    }
    private void setTempFiltersAccordingToFamilyOrFoundationPreferences() {

        if (mUser.getIF()) {
            if (!TextUtils.isEmpty(mFoundation.getSrT())) mTempSortOrder = mFoundation.getSrT();
            else mTempSortOrder = Utilities.getFlag(getApplicationContext()).getString(R.string.distance_ascending);

            if (!TextUtils.isEmpty(mFoundation.getTP())) mTempSelectedPetType = mFoundation.getTP();
            if (!TextUtils.isEmpty(mFoundation.getGP())) mTempSelectedGender = mFoundation.getGP();
            if (!TextUtils.isEmpty(mFoundation.getAP())) mTempSelectedAgeRange = mFoundation.getAP();
            if (!TextUtils.isEmpty(mFoundation.getSP())) mTempSelectedSize = mFoundation.getSP();
            if (!TextUtils.isEmpty(mFoundation.getDRP())) mTempSelectedDogBreed = mFoundation.getDRP();
            if (!TextUtils.isEmpty(mFoundation.getCRP())) mTempSelectedCatBreed = mFoundation.getCRP();
            if (!TextUtils.isEmpty(mFoundation.getPRP())) mTempSelectedParrotBreed = mFoundation.getPRP();
            if (!TextUtils.isEmpty(mFoundation.getCLP())) mTempSelectedCoatLength = mFoundation.getCLP();

            mTempSelectedGoodWithKids = mFoundation.getGKP();
            mTempSelectedGoodWithCats = mFoundation.getGCP();
            mTempSelectedGoodWithDogs = mFoundation.getGDP();
            mTempSelectedCastrated = mFoundation.getCsP();
            mTempSelectedHouseTrained = mFoundation.getHTP();
            mTempSelectedSpecialNeeds = mFoundation.getSNP();

            mTempPetDistance = mFoundation.getDP();
        }
        else {
            if (!TextUtils.isEmpty(mFamily.getSrT())) mTempSortOrder = mFamily.getSrT();
            else mTempSortOrder = Utilities.getFlag(getApplicationContext()).getString(R.string.distance_ascending);

            if (!TextUtils.isEmpty(mFamily.getTP())) mTempSelectedPetType = mFamily.getTP();
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
    }
    private void openPetProfile(int clickedItemIndex) {
        if (mUser.getIF()) {
            Intent intent = new Intent(this, UpdatePetActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.pet_profile_parcelable), mPetsAtDistance.get(clickedItemIndex));
            bundle.putParcelable(getString(R.string.foundation_profile_parcelable), mFoundation);
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivityForResult(intent, SHOW_PET_ACTIVITY_KEY);
        }
        else {
            Intent intent = new Intent(this, ShowPetProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.pet_profile_parcelable), mPetsAtDistance.get(clickedItemIndex));
            bundle.putParcelable(getString(R.string.family_profile_parcelable), mFamily);
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivityForResult(intent, SHOW_PET_ACTIVITY_KEY);
        }
    }
    private void openMap() {
        if (mPetsAtDistance!=null && mPetsAtDistance.size()>0) {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putParcelableArrayListExtra(getString(R.string.pets_at_distance_parcelable), new ArrayList<>(mPetsAtDistance));
            startActivity(intent);
        }
    }


    //View click listeners
    @OnClick(R.id.pet_list_filter_and_sort_button) public void onFilterAndSortButtonClick() {
        setTempFiltersAccordingToFamilyOrFoundationPreferences();
        setFilterParametersEqualToTempParameters();
        showFilterDialog();
    }
    @OnClick(R.id.pet_list_map_button) public void onMapButtonClick() {
        openMap();
    }


    //Communication with other classes:

    //Communication with RecyclerView adapters
    @Override public void onPetListItemClick(int clickedItemIndex) {
        if (mPetsAtDistance!=null && mPetsAtDistance.size()>0) {
            mPetsRecyclerViewAdapter.setSelectedProfile(clickedItemIndex);
            openPetProfile(clickedItemIndex);
        }
    }
    @Override public void onPetLoveImageClick(int clickedItemIndex, boolean isFavorited) {

        Pet pet = mPetsAtDistance.get(clickedItemIndex);
        String currentPetUI = pet.getUI();
        List<String> favoriteIds = mFamily.getFPI();
        if (favoriteIds==null) favoriteIds = new ArrayList<>();
        boolean isInFavoritesList = false;
        for (String id : favoriteIds) {
            if (currentPetUI.equals(id)) {
                mPetsAtDistance.get(clickedItemIndex).setFv(isFavorited);
                isInFavoritesList = true;
                break;
            }
        }

        if (isFavorited && !isInFavoritesList) favoriteIds.add(currentPetUI);
        else if (!isFavorited&& isInFavoritesList) favoriteIds.remove(currentPetUI);

        //mPetsRecyclerViewAdapter.setContents(mPetsAtDistance);

        mFamily.setFPI(favoriteIds);
        mFirebaseDao.updateObjectKeyValuePair(mFamily, "fpi", favoriteIds);
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
    @NonNull @Override public Loader<List<Pet>> onCreateLoader(int id, @Nullable Bundle args) {

        if (id == LIST_MAIN_IMAGES_SYNC_LOADER && mImageSyncAsyncTaskLoader==null) {
            mImageSyncAsyncTaskLoader =  new ImageSyncAsyncTaskLoader(this, getString(R.string.task_sync_list_main_images),
                    getString(R.string.pet_profile), mPetsAtDistance, null, null, this);
            return mImageSyncAsyncTaskLoader;
        }
        else if (id == LIST_ADDRESS_LANGUAGE_SYNC_LOADER && mAddressLanguageSyncAsyncTaskLoader==null) {
            mAddressLanguageSyncAsyncTaskLoader =  new AddressLanguageAsyncTaskLoader(this, mPetsAtDistance);
            return mAddressLanguageSyncAsyncTaskLoader;
        }
        return new ImageSyncAsyncTaskLoader(this, "", null, null, null, null, this);
    }
    @Override public void onLoadFinished(@NonNull Loader<List<Pet>> loader, List<Pet> data) {
        if (loader.getId() == LIST_MAIN_IMAGES_SYNC_LOADER) {
            mPetsRecyclerViewAdapter.notifyDataSetChanged();
            stopImageSyncThread();
        }
        else if (loader.getId() == LIST_ADDRESS_LANGUAGE_SYNC_LOADER) {
            mPetsAtDistance = data;
            mPetsRecyclerViewAdapter.setContents(mPetsAtDistance);
            if (getLoaderManager()!=null) getLoaderManager().destroyLoader(LIST_ADDRESS_LANGUAGE_SYNC_LOADER);
        }
    }
    @Override public void onLoaderReset(@NonNull Loader<List<Pet>> loader) {

    }
    private static class AddressLanguageAsyncTaskLoader extends AsyncTaskLoader<List<Pet>> {

        private final List<Pet> mPets;

        AddressLanguageAsyncTaskLoader(@NonNull Context context, List<Pet> pets) {
            super(context);
            this.mPets = pets;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Nullable @Override public List<Pet> loadInBackground() {

            for (Pet pet : mPets) {
                updatePetAddressWithLocalizedText(pet);
            }
            return mPets;
        }

        private void updatePetAddressWithLocalizedText(Pet pet) {

            try {
                Geocoder gc = new Geocoder(getContext(), getContext().getResources().getConfiguration().locale);
                String address = Utilities.getAddressStringFromComponents(pet.getStN(), pet.getSt(), pet.getCt(), pet.getSe(), pet.getCn());
                List<Address> addresses = gc.getFromLocationName(address, 1); // get the found Address Objects

                if (addresses.size()>0) {
                    pet.setCnL(Utilities.getCountryNameFromAddress(addresses.get(0)));
                    pet.setSeL(Utilities.getStateFromAddress(addresses.get(0)));
                    pet.setCtL(Utilities.getCityFromAddress(addresses.get(0)));
                    pet.setStL(Utilities.getStreetNameFromAddress(addresses.get(0)));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Communication with ImageSyncAsyncTaskLoader
    @Override public void onDisplayRefreshRequested() {

    }

    //Communication with Firebase Dao handler
    @Override public void onPetListFound(List<Pet> pets) {
        if (pets == null) return;

        if (!TextUtils.isEmpty(mTempListChoice) &&
                mTempListChoice.equals(Utilities.getFlag(getApplicationContext()).getString(R.string.dialog_filter_my_favorites))) {
            if (pets.size()>0 && pets.get(0) !=null) {
                mPetList = new ArrayList<>(mPetList); //A new arraylist is built since the old one may be "busy" while trying to update it, resulting in an error
                Pet pet = pets.get(0);
                pet.setFv(true);
                mPetList.add(pet);
            }
            mDocumentCounter++;
            if (mDocumentCounter == mFavoritePetIds.size()) {
                prepareAndDisplayPetList();
            }
        }
        else {
            mPetList = pets;
            prepareAndDisplayPetList();
        }
    }
    @Override public void onFamilyListFound(List<Family> families) {
        if (families == null) return;

        //If family is not in database then create it, otherwise update mPet
        if (families.size() == 0 || families.get(0)==null) {
            Toast.makeText(getBaseContext(), R.string.must_create_family_profile, Toast.LENGTH_SHORT).show();
            Utilities.startUpdateFamilyProfileActivity(PetListActivity.this);
        }
        else {
            mFamily = families.get(0);
            setTempFiltersAccordingToFamilyOrFoundationPreferences();
            setFilterParametersEqualToTempParameters();
            requestFilteredListFromFirebase();
        }
    }
    @Override public void onFoundationListFound(List<Foundation> foundations) {
        if (foundations == null) return;

        //If family is not in database then create it, otherwise update mPet
        if (foundations.size() == 0 || foundations.get(0)==null) {
            Toast.makeText(getBaseContext(), R.string.must_create_family_profile, Toast.LENGTH_SHORT).show();
            Utilities.startUpdateFamilyProfileActivity(PetListActivity.this);
        }
        else {
            mFoundation = foundations.get(0);
            setTempFiltersAccordingToFamilyOrFoundationPreferences();
            setFilterParametersEqualToTempParameters();
            requestFilteredListFromFirebase();
        }
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
