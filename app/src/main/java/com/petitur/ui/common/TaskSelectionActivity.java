package com.petitur.ui.common;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.maps.MapView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.petitur.R;
import com.petitur.data.*;
import com.petitur.resources.Utilities;
import com.petitur.ui.family.TipsInfoActivity;
import com.petitur.ui.foundation.UpdatePetActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static butterknife.internal.Utils.arrayOf;

public class TaskSelectionActivity extends BaseActivity implements
        FirebaseDao.FirebaseOperationsHandler {

    //TODO: put same menu in all activities: https://stackoverflow.com/questions/3270206/same-option-menu-in-all-activities-in-android

    //region Parameters
    @BindView(R.id.task_selection_find_pet) Button mFindPetButton;
    @BindView(R.id.task_selection_get_tips_info) Button mGetAdviceButton;
    @BindView(R.id.task_selection_see_favorites) Button mSeeFavoritesButton;
    @BindView(R.id.task_selection_see_my_pets) Button mSeeMyPetsButton;
    @BindView(R.id.task_selection_add_pet) Button mAddPetButton;
    @BindView(R.id.task_selection_search_users) Button mSearchUsersButton;
    @BindView(R.id.task_selection_welcome) TextView mWelcomeTextView;
    @BindView(R.id.task_selection_loading_indicator) ProgressBar mProgressBarLoadingIndicator;
    private static final String DEBUG_TAG = "Petitur TaskSelection";
    public static final int APP_PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 555;
    private static final int APP_PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 123;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mCurrentFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean hasStoragePermissions;
    private boolean hasLocationPermissions;
    private Unbinder mBinding;
    private Menu mMenu;
    private FirebaseDao mFirebaseDao;
    private User mUser;
    private Foundation mFoundation;
    private Family mFamily;
    //endregion


    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_selection);

        initializeParameters();
        showLoadingIndicator();
        hasStoragePermissions = checkStoragePermission();
        hasLocationPermissions = checkLocationPermission();
        if (hasStoragePermissions && hasLocationPermissions) getUserProfile();
        loadGooglePlayMapServicesInBackground();
    }
    @Override public void onStart() {
        super.onStart();
        setupFirebaseAuthentication();
    }
    @Override protected void onResume() {
        super.onResume();
        mCurrentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        checkIfSignedInOrShowSignInScreen();
        invalidateOptionsMenu();

    }
    @Override protected void onStop() {
        super.onStop();
        if (mFirebaseAuth!=null) mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        removeListeners();
        if (mBinding!=null) mBinding.unbind();
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utilities.FIREBASE_SIGN_IN_FLAG) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                showLoadingIndicator();
                mCurrentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (hasStoragePermissions && hasLocationPermissions) getUserProfile();
                if (mMenu!=null) Utilities.updateSignInMenuItem(mMenu, this, true);
            } else {
                hideLoadingIndicator();
                if (mMenu!=null) Utilities.updateSignInMenuItem(mMenu, this, false);
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
        else if (requestCode == Utilities.NEW_USER_FLAG) {

            if (resultCode == RESULT_OK) {
                boolean mustRestartApp = false;
                boolean returnedNullFamily = true;
                boolean returnedNullFoundation = true;
                if (data.hasExtra(getString(R.string.bundled_user))) {
                    User user = data.getParcelableExtra(getString(R.string.bundled_user));
                    if (!user.getLg().equals(mUser.getLg())) {
                        mustRestartApp = true;
                    }
                    mUser = user;
                    mFirebaseDao.updateObject(mUser);
                }
                if (data.hasExtra(getString(R.string.family_profile_parcelable))) {
                    mFamily = data.getParcelableExtra(getString(R.string.family_profile_parcelable));
                    if (mFamily != null) {
                        returnedNullFamily = false;
                        if (TextUtils.isEmpty(mFamily.getUI())) mFamily = (Family) mFirebaseDao.createObjectWithUIAndReturnIt(mFamily);
                        else {
                            mFamily.setDte(Utilities.getCurrentDate());
                            mFirebaseDao.updateObject(mFamily);
                        }
                    }
                }
                if (data.hasExtra(getString(R.string.foundation_profile_parcelable))) {
                    mFoundation = data.getParcelableExtra(getString(R.string.foundation_profile_parcelable));
                    if (mFoundation != null) {
                        returnedNullFoundation = false;
                        if (TextUtils.isEmpty(mFoundation.getUI())) mFoundation = (Foundation) mFirebaseDao.createObjectWithUIAndReturnIt(mFoundation);
                        else {
                            mFoundation.setDte(Utilities.getCurrentDate());
                            mFirebaseDao.updateObject(mFoundation);
                        }
                    }
                }

                if (mustRestartApp) {
                    mUser.setIFT(true);
                    mFirebaseDao.updateObject(mUser);
                    Utilities.restartApplication(TaskSelectionActivity.this);
                }
                else {
                    if (returnedNullFamily && returnedNullFoundation) {
                        mUser.setIFT(true);
                        mFirebaseDao.updateObject(mUser);
                        Toast.makeText(this, R.string.must_complete_registration, Toast.LENGTH_SHORT).show();
                        //Utilities.restartApplication(this);
                        finishAffinity(); //closes the app
                    }
                    else {
                        mUser.setIFT(false);
                        mFirebaseDao.updateObject(mUser);
                    }
                }

            }
        }
        else if (requestCode == Utilities.UPDATE_PROFILE_FLAG) {

            if (resultCode == RESULT_OK) {
                if (data.hasExtra(getString(R.string.family_profile_parcelable))) {
                    mFamily = data.getParcelableExtra(getString(R.string.family_profile_parcelable));
                }
                if (data.hasExtra(getString(R.string.foundation_profile_parcelable))) {
                    mFoundation = data.getParcelableExtra(getString(R.string.foundation_profile_parcelable));
                }
            }
        }
    }
    @Override public boolean onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;
        if (mCurrentFirebaseUser==null) Utilities.updateSignInMenuItem(mMenu, this, false);
        else Utilities.updateSignInMenuItem(mMenu, this, true);
        return super.onPrepareOptionsMenu(menu);
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_selection_menu, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        switch (itemThatWasClickedId) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_edit_preferences:
                Utilities.startPreferencesActivity(TaskSelectionActivity.this);
                return true;
            case R.id.action_edit_my_profile:
                if (mUser.getIF()) Utilities.startUpdateFoundationProfileActivity(TaskSelectionActivity.this, mFoundation);
                else Utilities.startUpdateFamilyProfileActivity(TaskSelectionActivity.this, mFamily);
                return true;
            case R.id.action_sign_in_out:
                if (mCurrentFirebaseUser!=null) {
                    Utilities.startSigningOut(TaskSelectionActivity.this, mCurrentFirebaseUser, mFirebaseAuth, mMenu, mUser, mFirebaseDao);
                }
                //if (mCurrentFirebaseUser!=null) showBlankTaskSelectionMenu(); //ie. show the blank screen when requesting sign-out for a logged-in user
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == APP_PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasStoragePermissions = true;
                Log.e(DEBUG_TAG, "Returned from WRITE_EXTERNAL_STORAGE permission request.");
            } else {
                Toast.makeText(this, R.string.no_permissions_terminating, Toast.LENGTH_SHORT).show();
                Utilities.closeApp(this);
            }
            if (!hasLocationPermissions) checkLocationPermission();
        }
        else if (requestCode == APP_PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasLocationPermissions = true;
                Log.e(DEBUG_TAG, "Returned from ACCESS_FINE_LOCATION permission request.");
            } else {
                hasLocationPermissions = false;
            }

            getUserProfile();
        }
    }


    //Functionality methods
    private void initializeParameters() {

        mBinding =  ButterKnife.bind(this);

        mFirebaseDao = new FirebaseDao(this, this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        showBlankTaskSelectionMenu();

    }
    private void checkIfSignedInOrShowSignInScreen() {
        if (mCurrentFirebaseUser==null) {
            Utilities.showSignInScreen(TaskSelectionActivity.this);
        }
    }
    private void setupFirebaseAuthentication() {


        // Check if user is signed in (non-null) and update UI accordingly.
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mCurrentFirebaseUser = firebaseAuth.getCurrentUser();
                if (mCurrentFirebaseUser != null) {
                    // User is signed in
                    Utilities.setAppPreferenceUserHasNotRefusedSignIn(getApplicationContext(), true);
                    Utilities.setAppPreferenceFirstTimeUsingApp(getApplicationContext(), false);
                    updateWelcomeMessage();
                    Log.d(DEBUG_TAG, "onAuthStateChanged:signed_in:" + mCurrentFirebaseUser.getUid());
                } else {
                    hideLoadingIndicator();
                    Utilities.setAppPreferenceUserHasNotRefusedSignIn(TaskSelectionActivity.this, true);
                    //Utilities.showSignInScreen(TaskSelectionActivity.this);
//                    // TinDogUser is signed out
//                    Log.d(DEBUG_TAG, "onAuthStateChanged:signed_out");
//                    //Showing the sign-in screen
//                    boolean firstTime = Utilities.getAppPreferenceFirstTimeUsingApp(getApplicationContext());
//                    if (!firstTime && Utilities.getAppPreferenceUserHasNotRefusedSignIn(getApplicationContext()))
//                        Utilities.showSignInScreen(TaskSelectionActivity.this);
                }
                mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
            }
        };
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.e(DEBUG_TAG, "User has granted EXTERNAL_STORAGE permission");
                return true;
            } else {
                Log.e(DEBUG_TAG, "User has asked for EXTERNAL_STORAGE permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, APP_PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                return false;
            }
        }
        else {
            Log.e(DEBUG_TAG,"User already has the permission");
            return true;
        }
    }
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.e(DEBUG_TAG, "User has granted ACCESS_FINE_LOCATION permission");
            return true;
        } else {
            Log.e(DEBUG_TAG, "User has asked for ACCESS_FINE_LOCATION permission");

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, APP_PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, R.string.location_rationale, Toast.LENGTH_SHORT).show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        APP_PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
            }
            return false;
        }
    }
    private void removeListeners() {
        mFirebaseDao.removeListeners();
    }
    private void getUserProfile() {
        if (mCurrentFirebaseUser != null) {
            mUser = new User();
            mUser.setOI(mCurrentFirebaseUser.getUid());
            mUser.setEm(mCurrentFirebaseUser.getEmail());
            mUser.setNm(mCurrentFirebaseUser.getDisplayName());

            mFirebaseDao.requestObjectsWithConditions(mUser, Utilities.getQueryConditionsForSingleObjectSearchByOwnerId(this, mUser));
        }
        else {
            showBlankTaskSelectionMenu();
        }
    }
    private void getFoundationProfile() {
        if (mCurrentFirebaseUser != null) {
            Foundation foundation = new Foundation(mCurrentFirebaseUser.getUid());
            mFirebaseDao.requestObjectsWithConditions(foundation, Utilities.getQueryConditionsForSingleObjectSearchByOwnerId(this, foundation));
        }
    }
    private void getFamilyProfile() {
        if (mCurrentFirebaseUser != null) {
            Family family = new Family(mCurrentFirebaseUser.getUid());
            mFirebaseDao.requestObjectsWithConditions(family, Utilities.getQueryConditionsForSingleObjectSearchByOwnerId(this, family));
        }
    }
    private void modifyUserInterfaceAccordingToCredentials() {

        //Fixing language code saved in the user profile if it was misrepresented even after a language change (can occur when switching users)
        String language = Locale.getDefault().getLanguage();
        if (!language.equals(mUser.getLg())) {
            mUser.setLg(language);
            mFirebaseDao.updateObject(mUser);
        }

        hideLoadingIndicator();
        if (mUser!=null) {
            mWelcomeTextView.setVisibility(View.VISIBLE);
            if (mUser.getIF()) {
                mFindPetButton.setVisibility(View.GONE);
                mGetAdviceButton.setVisibility(View.GONE);
                mSeeFavoritesButton.setVisibility(View.GONE);
                mSeeMyPetsButton.setVisibility(View.VISIBLE);
                mAddPetButton.setVisibility(View.VISIBLE);
                mSearchUsersButton.setVisibility(View.VISIBLE);
            }
            else {
                mFindPetButton.setVisibility(View.VISIBLE);
                mGetAdviceButton.setVisibility(View.VISIBLE);
                mSeeFavoritesButton.setVisibility(View.VISIBLE);
                mSeeMyPetsButton.setVisibility(View.GONE);
                mAddPetButton.setVisibility(View.GONE);
                mSearchUsersButton.setVisibility(View.GONE);
            }
        }
        else {
            Utilities.startSigningOut(TaskSelectionActivity.this, mCurrentFirebaseUser, mFirebaseAuth, mMenu, mUser, mFirebaseDao);
            showBlankTaskSelectionMenu();
        }
    }
    private void showBlankTaskSelectionMenu() {
        mWelcomeTextView.setVisibility(View.GONE);
        mFindPetButton.setVisibility(View.GONE);
        mGetAdviceButton.setVisibility(View.GONE);
        mSeeFavoritesButton.setVisibility(View.GONE);
        mSeeMyPetsButton.setVisibility(View.GONE);
        mAddPetButton.setVisibility(View.GONE);
        mSearchUsersButton.setVisibility(View.GONE);
    }
    private void openPetList(boolean requestedFavorites) {
        if (mUser.getIFT()) return;
        Intent intent = new Intent(this, PetListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.bundled_user), mUser);
        if (mFamily!=null) bundle.putParcelable(getString(R.string.family_profile_parcelable), mFamily);
        if (mFoundation!=null) bundle.putParcelable(getString(R.string.foundation_profile_parcelable), mFoundation);
        if (requestedFavorites) bundle.putBoolean(getString(R.string.bundled_requested_favorites), true);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
    private void openNewUserActivity() {

        Intent intent = new Intent(this, NewUserActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.bundled_user), mUser);
        if (mFamily!=null) bundle.putParcelable(getString(R.string.family_profile_parcelable), mFamily);
        if (mFoundation!=null) bundle.putParcelable(getString(R.string.foundation_profile_parcelable), mFoundation);
        bundle.putString(getString(R.string.firebase_name), mCurrentFirebaseUser.getDisplayName());
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(intent, Utilities.NEW_USER_FLAG);
    }
    private void openUpdatePetProfile(@Nonnull Pet pet) {
        if (mUser.getIFT()) return;
        Intent intent = new Intent(this, UpdatePetActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.selected_pet_id), pet.getUI());
        if (mFamily!=null) bundle.putParcelable(getString(R.string.family_profile_parcelable), mFamily);
        if (mFoundation!=null) bundle.putParcelable(getString(R.string.foundation_profile_parcelable), mFoundation);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
    private void updateWelcomeMessage() {
        Calendar calendar = Calendar.getInstance();
        Date currentTime = calendar.getTime();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String welcomeMessage = getString(R.string.welcome);
        String name = mCurrentFirebaseUser.getDisplayName();

        if (0 < hour && hour <= 5 || 22 < hour && hour <= 24) {
            welcomeMessage = getString(R.string.welcome_night) + name + "!";
        }
        else if (5 < hour && hour <= 11) {
            welcomeMessage = getString(R.string.welcome_morning) + name + "!";
        }
        else if (11 < hour && hour <= 17) {
            welcomeMessage = getString(R.string.welcome_day) + name + "!";
        }
        else if (17 < hour && hour <= 22) {
            welcomeMessage = getString(R.string.welcome_evening) + name + "!";
        }

        if (mWelcomeTextView!=null) mWelcomeTextView.setText(welcomeMessage);
    }
    private void openTipsInfoActivity() {

        Intent intent = new Intent(this, TipsInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
    private void loadGooglePlayMapServicesInBackground() {
        //taken from: https://stackoverflow.com/questions/26265526/what-makes-my-map-fragment-loading-slow
        // Fixing Later Map loading Delay
        if (Looper.myLooper() == null) Looper.prepare();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MapView mv = new MapView(getApplicationContext());
                    mv.onCreate(null);
                    mv.onPause();
                    mv.onDestroy();
                }catch (Exception ignored){

                }
            }
        }).start();
    }
    private void showLoadingIndicator() {
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.VISIBLE);
    }
    private void hideLoadingIndicator() {
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.INVISIBLE);
    }


    //View click listeners
    @OnClick(R.id.task_selection_find_pet) public void onFindPetButtonClick() {
        openPetList(false);
    }
    @OnClick(R.id.task_selection_see_favorites) public void onSeeFavoritesButtonClick() {
        openPetList(true);
    }
    @OnClick(R.id.task_selection_get_tips_info) public void onTipsInfoButtonClick() {
        openTipsInfoActivity();
    }
    @OnClick(R.id.task_selection_add_pet) public void onAddPetButtonClick() {
        openUpdatePetProfile(new Pet());
    }
    @OnClick(R.id.task_selection_see_my_pets) public void onSeeMyPetsButtonClick() {
        openPetList(false);
    }
    @OnClick(R.id.task_selection_search_users) public void onSearchUsersButtonClick() {
        Intent intent = new Intent(this, SearchProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }


    //Communication with other classes:

    //Communication with Firebase Dao handler
    @Override public void onPetListFound(List<Pet> pets) {

    }
    @Override public void onFamilyListFound(List<Family> families) {
        //If the family wasn't defined yet, go to the family update screen
        if (families.size() == 0 || families.get(0).getPn().equals("")) {
            Toast.makeText(getApplicationContext(), R.string.please_fill_user_profile, Toast.LENGTH_SHORT).show();
            Utilities.startUpdateFamilyProfileActivity(TaskSelectionActivity.this, mFamily);
        }
        else {
            mFamily = families.get(0);
            if (mUser.getIFT()) openNewUserActivity();
        }
    }
    @Override public void onFoundationListFound(List<Foundation> foundations) {
        //If the family wasn't defined yet, go to the family update screen
        if (foundations.size() == 0 || foundations.get(0).getNm().equals("")) {
            Toast.makeText(getApplicationContext(), R.string.please_fill_user_profile, Toast.LENGTH_SHORT).show();
            Utilities.startUpdateFoundationProfileActivity(TaskSelectionActivity.this, mFoundation);
        }
        else {
            mFoundation = foundations.get(0);
            if (mUser.getIFT()) openNewUserActivity();
        }
    }
    @Override public void onUserListFound(List<User> users) {
        if (users == null) return;

        //If user is not in database then create it, otherwise update mUser
        if (users.size() == 0 || users.get(0)==null) {
            if (!mUser.getOI().equals("")) {
                mUser = (User) mFirebaseDao.createObjectWithUIAndReturnIt(mUser);
                if (!mUser.getUI().equals("")) {
                    modifyUserInterfaceAccordingToCredentials();
                    if (mUser.getIFT()) openNewUserActivity();
                }
            }
        }
        else {
            mUser = users.get(0);
            modifyUserInterfaceAccordingToCredentials();
            if (mUser.getIF()) getFoundationProfile();
            else getFamilyProfile();
        }
    }
    @Override public void onMapMarkerListFound(List<MapMarker> mapMarkers) {

    }
    @Override public void onImageAvailable(boolean imageWasDownloaded, Uri imageUri, String imageName) {

    }
    @Override public void onImageUploaded(List<String> uploadTimes) {

    }
}
