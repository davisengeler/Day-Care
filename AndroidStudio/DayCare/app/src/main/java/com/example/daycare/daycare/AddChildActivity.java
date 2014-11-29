package com.example.daycare.daycare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class AddChildActivity extends Activity
{
    Button submitButton;
    String parentID;
    String inputInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        parentID = this.getIntent().getStringExtra("UserID");
        if(parentID == null)
        {
            final GetParentID pID = new GetParentID();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final EditText pSSN = new EditText(this);
            pSSN.setHint("Enter SSN Here");
            pSSN.setInputType(2);
            builder.setTitle("Parent Search")
                    .setMessage("Please Enter Parent SSN")
                    .setView(pSSN)
                    .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            pID.execute(pSSN.getText().toString());
                        }
                    });
            builder.create();
            builder.show();


        }
        final EditText cSsn = (EditText) findViewById(R.id.cSsn);
        final EditText cFirstName = (EditText) findViewById(R.id.cfirst_name);
        final EditText cLastName = (EditText) findViewById(R.id.clast_name);
        final EditText cDob = (EditText) findViewById(R.id.date_of_birth);
        submitButton = (Button) findViewById(R.id.cSubmitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ssn, fName, lName, dob;
                ssn = cSsn.getText().toString();
                fName = cFirstName.getText().toString();
                lName = cLastName.getText().toString();
                dob = cDob.getText().toString();
                AddChild add = new AddChild();
                add.execute(ssn, fName, lName, dob, parentID);

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_child, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }
    public class GetParentID extends AsyncTask<String, Void, String>
    {

        public String doInBackground(String...params)
        {
            String idNum = "";
            final String BASE_URL_LOGIN = "http://davisengeler.gwdnow.com/user.php?login";
            final String SSN_PARAM = "ssn";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr;

            try {
                Uri builtUri = Uri.parse(BASE_URL_LOGIN).buildUpon()
                        .appendQueryParameter(SSN_PARAM, params[0]).build();

                Log.v("Built URI " , builtUri.toString());
                URL getID = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) getID.openConnection();
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
                        Log.v("JSON String: " , jsonStr);
                        JSONObject userID = new JSONObject(jsonStr);
                        idNum = userID.getString("UserID");
                    }
                }


            } catch (MalformedURLException e) {
                Log.e("Error with URL: ", e.getMessage());

            } catch (IOException e) {
                // If the code didn't successfully get the data,
                Log.e("Error: ", e.getMessage());

            } catch (JSONException e)
            {
                Log.e("JSON ERROR", e.getMessage());
            }
            finally {
                urlConnection.disconnect();
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("Error closing stream", e.getMessage());
                }
            }

            return idNum;
        }
        protected void onPostExecute(String message)
        {
            Log.v("Message: ", message);
            if(message != null)
            {
                Toast.makeText(getApplicationContext(), "Found Parent", Toast.LENGTH_LONG).show();
                parentID = message;
            }
            else
            {
                Toast.makeText(getApplicationContext(), "No Match Found", Toast.LENGTH_LONG).show();
                ParentDialogFragment p1 = new ParentDialogFragment();
                p1.alertDialog().show();
                doInBackground(inputInfo); //problem might be here
            }
        }
    }
    public class AddChild extends AsyncTask<String, Void, String>
    {

        public String doInBackground(String...params)
        {
            String suc = "";
            final String BASE_URL = "http://davisengeler.gwdnow.com/child.php?add";
            final String SSN_PARAM = "ssn";
            final String FIRST_NAME = "firstname";
            final String LAST_NAME = "lastname";
            final String DOB = "dob";
            final String USER_ID = "UserID";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr;
            try
            {
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(SSN_PARAM, params[0])
                        .appendQueryParameter(FIRST_NAME, params[1])
                        .appendQueryParameter(LAST_NAME, params[2])
                        .appendQueryParameter(DOB, params[3])
                        .appendQueryParameter(USER_ID, params[4]).build();
                URL url = new URL(builtUri.toString());
                Log.v("Add Child: ", builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
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
                        Log.v("JSON String: ", jsonStr);
                        JSONObject jObj = new JSONObject(jsonStr);
                        suc = jObj.getString("successful");

                    }
                }
            }
            catch(Exception e)
            {
                Log.e("ERROR", e.getMessage());
            }
            finally {
                urlConnection.disconnect();
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("Error closing stream", e.getMessage());
                }
            }

            return suc;
        }

        protected void onPostExecute(String message)
        {
            if(message.compareTo("true")==0)
            {
                Toast.makeText(getApplicationContext(), "Added Child", Toast.LENGTH_LONG).show();
                AnotherChildDialog d = new AnotherChildDialog();
                Bundle b = new Bundle();
                b.putString("pID", parentID);
                d.setArguments(b);
                d.show(getFragmentManager(), "child");

            }
            else
            {
                Toast.makeText(getApplicationContext(), "Try Again", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class ParentDialogFragment {

        public Dialog alertDialog() {

            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            final EditText pSSN = new EditText(getApplicationContext());
            pSSN.setHint("Enter SSN Here");
            pSSN.setInputType(2);
            builder.setTitle("Parent Search")
                    .setMessage("Please Enter Parent SSN")
                    .setView(pSSN)
                    .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            inputInfo = pSSN.getText().toString();
                        }
                    });

            return builder.create();
        }
    }

    public static class AnotherChildDialog extends DialogFragment
    {
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            final String pID = getArguments().getString("pID");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Add Child")
                    .setMessage("Do you want to add another child?")
                    .setPositiveButton(R.string.yes_label, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(getActivity(), AddChildActivity.class);
                            intent.putExtra("UserID", pID);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.no_label, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(getActivity(), AdminActivity.class);
                            startActivity(intent);
                        }
                    });
            return builder.create();

        }
    }
}
