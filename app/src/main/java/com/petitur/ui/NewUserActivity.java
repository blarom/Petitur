package com.petitur.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.petitur.R;
import com.petitur.data.Family;
import com.petitur.data.Foundation;
import com.petitur.data.User;
import com.petitur.resources.LocaleHelper;
import com.petitur.resources.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class NewUserActivity extends BaseActivity {


    //region Parameters
    @BindView(R.id.new_user_welcome_title_textview) TextView mTextViewWelcomeTitle;
    @BindView(R.id.new_user_explanation_for_family_textview) TextView mTextViewExplanationForFamily;
    @BindView(R.id.new_user_explanation_for_foundation_textview) TextView mTextViewExplanationForFoundation;
    @BindView(R.id.new_user_explanation_family_settings_textview) TextView mTextViewFamilySettings;
    @BindView(R.id.new_user_explanation_foundation_settings_textview) TextView mTextViewFoundationSettings;
    @BindView(R.id.new_user_language_selection_spinner) Spinner mSpinnerLanguage;
    @BindView(R.id.new_user_family_pseudonym_container) TextInputLayout mEditTextFamilyPseudonymContainer;
    @BindView(R.id.new_user_family_pseudonym) TextInputEditText mEditTextFamilyPseudonym;
    @BindView(R.id.new_user_foundation_name_container) TextInputLayout mEditTextFoundationNameContainer;
    @BindView(R.id.new_user_foundation_name) TextInputEditText mEditTextFoundationName;
    @BindView(R.id.new_user_country) TextInputEditText mEditTextCountry;
    @BindView(R.id.new_user_state) TextInputEditText mEditTextState;
    @BindView(R.id.new_user_city) TextInputEditText mEditTextCity;
    @BindView(R.id.new_user_street_container) TextInputLayout mEditTextStreetContainer;
    @BindView(R.id.new_user_street) TextInputEditText mEditTextStreet;
    @BindView(R.id.new_user_street_number_container) TextInputLayout mEditTextStreetNumberContainer;
    @BindView(R.id.new_user_street_number) TextInputEditText mEditTextStreetNumber;
    @BindView(R.id.new_user_done_button) Button mButtonDone;
    private Unbinder mBinding;
    private User mUser;
    private String mFirebaseName;
    private Foundation mFoundation;
    private Family mFamily;
    private int mLastSelectedLanguagePosition;
    private String mLanguageCode;
    //endregion


    //Lifecycle methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        getExtras();
        initializeParameters();
        updateWelcomeMessage();
        setLayoutAccordingToUserType();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        mBinding.unbind();
    }
    @Override public void onBackPressed() {
        if (mFoundation==null && mFamily == null) {
            Toast.makeText(this, R.string.must_complete_registration, Toast.LENGTH_SHORT).show();
            //Utilities.restartApplication(this);
            finishAffinity(); //closes the app
        }
        super.onBackPressed();
    }

    //Functional methods
    private void getExtras() {

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.bundled_user))) {
            mUser = intent.getParcelableExtra(getString(R.string.bundled_user));
        }
        if (intent.hasExtra(getString(R.string.family_profile_parcelable))) {
            mFamily = intent.getParcelableExtra(getString(R.string.family_profile_parcelable));
        }
        if (intent.hasExtra(getString(R.string.foundation_profile_parcelable))) {
            mFoundation = intent.getParcelableExtra(getString(R.string.foundation_profile_parcelable));
        }
        if (intent.hasExtra(getString(R.string.firebase_name))) {
            mFirebaseName = intent.getStringExtra(getString(R.string.firebase_name));
        }
    }
    private void initializeParameters() {

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.new_user);
        }

        mBinding =  ButterKnife.bind(this);
        Utilities.hideSoftKeyboard(this);

        mSpinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {

                if (pos == mLastSelectedLanguagePosition) return;

                //Getting the selected language
                String selectedItem = (String) adapterView.getItemAtPosition(pos);

                String languageCode = "";
                if (selectedItem.equals(getString(R.string.language_selection_english))) {
                    languageCode = getString(R.string.language_code_english);
                }
                else if (selectedItem.equals(getString(R.string.language_selection_hebrew))) {
                    languageCode = getString(R.string.language_code_hebrew);
                }
                final String mLanguageCode = languageCode;

                //Creating an alert dialog to make sure the user wants to change the language
                AlertDialog alertDialog = new AlertDialog.Builder(NewUserActivity.this).create();
                alertDialog.setMessage(getString(R.string.sure_to_change_app_language));

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        mUser.setLg(mLanguageCode);
                        Utilities.setAppPreferenceLanguage(getBaseContext(), mLanguageCode);
                        Context context = LocaleHelper.setLocale(getBaseContext(), mLanguageCode);

                        //Returning to the TaskSelectionActivity in order to restart the app
                        Intent data = new Intent();
                        data.putExtra(getString(R.string.bundled_user), mUser);
                        setResult(RESULT_OK, data);
                        onBackPressed();
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
        mLastSelectedLanguagePosition = Utilities.setupLanguageInSpinner(NewUserActivity.this, mUser, mSpinnerLanguage);

    }
    private void setLayoutAccordingToUserType() {
        if (mUser.getIF()) {
            mTextViewExplanationForFamily.setVisibility(View.GONE);
            mTextViewExplanationForFoundation.setVisibility(View.VISIBLE);
            mTextViewFamilySettings.setVisibility(View.GONE);
            mTextViewFoundationSettings.setVisibility(View.VISIBLE);
            mEditTextFamilyPseudonymContainer.setVisibility(View.GONE);
            mEditTextFoundationNameContainer.setVisibility(View.VISIBLE);
            mEditTextStreetContainer.setVisibility(View.VISIBLE);
            mEditTextStreetNumberContainer.setVisibility(View.VISIBLE);

            SpannableString spanableString = new SpannableString(getString(R.string.registered_as_organization_or_click_link));
            int startIndex = spanableString.toString().indexOf(getString(R.string.link));
            int endIndex = spanableString.toString().indexOf(getString(R.string.link)) + getString(R.string.link).length();

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Utilities.sendEmailToAdmin(NewUserActivity.this, mUser, false);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(true);
                }
            };
            spanableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            mTextViewExplanationForFoundation.setText(spanableString);
            mTextViewExplanationForFoundation.setMovementMethod(LinkMovementMethod.getInstance());
            mTextViewExplanationForFoundation.setHighlightColor(Color.BLUE);
        }
        else {
            mTextViewExplanationForFamily.setVisibility(View.VISIBLE);
            mTextViewExplanationForFoundation.setVisibility(View.GONE);
            mTextViewFamilySettings.setVisibility(View.VISIBLE);
            mTextViewFoundationSettings.setVisibility(View.GONE);
            mEditTextFamilyPseudonymContainer.setVisibility(View.VISIBLE);
            mEditTextFoundationNameContainer.setVisibility(View.GONE);
            mEditTextStreetContainer.setVisibility(View.GONE);
            mEditTextStreetNumberContainer.setVisibility(View.GONE);

            SpannableString spanableString = new SpannableString(getString(R.string.if_organization_click_this_link));
            int startIndex = spanableString.toString().indexOf(getString(R.string.link));
            int endIndex = spanableString.toString().indexOf(getString(R.string.link)) + getString(R.string.link).length();

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Utilities.sendEmailToAdmin(NewUserActivity.this, mUser, true);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(true);
                }
            };
            spanableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            mTextViewExplanationForFamily.setText(spanableString);
            mTextViewExplanationForFamily.setMovementMethod(LinkMovementMethod.getInstance());
            mTextViewExplanationForFamily.setHighlightColor(Color.BLUE);
        }
    }
    private void updateProfileAccordingToUserType() {
        if (mUser.getIF()) {
            String name = mEditTextFoundationName.getText().toString();
            String country = mEditTextCountry.getText().toString();
            String state = mEditTextState.getText().toString();
            String city = mEditTextCity.getText().toString();
            String street = mEditTextStreet.getText().toString();
            String streetNumber = mEditTextStreetNumber.getText().toString();

            if (name.length()<2 || country.length() < 2 || city.length() < 1 || street.length() < 1 || streetNumber.length()<1) {
                Toast.makeText(this, R.string.must_enter_valid_values_to_continue, Toast.LENGTH_SHORT).show();
                return;
            }

            if (mFoundation==null) mFoundation = new Foundation();
            mFoundation.setOI(mUser.getOI());
            mFoundation.setNm(name);
            mFoundation.setCn(country);
            mFoundation.setSe(state);
            mFoundation.setCt(city);
            mFoundation.setSt(street);
            mFoundation.setStN(streetNumber);
        }
        else {
            String name = mEditTextFamilyPseudonym.getText().toString();
            String country = mEditTextCountry.getText().toString();
            String state = mEditTextState.getText().toString();
            String city = mEditTextCity.getText().toString();

            if (name.length()<2 || country.length() < 2 || city.length() < 1) {
                Toast.makeText(this, R.string.must_enter_valid_values_to_continue, Toast.LENGTH_SHORT).show();
                return;
            }

            if (mFamily==null) mFamily = new Family();
            mFamily.setOI(mUser.getOI());
            mFamily.setPn(name);
            mFamily.setCn(country);
            mFamily.setSe(state);
            mFamily.setCt(city);
        }
    }
    private void updateWelcomeMessage() {
        String welcomeMessage = getString(R.string.welcome);
        if (!TextUtils.isEmpty(mFirebaseName)) welcomeMessage = getString(R.string.welcome_) + mFirebaseName + "!";
        mTextViewWelcomeTitle.setText(welcomeMessage);
    }
    private void returnProfileToTaskActivity() {

        if (mUser.getIF() && mFoundation!=null) {
            Intent data = new Intent();
            data.putExtra(getString(R.string.bundled_user), mUser);
            data.putExtra(getString(R.string.foundation_profile_parcelable), mFoundation);
            setResult(RESULT_OK, data);
            finish();
        }
        else if (!mUser.getIF() && mFamily!=null) {
            Intent data = new Intent();
            data.putExtra(getString(R.string.bundled_user), mUser);
            data.putExtra(getString(R.string.family_profile_parcelable), mFamily);
            setResult(RESULT_OK, data);
            finish();
        }
        else if (mFoundation==null && mFamily == null) {
            Intent data = new Intent();
            data.putExtra(getString(R.string.bundled_user), mUser);
            setResult(RESULT_OK, data);
            finish();
        }
    }


    //View click listeners
    @OnClick(R.id.new_user_done_button) public void onDoneButtonClick() {
        updateProfileAccordingToUserType();
        returnProfileToTaskActivity();
    }
}
