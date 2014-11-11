package com.example.daycare.daycare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class NewsFeedActivity extends Activity {
    private ListView mListView;
    private View mNewsFeedView, mProgressView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);
        String[] childArray = null;
        try{

            JSONArray passedJSON = new JSONArray(this.getIntent().getStringExtra("JSONString"));
            if(passedJSON != null)
            {

                childArray = passedJSON.getJSONObject(0).getString("children").replaceAll("\\[", "")
                        .replaceAll("\\]", "").replaceAll("\"", "").split(",");


            }
        }
        catch(Exception e)
        {
            Log.e("JSON String: ", e.getMessage());
        }

        GetNotes note = new GetNotes();
        note.execute(childArray);
        mListView = (ListView) findViewById(R.id.list_container);
        mProgressView = findViewById(R.id.progress_bar_news);
        mNewsFeedView = findViewById(R.id.activity_news_form);
        showProgress(true);

        final String [] testChildren = {"Johnny", "Jackie"};
        final String[] testNotes = {"Pooped Pants", "School Field Trip","Slapped Clarissa"};

       ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_2,android.R.id.text1, testNotes)
       {
           @Override
           public View getView(int position, View convertView, ViewGroup parent){

               View view = super.getView(position, convertView, parent);
               TextView text1 = (TextView) view.findViewById(android.R.id.text1);
               TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text1.setText(testChildren[position]);
               text2.setText(testNotes[position]);
//               text1.setText(persons.get(position).getName());
//               text2.setText(persons.get(position).getAge());
               return view;
           }
       };

       mListView.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.news_feed, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }

    public class GetNotes extends AsyncTask<String [], Void, Void>
    {

        public Void doInBackground(String[]...params)
        {

            final String BASE_URL = "http://davisengeler.gwdnow.com/child.php?getnotes";
            final String CHILD_ID = "childids";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";

            for(String s : params[0])
            {
                try
                {
                    Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                            .appendQueryParameter(CHILD_ID,s).build();

                    Log.v("TEST:   ", builtUri.toString());

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
                            jsonStr += buffer.toString();
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
            }

            try
            {

                JSONArray notes = new JSONArray(jsonStr);

                //save note information here

            }
            catch(JSONException e)
            {
                Log.e("JSON Error: ", e.getMessage());
            }
            return null;
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mNewsFeedView.setVisibility(show ? View.GONE : View.VISIBLE);
            mNewsFeedView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mNewsFeedView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mNewsFeedView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

