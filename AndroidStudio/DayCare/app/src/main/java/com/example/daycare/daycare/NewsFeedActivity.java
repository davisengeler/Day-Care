package com.example.daycare.daycare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import java.util.ArrayList;


public class NewsFeedActivity extends Activity {
    private ListView mListView;
    private ProgressBar mProgressView;
    private JSONArray notes;
    private static JSONArray cInfo, jArray;
    private static boolean[] checkedChildren, checkedStudents;
    private boolean progress = false;
    private static String[] childNames, students;
    private String childIDList;
    private String teacherView = "";
    private static String IDs = "", sMethod = "", noteIDs = "";

    private ArrayList<ChildrenNotes> cNotes = new ArrayList<ChildrenNotes>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);
        mListView = (ListView) findViewById(android.R.id.list);
        mProgressView = (ProgressBar) findViewById(R.id.progress_bar_news);

        teacherView = this.getIntent().getStringExtra("teacherView");
        JSONArray passedJSON = null;
        GetNotes note = new GetNotes();

        try {
            if (teacherView == null) {
                passedJSON = new JSONArray(this.getIntent().getStringExtra("JSONString"));
                childIDList = passedJSON.getJSONObject(0).getString("children").replaceAll("\"", "");
            } else {
                JSONObject passedChild = new JSONObject(teacherView);
                childIDList = "[" + passedChild.getString("childID") + "]";
            }
            showProgress(true);
            note.execute(childIDList);
        } catch (JSONException e) {
            Log.e("JSON String: ", e.getMessage());
        }


    }

    public void setList() {
        ChildrenNotesAdapter adapter = new ChildrenNotesAdapter(this, cNotes);

        mListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (teacherView == null) {
            getMenuInflater().inflate(R.menu.news_feed, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_sign_in) {

            GetChildInfo info = new GetChildInfo();
            info.execute(childIDList);
        } else if (id == R.id.add_note_option) {
            AddNoteDialog noteDialog = new AddNoteDialog();
            noteDialog.show(getFragmentManager(), "notething");
        }
        return super.onOptionsItemSelected(item);
    }

    public class GetNotes extends AsyncTask<String, Void, Boolean> {
        String ofIDS = "";

        protected Boolean doInBackground(String... params) {
            //showProgress(true);
            final String BASE_URL = "http://davisengeler.gwdnow.com/child.php?getnotes";
            final String CHILD_ID = "childids";
//            final String DEVICE_ID = "deviceID";
//            String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
//                    Settings.Secure.ANDROID_ID);
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";
            ofIDS = params[0];


            try {
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(CHILD_ID, params[0])
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

                notes = new JSONArray(jsonStr);

                //save note information here


            } catch (JSONException e) {
                Log.e("JSON Error: ", e.getMessage());
            }

            try {
                for (int i = 0; i < notes.length(); ++i) {
                    ChildrenNotes c = new ChildrenNotes(notes.getJSONObject(i).getString("ChildID"),
                            notes.getJSONObject(i).getString("NoteID"),
                            notes.getJSONObject(i).getString("Message"), notes.getJSONObject(i).getString("SubjectID"),
                            notes.getJSONObject(i).getString("NoteType"));
                    if (teacherView != null) {
                        if (c.getNoteType() == 0) {
                            cNotes.add(c);
                        }
                    } else {
                        if (c.getNoteType() == 1) {
                            cNotes.add(c);
                        }
                    }


                }

                return true;
            } catch (JSONException e) {
                Log.e("LOG", e.getMessage());
            }


            return false;
        }


        protected void onPostExecute(Boolean success) {
            if (success) {
                ChildLookup getInfo = new ChildLookup();
                getInfo.execute(ofIDS);
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
                showProgress(false);
                setList();
            } else {
                showProgress(false);
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                this.cancel(true);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_longAnimTime);

            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
            mListView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mListView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public static class SignInOutDialog extends DialogFragment {

        public Dialog onCreateDialog(Bundle savedInstanceState) {


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Sign In Or Out")
                    .setMultiChoiceItems(childNames, checkedChildren, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                            checkedChildren[i] = b;
                        }
                    })
                    .setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            ArrayList<String> temp = new ArrayList<String>();
                            IDs = "[";
                            try {
                                for (int j = 0; j < checkedChildren.length; ++j) {
                                    if (checkedChildren[j]) {
                                        temp.add(cInfo.getJSONObject(j).getString("childID"));
                                    }
                                }
                                for (int j = 0; j < temp.size(); ++j) {
                                    IDs += temp.get(j);
                                    if (j < temp.size() - 1) {
                                        IDs += ",";
                                    }
                                }
                                IDs += "]";
                                sMethod = "signin";
                            } catch (JSONException e) {
                                Log.e("JSON", e.getMessage());
                            }
                        }
                    })
                    .setNegativeButton("Sign Out", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ArrayList<String> temp = new ArrayList<String>();
                            IDs = "[";
                            try {
                                for (int j = 0; j < checkedChildren.length; ++j) {
                                    if (checkedChildren[j]) {
                                        temp.add(cInfo.getJSONObject(j).getString("attendID"));
                                    }
                                }
                                for (int j = 0; j < temp.size(); ++j) {
                                    IDs += temp.get(j);
                                    if (j < temp.size() - 1) {
                                        IDs += ",";
                                    }
                                }
                                IDs += "]";
                                sMethod = "signout";
                            } catch (JSONException e) {
                                Log.e("JSON", e.getMessage());
                            }

                        }
                    });
            return builder.create();
        }

        public void onDismiss(DialogInterface dialogInterface) {

            ((NewsFeedActivity) getActivity()).callSignAsync(IDs, sMethod);
        }
    }

    public void callSignAsync(String IDs, String sMethod) {
        SignInOut signInOut = new SignInOut();
        signInOut.execute(IDs, sMethod);
    }

    public class GetChildInfo extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... params) {

            final String BASE_URL = "http://davisengeler.gwdnow.com/child.php?getinfo";
            final String CHILD_ID = "childids";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";


            try {
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(CHILD_ID, params[0]).build();

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

                cInfo = new JSONArray(jsonStr);
                childNames = new String[cInfo.length()];

                for (int i = 0; i < childNames.length; ++i) {
                    childNames[i] = cInfo.getJSONObject(i).getString("firstName") + " " +
                            cInfo.getJSONObject(i).getString("lastName");
                }
                checkedChildren = new boolean[childNames.length];

                try {
                    for (int i = 0; i < checkedChildren.length; ++i) {
                        Log.v("Attend status: ", cInfo.getJSONObject(i).getString("attendID"));
                        if (cInfo.getJSONObject(i).getString("attendID").compareTo("null") == 0) {
                            checkedChildren[i] = false;
                        } else {
                            checkedChildren[i] = true;
                        }

                    }
                } catch (JSONException e) {
                    Log.e("JSON ERROR", e.getMessage());
                }


                return true;


            } catch (JSONException e) {
                Log.e("JSON Error: ", e.getMessage());
            }

            return false;
        }

        protected void onPostExecute(Boolean success) {
            if (success) {
                DialogFragment s = new SignInOutDialog();
                s.show(getFragmentManager(), "sign");


            } else {
                Toast.makeText(getApplicationContext(), "Couldn't Retrieve Info", Toast.LENGTH_LONG).show();
            }

        }

    }

    public class SignInOut extends AsyncTask<String, Void, Boolean> {
        JSONObject statement;

        protected Boolean doInBackground(String... params) {
            boolean success = false;
            String BASE_URL, ID;
            if (sMethod.compareTo("signin") == 0) {
                BASE_URL = "http://davisengeler.gwdnow.com/child.php?signin";
                ID = "childids";
            } else {
                BASE_URL = "http://davisengeler.gwdnow.com/child.php?signout";
                ID = "attendids";
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";


            try {
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(ID, params[0]).build();

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

    public class ChildLookup extends AsyncTask<String, Void, Boolean> {
        private String jsonStr;
        protected final String VALIDATE = "verified", ACCT_ID = "accID";


        @Override
        protected Boolean doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            final String BASE_URL = "http://davisengeler.gwdnow.com/child.php?getinfo";
            String CHILD_PARAM = "childids";

            jsonStr = processQuery(urlConnection, reader, BASE_URL, params[0], CHILD_PARAM);


            try {
                jArray = new JSONArray(jsonStr);
                students = new String[jArray.length()];
                for (int i = 0; i < students.length; ++i) {
                    students[i] = jArray.getJSONObject(i).getString("firstName") + " " +
                            jArray.getJSONObject(i).getString("lastName");
                }
                for (int i = 0; i < cNotes.size(); ++i) {
                    for (int j = 0; j < jArray.length(); ++j) {
                        if (cNotes.get(i).getChildID().compareTo(jArray.getJSONObject(j).getString("childID")) == 0) {
                            cNotes.get(i).setChildName(jArray.getJSONObject(j).getString("firstName"),
                                    jArray.getJSONObject(j).getString("lastName"));
                        }
                    }

                }

            } catch (JSONException e) {
                Log.e("Error", e.getMessage());
            }


            return true;
        }

        protected void onPostExecute(Boolean success) {
            setList();
        }

    }

    public String processQuery(HttpURLConnection urlConnection, BufferedReader reader, String BASE_URL, String idArray, String SEARCH) {
        String jsonStr = null;

        try {
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(SEARCH, idArray).build();

            Log.v("Built URI ", builtUri.toString());
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
                    Log.v("JSON String: ", jsonStr);
                } else {

                }
            }
        } catch (MalformedURLException e) {
            Log.e("Error with URL: ", e.getMessage());

        } catch (IOException e) {
            // If the code didn't successfully get the data,
            Log.e("Error: ", e.getMessage());

        } finally {
            urlConnection.disconnect();
            try {
                reader.close();
            } catch (final IOException e) {
                Log.e("Error closing stream", e.getMessage());
            }

        }
        return jsonStr;
    }

    public static class AddNoteDialog extends DialogFragment {



        public Dialog onCreateDialog(Bundle savedInstanceState) {
            checkedStudents = new boolean[students.length];
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.add_note_title)
                    .setMultiChoiceItems(students, checkedStudents, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                            checkedStudents[i] = b;
                        }
                    })
                    .setPositiveButton("Add Note", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            ArrayList<String> temp = new ArrayList<String>();
                            noteIDs = "[";
                            try {
                                for (int j = 0; j < checkedStudents.length; ++j) {
                                    if (checkedStudents[j]) {
                                        temp.add(jArray.getJSONObject(j).getString("childID"));
                                    }
                                }
                                for (int j = 0; j < temp.size(); ++j) {
                                    noteIDs += temp.get(j);
                                    if (j < temp.size() - 1) {
                                        noteIDs += ",";
                                    }
                                }
                                noteIDs += "]";
                                NoteDialogFragment nextStep = new NoteDialogFragment();
                                nextStep.show(getFragmentManager(), "notestuff");
                            } catch (JSONException e) {
                                Log.e("JSON", e.getMessage());
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel_label, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AddNoteDialog.this.getDialog().cancel();
                        }
                    });
            return builder.create();
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
                            String noteIDSelect = "" + (noteIDChosen+1);
                            ((NewsFeedActivity)getActivity()).addNoteAsyncCall(mContent, noteIDSelect);

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

    public void addNoteAsyncCall(String message, String subjectID)
    {
        AddNote add = new AddNote();
        String notetype = "0";

        add.execute(message, subjectID, notetype, noteIDs);
    }

    public class AddNote extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {

            final String BASE_URL = "http://davisengeler.gwdnow.com/child.php?addnote";
            final String MESSAGE_ID = "message";
            final String NOTE_ID = "notetype";
            final String SUBJECT_ID = "subjectid";
            final String CHILD_IDS = "children";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";


            try {
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(MESSAGE_ID, params[0])
                        .appendQueryParameter(SUBJECT_ID, params[1])
                        .appendQueryParameter(NOTE_ID, params[2])
                        .appendQueryParameter(CHILD_IDS, params[3]).build();

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


