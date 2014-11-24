package com.example.daycare.daycare.dummy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.daycare.daycare.R;

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

public class SignInOut extends Activity {
    private static JSONArray userInfo, cInfo;
    private static String [] childNames;
    private static boolean [] checkedChildren;
    private static String sMethod = "", IDs = "";
    private ProgressBar loader;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_out);
        loader = (ProgressBar) findViewById(R.id.progressBar2);
        mListView = (ListView) findViewById(R.id.container);

        final GetUserInfo g = new GetUserInfo();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText pSSN = new EditText(this);
        pSSN.setHint("Enter SSN Here");
        pSSN.setInputType(2);
        builder.setTitle("Account Search")
                .setMessage("Please Enter SSN")
                .setView(pSSN)
                .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        g.execute(pSSN.getText().toString());

                    }
                });
        builder.create();
        builder.show();


//        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
//            @Override
//            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
//                checkedChildren[i] = b;
//                Log.v("Checked", " " + checkedChildren[i]);
//            }
//
//            @Override
//            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
//                return true;
//            }
//
//            @Override
//            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
//                return true;
//            }
//
//            @Override
//            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
//                //set background color
//                return true;
//            }
//
//            @Override
//            public void onDestroyActionMode(ActionMode actionMode) {
//
//            }
//        });


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
                                for(int j=0; j<temp.size(); ++j)
                                {
                                    IDs += temp.get(j);
                                    if(j<temp.size()-1)
                                    {
                                        IDs += ",";
                                    }
                                }
                                IDs += "]";
                                sMethod = "signin";
                                ((SignInOut)getActivity()).callSignAsync(IDs, sMethod);
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
                                for(int j=0; j<temp.size(); ++j)
                                {
                                    IDs += temp.get(j);
                                    if(j<temp.size()-1)
                                    {
                                        IDs += ",";
                                    }
                                }
                                IDs += "]";
                                sMethod = "signout";
                                ((SignInOut)getActivity()).callSignAsync(IDs, sMethod);
                            } catch (JSONException e) {
                                Log.e("JSON", e.getMessage());
                            }

                        }
                    });
            return builder.create();
        }
        public void onDismiss(DialogInterface dialogInterface)
        {
            getActivity().finish();

        }
    }

    public void callSignAsync(String IDs, String sMethod)
    {
        SignInOutAsync signInOut = new SignInOutAsync();
        signInOut.execute(IDs, sMethod);
    }

//    public void setAdapterList()
//    {
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_selectable_list_item, childNames);
//        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
//        mListView.setAdapter(adapter);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_sign_in_out, menu);
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

    public class GetUserInfo extends AsyncTask<String, Void, Boolean>
    {
        protected Boolean doInBackground(String...params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";
            final String USER_BASE_URL = "http://davisengeler.gwdnow.com/user.php?getaccountbyssn";
            final String SSN_PARAM = "ssn";

            try {


                Uri builtUri = Uri.parse(USER_BASE_URL).buildUpon()
                        .appendQueryParameter(SSN_PARAM, params[0])
                        .build();
                URL url = new URL(builtUri.toString());

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
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            try{
                userInfo = new JSONArray(jsonStr);

                return true;
            }
            catch(JSONException e)
            {
                Log.e("JSON", e.getMessage());
            }


            return false;
        }

        protected void onPostExecute(Boolean success)
        {
            if(success)
            {
                Toast.makeText(getApplicationContext(), "Found User", Toast.LENGTH_LONG).show();
                try
                {
                    JSONObject j1 = new JSONObject(userInfo.getJSONObject(0).toString());
                    GetChildInfo children = new GetChildInfo();
                    String childIDs = j1.getString("children").toString().replaceAll("\"", "");
                    children.execute(childIDs);
                }
                catch(JSONException e)
                {
                    Log.e("JSON ", e.getMessage());
                }

            }
            else
            {
                Toast.makeText(getApplicationContext(), "Couldn't Find User", Toast.LENGTH_LONG).show();
                loader.setVisibility(View.GONE);
            }

        }
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

                for(int i=0; i<childNames.length; ++i)
                {
                    childNames[i] = cInfo.getJSONObject(i).getString("firstName") + " " +
                            cInfo.getJSONObject(i).getString("lastName");
                }
                checkedChildren = new boolean[childNames.length];

                try
                {
                    for(int i=0; i<checkedChildren.length; ++i)
                    {
                        Log.v("Attend status: ", cInfo.getJSONObject(i).getString("attendID"));
                        if(cInfo.getJSONObject(i).getString("attendID").compareTo("null")==0)
                        {
                            checkedChildren[i] = false;
                        }
                        else
                        {
                            checkedChildren[i] = true;
                        }

                    }
                }catch(JSONException e)
                {
                    Log.e("JSON ERROR", e.getMessage());
                }


                return true;


            } catch (JSONException e) {
                Log.e("JSON Error: ", e.getMessage());
            }

            return false;
        }

        protected void onPostExecute(Boolean success){
            if(success)
            {
                loader.setVisibility(View.GONE);
                SignInOutDialog dialog = new SignInOutDialog();
                dialog.show(getFragmentManager(), "dialog");
            }
            else
            {
                loader.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Couldn't Retrieve Info", Toast.LENGTH_LONG).show();
            }

        }

    }

    public class SignInOutAsync extends AsyncTask<String, Void, Boolean> {
        JSONObject statement;
        protected Boolean doInBackground(String... params) {
            boolean success = false;
            String BASE_URL, ID;
            if(sMethod.compareTo("signin")==0)
            {
                BASE_URL = "http://davisengeler.gwdnow.com/child.php?signin";
                ID = "childids";
            }
            else
            {
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

        protected void onPostExecute(Boolean success)
        {
            if(success)
            {
                try
                {
                    Toast.makeText(getApplicationContext(), statement.getString("statusMessage"), Toast.LENGTH_LONG).show();
                }catch (JSONException e)
                {
                    Log.v("JSON", e.getMessage());
                }

            }
            else
            {
                try
                {
                    Toast.makeText(getApplicationContext(), statement.getString("statusMessage"), Toast.LENGTH_LONG).show();
                }catch (JSONException e)
                {
                    Log.v("JSON", e.getMessage());
                }
            }
        }

    }
}
