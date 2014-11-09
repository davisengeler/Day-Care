package com.example.daycare.daycare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
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
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A login screen that offers login via email/password.

 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor>{

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:abc", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private AccountAuthorize mAuthTask = null;
    private UserLoginTask mUserTask = null;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
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
        mAuthTask = new AccountAuthorize();
        mAuthTask.execute( );
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

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
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
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
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
        protected final String VALIDATE = "Verified", USER_ID = "UserID", F_NAME = "FirstName",
                L_NAME = "LastName", ADDRESS = "Address", PHONE = "Phone", EMAIL = "Email", ACCT_ID = "AccID";

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String LOG_TAG = "Test Info";

            final String BASE_URL = "http://davisengeler.gwdnow.com/user.php?login";
            final String EMAIL_PARAM = "email";
            final String PASS_PARAM = "pass";


            try
            {
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(EMAIL_PARAM, mEmail)
                        .appendQueryParameter(PASS_PARAM, mPassword).build();

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
                JSONObject acctValidate = new JSONObject(jsonStr);
                Bundle b = new Bundle();
                Log.v("JSON string ", acctValidate.toString());
                String verified = acctValidate.getString(VALIDATE);
                b.putString("verified", verified);
                if(verified != null)
                {

                    if(verified.compareTo("1")==0)
                    {

                        acctType = Integer.parseInt(acctValidate.getString(ACCT_ID));

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
            mAuthTask = null;
            showProgress(false);

            if (success) { //work on switch
                switch (acctType)
                {
                    case 1:
                        break;
                    case 2:
                        Intent teachIntent = new Intent(getApplicationContext(), StudentViewActivity.class);
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
            mAuthTask = null;
            showProgress(false);
        }
    }
    public class AccountAuthorize extends AsyncTask<Void, Void, Boolean>{

        protected Boolean doInBackground(Void...params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = null;
            String LOG_TAG = "Test Info";

            final String DE = "http://davisengeler.gwdnow.com/add-device.php?deviceID=";

            String android_id = Secure.getString(getApplicationContext().getContentResolver(),
                    Secure.ANDROID_ID);

            Log.d("Android", "Android ID : " + android_id);

            try {
                String building = DE + android_id; //Putting together the URL
                Log.v(LOG_TAG, "Built URL " + building);
                URL url = new URL(building);

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



