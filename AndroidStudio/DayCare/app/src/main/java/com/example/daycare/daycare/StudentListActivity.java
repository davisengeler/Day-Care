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


public class StudentListActivity extends Activity {
    private ListView mListView;
    String[] students = {"Coby", "Rachel", "Seth", "Collin"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);
        String JSONString = this.getIntent().getStringExtra("JSONString");
        try
        {
            JSONArray teach = new JSONArray(JSONString);
            String childIDs = teach.getJSONObject(0).getString("children").toString().replaceAll("\"", "");
            ChildLookup lookup = new ChildLookup();
            lookup.execute(childIDs);
        }
        catch (JSONException e)
        {
            Log.v("Stuff" , e.getMessage());
        }




        mListView = (ListView) findViewById(R.id.container);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, students);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), StudentViewActivity.class);
                intent.putExtra("chosenStudent", students[i]);
                startActivity(intent);
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

            jsonStr += processQuery(urlConnection, reader, BASE_URL, params[0]);

            try
            {
                JSONObject j = new JSONObject(jsonStr);
                JSONArray jArray = new JSONArray(jsonStr);
                Log.v("Object/Array", j.toString() + "\n");

            }
            catch (JSONException e)
            {
                Log.e("Error", e.getMessage());
            }


            return true;
        }

    }
    public String processQuery(HttpURLConnection urlConnection, BufferedReader reader, String BASE_URL, String idArray)
    {
        String jsonStr = null;
        String CHILD_PARAM = "childid";
        try {
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(CHILD_PARAM, idArray).build();

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
}
