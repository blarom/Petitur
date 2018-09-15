package com.petitur.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.petitur.R;
import com.petitur.data.Family;
import com.petitur.data.FirebaseDao;
import com.petitur.data.Foundation;
import com.petitur.data.MapMarker;
import com.petitur.data.Pet;
import com.petitur.data.QueryCondition;
import com.petitur.data.User;
import com.petitur.resources.Utilities;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SearchProfileActivity extends BaseActivity implements
        FirebaseDao.FirebaseOperationsHandler,
        PetProfileFragment.PetProfileFragmentOperationsHandler {


    //region Parameters
    private Unbinder mBinding;
    private FirebaseDao mFirebaseDao;
    private Family mFamily;
    private Pet mPet;
    private Foundation mFoundation;
    private FirebaseUser mCurrentFirebaseUser;
    private String mQuery;
    @BindView(R.id.search_profile_query) EditText mQueryEditText;
    @BindView(R.id.search_profile_loading_indicator) ProgressBar mProgressBarLoadingIndicator;
    @BindView(R.id.search_profile_fragment_container) FrameLayout mFragmentContainer;
    @BindView(R.id.search_profile_query_container) FrameLayout mQueryContainer;
    private FragmentManager mFragmentManager;
    private FamilyProfileFragment mFamilyProfileFragment;
    private FoundationProfileFragment mFoundationProfileFragment;
    private String mRequestedFoundationProfileID;
    private PetProfileFragment mPetProfileFragment;
    private boolean mRequestedFoundationForPet;
    private String mFullName;
    private boolean mReceivedFullPetProfile;
    private String mRequestedPetProfileID;
    private boolean mReceivedFullFamilyProfile;
    private boolean mReceivedFullFoundationProfile;
    private String mRequestedFamilyProfileID;
    //endregion


    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_profile);

        getExtras();
        initializeParameters();
        handleProfileActions();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        if (mBinding!=null) mBinding.unbind();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        if (!mReceivedFullPetProfile) getMenuInflater().inflate(R.menu.search_profile_menu, menu);
        else getMenuInflater().inflate(R.menu.show_pet_menu, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        switch (itemThatWasClickedId) {
            case android.R.id.home:
                if (mReceivedFullPetProfile) {
                    Intent data = new Intent();
                    data.putExtra(getString(R.string.pet_profile_parcelable), mPet);
                    data.putExtra(getString(R.string.family_profile_parcelable), mFamily);
                    setResult(RESULT_OK, data);
                }
                onBackPressed();
                return true;
            case R.id.action_search:
                getProfileFromFirebase();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //Functionality methods
    private void getExtras() {
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.foundation_profile_id))) {
            mRequestedFoundationProfileID = intent.getStringExtra(getString(R.string.foundation_profile_id));
        }
        if (intent.hasExtra(getString(R.string.family_profile_id))) {
            mRequestedFamilyProfileID = intent.getStringExtra(getString(R.string.family_profile_id));
        }
        if (intent.hasExtra(getString(R.string.pet_profile_id))) {
            mRequestedPetProfileID = intent.getStringExtra(getString(R.string.pet_profile_id));
        }

        if (intent.hasExtra(getString(R.string.pet_profile_parcelable))) {
            mPet = intent.getParcelableExtra(getString(R.string.pet_profile_parcelable));
        }
        if (intent.hasExtra(getString(R.string.family_profile_parcelable))) {
            mFamily = intent.getParcelableExtra(getString(R.string.family_profile_parcelable));
        }
        if (intent.hasExtra(getString(R.string.foundation_profile_parcelable))) {
            mFoundation = intent.getParcelableExtra(getString(R.string.foundation_profile_parcelable));
        }

        if (intent.hasExtra(getString(R.string.user_name))) {
            mFullName = intent.getStringExtra(getString(R.string.user_name));
        }
    }
    private void initializeParameters() {

        mFirebaseDao = new FirebaseDao(this, this);
        mCurrentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mBinding =  ButterKnife.bind(this);

        mReceivedFullPetProfile = mPet != null;
        mReceivedFullFamilyProfile = mFamily != null;
        mReceivedFullFoundationProfile = mFoundation != null;

        int titleTextId;
        if (mReceivedFullPetProfile) titleTextId = R.string.pet_profile;
        else if (mReceivedFullFamilyProfile) titleTextId = R.string.family_profile;
        else if (mReceivedFullFoundationProfile) titleTextId = R.string.foundation_profile;
        else titleTextId = R.string.find_a_profile;

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(titleTextId);
        }

        Utilities.hideSoftKeyboard(this);
        mFragmentContainer.setVisibility(View.INVISIBLE);
        mFragmentManager = getSupportFragmentManager();

        mQueryEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                getProfileFromFirebase();
                Utilities.hideSoftKeyboard(SearchProfileActivity.this);
            }
            return true;
        } });

        if (!TextUtils.isEmpty(mRequestedFoundationProfileID)) {
            mQuery = mRequestedFoundationProfileID;
            mQueryEditText.setText(mQuery);
        }
        else if (!TextUtils.isEmpty(mRequestedFamilyProfileID)) {
            mQuery = mRequestedFamilyProfileID;
            mQueryEditText.setText(mQuery);
        }
        else if (!TextUtils.isEmpty(mRequestedPetProfileID)) {
            mQuery = mRequestedPetProfileID;
            mQueryEditText.setText(mQuery);
        }
        else if (mReceivedFullFoundationProfile || mReceivedFullFamilyProfile || mReceivedFullPetProfile) {
            mQueryContainer.setVisibility(View.GONE);
        }
        else {
            mQuery = "";
        }

        mRequestedFoundationForPet = false;

    }
    private void handleProfileActions() {

        if (mReceivedFullPetProfile) {
            showPetProfileFragment();
            getFoundationProfileForPet();
        }
        else if (mReceivedFullFoundationProfile) {
            showFoundationProfileFragment();
        }
        else if (mReceivedFullFamilyProfile) {
            showFamilyProfileFragment();
        }
        else if (!TextUtils.isEmpty(mRequestedFoundationProfileID)
                || !TextUtils.isEmpty(mRequestedFamilyProfileID)
                || !TextUtils.isEmpty(mRequestedPetProfileID)) {
            getProfileFromFirebase();
        }
    }
    private void getProfileFromFirebase() {
        if (mCurrentFirebaseUser != null) {

            mQuery = mQueryEditText.getText().toString();

            if (!TextUtils.isEmpty(mQuery)) {

                List<QueryCondition> queryConditions = new ArrayList<>();
                QueryCondition queryCondition;
                if (Utilities.isCandidateForEmail(mQuery) && mRequestedFoundationProfileID==null) {

                    queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "em", mQuery, true, 0);
                    queryConditions.add(queryCondition);

                    //TODO: continue making adjustments here

                    showLoadingIndicator();
                    mFirebaseDao.requestObjectsWithConditions(new Foundation(), queryConditions);
                    if (TextUtils.isEmpty(mRequestedFoundationProfileID)) mFirebaseDao.requestObjectsWithConditions(new Family(), queryConditions);
                }
                else if (Utilities.isCandidateForFirebaseId(mQuery)) {

                    queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "ui", mQuery, true, 0);
                    queryConditions.add(queryCondition);

                    showLoadingIndicator();
                    if (mRequestedFoundationProfileID != null) {
                        mFirebaseDao.requestObjectsWithConditions(new Foundation(), queryConditions);
                    }
                    else if (mRequestedPetProfileID != null) {
                        mFirebaseDao.requestObjectsWithConditions(new Pet(), queryConditions);
                    }
                    else {
                        mFirebaseDao.requestObjectsWithConditions(new Foundation(), queryConditions);
                        mFirebaseDao.requestObjectsWithConditions(new Family(), queryConditions);
                        mFirebaseDao.requestObjectsWithConditions(new Pet(), queryConditions);
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.invalid_search_parameters, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void showFamilyProfileFragment() {

        mFragmentContainer.setVisibility(View.VISIBLE);
        if (mFamilyProfileFragment==null) {
            mFamilyProfileFragment = new FamilyProfileFragment();

            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.family_profile_parcelable), mFamily);
            mFamilyProfileFragment.setArguments(bundle);

            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.search_profile_fragment_container, mFamilyProfileFragment);
            fragmentTransaction.commit();
        }
        else {
            mFamilyProfileFragment.updateProfile(mFamily);
        }
    }
    private void showPetProfileFragment() {

        mFragmentContainer.setVisibility(View.VISIBLE);
        if (mPetProfileFragment==null) {
            mPetProfileFragment = new PetProfileFragment();

            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.pet_profile_parcelable), mPet);
            bundle.putParcelable(getString(R.string.family_profile_parcelable), mFamily);
            bundle.putString(getString(R.string.user_name), mFullName);
            mPetProfileFragment.setArguments(bundle);

            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.search_profile_fragment_container, mPetProfileFragment);
            fragmentTransaction.commit();
        }
        else {
            mPetProfileFragment.updateProfile(mPet);
        }
    }
    private void showFoundationProfileFragment() {

        mFragmentContainer.setVisibility(View.VISIBLE);
        if (mFoundationProfileFragment==null) {
            mFoundationProfileFragment = new FoundationProfileFragment();

            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.family_profile_parcelable), mFoundation);
            mFoundationProfileFragment.setArguments(bundle);

            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.search_profile_fragment_container, mFoundationProfileFragment);
            fragmentTransaction.commit();
        }
        else {
            mFoundationProfileFragment.updateProfile(mFoundation);
        }
    }
    private void showLoadingIndicator() {
        mProgressBarLoadingIndicator.setVisibility(View.VISIBLE);
        mFragmentContainer.setVisibility(View.INVISIBLE);
    }
    private void hideLoadingIndicator() {
        mProgressBarLoadingIndicator.setVisibility(View.INVISIBLE);
    }
    private void getFoundationProfileForPet() {
        if (!TextUtils.isEmpty(mPet.getOI())) {

            List<QueryCondition> queryConditions = new ArrayList<>();
            QueryCondition queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "ui", mPet.getOI(), true, 0);
            queryConditions.add(queryCondition);

            mRequestedFoundationForPet = true;
            mFirebaseDao.requestObjectsWithConditions(new Foundation(), queryConditions);
        }
    }


    //Communication with Firebase Dao handler
    @Override public void onPetListFound(List<Pet> pets) {
        if (pets == null) return;

        //If family is not in database then create it, otherwise update mPet
        if (pets.size() == 0 || pets.get(0)==null) {
            Toast.makeText(getBaseContext(), R.string.no_pet_found_for_your_search, Toast.LENGTH_SHORT).show();
        }
        else {
            mPet = pets.get(0);
            hideLoadingIndicator();
            showPetProfileFragment();
            getFoundationProfileForPet();
        }
    }
    @Override public void onFamilyListFound(List<Family> families) {
        if (families == null) return;

        //If family is not in database then create it, otherwise update mPet
        if (families.size() == 0 || families.get(0)==null) {
            Toast.makeText(getBaseContext(), R.string.no_family_found_for_your_search, Toast.LENGTH_SHORT).show();
        }
        else {
            mFamily = families.get(0);
            hideLoadingIndicator();
            showFamilyProfileFragment();
        }
    }
    @Override public void onFoundationListFound(List<Foundation> foundations) {
        if (foundations == null) return;

        //If family is not in database then create it, otherwise update mPet
        if (foundations.size() == 0 || foundations.get(0)==null) {
            if (!mRequestedFoundationForPet) Toast.makeText(getBaseContext(), R.string.no_foundation_found_for_your_search, Toast.LENGTH_SHORT).show();
        }
        else {
            mFoundation = foundations.get(0);
            if (mRequestedFoundationForPet && mPetProfileFragment!=null) {
                mPetProfileFragment.updateProfile(mFoundation);
                mRequestedFoundationForPet = false;
            }
            else if (!mRequestedFoundationForPet) {
                hideLoadingIndicator();
                showFoundationProfileFragment();
            }
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

    //Communication with PetProfileFragment
    @Override public void onProfilesModified(Pet pet, Family family) {
        mPet = pet;
        mFamily = family;
    }
    @Override public void onFavoriteIdsModified(List<String> favoriteIds) {
        mFirebaseDao.updateObjectKeyValuePair(mFamily, "fpi", favoriteIds);
    }
}
