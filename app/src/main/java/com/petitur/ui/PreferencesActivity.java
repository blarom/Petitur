package com.petitur.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.petitur.R;
import com.petitur.data.Family;
import com.petitur.data.FirebaseDao;
import com.petitur.data.Foundation;
import com.petitur.data.MapMarker;
import com.petitur.data.Pet;
import com.petitur.data.User;
import com.petitur.resources.LocaleHelper;
import com.petitur.resources.Utilities;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class PreferencesActivity extends BaseActivity implements FirebaseDao.FirebaseOperationsHandler {


    //region Parameters
    private static final String DEBUG_TAG = "Petitur Preferences";
    @BindView(R.id.preferences_container) ConstraintLayout mPreferencesContainer;
    @BindView(R.id.preferences_name_value) TextView mTextViewUserName;
    @BindView(R.id.preferences_email_value) TextView mTextViewUserEmail;
    @BindView(R.id.preferences_change_name) ImageView mImageViewChangeName;
    @BindView(R.id.preferences_change_email) ImageView mImageViewChangeEmail;
    @BindView(R.id.preferences_change_password) ImageView mImageViewChangePassword;
    @BindView(R.id.preferences_search_country_only_checkbox) CheckBox mCheckBoxLimitToCountry;
    @BindView(R.id.preferences_please_sign_in) TextView mPleaseSignInTextView;
    @BindView(R.id.preferences_language_selection_spinner) Spinner mLanguageSelectionSpinner;
    private Unbinder mBinding;
    private User mUser;
    private FirebaseDao mFirebaseDao;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mCurrentFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean mLimitToCountry;
    private Bundle mSavedInstanceState;
    private Menu mMenu;
    private String mLanguageCode;
    private int mLastSelectedPosition;
    private String mLanguageEnglish;
    private String mLanguageHebrew;
    //endregion


    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        mSavedInstanceState = savedInstanceState;
        initializeParameters();
        getUserProfile();
    }
    @Override public void onStart() {
        super.onStart();
        setupFirebaseAuthentication();
    }
    @Override protected void onResume() {
        super.onResume();
        mCurrentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        invalidateOptionsMenu();
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
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Utilities.FIREBASE_SIGN_IN_KEY) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                mCurrentFirebaseUser = mFirebaseAuth.getCurrentUser();
                Utilities.updateSignInMenuItem(mMenu, this, true);
                getUserProfile();
            } else {
                Utilities.updateSignInMenuItem(mMenu, this, false);
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
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
        getMenuInflater().inflate(R.menu.preferences_update_menu, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        switch (itemThatWasClickedId) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_done:
                updatePreferencesWithUserInput();
                mFirebaseDao.updateObject(mUser);
                finish();
                return true;
            case R.id.action_signin:
                Utilities.handleUserSignIn(PreferencesActivity.this, mCurrentFirebaseUser, mFirebaseAuth, mMenu);
                if (mCurrentFirebaseUser!=null) showBlankPreferences(); //ie. show the blank screen when requesting sign-out for a logged-in user
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getString(R.string.saved_user), mUser);
    }
    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState!=null) {
            mUser = savedInstanceState.getParcelable(getString(R.string.saved_user));
            updateUserInfoShownToUser();
            updatePreferencesShownToUser();
        }
    }


    //Functional methods
    private void initializeParameters() {

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.preferences);
        }

        mBinding =  ButterKnife.bind(this);

        mUser = new User();
        mFirebaseDao = new FirebaseDao(getBaseContext(), this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentFirebaseUser = mFirebaseAuth.getCurrentUser();

        mCheckBoxLimitToCountry.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mLimitToCountry = b;
            }
        });

        mLanguageEnglish = getString(R.string.language_selection_english);
        mLanguageHebrew = getString(R.string.language_selection_hebrew);

        setLanguageInSpinner();

        mLanguageSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {

                if (pos == mLastSelectedPosition) return;

                //Getting the selected language
                String selectedItem = (String) adapterView.getItemAtPosition(pos);
                mLanguageCode = "en";

                if (selectedItem.equals(mLanguageEnglish)) {
                    mLanguageCode = "en";
                }
                else if (selectedItem.equals(mLanguageHebrew)) {
                    mLanguageCode = "iw";
                }

                //Creating an alert dialog to make sure the user wants to change the language
                AlertDialog alertDialog = new AlertDialog.Builder(PreferencesActivity.this).create();
                alertDialog.setMessage(getString(R.string.sure_to_change_app_language));

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        mUser.setLg(mLanguageCode);
                        mFirebaseDao.updateObject(mUser);

                        Utilities.setAppPreferenceLanguage(getApplicationContext(), mLanguageCode);
                        Context context = LocaleHelper.setLocale(getApplicationContext(), mLanguageCode);

                        //Relaunching the app
                        Intent intent = getPackageManager().getLaunchIntentForPackage( getPackageName() );
                        if (intent!=null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                alertDialog.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Utilities.hideSoftKeyboard(this);
    }
    private void updateUserInfoShownToUser() {
        mTextViewUserName.setText(mCurrentFirebaseUser.getDisplayName());
        mTextViewUserEmail.setText(mCurrentFirebaseUser.getEmail());
    }
    private void updatePreferencesShownToUser() {

        mLimitToCountry = mUser.getLC();
        mCheckBoxLimitToCountry.setChecked(mLimitToCountry);

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
            showBlankPreferences();
        }
    }
    private void updatePreferencesWithUserInput() {

        mUser.setEm(mCurrentFirebaseUser.getEmail());
        mUser.setNm(mCurrentFirebaseUser.getDisplayName());
        mUser.setUI(mCurrentFirebaseUser.getUid());

        mUser.setLC(mCheckBoxLimitToCountry.isChecked());

        if (TextUtils.isEmpty(mUser.getUI())) Log.i(DEBUG_TAG, "Error: TinDog User has empty unique ID!");
    }
    private void setupFirebaseAuthentication() {
        // Check if user is signed in (non-null) and update UI accordingly.
        mCurrentFirebaseUser = mFirebaseAuth.getCurrentUser();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mCurrentFirebaseUser = firebaseAuth.getCurrentUser();
                if (mCurrentFirebaseUser != null) {
                    // User is signed in
                    Utilities.setAppPreferenceUserHasNotRefusedSignIn(getApplicationContext(), true);
                    updateUserInfoShownToUser();
                    if (mSavedInstanceState==null) updatePreferencesShownToUser();
                    Log.d(DEBUG_TAG, "onAuthStateChanged:signed_in:" + mCurrentFirebaseUser.getUid());
                } else {
                    // User is signed out
                    Log.d(DEBUG_TAG, "onAuthStateChanged:signed_out");
                    //Showing the sign-in screen
                    if (Utilities.getAppPreferenceUserHasNotRefusedSignIn(getApplicationContext())) {
                        mSavedInstanceState = null;
                        Utilities.showSignInScreen(PreferencesActivity.this);
                    }
                }
            }
        };
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
    private void showUserInfoUpdateDialog(final String infoType) {

        //Get the dialog view
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_enter_information, null);
        final EditText inputTextOld = dialogView.findViewById(R.id.dialog_user_info_input_text_old);
        final EditText inputTextNew = dialogView.findViewById(R.id.dialog_user_info_input_text_new);
        final TextInputLayout inputTextOldParent = dialogView.findViewById(R.id.dialog_user_info_input_text_old_parent);
        final TextInputLayout inputTextNewParent = dialogView.findViewById(R.id.dialog_user_info_input_text_new_parent);

        //Building the dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        switch (infoType) {
            case "name":
                builder.setTitle(R.string.please_enter_your_name);
                inputTextOld.setText("");
                inputTextOld.setEnabled(true);
                inputTextNew.setInputType(InputType.TYPE_CLASS_TEXT);
                inputTextOldParent.setHint(getString(R.string.current_password));
                inputTextNewParent.setHint("Enter new name (current: " + mCurrentFirebaseUser.getDisplayName() + ")");
                break;
            case "email":
                builder.setTitle(R.string.please_enter_your_email);
                inputTextOld.setText("");
                inputTextOld.setEnabled(true);
                inputTextNew.setInputType(InputType.TYPE_CLASS_TEXT);
                inputTextOldParent.setHint(getString(R.string.current_password));
                inputTextNewParent.setHint("Enter new email (current: " + mCurrentFirebaseUser.getEmail() + ")");
                break;
            case "password":
                builder.setTitle(R.string.please_enter_your_password);
                inputTextOld.setText("");
                inputTextOld.setEnabled(true);
                inputTextNew.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                inputTextOldParent.setHint(getString(R.string.current_password));
                inputTextNewParent.setHint(getString(R.string.new_password));
                break;
        }

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String currentPassword = inputTextOld.getText().toString();
                final String newInfo = inputTextNew.getText().toString();

                switch (infoType) {
                    case "name":
                        if (mCurrentFirebaseUser!=null) {
                            Utilities.updateFirebaseUserName(getApplicationContext(), mCurrentFirebaseUser, currentPassword, newInfo);
                            mTextViewUserName.setText(newInfo);
                        }
                        break;
                    case "email":
                        if (mCurrentFirebaseUser!=null) {
                            Utilities.updateFirebaseUserEmail(getApplicationContext(), mCurrentFirebaseUser, currentPassword, newInfo);
                            mTextViewUserEmail.setText(newInfo);
                        }
                        break;
                    case "password":
                        if (mCurrentFirebaseUser!=null) {
                            Utilities.updateFirebaseUserPassword(getApplicationContext(), mCurrentFirebaseUser, currentPassword, newInfo);
                        }
                        break;
                }

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
    }
    private void removeListeners() {
        mFirebaseDao.removeListeners();
    }
    private void showBlankPreferences() {
        mPreferencesContainer.setVisibility(View.GONE);
        mPleaseSignInTextView.setVisibility(View.VISIBLE);
    }
    private void modifyLayoutAccordingToCredentials() {
        if (mUser!=null && mPreferencesContainer!=null) {
            mPreferencesContainer.setVisibility(View.VISIBLE);
            mPleaseSignInTextView.setVisibility(View.GONE);
        }
        else {
            showBlankPreferences();
        }
    }
    private void setLanguageInSpinner() {

        int position = 0;
        if (!TextUtils.isEmpty(mUser.getUI())) {
            switch (mUser.getLg()) {
                case "en":
                    position = Utilities.getSpinnerPositionFromText(mLanguageSelectionSpinner, mLanguageEnglish);
                    break;
                case "he":
                    position = Utilities.getSpinnerPositionFromText(mLanguageSelectionSpinner, mLanguageHebrew);
                    break;
            }
        }
        else {
            switch (Utilities.getAppPreferenceLanguage(getApplicationContext())) {
                case "en":
                    position = Utilities.getSpinnerPositionFromText(mLanguageSelectionSpinner, mLanguageEnglish);
                    break;
                case "he":
                    position = Utilities.getSpinnerPositionFromText(mLanguageSelectionSpinner, mLanguageHebrew);
                    break;
            }
        }
        mLastSelectedPosition = position;
        mLanguageSelectionSpinner.setSelection(position);
    }


    //View click listeners
    @OnClick(R.id.preferences_change_name) public void onPreferenceNameChangeButtonClick() {
        showUserInfoUpdateDialog("name");
    }
    @OnClick(R.id.preferences_change_email) public void onPreferenceEmailChangeButtonClick() {
        showUserInfoUpdateDialog("email");
    }
    @OnClick(R.id.preferences_change_password) public void onPreferencePasswordChangeButtonClick() {
        showUserInfoUpdateDialog("password");
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

            mUser = (User) mFirebaseDao.createObjectWithUIAndReturnIt(mUser);
            modifyLayoutAccordingToCredentials();
        }
        else {
            mUser = users.get(0);
            modifyLayoutAccordingToCredentials();
            setLanguageInSpinner();
        }
    }
    @Override public void onMapMarkerListFound(List<MapMarker> mapMarkers) {

    }
    @Override public void onImageAvailable(boolean imageWasDownloaded, Uri imageUri, String imageName) {

    }
    @Override public void onImageUploaded(List<String> uploadTimes) {

    }
}
