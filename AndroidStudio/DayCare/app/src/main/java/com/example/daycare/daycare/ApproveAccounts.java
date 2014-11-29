package com.example.daycare.daycare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class ApproveAccounts extends Activity {
    private ListView mListView;
    private static int listChoice;
    private String []approveList, toDisplay, acctTypeList;
    private static JSONArray account_Type;
    private String apikey, apipass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getPreferences(getApplicationContext());
        apikey = prefs.getString(LoginActivity.PROPERTY_API_KEY, "");
        apipass = prefs.getString(LoginActivity.PROPERTY_API_PASS, "");
        setContentView(R.layout.activity_approve_accounts);
        mListView = (ListView) findViewById(R.id.container);
        AcctApprovals a = new AcctApprovals();
        a.execute();
        try
        {
            acctTypeList = this.getIntent().getStringArrayExtra("AcctTypeList");
        }
        catch (Exception e)
        {
            Log.e("Getting acct types", e.getMessage());
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                listChoice = i;
                ApproveDialog dialog = new ApproveDialog();
                dialog.show(getFragmentManager(), "approve");

            }
        });


    }
    public void setList(String[] toDisplay)
    {


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, toDisplay);
        mListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_approve_accounts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.logout_option) {
            SharedPreferences prefs = getPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(LoginActivity.PROPERTY_API_KEY, "");
            editor.putString(LoginActivity.PROPERTY_API_PASS, "");
            editor.putBoolean(LoginActivity.PROPERTY_API_LOGIN, false);
            editor.commit();
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
            finish();

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getPreferences(Context context) {
        return getSharedPreferences(LoginActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    public class AcctApprovals extends AsyncTask<Void, Void, Boolean>
    {

        protected Boolean doInBackground(Void... params)
        {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";
                String userUrl = "http://davisengeler.gwdnow.com/user.php?getpendingaccounts";
                final String API_KEY_PARAM = "apikey";
                final String API_PASS_PARAM = "apipass";
                try {
                    Uri builtUri = Uri.parse(userUrl).buildUpon()
                            .appendQueryParameter(API_KEY_PARAM, apikey)
                            .appendQueryParameter(API_PASS_PARAM, apipass)
                            .build();

                    Log.v("Built URI ", builtUri.toString());
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
                        Log.v("JSON String: ", jsonStr);
                    }
                }
            }
            catch (MalformedURLException e)
            {
                Log.e("URL Error: ", e.getMessage());
            }
            catch (IOException e)
            {
                Log.e("Connection: ", e.getMessage());
            }
            finally
            {
                urlConnection.disconnect();
                try
                {
                    if (reader != null)
                        reader.close();
                } catch (IOException e)
                {
                    Log.e("Error closing stream", e.getMessage());
                }
            }

            try
            {

                account_Type = new JSONArray(jsonStr);

                approveList = new String[account_Type.length()];
                for(int i=0; i<account_Type.length(); ++i)
                {
                    approveList[i] = account_Type.get(i).toString();
                }
                return true;
            }
            catch(JSONException e)
            {
                Log.e("JSON Error: ", e.getMessage());
            }

            return false;
        }

        protected void onPostExecute(Boolean success)
        {
            if(success)
            {
                toDisplay = new String[account_Type.length()];
                try
                {

                    for(int i=0; i<account_Type.length(); ++i)
                    {
                        JSONObject j = new JSONObject(approveList[i]);
                        toDisplay[i] = j.getString("firstName");
                        toDisplay[i] +=  " " + j.getString("lastName");
                        toDisplay[i] += " - " +  "Account: ";
                        int idNum = Integer.parseInt(j.getString("accID"));
                        toDisplay[i] += acctTypeList[idNum-1];

                        Log.v("toDisplay " ,acctTypeList[idNum-1] + idNum);
                    }
                }
                catch (Exception e)
                {
                    e.getMessage();
                }
                setList(toDisplay);
            }
        }

    }
    public static class ApproveDialog extends DialogFragment
    {
        private String[] temp = {"Approve", "Deny"};
        private String userID, decision;
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            try
            {
                userID = account_Type.getJSONObject(listChoice).getString("userID");

            }
            catch (JSONException e)
            {
                Log.e("JSON APPROVE", e.getMessage());
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Approve Account")
                    .setItems(temp, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {

                                    if(which == 0)
                                    {
                                        decision = "approve";

                                    }
                                    else
                                    {
                                        decision = "deny";
                                    }
                                    ((ApproveAccounts)getActivity()).accountAsync(userID, decision);
                            }
                            }
                    );
            return builder.create();
        }
        public void onDismiss(DialogInterface dialog)
        {
            ((ApproveAccounts)getActivity()).recallList();
            ApproveDialog.this.getDialog().cancel();
        }
    }

    public void recallList()
    {
        AcctApprovals app = new AcctApprovals();
        app.execute();
    }
    public void accountAsync(String userID, String decision)
    {
        SendAccountApproval s1 = new SendAccountApproval();
        s1.execute(userID, decision);
    }

    public class SendAccountApproval extends AsyncTask<String, Void, Boolean> {
        JSONObject statement;

        protected Boolean doInBackground(String... params) {
            boolean success = false;
            String BASE_URL, USER_ID, DECISION;
            BASE_URL = "http://davisengeler.gwdnow.com/user.php?setapproval";
            USER_ID = "userid";
            DECISION = "decision";
            final String API_KEY_PARAM = "apikey";
            final String API_PASS_PARAM = "apipass";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";


            try {
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(USER_ID, params[0])
                        .appendQueryParameter(DECISION, params[1])
                        .appendQueryParameter(API_KEY_PARAM, apikey)
                        .appendQueryParameter(API_PASS_PARAM, apipass)
                        .build();

                Log.v("TEST:   ", builtUri.toString());

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
                        jsonStr += buffer.toString();
                    }
                }
            } catch (MalformedURLException e) {
                Log.e("URL Error: ", e.getMessage());
            } catch (IOException e) {
                Log.e("Connection: ", e.getMessage());
            } finally {
                urlConnection.disconnect();
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    Log.e("Error closing stream", e.getMessage());
                }
            }

            try {
                statement = new JSONObject(jsonStr);
                success = statement.getBoolean("successful");

            } catch (JSONException e) {
                Log.e("JSON Error: ", e.getMessage());
            }

            return success;
        }

        protected void onPostExecute(Boolean success) {
            if (success) {
                try {
                    Toast.makeText(getApplicationContext(), statement.getString("statusMessage"), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Log.v("JSON", e.getMessage());
                }

            } else {
                try {
                    Toast.makeText(getApplicationContext(), statement.getString("statusMessage"), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Log.v("JSON", e.getMessage());
                }
            }
        }

    }
}
