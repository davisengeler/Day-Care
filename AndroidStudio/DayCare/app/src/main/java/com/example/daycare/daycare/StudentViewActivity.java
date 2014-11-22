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
import android.widget.EditText;
import android.widget.ListView;
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


public class StudentViewActivity extends Activity {
    protected String [] actions = {"Contact Parent", "Add Note","View Notes", "Move Student"};
    static final String [] teachers = {"Jane Smith", "John Doe", "Davis Engeler", "John Sloan", "Michael Hetzel"};
    private ListView mListView;
    private static String []  teacherNames;
    private TextView tView, dView, teachView, pView;
    private JSONArray pInfo;
    private static JSONArray tList;
    private static JSONObject childInfo;
    private String JSONString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_view);
        JSONString = this.getIntent().getStringExtra("JSONString");
        String chosenStudent = this.getIntent().getStringExtra("chosenStudent");
        String teacherString = this.getIntent().getStringExtra("teacherInfo");

        try
        {
            //JSONArray childArray = new JSONArray(chosenStudent);
            childInfo = new JSONObject(chosenStudent);
            //JSONArray teacherArray = new JSONArray(teacherString);
            JSONObject teacherInfo = new JSONObject(teacherString);
            GetParentInfo p = new GetParentInfo();
            p.execute(childInfo.getString("parentID"));
            tView = (TextView) findViewById(R.id.student_name_info);
            tView.setText(childInfo.getString("firstName") + " " + childInfo.getString("lastName"));
            dView = (TextView) findViewById(R.id.dob_info);
            dView.setText(childInfo.getString("dob"));
            teachView = (TextView) findViewById(R.id.teacher_name);
            teachView.setText(teacherInfo.getString("firstName") + " "+ teacherInfo.getString("lastName"));
            pView = (TextView) findViewById(R.id.contact_name);

        }
        catch (JSONException e)
        {
            Log.e("JSON", e.getMessage());
        }

        mListView = (ListView) findViewById(R.id.container);
        GetTeacherList GTL = new GetTeacherList();
        GTL.execute();
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, actions);

        //android ic_menu_edit, ic_perm_group_phone_calls.png, ic_menu_myplaces.png
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i)
                {
                    case 0:
                        //for testing purposes
                        DialogFragment dgE = new ErrorMessageDialog();
                        dgE.show(getFragmentManager(), "error");
                        break;
                    case 1:
                        DialogFragment dg2 = new NoteDialogFragment();
                        dg2.show(getFragmentManager(), "notes");
                        break;
                    case 2:

                        break;
                    case 3:
                        DialogFragment dg1 = new TeacherDialogFragment();
                        dg1.show(getFragmentManager(), "teachers");
                        break;
                    default:
                        break;
                }
            }
        });
    }
    @Override
    public void onBackPressed(){

        Intent intent = new Intent();
        intent.putExtra("JSONString", JSONString);
        setResult(Activity.RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }
//    @Override
//    public void onPause()
//    {
//        Intent intent = new Intent();
//        intent.putExtra("JSONString", JSONString);
//        setResult(Activity.RESULT_OK, intent);
//        finish();
//        super.onPause();
//    }
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event)
//    {
//        Toast.makeText(getApplicationContext(), "KEY UP", Toast.LENGTH_LONG).show();
//        Intent intent = new Intent();
//        intent.putExtra("JSONString", JSONString);
//        setResult(Activity.RESULT_OK, intent);
//        finish();
//        super.onKeyUp(keyCode, event);
//        return true;
//    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_student_view, menu);
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
    public static class TeacherDialogFragment extends DialogFragment
    {
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.pick_teacher)
                    .setItems(teacherNames, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which) {
                            ((StudentViewActivity)getActivity()).moveTheChildTask(which);
                        }
                    }
                    );
            return builder.create();
        }

    }
    public void moveTheChildTask(int which)
    {
        try {
            MoveChild move = new MoveChild();
            move.execute(tList.getJSONObject(which).getString("userID"), childInfo.getString("childID"));
        } catch (JSONException e) {
            Log.e("JSON MOVE", e.getMessage());
        }
    }
    public static class NoteDialogFragment extends DialogFragment
    {

        int noteIDChosen =4;
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final EditText message = new EditText(getActivity());
            message.setHint(R.string.enter_message);
            message.setInputType(41); //check to see if this works
            builder.setTitle(R.string.add_note_title)
                    .setSingleChoiceItems(R.array.note_id, noteIDChosen, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            noteIDChosen = i;
                        }
                    })
                    .setView(message)
                    .setPositiveButton(R.string.save_label, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            String mContent = message.getText().toString();
                            Log.v("NoteMessage", noteIDChosen + " " + mContent);
                            //submit this as a note to server with noteIDChosen
                        }
                    })
                    .setNegativeButton(R.string.cancel_label, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            NoteDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }
    }

    public static class ErrorMessageDialog extends DialogFragment
    {
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Must select a note type")
                    .setMessage("Please select the type of note")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert);
            return builder.create();
        }


    }
    public class GetParentInfo extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... params) {

            final String BASE_URL = "http://davisengeler.gwdnow.com/user.php?getaccountbyuserid";
            final String USER_ID = "userid";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";


            try {
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(USER_ID, params[0]).build();

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

                pInfo = new JSONArray(jsonStr);
                Log.v("ARRAY ", pInfo.toString());

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
                    pView.setText(pInfo.getJSONObject(0).getString("firstName")+ " " + pInfo.getJSONObject(0).getString("lastName"));

                }
                catch(JSONException e)
                {
                    Log.e("JSON PINFO", e.getMessage());
                }


            }
            else
            {
                Toast.makeText(getApplicationContext(), "Couldn't Retrieve Parent Info", Toast.LENGTH_LONG).show();
            }

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

    public class MoveChild extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {

            final String BASE_URL = "http://davisengeler.gwdnow.com/child.php?setclass";
            final String TEACH_ID = "teacherid";
            final String CHILD_ID = "childid";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";


            try {
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(TEACH_ID, params[0])
                        .appendQueryParameter(CHILD_ID, params[1]).build();

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

            }
            catch(JSONException e)
            {
                Log.e("JSON STMT", e.getMessage());
            }

        }

    }
}
