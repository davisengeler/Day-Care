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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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


public class AddChildActivity extends Activity
{
    private Button submitButton;
    private JSONArray tList;
    private int chosenTeach;
    private String [] teacherNames;
    private String parentID, childID = "",teachString;
    private static String inputInfo;
    private String edit ="";
    private EditText cSsn, cFirstName, cLastName, cDob, classID;
    Spinner dropdown;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);
        GetTeacherList list = new GetTeacherList();
        list.execute();
        dropdown = (Spinner) findViewById(R.id.spinner);
        edit = this.getIntent().getStringExtra("Edit");
        parentID = this.getIntent().getStringExtra("UserID");

        if(edit != null)
        {
            final GetChildInfoBySSN childInfoBySSN = new GetChildInfoBySSN();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final EditText cSSN = new EditText(this);
            cSSN.setHint("Enter child's SSN Here");
            cSSN.setInputType(2);
            builder.setTitle("Child Search")
                    .setMessage("Please Enter SSN")
                    .setView(cSSN)
                    .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            childInfoBySSN.execute(cSSN.getText().toString());
                        }
                    });
            builder.create();
            builder.show();


        }
        else if(parentID == null)
        {
            ParentDialogFragment p1 = new ParentDialogFragment();
            p1.show(getFragmentManager(), "pdialog");
        }

        cSsn = (EditText) findViewById(R.id.cSsn);
        cFirstName = (EditText) findViewById(R.id.cfirst_name);
        cLastName = (EditText) findViewById(R.id.clast_name);
        cDob = (EditText) findViewById(R.id.date_of_birth);
        submitButton = (Button) findViewById(R.id.cSubmitButton);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                chosenTeach = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ssn, fName, lName, dob;
                ssn = cSsn.getText().toString();
                fName = cFirstName.getText().toString();
                lName = cLastName.getText().toString();
                dob = cDob.getText().toString();
                AddChild add = new AddChild();
                try
                {
                    teachString = tList.getJSONObject(chosenTeach).getString("userID");
                }
                catch(Exception e)
                {
                    Log.e("JSON SUBMIT", e.getMessage());
                }

                add.execute(ssn, fName, lName, dob, parentID, teachString);

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
            final String BASE_URL_LOGIN = "http://davisengeler.gwdnow.com/user.php?getaccountbyssn";
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
                        JSONArray another = new JSONArray(jsonStr);
                        JSONObject userID = new JSONObject(another.getJSONObject(0).toString());
                        idNum = userID.getString("userID");
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
                p1.show(getFragmentManager(), "anotherp1");

            }
        }
    }
    public class AddChild extends AsyncTask<String, Void, String>
    {
        String BASE_URL, SSN_PARAM, FIRST_NAME, LAST_NAME, DOB, PARENT_ID, TEACH_ID, CHILD_ID;
        Uri builtUri;

        protected String doInBackground(String...params)
        {
            SSN_PARAM = "ssn";
            FIRST_NAME = "firstname";
            LAST_NAME = "lastname";
            DOB = "dob";
            PARENT_ID = "parentid";
            TEACH_ID = "teacherid"; //teacherID
            CHILD_ID = "childid";
            if(edit == null)
            {
                BASE_URL = "http://davisengeler.gwdnow.com/child.php?add";
                builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(SSN_PARAM, params[0])
                        .appendQueryParameter(FIRST_NAME, params[1])
                        .appendQueryParameter(LAST_NAME, params[2])
                        .appendQueryParameter(DOB, params[3])
                        .appendQueryParameter(PARENT_ID, params[4])
                        .appendQueryParameter(TEACH_ID, params[5]).build();
            }
            else
            {
                BASE_URL = "http://davisengeler.gwdnow.com/child.php?edit";
                builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(SSN_PARAM, params[0])
                        .appendQueryParameter(FIRST_NAME, params[1])
                        .appendQueryParameter(LAST_NAME, params[2])
                        .appendQueryParameter(DOB, params[3])
                        .appendQueryParameter(PARENT_ID, params[4])
                        .appendQueryParameter(TEACH_ID, params[5])
                        .appendQueryParameter(CHILD_ID, childID).build();
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";
            try
            {

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

            return jsonStr;
        }

        protected void onPostExecute(String message)
        {
            try
            {
                JSONObject jMessage = new JSONObject(message);
                if(jMessage.getString("successful").compareTo("true")==0)
                {
                    if(edit == null)
                    {
                        Toast.makeText(getApplicationContext(), jMessage.getString("statusMessage"), Toast.LENGTH_LONG).show();
                        AnotherChildDialog d = new AnotherChildDialog();
                        Bundle b = new Bundle();
                        b.putString("pID", parentID);
                        d.setArguments(b);
                        d.show(getFragmentManager(), "child");
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), jMessage.getString("statusMessage"), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), jMessage.getString("statusMessage"), Toast.LENGTH_LONG).show();
                }
            }
            catch(JSONException e)
            {
                Log.e("JSON MESSAGE", e.getMessage());
            }


        }
    }

    public static class ParentDialogFragment extends DialogFragment {

        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final EditText pSSN = new EditText(getActivity());
            pSSN.setHint("Enter SSN Here");
            pSSN.setInputType(2);
            builder.setTitle("Parent Search")
                    .setMessage("Please Enter Parent SSN")
                    .setView(pSSN)
                    .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            inputInfo = pSSN.getText().toString();
                            ((AddChildActivity)getActivity()).retrievePID();
                        }
                    });

            return builder.create();
        }
    }
    public void retrievePID()
    {
        GetParentID getPID = new GetParentID();
        getPID.execute(inputInfo);
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

    public class GetChildInfoBySSN extends AsyncTask<String, Void, String>
    {

        public String doInBackground(String...params)
        {

            final String BASE_URL_LOGIN = "http://davisengeler.gwdnow.com/child.php?getinfo";
            final String SSN_PARAM = "ssn";
            String param = "[" + params[0] + "]";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr ="";

            try {
                Uri builtUri = Uri.parse(BASE_URL_LOGIN).buildUpon()
                        .appendQueryParameter(SSN_PARAM, param).build();

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
                    }
                }


            } catch (MalformedURLException e) {
                Log.e("Error with URL: ", e.getMessage());

            } catch (IOException e) {
                // If the code didn't successfully get the data,
                Log.e("Error: ", e.getMessage());

            }
            finally {
                urlConnection.disconnect();
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("Error closing stream", e.getMessage());
                }
            }

            return jsonStr;
        }
        protected void onPostExecute(String message)
        {
            Log.v("Message: ", message);
            if(message != null)
            {
                Toast.makeText(getApplicationContext(), "Found Child", Toast.LENGTH_LONG).show();
                setValues(message);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "No Match Found", Toast.LENGTH_LONG).show();

            }
        }
    }

    public void setValues(String jsonStr)
    {
        try
        {
            JSONArray temp = new JSONArray(jsonStr);
            JSONObject jsonObject = new JSONObject(temp.getJSONObject(0).toString());
            cFirstName.setText(jsonObject.getString("firstName"), TextView.BufferType.EDITABLE);
            cLastName.setText(jsonObject.getString("lastName"), TextView.BufferType.EDITABLE);
            cDob.setText(jsonObject.getString("dob"), TextView.BufferType.EDITABLE);
            cSsn.setText(jsonObject.getString("ssn"), TextView.BufferType.EDITABLE);
            parentID = jsonObject.getString("parentID");
            childID = jsonObject.getString("childID");


            for(int i=0; i<tList.length(); ++i)
            {
                if(jsonObject.getString("teacherID").compareTo(tList.getJSONObject(i).getString("teacherID"))==0)
                {
                    chosenTeach = i;
                }
            }
            dropdown.setSelection(chosenTeach);
        }
        catch(JSONException e)
        {
            Log.e("JSON SET", e.getMessage());
        }


    }
    public class GetTeacherList extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... params) {

            final String BASE_URL = "http://davisengeler.gwdnow.com/user.php?teacherlist";

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";


            try {
                Uri builtUri = Uri.parse(BASE_URL).buildUpon().build();

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

                tList = new JSONArray(jsonStr);
                Log.v("ARRAY ", tList.toString());

                return true;


            } catch (JSONException e) {
                Log.e("JSON Error: ", e.getMessage());
            }

            return false;
        }

        protected void onPostExecute(Boolean success){
            if(success)
            {
                try
                {
                    teacherNames = new String[tList.length()];
                    for(int i=0; i<tList.length(); ++i)
                    {
                        teacherNames[i] = tList.getJSONObject(i).getString("firstName") + " " +
                                tList.getJSONObject(i).getString("lastName");
                    }
                    teacherListNames();
                }
                catch(JSONException e)
                {
                    Log.e("JSON TEACH", e.getMessage());
                }


            }
            else
            {
                //Toast.makeText(getApplicationContext(), "Couldn't Retrieve Info", Toast.LENGTH_LONG).show();
            }

        }

    }
    public void teacherListNames ()
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, teacherNames);
        dropdown.setAdapter(adapter);
    }

}
