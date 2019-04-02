package erp.droid.studentattendancesystem;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUserId;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUserId = (AutoCompleteTextView) findViewById(R.id.userId);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserId.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String userId = mUserId.getText().toString();
        String password = mPasswordView.getText().toString();
        String userType = "";

        if (((RadioButton)findViewById(R.id.radio_student)).isChecked()) {
            userType = "student";
        }
        else {
            userType = "staff";
        }

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid userId address.
        if (TextUtils.isEmpty(userId)) {
            mUserId.setError(getString(R.string.error_field_required));
            focusView = mUserId;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(userId, userType, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUserId;
        private final String mUserType;
        private final String mPassword;

        UserLoginTask(String email, String userType, String password) {
            mUserId = email;
            mUserType = userType;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                JSONObject requestParameters = new JSONObject();
                final JSONObject authObject = new JSONObject();
                try {

                    authObject.put("userId", mUserId);
                    authObject.put("userType", mUserType);
                    requestParameters.put("authenticationObject", authObject);
                    requestParameters.put("password", mPassword);
                }catch(JSONException e){
                    Log.e("Error", e.getMessage());
                }

                VolleyUtils.makeJsonObjectRequest(LoginActivity.this.getApplicationContext(), new ApiRoute().LOGIN, requestParameters, new VolleyResponseListener() {
                    @Override
                    public void onError(String message) {
                        mAuthTask = null;
                        mLoginFormView.setVisibility(View.VISIBLE);
                        EditText errorText = ((EditText)findViewById(R.id.errorText));
                        errorText.setText(message);
                        errorText.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            showProgress(false);
                            if (response.getBoolean("success")) {
                                //Save token and proceed to main page if success
                                SharedPreferences sharedPref = LoginActivity.this.getApplicationContext().getSharedPreferences(
                                        "Config", LoginActivity.this.getApplicationContext().MODE_PRIVATE);

                                SharedPreferences.Editor editor = sharedPref.edit();
                                JSONObject dataJson = new JSONObject(response.getString("data"));
                                authObject.put("token", dataJson.getString("token"));
                                authObject.put("name", dataJson.getString("name"));
                                authObject.put("userId", mUserId);
                                authObject.put("userType", mUserType);
                                editor.putString("AuthenticationObject", authObject.toString());
                                editor.commit();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                LoginActivity.this.startActivity(intent);
                                finish();
                            } else {
                                mAuthTask = null;
                                mLoginFormView.setVisibility(View.VISIBLE);
                                mPasswordView.setError(getString(R.string.error_incorrect_password));
                                mPasswordView.requestFocus();
                            }
                        }
                        catch (Exception ex) {
                            Log.e("Error", ex.getMessage());
                        }
                    }
                });
            }
            catch (Exception ex) {
                Log.e("Error", ex.getMessage());
            }

            return true;
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
