package com.example.daycare.daycare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends Activity implements LoaderCallbacks<Cursor>{

    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_API_KEY = "api_key";
    public static final String PROPERTY_API_PASS = "api_password";
    public static final String PROPERTY_API_LOGIN = "api_login";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "385041079398";
    private static final String TAG = "GCM_setup";

    private UserLoginTask mUserTask = null;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private CheckBox mRememberView;
    private View mProgressView;
    private View mLoginFormView;

    private GoogleCloudMessaging gcm;
    private SharedPreferences prefs;
    public static Context context;
    private boolean rememberLogin = true;
    private String userid;
    private String regid;
    private boolean apilogin;
    private String apikey;
    private String apipass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        // Check device for Play Services APK. (For GCM)
        Log.i(TAG, "Checking for Play Service APK");
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            if (regid.equals("")) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
            Toast.makeText(getApplicationContext(), "Play Services required for Push " +
                    "Notifications.", Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView)findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.login || id == EditorInfo.IME_NULL)
                {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mRememberView = (CheckBox) findViewById(R.id.remember_me);
        mRememberView.setChecked(true);
        mRememberView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                if (checked) {
                    rememberLogin = true;
                    Log.i("RememberME", "Persistent login");
                } else {
                    rememberLogin = false;
                    Log.i("RememberME", "One time login");
                }
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // Check device for saved API key/pass
        apikey = prefs.getString(PROPERTY_API_KEY, "");
        if (!apikey.equals("")){
            Log.i("API_login", "Stored API key found");
            apipass = prefs.getString(PROPERTY_API_PASS, "");
            if (apipass.equals("")) {
                Log.i("API_login", "No stored API pass found, deleting key");
                apikey = "";
            }
        } else {
            Log.i("API_login", "No stored API key found");
        }

        // If stored API key/pass, auto login
        apilogin = prefs.getBoolean(PROPERTY_API_LOGIN, false);
        if (apilogin) {
            showProgress(true);
            mUserTask = new UserLoginTask(apikey, apipass);
            mUserTask.execute((Void) null);
        }
    }

    // You need to do the Play Services APK check here too. (For GCM)
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.equals("")) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);

        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}. (For GCM)
     */
    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(LoginActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}. (For GCM)
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        Log.i(TAG, "Registering new regid");
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i(TAG, msg);
            }
        }.execute(null, null, null);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        AccountAuthorize tickleAuthTask = new AccountAuthorize();
        tickleAuthTask.execute(regid);
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mUserTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
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
            mUserTask = new UserLoginTask(email, password);
            mUserTask.execute((Void) null);
        }
    }
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 2;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                                                                     .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private int acctType;
        private String jsonStr;
        private final String mEmail;
        private final String mPassword;
        protected final String VALIDATE = "verified", ACCT_ID = "accID";

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String LOG_TAG = "Test Info";
            final String EMAIL_PARAM;
            final String PASS_PARAM;

            final String BASE_URL = "http://davisengeler.gwdnow.com/user.php?login";
            if (apilogin) {
                EMAIL_PARAM = "apikey";
                PASS_PARAM = "apipass";
            } else {
                EMAIL_PARAM = "email";
                PASS_PARAM = "pass";
            }
            final String DEVICE_ID ="deviceID";
            String android_id = Secure.getString(getApplicationContext().getContentResolver(),
                    Secure.ANDROID_ID);
            JSONArray acctValidate;

            try
            {
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(EMAIL_PARAM, mEmail)
                        .appendQueryParameter(PASS_PARAM, mPassword)
                        .appendQueryParameter(DEVICE_ID, android_id).build();

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream != null)
                {
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        //makes easy to read in logs
                        buffer.append(line + "\n");
                    }
                    if (buffer.length() != 0)
                    {
                        jsonStr = buffer.toString();
                        Log.v(LOG_TAG, "JSON String: " + jsonStr);
                    }
                    else
                    {
                        return false;
                    }
                }
            }
            catch (MalformedURLException e)
            {
                Log.e(LOG_TAG, "Error with URL: " + e.getMessage());
                return false;
            }
            catch (IOException e) {
            // If the code didn't successfully get the data,
                Log.e(LOG_TAG, "Error: " + e.getMessage());
                return false;
            }
            finally
            {
               urlConnection.disconnect();
               try
               {
                    reader.close();
               }
               catch (final IOException e)
               {
                    Log.e(LOG_TAG, "Error closing stream", e);
               }

            }
            //get true from json
            try
            {
                acctValidate = new JSONArray(jsonStr);
                Bundle b = new Bundle();
                Log.v("JSON string ", acctValidate.toString());

                String verified = acctValidate.getJSONObject(0).getString(VALIDATE);
                b.putString("verified", verified);
                if(verified != null)
                {

                    if(verified.compareTo("1")==0)
                    {

                        acctType = Integer.parseInt(acctValidate.getJSONObject(0).getString(ACCT_ID));
                        userid = acctValidate.getJSONObject(0).getString("userID");
                        apikey = acctValidate.getJSONObject(0).getString("apiKey");
                        apipass = acctValidate.getJSONObject(0).getString("apiPass");
                        Log.i("API_login", "API key/pass: " + apikey + " / " + apipass);
                        Log.i("API_login", "Saving API key/pass");
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(PROPERTY_API_KEY, apikey);
                        editor.putString(PROPERTY_API_PASS, apipass);
                        if (rememberLogin) {
                            editor.putBoolean(PROPERTY_API_LOGIN, true);
                        } else {
                            editor.putBoolean(PROPERTY_API_LOGIN, false);
                        }
                        editor.commit();
                    }
                    else
                    {
                        DialogFragment eDialog = new ErrorMessageDialog();
                        eDialog.setArguments(b);
                        eDialog.show(getFragmentManager(), "error");
                        return false;
                    }
                }
            }
            catch (JSONException e)
            {
                Log.e("Error", e.getMessage());
            }


            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
//            mAuthTask = null;
            showProgress(false);
            if(regid.equals("")){
                Log.wtf(TAG, "No regigd");
            }
            else {
                sendRegistrationIdToBackend();
            }

            if (success) { //work on switch
                switch (acctType)
                {
                    case 1:
                        Intent officeIntent = new Intent(getApplicationContext(), AdminActivity.class);
                        officeIntent.putExtra("JSONString", jsonStr);
                        startActivity(officeIntent);
                        finish();
                        break;
                    case 2:
                        Intent teachIntent = new Intent(getApplicationContext(), StudentListActivity.class);
                        teachIntent.putExtra("JSONString", jsonStr);
                        startActivity(teachIntent);
                        finish();
                        break;
                    case 3:
                        Intent parentIntent = new Intent(getApplicationContext(), NewsFeedActivity.class);
                        parentIntent.putExtra("JSONString", jsonStr);
                        startActivity(parentIntent);
                        finish();
                        break;
                    case 4:
                        Intent adminIntent = new Intent(getApplicationContext(), AdminActivity.class);
                        adminIntent.putExtra("JSONString", jsonStr);
                        startActivity(adminIntent);
                        finish();
                        break;
                    default:
                        break;
                }

            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
//            mAuthTask = null;
            showProgress(false);
        }
    }

    public class AccountAuthorize extends AsyncTask<String, Void, Boolean>{
        protected Boolean doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = null;
            String LOG_TAG = "Test Info";

            final String DE = "http://davisengeler.gwdnow.com/user.php?updategcm=";
            Uri builtUri = Uri.parse(DE).buildUpon().appendQueryParameter("userid", userid)
                    .appendQueryParameter("gcm", regid)
                    .appendQueryParameter(PROPERTY_API_KEY, apikey)
                    .appendQueryParameter(PROPERTY_API_PASS, apipass).build();

            try {

                Log.v(LOG_TAG, "Built URL " + builtUri.toString());
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream != null) {
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        //makes easy to read in logs
                        buffer.append(line + "\n");
                    }
                    if (buffer.length() != 0) {
                        jsonStr = buffer.toString();
                        Log.v(LOG_TAG, "JSON String: " + jsonStr);
                    }
                }
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error with URL: " + e.getMessage());
                return false;
            } catch (IOException e) {
                // If the code didn't successfully get the data,
                Log.e(LOG_TAG, "Error: " + e.getMessage());
                return false;
            } finally {
                urlConnection.disconnect();
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }

            }

            return true;

        }
    }
    public static class ErrorMessageDialog extends DialogFragment
    {
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            String verified = getArguments().getString("verified");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            if(verified.compareTo("0")==0)
            {
                builder.setTitle("Account Approval")
                        .setMessage("Your account was not approved by the Administrator!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ErrorMessageDialog.this.getDialog().cancel();

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert);
            }
            else
            {
                builder.setTitle("Account Approval")
                        .setMessage("Your account must first be approved by the Administrator!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ErrorMessageDialog.this.getDialog().cancel();

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert);
            }
            return builder.create();
        }


    }
}



