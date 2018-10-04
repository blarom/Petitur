package com.petitur.ui.common;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.petitur.R;
import com.petitur.resources.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class CreateAccountActivity extends BaseActivity {


    //region Parameters
    public static final String DEBUG_TAG = "CreateAccount Debug";
    private static final boolean REQUIRE_VERIFICATION_EMAIL = true;
    @BindView(R.id.create_account_value_username) TextInputEditText mUsernameEditText;
    @BindView(R.id.create_account_value_email) TextInputEditText mEmailEditText;
    @BindView(R.id.create_account_value_password) TextInputEditText mPasswordEditText;
    @BindView(R.id.create_account_value_confirm_password) TextInputEditText mConfirmPasswordEditText;
    private FirebaseAuth mFirebaseAuth;
    private String mUsername;
    private String mEmail;
    private String mPassword;
    private String mConfirmPassword;
    private FirebaseUser mFirebaseUser;
    private Unbinder mBinding;
    //endregion


    //region Activity lifecycle
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        initializeParameters();
    }
    @Override protected void onResume() {
        super.onResume();
        if (REQUIRE_VERIFICATION_EMAIL) checkIfEmailIsVerifiedAndProceed();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        if (mBinding!=null) mBinding.unbind();
    }
    @Override public void onBackPressed() {
        super.onBackPressed();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_blank_menu, menu);
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
    //endregion


    //region Functionality methods
    private void initializeParameters() {
        mFirebaseAuth = FirebaseAuth.getInstance();

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.create_an_account);
        }
    }
    public void showWishToSignInDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(getString(R.string.credentials_already_in_use_do_you_wish_to_sign_in));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        signIn();
                        finish();
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
    private void signIn() {
        mFirebaseAuth.signInWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(CreateAccountActivity.this, R.string.failed_to_sign_in_with_credentials, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void createAccount() {

        mBinding =  ButterKnife.bind(this);

        mUsername = mUsernameEditText.getText().toString();
        mEmail = mEmailEditText.getText().toString();
        mPassword = mPasswordEditText.getText().toString();
        mConfirmPassword = mConfirmPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(mUsername) && TextUtils.isEmpty(mEmail) && TextUtils.isEmpty(mPassword) && TextUtils.isEmpty(mConfirmPassword)) {
            Toast.makeText(this, R.string.please_fill_all_info, Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(mPassword)) {
            Toast.makeText(this, R.string.please_enter_password, Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(mPassword) && TextUtils.isEmpty(mConfirmPassword) && !mPassword.equals(mConfirmPassword)) {
            Toast.makeText(this, R.string.passwords_dont_match, Toast.LENGTH_SHORT).show();
        }
        else if (!TextUtils.isEmpty(mPassword) && mPassword.length()<6) {
            Toast.makeText(this, getString(R.string.please_enter_password_with_at_least_6_chars), Toast.LENGTH_SHORT).show();
        }
        else {

            mFirebaseAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                try {
                                    throw task.getException();
                                }
                                catch (FirebaseAuthWeakPasswordException weakPassword) {
                                    Toast.makeText(CreateAccountActivity.this, R.string.please_enter_password_with_at_least_6_chars, Toast.LENGTH_SHORT).show();
                                }
                                catch (FirebaseAuthInvalidUserException malformedEmail) {
                                    Toast.makeText(CreateAccountActivity.this, R.string.no_valid_account_found, Toast.LENGTH_SHORT).show();
                                }
                                catch (FirebaseAuthInvalidCredentialsException malformedEmail) {
                                    Toast.makeText(CreateAccountActivity.this, R.string.password_invalid_try_again, Toast.LENGTH_SHORT).show();
                                }
                                catch (FirebaseAuthUserCollisionException existEmail) {
                                    showWishToSignInDialog();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                updateUserProfile();
                                if (REQUIRE_VERIFICATION_EMAIL) sendVerificationEmail();
                                else goToTaskSelectionScreen();
                            }
                        }
                    });
        }
    }
    private void updateUserProfile() {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(mUsername)
                .build();

        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser!=null) {
            mFirebaseUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                Log.d(DEBUG_TAG, "User profile updated.");
                            }
                        }
                    });
        }
    }
    private void sendVerificationEmail() {
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser!=null) {
            mFirebaseUser.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (!task.isSuccessful()) {
                                Toast.makeText(CreateAccountActivity.this, R.string.sent_verification_email, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }
    private void checkIfEmailIsVerifiedAndProceed() {
        if (mFirebaseAuth==null) return;

        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser!=null) {
            Task<Void> usertask = mFirebaseUser.reload();
            usertask.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (mFirebaseUser.isEmailVerified()) {
                        Toast.makeText(CreateAccountActivity.this, R.string.successfully_verified_email, Toast.LENGTH_SHORT).show();
                        goToTaskSelectionScreen();
                    }
                    else {
                        Toast.makeText(CreateAccountActivity.this, R.string.created_acount_but_email_not_verified, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void goToTaskSelectionScreen() {
        startActivity(new Intent(CreateAccountActivity.this, TaskSelectionActivity.class));
        finish();
    }
    //endregion

    @OnClick (R.id.create_account_continue) public void onContinueButtonClick() {
        createAccount();
    }
}
