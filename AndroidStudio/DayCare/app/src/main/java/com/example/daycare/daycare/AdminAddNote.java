package com.example.daycare.daycare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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
//waiting for param format to get notes to post

public class AdminAddNote extends Activity {
    EditText mEditText;
    RadioButton radioButton1, radioButton2, radioButton3, radioButton4, radioButton5;
    int noteIndex;
    private JSONObject cInfo;
    Button button;
    private String apikey, apipass;
    private boolean bySSN = false;
    private static String [] teacherNames;
    private static JSONArray tList;
    private String childIDs = "", JSONString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getPreferences(getApplicationContext());
        apikey = prefs.getString(LoginActivity.PROPERTY_API_KEY, "");
        apipass = prefs.getString(LoginActivity.PROPERTY_API_PASS, "");
        setContentView(R.layout.activity_admin_add_note);
        Resources res = getResources();
        GetTeacherList teachAsync = new GetTeacherList();
        teachAsync.execute();
        JSONString = this.getIntent().getStringExtra("JSONString");
        final String [] notes = res.getStringArray(R.array.note_id);
        radioButton1 = (RadioButton) findViewById(R.id.radio_1);
        radioButton2 = (RadioButton) findViewById(R.id.radio_2);
        radioButton3 = (RadioButton) findViewById(R.id.radio_3);
        radioButton4 = (RadioButton) findViewById(R.id.radio_4);
        radioButton5 = (RadioButton) findViewById(R.id.radio_5);
        radioButton1.setText(notes[0]);
        radioButton2.setText(notes[1]);
        radioButton3.setText(notes[2]);
        radioButton4.setText(notes[3]);
        radioButton5.setText(notes[4]);
        radioButton1.isChecked();
        mEditText = (EditText) findViewById(R.id.note_message_text);

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("Class or Child Note")
                .setMessage("Choose class or child note")
                .setPositiveButton("Child Note", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ChildSearchDialog search = new ChildSearchDialog();
                        search.show(getFragmentManager(), "childSearch");
                    }
                })
                .setNegativeButton("Class Note", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TeacherDialogFragment tDialog = new TeacherDialogFragment();
                        tDialog.show(getFragmentManager(), "teach");
                    }
                });
        builder1.create();
        builder1.show();




        button = (Button) findViewById(R.id.addNoteButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mEditText.getText().toString();
                Log.v("Note: ", message + " notes index:" + notes[noteIndex]);
                String noteType = "1";
                String noteIndexString = "" + (noteIndex+1);
                AddNote newNote = new AddNote();
                newNote.execute(message, noteIndexString, noteType,childIDs);
            }
        });




    }

    public void childSearchAsync(String cSSN, boolean trans) {
        GetChildInfo childInfoBySSN = new GetChildInfo();
        bySSN = trans;
        String ssnArray = "[" + cSSN + "]";
        childInfoBySSN.execute(ssnArray);
    }


    public void onRadioButtonClicked(View view)
    {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_1:
                if (checked)
                {
                    noteIndex = 0;
                }
                break;
            case R.id.radio_2:
                if (checked)
                {
                    noteIndex = 1;
                }
                break;
            case R.id.radio_3:
                if (checked)
                {
                    noteIndex = 2;
                }
                break;
            case R.id.radio_4:
                if (checked)
                {
                    noteIndex = 3;
                }
                break;
            case R.id.radio_5:
                if (checked)
                {
                    noteIndex = 4;
                }
                break;
            default:
                break;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_admin_add_note, menu);
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

    public class AddNote extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {

            final String BASE_URL = "http://davisengeler.gwdnow.com/child.php?addnote";
            final String MESSAGE_ID = "message";
            final String NOTE_ID = "notetype";
            final String SUBJECT_ID = "subjectid";
            final String CHILD_IDS = "children";
            final String API_KEY_PARAM = "apikey";
            final String API_PASS_PARAM = "apipass";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";


            try {
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(MESSAGE_ID, params[0])
                        .appendQueryParameter(SUBJECT_ID, params[1])
                        .appendQueryParameter(NOTE_ID, params[2])
                        .appendQueryParameter(CHILD_IDS, params[3])
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

            return jsonStr;
        }

        protected void onPostExecute(String stmt){

            try
            {
                JSONObject j = new JSONObject(stmt);
                Toast.makeText(getApplicationContext(), j.getString("statusMessage"), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
                intent.putExtra("JSONString", JSONString);
                finish();

            }
            catch(JSONException e)
            {
                Log.e("JSON STMT", e.getMessage());
            }

        }

    }

    public class GetChildInfo extends AsyncTask<String, Void, String>
    {

        public String doInBackground(String...params)
        {

            final String BASE_URL_LOGIN = "http://davisengeler.gwdnow.com/child.php?getinfo";
            final String API_KEY_PARAM = "apikey";
            final String API_PASS_PARAM = "apipass";
            final String SSN_PARAM = "ssn";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr ="";

            try {
                Uri builtUri = Uri.parse(BASE_URL_LOGIN).buildUpon()
                        .appendQueryParameter(SSN_PARAM, params[0])
                        .appendQueryParameter(API_KEY_PARAM, apikey)
                        .appendQueryParameter(API_PASS_PARAM, apipass)
                        .build();

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
                try
                {
                    JSONArray cArray = new JSONArray(message);
                    cInfo = new JSONObject(cArray.getJSONObject(0).toString());
                    childIDs = "[" + cInfo.getString("childID") + "]";
                    //childIDs = cInfo.getString("childID");
                }
                catch(Exception e)
                {
                    Log.e("JSON GET CHILD", e.getMessage());
                }
                Toast.makeText(getApplicationContext(), "Found Child", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "No Match Found", Toast.LENGTH_LONG).show();

            }
        }
    }

    public static class TeacherDialogFragment extends DialogFragment
    {
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.pick_teacher)
                    .setItems(teacherNames, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which) {
                                    ((AdminAddNote)getActivity()).storeIDs(which);
                                }
                            }
                    );
            return builder.create();
        }

    }

    public void storeIDs(int whichTeacher)
    {
        try{

            childIDs = tList.getJSONObject(whichTeacher).getString("children").replaceAll("\"", "");
        }
        catch (JSONException e)
        {
            Log.e("TEACHER JSON", e.getMessage());
        }

    }
    public static class ChildSearchDialog extends DialogFragment
    {
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final EditText cSSN = new EditText(getActivity());
            cSSN.setHint("Enter child's SSN Here");
            cSSN.setInputType(2);
            builder.setTitle("Child Search")
                    .setMessage("Please Enter SSN")
                    .setView(cSSN)
                    .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((AdminAddNote)getActivity()).childSearchAsync(cSSN.getText().toString(), true);

                        }
                    });
            return builder.create();
        }

    }

    public class GetTeacherList extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... params) {

            final String BASE_URL = "http://davisengeler.gwdnow.com/user.php?teacherlist";
            final String API_KEY_PARAM = "apikey";
            final String API_PASS_PARAM = "apipass";

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";


            try {
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
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
}
