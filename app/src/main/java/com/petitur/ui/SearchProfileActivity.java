package com.petitur.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
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
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SearchProfileActivity extends AppCompatActivity implements FirebaseDao.FirebaseOperationsHandler {


    //region Parameters
    private Unbinder mBinding;
    private FirebaseDao mFirebaseDao;
    private Family mFamily;
    private Foundation mFoundation;
    private FirebaseUser mCurrentFirebaseUser;
    private String mQuery;
    @BindView(R.id.search_profile_query) EditText mQueryEditText;
    @BindView(R.id.search_profile_loading_indicator) ProgressBar mProgressBarLoadingIndicator;
    @BindView(R.id.search_profile_fragment_container) FrameLayout mFragmentContainer;
    private FragmentManager mFragmentManager;
    private FamilyProfileFragment mFamilyProfileFragment;
    private FoundationProfileFragment mFoundationProfileFragment;
    private String mRequestedFoundationProfileID;
    //endregion


    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_profile);

        getExtras();
        initializeParameters();
        Utilities.setLocale(this);
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        mBinding.unbind();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_profile_menu, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        switch (itemThatWasClickedId) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_search:
                getFamilyOrFoundationProfileFromFirebase();
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
    }
    private void initializeParameters() {

        mFirebaseDao = new FirebaseDao(this, this);
        mCurrentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.find_a_profile);
        }

        mBinding =  ButterKnife.bind(this);
        Utilities.hideSoftKeyboard(this);
        mFragmentContainer.setVisibility(View.INVISIBLE);
        mFragmentManager = getSupportFragmentManager();

        if (!TextUtils.isEmpty(mRequestedFoundationProfileID)) {
            mQuery = mRequestedFoundationProfileID;
            mQueryEditText.setText(mQuery);
        }
        else {
            mQuery = "";
        }
    }
    private void getFamilyOrFoundationProfileFromFirebase() {
        if (mCurrentFirebaseUser != null) {

            mQuery = mQueryEditText.getText().toString();

            if (!TextUtils.isEmpty(mQuery)) {

                List<QueryCondition> queryConditions = new ArrayList<>();
                QueryCondition queryCondition;
                if (Utilities.isCandidateForEmail(mQuery) && mRequestedFoundationProfileID==null) {

                    queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "em", mQuery, true, 0);
                    queryConditions.add(queryCondition);

                    showLoadingIndicator();
                    mFirebaseDao.requestObjectsWithConditions(new Foundation(), queryConditions);
                    if (mRequestedFoundationProfileID==null) mFirebaseDao.requestObjectsWithConditions(new Family(), queryConditions);
                }
                else if (Utilities.isCandidateForFirebaseId(mQuery)) {

                    queryCondition = new QueryCondition(getString(R.string.query_condition_equalsString), "ui", mQuery, true, 0);
                    queryConditions.add(queryCondition);

                    showLoadingIndicator();
                    if (mRequestedFoundationProfileID != null) {
                        mFirebaseDao.requestObjectsWithConditions(new Foundation(), queryConditions);
                    }
                    else {
                        mFirebaseDao.requestObjectsWithConditions(new Foundation(), queryConditions);
                        mFirebaseDao.requestObjectsWithConditions(new Family(), queryConditions);
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.invalid_search_parameters, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void showFamilyProfileFragment() {
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
    private void showFoundationProfileFragment() {
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
        mFragmentContainer.setVisibility(View.VISIBLE);
    }


    //Communication with Firebase Dao handler
    @Override public void onPetListFound(List<Pet> pets) {
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
            Toast.makeText(getBaseContext(), R.string.no_foundation_found_for_your_search, Toast.LENGTH_SHORT).show();
        }
        else {
            mFoundation = foundations.get(0);
            hideLoadingIndicator();
            showFoundationProfileFragment();
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
