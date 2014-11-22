package com.example.daycare.daycare;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class StudentListActivity extends Activity {
    private ListView mListView;
    String[] students;
    JSONArray jArray;
    JSONArray teach;
    ProgressBar pLoader;
    private String JSONString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);
        pLoader = (ProgressBar) findViewById(R.id.progressBarList);
        if(JSONString == null)
        {
            JSONString = this.getIntent().getStringExtra("JSONString");
        }

        try
        {
            teach = new JSONArray(JSONString);
            String childIDs = teach.getJSONObject(0).getString("children").toString().replaceAll("\"", "");
            ChildLookup lookup = new ChildLookup();
            pLoader.setVisibility(View.VISIBLE);
            lookup.execute(childIDs);
        }
        catch (JSONException e)
        {
            Log.v("Stuff" , e.getMessage());
        }




        mListView = (ListView) findViewById(R.id.container);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), StudentViewActivity.class);
                try
                {
                    intent.putExtra("teacherInfo", teach.getJSONObject(0).toString());
                    intent.putExtra("chosenStudent", jArray.getJSONObject(i).toString());
                    intent.putExtra("JSONString", JSONString);
                    startActivity(intent);
                }
                catch(JSONException e)
                {
                    Log.e("JSON", e.getMessage());
                }

        }
    });

    }
    public void setAdapterList()
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, students);
        mListView.setAdapter(adapter);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                    Bundle b = data.getExtras();
                    JSONString = this.getIntent().getStringExtra("JSONString");
                    Log.v("JSON ACTIVITY", JSONString);
                }
            }
        }


    public void onResume() //do a new search by teach id call async then call array list to udpate
    {
        super.onResume();
        pLoader = (ProgressBar) findViewById(R.id.progressBarList);
        if(JSONString == null)
        {
            JSONString = this.getIntent().getStringExtra("JSONString");
        }

        try
        {
            teach = new JSONArray(JSONString);
            pLoader.setVisibility(View.VISIBLE);
            TeacherLookup teachLookup = new TeacherLookup();
            teachLookup.execute(teach.getJSONObject(0).getString("ssn"));

        }
        catch (JSONException e)
        {
            Log.v("Stuff" , e.getMessage());
        }

        mListView = (ListView) findViewById(R.id.container);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), StudentViewActivity.class);
                try
                {
                    intent.putExtra("teacherInfo", teach.getJSONObject(0).toString());
                    intent.putExtra("chosenStudent", jArray.getJSONObject(i).toString());
                    intent.putExtra("JSONString", JSONString);
                    startActivity(intent);
                }
                catch(JSONException e)
                {
                    Log.e("JSON", e.getMessage());
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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


            try
            {
                jArray = new JSONArray(jsonStr);
                Log.v("Object/Array", jArray.toString() + "\n");
                students = new String[jArray.length()];
                for(int i=0; i<students.length; ++i)
                {
                    students[i] = jArray.getJSONObject(i).getString("firstName") + " " +
                            jArray.getJSONObject(i).getString("lastName");
                }

            }
            catch (JSONException e)
            {
                Log.e("Error", e.getMessage());
            }


            return true;
        }
        protected void onPostExecute(Boolean success)
        {
            pLoader.setVisibility(View.GONE);
            setAdapterList();
        }

    }
    public String processQuery(HttpURLConnection urlConnection, BufferedReader reader, String BASE_URL, String idArray, String SEARCH)
    {
        String jsonStr = null;

        try {
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(SEARCH, idArray).build();

            Log.v("Built URI " , builtUri.toString());
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
                    Log.v("JSON String: " , jsonStr);
                } else {

                }
            }
        } catch (MalformedURLException e) {
            Log.e( "Error with URL: " , e.getMessage());

        } catch (IOException e) {
            // If the code didn't successfully get the data,
            Log.e( "Error: " , e.getMessage());

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

    public class TeacherLookup extends AsyncTask<String, Void, Boolean> {
        private String jsonStr;
        protected final String VALIDATE = "verified", ACCT_ID = "accID";
        String childIDs="";

        @Override
        protected Boolean doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            final String BASE_URL = "http://davisengeler.gwdnow.com/user.php?getaccountbyssn";
            String ID_PARAM = "ssn";

            jsonStr = processQuery(urlConnection, reader, BASE_URL, params[0], ID_PARAM);


            try
            {
                teach = new JSONArray(jsonStr);
                Log.v("Teach Lookup", jArray.toString() + "\n");
                childIDs = teach.getJSONObject(0).getString("children").toString().replaceAll("\"", "");


            }
            catch (JSONException e)
            {
                Log.e("Error", e.getMessage());
            }


            return true;
        }
        protected void onPostExecute(Boolean success)
        {
            ChildLookup lookup = new ChildLookup();
            lookup.execute(childIDs);
        }

    }
}
