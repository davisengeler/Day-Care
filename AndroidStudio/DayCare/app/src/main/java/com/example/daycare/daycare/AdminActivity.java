package com.example.daycare.daycare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.daycare.daycare.dummy.SignInOut;

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


public class AdminActivity extends Activity
       {
    private static String [] typesList;
    private String actType;


    private ListView mListView;
    private ListView currentItem;


    private CharSequence mTitle;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        final String JSONString = this.getIntent().getStringExtra("JSONString");
        if(JSONString!=null)
        {
            processJSON(JSONString);
        }

        mListView = (ListView) findViewById(R.id.container);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.admin_list));
        mListView.setAdapter(adapter);
        final AcctTypes a2 = new AcctTypes();
        a2.execute();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent;
                switch(i) { //add acct, edit acct, add child, edit child, acct approve, notes, sign inout
                    case 0:
                        intent = new Intent(getApplicationContext(), AddAccountActivity.class);
                        intent.putExtra("AcctTypeList", typesList);
                        intent.putExtra("JSONString", JSONString);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(getApplicationContext(), AddAccountActivity.class);
                        intent.putExtra("AcctTypeList", typesList);
                        intent.putExtra("Edit", "Edit");
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(getApplicationContext(), AddChildActivity.class);
                        startActivity(intent);
                        break;
                    case 3:
                        intent = new Intent(getApplicationContext(), AddChildActivity.class);
                        intent.putExtra("Edit", "Edit");
                        startActivity(intent);
                        break;
                    case 4:
                        if(actType.compareTo("1")!=0)
                        {
                            intent = new Intent(getApplicationContext(), ApproveAccounts.class);
                            intent.putExtra("AcctTypeList", typesList);
                            startActivity(intent);
                        }
                        else
                        {
                            RestrictDialog dialog = new RestrictDialog();
                            dialog.show(getFragmentManager(), "restrict");
                        }
                        break;
                    case 5:
                        intent = new Intent(getApplicationContext(), AdminAddNote.class);
                        intent.putExtra("JSONString", JSONString);
                        startActivity(intent);
                        break;
                    case 6:
                        intent = new Intent(getApplicationContext(), SignInOut.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });

    }

    public void processJSON(String JSONString)
    {
        try {
            JSONArray j = new JSONArray(JSONString);
            actType = j.getJSONObject(0).getString("accID");

        }
        catch(JSONException e)
        {
            Log.e("JSON", e.getMessage());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.admin, menu);
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
            * @return Application's {@code SharedPreferences}. (For GCM)
            */
           private SharedPreferences getPreferences(Context context) {
               return getSharedPreferences(LoginActivity.class.getSimpleName(),
                       Context.MODE_PRIVATE);
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
    public static class RestrictDialog extends DialogFragment{
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            builder.setTitle("Restricted Access")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("Must be an Administrator!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            RestrictDialog.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }
    }
}
