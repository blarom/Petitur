package com.petitur.ui.common;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.petitur.R;
import com.petitur.resources.LocaleHelper;
import com.petitur.resources.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SignInActivity extends BaseActivity {


    //region Parameters
    public static final int GOOGLE_SIGN_IN_KEY = 8523;
    public static final String DEBUG_TAG = "SignIn Debug";
    @BindView(R.id.sign_in_with_email_value_email) TextInputEditText mEmailEditText;
    @BindView(R.id.sign_in_with_email_value_password) TextInputEditText mPasswordEditText;
    @BindView(R.id.sign_in_with_google) SignInButton mGoogleSignInButtonFrame;
    @BindView(R.id.sign_in_click_here) TextView mForgotPasswordClickHereTextView;
    @BindView(R.id.sign_in_with_google_loading_indicator) ProgressBar mProgressBarLoadingIndicator;
    @BindView(R.id.sign_in_language_selection_spinner) Spinner mLanguageSelectionSpinner;
    private FirebaseAuth mFirebaseAuth;
    private String mEmail;
    private String mPassword;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseUser mCurrentUser;
    private Unbinder mBinding;
    private String mLanguageCode;
    //endregion


    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initializeParameters();
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN_KEY) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, R.string.failed_sign_in_google, Toast.LENGTH_SHORT).show();
                // Google Sign In failed, update UI appropriately
                //Log.w(DEBUG_TAG, "Google sign in failed", e);
                // ...
            }
        }
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        if (mBinding!=null) mBinding.unbind();
    }
    @Override public void onBackPressed() {
        //super.onBackPressed();
        showExitAppDialog();
    }


    //Functionality methods
    private void initializeParameters() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mBinding =  ButterKnife.bind(this);

        TextView textView = (TextView) mGoogleSignInButtonFrame.getChildAt(0);
        textView.setText(R.string.sign_in_with_google);

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.sign_in);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.googleOAuth2WebAppId))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Making the "Click Here" text look like a hyperlink
        String styledText = "<u><font color='blue'>" + getString(R.string.click_here) + "</font></u>.";
        mForgotPasswordClickHereTextView.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);

        //Showing the last email that signed in
        String lastEmail = Utilities.getAppPreferenceLastEmailSignedIn(this);
        mEmailEditText.setText(lastEmail);

        //Setting the language listener
        mLanguageSelectionSpinner.setSelection(0,false);
        mLanguageSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {

                //Getting the selected language
                String selectedItem = (String) adapterView.getItemAtPosition(pos);
                mLanguageCode = getString(R.string.language_code_english);

                if (selectedItem.equals(getString(R.string.language_selection_english))) {
                    mLanguageCode = getString(R.string.language_code_english);
                }
                else if (selectedItem.equals(getString(R.string.language_selection_hebrew))) {
                    mLanguageCode = getString(R.string.language_code_hebrew);
                }

                //Creating an alert dialog to make sure the user wants to change the language
                AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this).create();
                alertDialog.setMessage(getString(R.string.sure_to_change_app_language));

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Utilities.setAppPreferenceLanguage(getApplicationContext(), mLanguageCode);
                        Context context = LocaleHelper.setLocale(getApplicationContext(), mLanguageCode);

                        //Relaunching the app
                        Utilities.restartApplication(SignInActivity.this);
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
    }
    private void signInWithEmail() {
        mEmail = mEmailEditText.getText().toString();
        mPassword = mPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(mEmail) || !mEmail.contains("@") || !mEmail.contains(".")) {
            Toast.makeText(this, getString(R.string.password_invalid_try_again), Toast.LENGTH_SHORT).show();
            return;
        }
        else if (TextUtils.isEmpty(mPassword) || mPassword.length()<6) {
            Toast.makeText(this, getString(R.string.failed_to_sign_in_with_credentials), Toast.LENGTH_SHORT).show();
            return;
        }

        mFirebaseAuth.signInWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            }
                            catch (FirebaseAuthInvalidUserException malformedEmail) {
                                Toast.makeText(SignInActivity.this, R.string.no_valid_account_found, Toast.LENGTH_SHORT).show();
                            }
                            catch (FirebaseAuthInvalidCredentialsException malformedEmail) {
                                Toast.makeText(SignInActivity.this, R.string.password_invalid_try_again, Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e) {
                                Toast.makeText(SignInActivity.this, R.string.failed_to_sign_in_with_credentials, Toast.LENGTH_SHORT).show();
                                openCreateAccountActivity();
                                e.printStackTrace();
                            }
                        }
                        else {
                            Utilities.setAppPreferenceLastEmailSignedIn(SignInActivity.this, mEmail);
                            returnToCallingActivity();
                        }
                    }
                });
    }
    private void signInWithGoogle() {
        showSignInWithGoogleLoadingIndicator();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_KEY);
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            //Toast.makeText(SignInActivity.this, R.string.sign_in_successful, Toast.LENGTH_SHORT).show();
                            hideSignInWithGoogleLoadingIndicator();
                            returnToCallingActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(DEBUG_TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, R.string.failed_to_sign_in_with_credentials, Toast.LENGTH_SHORT).show();
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void showForgotPasswordDialog() {
        //region Get the dialog view
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_forgot_password, null);
        final TextInputEditText emailEditText = dialogView.findViewById(R.id.dialog_forgot_password_value_email);
        //endregion

        //region Building the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.password_reset);

        builder.setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String email = emailEditText.getText().toString();

                mFirebaseAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignInActivity.this, R.string.sent_reset_password_email, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SignInActivity.this, R.string.failed_to_reset_email, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        //endregion

        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void returnToCallingActivity() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
    private void openCreateAccountActivity() {
        Intent intent = new Intent(SignInActivity.this, CreateAccountActivity.class);
        startActivity(intent);
        finish();
    }
    private void showSignInWithGoogleLoadingIndicator() {
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.VISIBLE);
    }
    private void hideSignInWithGoogleLoadingIndicator() {
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.INVISIBLE);
    }
    public void showExitAppDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(getString(R.string.sure_you_want_to_exit));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
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



    @OnClick(R.id.sign_in_with_google) public void onSignInWithGoogleClick() {
        signInWithGoogle();
    }
    @OnClick(R.id.sign_in_with_email_sign_in_button) public void onSignInWithEmailClick() {
        signInWithEmail();
    }
    @OnClick(R.id.sign_in_click_here) public void onForgotPasswordClick() {
        showForgotPasswordDialog();
    }
    @OnClick(R.id.sign_in_with_email_create_account_button) public void onCreateAccountClick() {
        openCreateAccountActivity();
    }
}
