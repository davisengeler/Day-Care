package com.example.daycare.daycare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
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


public class ApproveAccounts extends Activity {
    private ListView mListView;
    private String []approveList, toDisplay;
    private JSONArray account_Type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_accounts);
        mListView = (ListView) findViewById(R.id.container);
        AcctApprovals a = new AcctApprovals();
        a.execute();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                ApproveDialog dialog = new ApproveDialog();
                dialog.show(getFragmentManager(), "approve");

            }
        });


    }
    public void setList(String[] toDisplay)
    {


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, toDisplay);
        mListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_approve_accounts, menu);
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

    public class AcctApprovals extends AsyncTask<Void, Void, Boolean>
    {

        protected Boolean doInBackground(Void... params)
        {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = "";
            try
            {
                String userUrl = "http://davisengeler.gwdnow.com/user.php?getpendingaccounts";
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

                account_Type = new JSONArray(jsonStr);

                approveList = new String[account_Type.length()];
                for(int i=0; i<account_Type.length(); ++i)
                {
                    approveList[i] = account_Type.get(i).toString();
                }
                return true;
            }
            catch(JSONException e)
            {
                Log.e("JSON Error: ", e.getMessage());
            }

            return false;
        }

        protected void onPostExecute(Boolean success)
        {
            if(success)
            {
                toDisplay = new String[account_Type.length()];
                try
                {

                    for(int i=0; i<account_Type.length(); ++i)
                    {
                        JSONObject j = new JSONObject(approveList[i]);
                        toDisplay[i] = j.getString("firstName");
                        toDisplay[i] +=  " " + j.getString("lastName");
                        toDisplay[i] += " - " +  "Account Type: Admin";
                        Log.v("toDisplay " , toDisplay[i]);
                    }
                }
                catch (Exception e)
                {
                    e.getMessage();
                }
                setList(toDisplay);
            }
        }

    }
    public static class ApproveDialog extends DialogFragment
    {
        private String[] temp = {"Approve", "Deny"};
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Approve Account")
                    .setItems(temp, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                }
                            }
                    );
            return builder.create();
        }
    }
}
