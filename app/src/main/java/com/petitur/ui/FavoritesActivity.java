package com.petitur.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
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

public class FavoritesActivity extends AppCompatActivity implements
        PetListRecycleViewAdapter.PetListItemClickHandler,
        CustomLocationListener.LocationListenerHandler,
        LoaderManager.LoaderCallbacks<String>,
        ImageSyncAsyncTaskLoader.OnImageSyncOperationsHandler,
        FirebaseDao.FirebaseOperationsHandler {


    //regionParameters
    private static final String DEBUG_TAG = "Petitur Pet List";
    private static final int LIST_MAIN_IMAGES_SYNC_LOADER = 3695;
    @BindView(R.id.favorites_loading_indicator) ProgressBar mProgressBarLoadingIndicator;
    @BindView(R.id.favorites_recyclerview) RecyclerView mPetsRecyclerView;
    private Unbinder mBinding;
    private PetListRecycleViewAdapter mPetsRecyclerViewAdapter;
    private double mUserLongitude;
    private double mUserLatitude;
    private boolean hasLocationPermissions;
    private LocationManager mLocationManager;
    private CustomLocationListener mLocationListener;
    private List<Pet> mPets;
    private ImageSyncAsyncTaskLoader mImageSyncAsyncTaskLoader;
    private int mPetsRecyclerViewPosition;
    private FirebaseDao mFirebaseDao;
    private Family mFamily;
    private FirebaseUser mCurrentFirebaseUser;
    private String mTempSortField;
    private SortOptionsRecycleViewAdapter mListAdapter;
    private String mSortField;
    private boolean mSortAscending;
    private boolean mTempSortAscending;
    private List<String> mSortOptions;
    private List<String> mFavoritePetIds;
    private int mDocumentCounter;
    //endregion


    //Lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        getExtras();
        initializeParameters();
        getFamilyProfileFromFirebase();
        setupRecyclerView();
        if (hasLocationPermissions) startListeningForUserLocation();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        mBinding.unbind();
    }
    @Override public void onBackPressed() {
        super.onBackPressed();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorites_menu, menu);
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


    //Functionality methods
    private void getExtras() {
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

        mFavoritePetIds = new ArrayList<>();

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
    private void setupRecyclerView() {

        //Setting up the RecyclerView adapters
        mPetsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (mPetsRecyclerViewAdapter ==null) mPetsRecyclerViewAdapter = new PetListRecycleViewAdapter(this, this, null, mUserLatitude, mUserLongitude);
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

        //Sort options
        mSortField = mTempSortField;
        mSortAscending = mTempSortAscending;
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
                if (mPets!=null && mPets.size()>0) {
                    sortPets();
                    mPetsRecyclerViewAdapter.setContents(mPets);
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
        mFamily.setSrT(mTempSortField);
        mFamily.setSrA(mTempSortAscending);
    }
    private void requestFavoritesListFromFirebase() {

        showLoadingIndicator();

        mFavoritePetIds = mFamily.getFPI();
        mDocumentCounter = 0;
        mPets = new ArrayList<>();
        for (String id : mFavoritePetIds) {
            mFirebaseDao.requestObjectWithId(new Pet(id));
        }
        //requestNextPetInFavorites();

    }
    private void updatePetDistances() {
        for (Pet pet : mPets) {
            int distance = Utilities.getDistanceFromLatLong(mUserLatitude, mUserLongitude, pet.getGeo().getLatitude(), pet.getGeo().getLongitude());
            pet.setDt(distance);
        }
    }
    private void sortAndDisplayPetList() {

        updatePetDistances();
        sortPets();
        startImageSyncThread();
        hideLoadingIndicator();
        mPetsRecyclerViewAdapter.setContents(mPets);
    }
    private void sortPets() {

        Pet[] pets = new Pet[mPets.size()];
        pets = mPets.toArray(pets);

        if (mSortField.equals(getString(R.string.distance)) && mSortAscending) Arrays.sort(pets, Pet.PetDistanceComparatorAscending);
        else if (mSortField.equals(getString(R.string.distance)) && !mSortAscending) Arrays.sort(pets, Pet.PetDistanceComparatorDescending);
        else if (mSortField.equals(getString(R.string.age)) && mSortAscending) Arrays.sort(pets, Pet.PetAgeComparatorAscending);
        else if (mSortField.equals(getString(R.string.age)) && !mSortAscending) Arrays.sort(pets, Pet.PetAgeComparatorDescending);
        else if (mSortField.equals(getString(R.string.breed)) && mSortAscending) Arrays.sort(pets, Pet.PetBreedComparatorAscending);
        else if (mSortField.equals(getString(R.string.breed)) && !mSortAscending) Arrays.sort(pets, Pet.PetBreedComparatorDescending);
        else Arrays.sort(pets, Pet.PetDistanceComparatorAscending);

        mPets = Arrays.asList(pets);
    }
    private void setTempSortParametersAccordingToFamilyPreferences() {
        if (!TextUtils.isEmpty(mFamily.getSrT())) mTempSortField = mFamily.getSrT();
        else mTempSortField = getString(R.string.distance);

        mTempSortAscending = mFamily.getSrA();
    }
    private void openPetProfile(int clickedItemIndex) {
        Intent intent = new Intent(this, ShowPetProfileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.pet_profile_parcelable), mPets.get(clickedItemIndex));
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }


    //View click listeners
    @OnClick(R.id.favorites_sort_button) public void onSortButtonClick() {
        setTempSortParametersAccordingToFamilyPreferences();
        setSearchParametersEqualToTempParameters();
        showSortDialog();
    }


    //Communication with other classes:

    //Communication with RecyclerView adapters
    @Override public void onPetListItemClick(int clickedItemIndex) {
        if (mPets!=null && mPets.size()>0) {
            mPetsRecyclerViewAdapter.setSelectedProfile(clickedItemIndex);
            openPetProfile(clickedItemIndex);
        }
    }
    @Override public void onPetLoveImageClick(int clickedItemIndex, boolean isFavorited) {

        Pet pet = mPets.get(clickedItemIndex);
        String currentPetUI = pet.getUI();
        List<String> favoriteIds = mFamily.getFPI();
        boolean isInFavoritesList = false;
        for (String id : favoriteIds) {
            if (currentPetUI.equals(id)) {
                pet.setFv(isFavorited);
                isInFavoritesList = true;
                break;
            }
        }

        if (isFavorited && !isInFavoritesList) favoriteIds.add(currentPetUI);
        else if (!isFavorited&& isInFavoritesList) favoriteIds.remove(currentPetUI);

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
    @NonNull @Override public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {

        if (id == LIST_MAIN_IMAGES_SYNC_LOADER && mImageSyncAsyncTaskLoader==null) {
            mImageSyncAsyncTaskLoader =  new ImageSyncAsyncTaskLoader(this, getString(R.string.task_sync_list_main_images),
                    getString(R.string.pet_profile), mPets, null, null, this);
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
        if (pets.size()>0 && pets.get(0) !=null) {
            mPets = new ArrayList<>(mPets); //A new arraylist is built since the old one may be "busy" while trying to update it, resulting in an error
            Pet pet = pets.get(0);
            pet.setFv(true);
            mPets.add(pet);
            //requestNextPetInFavorites();
            mDocumentCounter++;
        }
        if (mDocumentCounter == mFavoritePetIds.size()) {
            sortAndDisplayPetList();
        }
    }
    @Override public void onFamilyListFound(List<Family> families) {
        if (families == null) return;

        //If family is not in database then create it, otherwise update mPet
        if (families.size() == 0 || families.get(0)==null) {
            Toast.makeText(getBaseContext(), R.string.must_create_family_profile, Toast.LENGTH_SHORT).show();
            Utilities.startUpdateFamilyProfileActivity(FavoritesActivity.this);
        }
        else {
            mFamily = families.get(0);
            setTempSortParametersAccordingToFamilyPreferences();
            setSearchParametersEqualToTempParameters();
            requestFavoritesListFromFirebase();
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
