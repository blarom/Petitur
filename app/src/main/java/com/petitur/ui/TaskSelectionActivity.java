package com.petitur.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.petitur.R;
import com.petitur.data.*;
import com.petitur.resources.Utilities;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static butterknife.internal.Utils.arrayOf;

public class TaskSelectionActivity extends AppCompatActivity implements FirebaseDao.FirebaseOperationsHandler {

    //region Parameters
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
    //endregion


    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_selection);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mBinding =  ButterKnife.bind(this);

        setupFirebaseAuthentication();
        hasStoragePermissions = checkStoragePermission();
        hasLocationPermissions = checkLocationPermission();
        if (hasStoragePermissions && hasLocationPermissions) getUserProfile();
    }
    @Override protected void onResume() {
        super.onResume();
        mCurrentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

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
        removeListeners();
        mBinding.unbind();
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utilities.FIREBASE_SIGN_IN_KEY) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                mCurrentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (hasStoragePermissions && hasLocationPermissions) getUserProfile();
                Utilities.updateSignInMenuItem(mMenu, this, true);
            } else {
                Utilities.updateSignInMenuItem(mMenu, this, false);
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_selection_menu, menu);
        mMenu = menu;
        if (mCurrentFirebaseUser==null) Utilities.updateSignInMenuItem(mMenu, this, false);
        else Utilities.updateSignInMenuItem(mMenu, this, true);
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
                Utilities.startEditProfileActtivity(TaskSelectionActivity.this);
                return true;
            case R.id.action_signin:
                if (mCurrentFirebaseUser==null) {
                    Utilities.setAppPreferenceUserHasNotRefusedSignIn(getApplicationContext(), true);
                    Utilities.showSignInScreen(TaskSelectionActivity.this);
                }
                else {
                    Utilities.setAppPreferenceUserHasNotRefusedSignIn(getApplicationContext(), false);
                    mFirebaseAuth.signOut();
                    Utilities.updateSignInMenuItem(mMenu, this, false);
                }
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
                    Utilities.setAppPreferenceFirstTimeUsingApp(getApplicationContext(), false);
                    Log.d(DEBUG_TAG, "onAuthStateChanged:signed_in:" + mCurrentFirebaseUser.getUid());
                } else {
                    // TinDogUser is signed out
                    Log.d(DEBUG_TAG, "onAuthStateChanged:signed_out");
                    //Showing the sign-in screen
                    boolean firstTime = Utilities.getAppPreferenceFirstTimeUsingApp(getApplicationContext());
                    if (!firstTime && Utilities.getAppPreferenceUserHasNotRefusedSignIn(getApplicationContext()))
                        Utilities.showSignInScreen(TaskSelectionActivity.this);
                }
            }
        };
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
    public boolean checkStoragePermission() {
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
        mFirebaseDao = new FirebaseDao(getBaseContext(), this);
        if (mCurrentFirebaseUser != null) {
            mUser = new User();
            mUser.setUI(mCurrentFirebaseUser.getUid());
            mUser.setEm(mCurrentFirebaseUser.getEmail());
            mUser.setNm(mCurrentFirebaseUser.getDisplayName());
            mFirebaseDao.requestObjectWithId(mUser);
        }
    }


    //View click listeners
    @OnClick(R.id.task_selection_find_pet) public void onFindPetButtonClick() {
    }
    @OnClick(R.id.task_selection_get_advice) public void onGetAdviceButtonClick() {
    }
    @OnClick(R.id.task_selection_see_favorites) public void onSeeFavoritesButtonClick() {
    }
    @OnClick(R.id.task_selection_see_my_pets) public void onSeeMyPetsButtonClick() {
    }
    @OnClick(R.id.task_selection_add_pet) public void onAddPetButtonClick() {
    }
    @OnClick(R.id.task_selection_search_users) public void onSearchUsersButtonClick() {
    }


    //Communication with other classes:

    //Communication with Firebase Dao handler
    @Override public void onPetListFound(List<Pet> pets) {

    }
    @Override public void onFamilyListFound(List<Family> families) {

    }
    @Override public void onFoundationListFound(List<Foundation> foundations) {

    }
    @Override public void onUserListFound(List<User> users) {
        if (users == null) return;

        //If user is not in database then create it, otherwise update mUser
        if (users.size() == 0 || users.get(0)==null) {
            if (!mUser.getUI().equals("")) {
                String id = mFirebaseDao.createObject(mUser);
                mUser.setUI(id);
                mFirebaseDao.updateObject(mUser);
            }
        }
        else {
            mUser = users.get(0);
        }
    }
    @Override public void onMapMarkerListFound(List<MapMarker> mapMarkers) {

    }
    @Override public void onImageAvailable(Uri imageUri, String imageName) {

    }
    @Override public void onImageUploaded(List<String> uploadTimes) {

    }
}
