package com.example.daycare.daycare.dummy;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.daycare.daycare.R;

import java.net.URL;

public class AddAccountActivity extends Activity {

    Button submitButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        String [] test = this.getIntent().getStringArrayExtra("AcctTypeList");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);


        Spinner dropdown = (Spinner)findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, test);
        dropdown.setAdapter(adapter);

        final EditText fName = (EditText) findViewById(R.id.first_name);
        final EditText lName = (EditText) findViewById(R.id.last_name);
        final EditText sAddress = (EditText) findViewById(R.id.street_address);
        final EditText sCity = (EditText) findViewById(R.id.city);
        final EditText sState = (EditText) findViewById(R.id.state);
        final EditText sZip = (EditText) findViewById(R.id.zip);
        final EditText emailAddress = (EditText) findViewById(R.id.email);
        final EditText pNum = (EditText) findViewById(R.id.phone);
        final EditText s_Ssn = (EditText) findViewById(R.id.ssn);
        final EditText sPass = (EditText) findViewById(R.id.pass);

        submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //take the info and send to php
                String firstName, lastName, fullAddress, email, phone, ssn, pass, typeID;
                firstName = fName.getText().toString();
                lastName = lName.getText().toString();
                fullAddress = sAddress.getText().toString() + sCity.getText().toString() + sState.getText().toString() +
                        sZip.getText().toString();
                email = emailAddress.getText().toString();
                phone = pNum.getText().toString();
                pass = sPass.getText().toString();
                typeID = "put type here";
                ssn = s_Ssn.getText().toString();
                SendUserInfo sendInfo = new SendUserInfo();
                sendInfo.execute(ssn, firstName, lastName, fullAddress, email, phone, pass, typeID);


            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_account, menu);
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


    public class SendUserInfo extends AsyncTask<String, Void, Void>
    {
        private final String LOG_TAG = SendUserInfo.class.getSimpleName();

        protected Void doInBackground(String...params)
        {
            try
            {
                final String USER_BASE_URL = "http://davisengeler.gwdnow.com/add-device.php?";
                final String SSN_PARAM = "ssn";
                final String F_NAME_PARAM = "firstname";
                final String L_NAME_PARAM = "lastname";
                final String ADDRESS_PARAM = "address";
                final String EMAIL_PARAM = "email";
                final String PHONE_PARAM = "phone";
                final String PASS_PARAM = "pass";
                final String TYPE_ID_PARAM = "accid";

                Uri builtUri = Uri.parse(USER_BASE_URL).buildUpon()
                        .appendQueryParameter(SSN_PARAM, params[0])
                        .appendQueryParameter(F_NAME_PARAM, params[1])
                        .appendQueryParameter(L_NAME_PARAM, params[2])
                        .appendQueryParameter(ADDRESS_PARAM, params[3])
                        .appendQueryParameter(EMAIL_PARAM, params[4])
                        .appendQueryParameter(PHONE_PARAM, params[5])
                        .appendQueryParameter(PASS_PARAM, params[6])
                        .appendQueryParameter(TYPE_ID_PARAM, params[7])
                        .build();
                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
            return null;
        }
    }
}
