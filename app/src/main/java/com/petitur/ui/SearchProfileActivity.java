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
        ShowPetProfileFragment.PetProfileFragmentOperationsHandler {


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
    private ShowFamilyProfileFragment mShowFamilyProfileFragment;
    private ShowFoundationProfileFragment mShowFoundationProfileFragment;
    private String mRequestedFoundationProfileID;
    private ShowPetProfileFragment mShowPetProfileFragment;
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
        displayProfile();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        if (mBinding!=null) mBinding.unbind();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        if (!mReceivedFullPetProfile && !mReceivedFullFamilyProfile && !mReceivedFullFoundationProfile) getMenuInflater().inflate(R.menu.search_profile_menu, menu);
        else getMenuInflater().inflate(R.menu.show_blank_menu, menu);
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
            mQueryContainer.setVisibility(View.GONE);
            mQuery = mRequestedFoundationProfileID;
            mQueryEditText.setText(mQuery);
        }
        else if (!TextUtils.isEmpty(mRequestedFamilyProfileID)) {
            mQueryContainer.setVisibility(View.GONE);
            mQuery = mRequestedFamilyProfileID;
            mQueryEditText.setText(mQuery);
        }
        else if (!TextUtils.isEmpty(mRequestedPetProfileID)) {
            mQueryContainer.setVisibility(View.GONE);
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
    private void displayProfile() {

        //Priority order is pet, family then foundation
        if (mReceivedFullPetProfile) {
            showPetProfileFragment();
            getFoundationProfileForPet();
        }
        else if (mReceivedFullFamilyProfile) {
            showFamilyProfileFragment();
        }
        else if (mReceivedFullFoundationProfile) {
            showFoundationProfileFragment();
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
            if (TextUtils.isEmpty(mQuery)) return;

            List<QueryCondition> queryConditions = new ArrayList<>();
            QueryCondition queryCondition;

            if (Utilities.isCandidateForEmail(mQuery)) {

                queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "em", mQuery, true, 0);
                queryConditions.add(queryCondition);

                showLoadingIndicator();
                if (!TextUtils.isEmpty(mRequestedFoundationProfileID)) mFirebaseDao.requestObjectsWithConditions(new Foundation(), queryConditions);
                else if (!TextUtils.isEmpty(mRequestedFamilyProfileID)) mFirebaseDao.requestObjectsWithConditions(new Family(), queryConditions);
                else {
                    mFirebaseDao.requestObjectsWithConditions(new Foundation(), queryConditions);
                    mFirebaseDao.requestObjectsWithConditions(new Family(), queryConditions);
                }
            }
            else if (Utilities.isCandidateForFirebaseId(mQuery)) {

                queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "ui", mQuery, true, 0);
                queryConditions.add(queryCondition);

                showLoadingIndicator();
                if (!TextUtils.isEmpty(mRequestedFoundationProfileID)) mFirebaseDao.requestObjectsWithConditions(new Foundation(), queryConditions);
                else if (!TextUtils.isEmpty(mRequestedFamilyProfileID)) mFirebaseDao.requestObjectsWithConditions(new Family(), queryConditions);
                else if (!TextUtils.isEmpty(mRequestedPetProfileID)) mFirebaseDao.requestObjectsWithConditions(new Pet(), queryConditions);
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
    private void showFamilyProfileFragment() {

        mFragmentContainer.setVisibility(View.VISIBLE);
        if (mShowFamilyProfileFragment ==null) {
            mShowFamilyProfileFragment = new ShowFamilyProfileFragment();

            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.family_profile_parcelable), mFamily);
            mShowFamilyProfileFragment.setArguments(bundle);

            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.search_profile_fragment_container, mShowFamilyProfileFragment);
            fragmentTransaction.commit();
        }
        else {
            mShowFamilyProfileFragment.updateProfile(mFamily);
        }
    }
    private void showPetProfileFragment() {

        mFragmentContainer.setVisibility(View.VISIBLE);
        if (mShowPetProfileFragment ==null) {
            mShowPetProfileFragment = new ShowPetProfileFragment();

            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.pet_profile_parcelable), mPet);
            if (mFamily!=null) bundle.putParcelable(getString(R.string.family_profile_parcelable), mFamily);
            if (mFoundation!=null) bundle.putParcelable(getString(R.string.foundation_profile_parcelable), mFoundation);
            bundle.putString(getString(R.string.user_name), mFullName);
            mShowPetProfileFragment.setArguments(bundle);

            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.search_profile_fragment_container, mShowPetProfileFragment);
            fragmentTransaction.commit();
        }
        else {
            mShowPetProfileFragment.updateProfile(mPet);
        }
    }
    private void showFoundationProfileFragment() {

        mFragmentContainer.setVisibility(View.VISIBLE);
        if (mShowFoundationProfileFragment ==null) {
            mShowFoundationProfileFragment = new ShowFoundationProfileFragment();

            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.foundation_profile_parcelable), mFoundation);
            mShowFoundationProfileFragment.setArguments(bundle);

            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.search_profile_fragment_container, mShowFoundationProfileFragment);
            fragmentTransaction.commit();
        }
        else {
            mShowFoundationProfileFragment.updateProfile(mFoundation);
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
        if (mFoundation==null) {

            if (TextUtils.isEmpty(mPet.getOI())) return;

            List<QueryCondition> queryConditions = new ArrayList<>();
            QueryCondition queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "ui", mPet.getOI(), true, 0);
            queryConditions.add(queryCondition);

            mRequestedFoundationForPet = true;
            mFirebaseDao.requestObjectsWithConditions(new Foundation(), queryConditions);
        }
        else {
            mShowPetProfileFragment.updateProfile(mFoundation);
            mRequestedFoundationForPet = false;
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
            if (mRequestedFoundationForPet && mShowPetProfileFragment !=null) {
                mShowPetProfileFragment.updateProfile(mFoundation);
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
