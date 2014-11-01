package com.example.daycare.daycare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.daycare.daycare.dummy.AddAccountActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class AdminActivity extends Activity
       {
    private static String [] typesList;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */


    private ListView mListView;


    private CharSequence mTitle;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);



        mListView = (ListView) findViewById(R.id.container);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.admin_list));
        mListView.setAdapter(adapter);
        final AcctTypes a2 = new AcctTypes();
        a2.execute();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i) {
                    case 0:
                        Intent intent = new Intent(getApplicationContext(), AddAccountActivity.class);
                        intent.putExtra("AcctTypeList", typesList);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });

    }








    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

            getMenuInflater().inflate(R.menu.admin, menu);


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

    /**
     * A placeholder fragment containing a simple view.
     */

    public class AcctTypes extends AsyncTask<Void, Void, Void>
    {
        public Void doInBackground(Void... params)
        {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";
            try
            {
                String userUrl = "http://davisengeler.gwdnow.com/user.php?getaccounttypes";
                URL url = new URL(userUrl);

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

                JSONObject account_Type = new JSONObject(jsonStr);
                //SparseArray<String> testSparseArray = new SparseArray<String>(account_Type.length());
                typesList = new String[account_Type.length()];
                for(int i=0; i<account_Type.length(); ++i)
                {
                    String convert = Integer.toString(i+1);

                    typesList[i] = account_Type.getString(convert);
                }

            }
            catch(JSONException e)
            {
                Log.e("JSON Error: ", e.getMessage());
            }

            return null;
        }

    }

}
