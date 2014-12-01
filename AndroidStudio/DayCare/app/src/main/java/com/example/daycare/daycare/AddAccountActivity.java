package com.example.daycare.daycare;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddAccountActivity extends Activity
{

	private static String ssn, JSONString;
	private EditText fName;
	private EditText lName;
	private EditText sAddress;
	private EditText sCity;
	private EditText sState;
	private EditText sZip;
	private EditText emailAddress;
	private EditText pNum;
	private EditText s_Ssn;
	private EditText sPass;
	private Spinner dropdown;
	private int typeID;
	private JSONArray userInfo;
	private ProgressBar loader;
	private String apikey, apipass;
	private String userID = "";
	private String type;

	@Override
	@TargetApi(17)
	protected void onCreate(Bundle savedInstanceState)
	{
		String[] test = this.getIntent().getStringArrayExtra("AcctTypeList");
		type = this.getIntent().getStringExtra("Edit");
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = getPreferences();
		apikey = prefs.getString(LoginActivity.PROPERTY_API_KEY, "");
		apipass = prefs.getString(LoginActivity.PROPERTY_API_PASS, "");
		setContentView(R.layout.activity_add_account);
		loader = (ProgressBar) findViewById(R.id.progressBar1);
		loader.setVisibility(View.GONE);
		Spinner s1 = (Spinner) findViewById(R.id.spinner1);
		JSONString = this.getIntent().getStringExtra("JSONString");
		dropdown = s1;
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, test);
		dropdown.setAdapter(adapter);
		fName = (EditText) findViewById(R.id.first_name);
		lName = (EditText) findViewById(R.id.last_name);
		sAddress = (EditText) findViewById(R.id.street_address);
		sCity = (EditText) findViewById(R.id.city);
		sState = (EditText) findViewById(R.id.state);
		sZip = (EditText) findViewById(R.id.zip);
		emailAddress = (EditText) findViewById(R.id.email);
		pNum = (EditText) findViewById(R.id.phone);
		s_Ssn = (EditText) findViewById(R.id.ssn);
		sPass = (EditText) findViewById(R.id.pass);
		s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override

			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
			{
				Log.v("Item Selected", "list num" + i);
				typeID = i + 1;

			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView)
			{

			}
		});

		if (type != null)
		{
			final GetUserInfo g = new GetUserInfo();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final EditText pSSN = new EditText(this);
			pSSN.setHint("Enter SSN Here");
			pSSN.setInputType(2);
			builder.setTitle("Account Search")
				   .setMessage("Please Enter SSN")
				   .setView(pSSN)
				   .setPositiveButton("Search", new DialogInterface.OnClickListener()
				   {
					   @Override
					   public void onClick(DialogInterface dialogInterface, int i)
					   {
						   loader.setVisibility(View.VISIBLE);
						   g.execute(pSSN.getText().toString());

					   }
				   });
			builder.create();
			builder.show();
		}

		//send editaccount

		Button submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				//take the info and send to php
				String firstName, lastName, fullAddress, email, phone, pass, sTypeID;
				firstName = fName.getText().toString();
				lastName = lName.getText().toString();
				fullAddress = sAddress.getText().toString() + "," + sCity.getText().toString() + "," +
						sState.getText().toString() + "," + sZip.getText().toString();
				email = emailAddress.getText().toString();
				phone = pNum.getText().toString();
				pass = sPass.getText().toString();
				sTypeID = Integer.toString(typeID);
				ssn = s_Ssn.getText().toString();
				SendUserInfo sendInfo = new SendUserInfo();
				if (type != null)
				{
					sendInfo.execute(ssn, firstName, lastName, fullAddress, email, phone, pass, sTypeID);
				} else
				{
					if (pass != null && ssn != null)
					{
						sendInfo.execute(ssn, firstName, lastName, fullAddress, email, phone, pass, sTypeID);
					}
				}

			}
		});
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getPreferences()
	{
		return getSharedPreferences(LoginActivity.class.getSimpleName(),
									Context.MODE_PRIVATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.add_account, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		return id == R.id.action_settings || super.onOptionsItemSelected(item);
	}

	void setValues(JSONObject j1)
	{
		try
		{
			userID = j1.getString("userID");
			String accID = j1.getString("accID");
			Log.v("WTF : ", j1.toString() + " " + userID + " " + accID);
			fName.setText(j1.getString("firstName"), TextView.BufferType.EDITABLE);
			lName.setText(j1.getString("lastName"), TextView.BufferType.EDITABLE);
			String addyString = j1.getString("address");
			String[] splitAddy = addyString.split(",");
			sAddress.setText(splitAddy[0], TextView.BufferType.EDITABLE);
			if (splitAddy.length > 1)
			{
				sCity.setText(splitAddy[1], TextView.BufferType.EDITABLE);
			}
			if (splitAddy.length > 2)
			{
				sState.setText(splitAddy[2], TextView.BufferType.EDITABLE);
			}
			if (splitAddy.length > 3)
			{
				sZip.setText(splitAddy[3], TextView.BufferType.EDITABLE);
			}
			s_Ssn.setText(j1.getString("ssn"), TextView.BufferType.EDITABLE);
			pNum.setText(j1.getString("phone"), TextView.BufferType.EDITABLE);
			emailAddress.setText(j1.getString("email"), TextView.BufferType.EDITABLE);
			int choice = Integer.parseInt(accID);
			dropdown.setSelection(choice - 1);
			loader.setVisibility(View.GONE);
		} catch (JSONException e)
		{
			Log.e("JSON ", e.getMessage());
		}

	}

	public static class ChildDialogFragment extends DialogFragment
	{

		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Add a Child")
				   .setMessage("Do you want to add a child now?")
				   .setPositiveButton(R.string.yes_label, new DialogInterface.OnClickListener()
				   {
					   @Override
					   public void onClick(DialogInterface dialogInterface, int i)
					   {
						   Intent cIntent = new Intent(getActivity(), AddChildActivity.class);
						   cIntent.putExtra("UserSSN", ssn);
						   cIntent.putExtra("JSONString", JSONString);

						   startActivity(cIntent);
					   }
				   })
				   .setNegativeButton(R.string.cancel_label, new DialogInterface.OnClickListener()
				   {
					   @Override
					   public void onClick(DialogInterface dialogInterface, int i)
					   {
						   ChildDialogFragment.this.getDialog().cancel();
						   getActivity().finish();
					   }
				   });
			return builder.create();
		}
	}

	public class SendUserInfo extends AsyncTask<String, Void, String>
	{
		private String acctID;
		private final String idNum = "";
		private String USER_BASE_URL;
		private JSONObject jObj;

		protected String doInBackground(String... params)
		{
			acctID = params[7];
			HttpURLConnection urlConnection;
			BufferedReader reader;
			String jsonStr;
			String suc = "";
			Uri builtUri = null;
			final String SSN_PARAM = "ssn";
			final String F_NAME_PARAM = "firstname";
			final String L_NAME_PARAM = "lastname";
			final String ADDRESS_PARAM = "address";
			final String EMAIL_PARAM = "email";
			final String PHONE_PARAM = "phone";
			final String PASS_PARAM = "pass";
			final String TYPE_ID_PARAM = "accid";
			final String API_KEY_PARAM = "apikey";
			final String API_PASS_PARAM = "apipass";
			if (type == null)
			{
				USER_BASE_URL = "http://davisengeler.gwdnow.com/user.php?addaccount";

				try
				{

					builtUri = Uri.parse(USER_BASE_URL).buildUpon()
								  .appendQueryParameter(SSN_PARAM, params[0])
								  .appendQueryParameter(F_NAME_PARAM, params[1])
								  .appendQueryParameter(L_NAME_PARAM, params[2])
								  .appendQueryParameter(ADDRESS_PARAM, params[3])
								  .appendQueryParameter(EMAIL_PARAM, params[4])
								  .appendQueryParameter(PHONE_PARAM, params[5])
								  .appendQueryParameter(PASS_PARAM, params[6])
								  .appendQueryParameter(TYPE_ID_PARAM, params[7])
								  .appendQueryParameter(API_KEY_PARAM, apikey)
								  .appendQueryParameter(API_PASS_PARAM, apipass)
								  .build();
				} catch (Exception e)
				{
					System.out.println(e.getMessage());
				}

			} else
			{
				USER_BASE_URL = "http://davisengeler.gwdnow.com/user.php?editaccount";
				try
				{
					if (params[6].compareTo("") != 0)
					{

						builtUri = Uri.parse(USER_BASE_URL).buildUpon()
									  .appendQueryParameter(SSN_PARAM, params[0])
									  .appendQueryParameter(F_NAME_PARAM, params[1])
									  .appendQueryParameter(L_NAME_PARAM, params[2])
									  .appendQueryParameter(ADDRESS_PARAM, params[3])
									  .appendQueryParameter(EMAIL_PARAM, params[4])
									  .appendQueryParameter(PHONE_PARAM, params[5])
									  .appendQueryParameter(PASS_PARAM, params[6])
									  .appendQueryParameter(TYPE_ID_PARAM, params[7])
									  .appendQueryParameter("userid", userID)
									  .appendQueryParameter(API_KEY_PARAM, apikey)
									  .appendQueryParameter(API_PASS_PARAM, apipass)
									  .build();
					} else
					{
						builtUri = Uri.parse(USER_BASE_URL).buildUpon()
									  .appendQueryParameter(SSN_PARAM, params[0])
									  .appendQueryParameter(F_NAME_PARAM, params[1])
									  .appendQueryParameter(L_NAME_PARAM, params[2])
									  .appendQueryParameter(ADDRESS_PARAM, params[3])
									  .appendQueryParameter(EMAIL_PARAM, params[4])
								.appendQueryParameter(PHONE_PARAM, params[5])
										//no password update
								.appendQueryParameter(TYPE_ID_PARAM, params[7])
								.appendQueryParameter("userid", userID)
								.appendQueryParameter(API_KEY_PARAM, apikey)
								.appendQueryParameter(API_PASS_PARAM, apipass)
								.build();
					}
				} catch (Exception e)
				{
					System.out.println(e.getMessage());
				}

			}
			try
			{

				URL url = new URL(builtUri != null ? builtUri.toString() : null);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();
				Log.v("URL ", builtUri != null ? builtUri.toString() : null);
				InputStream inputStream = urlConnection.getInputStream();
				StringBuilder buffer = new StringBuilder();

				if (inputStream != null)
				{
					reader = new BufferedReader(new InputStreamReader(inputStream));

					String line;
					while ((line = reader.readLine()) != null)
					{
						//makes easy to read in logs
						buffer.append(line).append("\n");
					}
					if (buffer.length() != 0)
					{
						jsonStr = buffer.toString();
						Log.v("JSON String: ", jsonStr);
						jObj = new JSONObject(jsonStr);
						suc = jObj.getString("successful");
					}
				}
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
			}

			return suc;
		}

		protected void onPostExecute(String message)
		{
			if (message.compareTo("true") == 0)
			{
				try
				{
					Toast.makeText(getApplicationContext(), jObj.getString("statusMessage"), Toast.LENGTH_LONG).show();
				} catch (JSONException e)
				{
					Log.e("JSON", e.getMessage());
				}

				if (acctID.compareTo("3") == 0 && type == null)
				{
					Bundle b = new Bundle();
					b.putString("idNum", idNum);
					ChildDialogFragment dialog = new ChildDialogFragment();
					dialog.setArguments(b);
					dialog.show(getFragmentManager(), "child");
				} else
				{
					Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
					intent.putExtra("JSONString", JSONString);
					//startActivity(intent);
					finish();

				}
			} else
			{
				try
				{
					Toast.makeText(getApplicationContext(), jObj.getString("statusMessage"), Toast.LENGTH_LONG).show();
				} catch (JSONException e)
				{
					Log.e("JSON", e.getMessage());
				}
			}
		}
	}

	private class GetUserInfo extends AsyncTask<String, Void, Boolean>
	{
		protected Boolean doInBackground(String... params)
		{

			HttpURLConnection urlConnection;
			BufferedReader reader;
			String jsonStr = "";
			final String USER_BASE_URL = "http://davisengeler.gwdnow.com/user.php?getaccountbyssn";
			final String SSN_PARAM = "ssn";
			final String API_KEY_PARAM = "apikey";
			final String API_PASS_PARAM = "apipass";

			try
			{

				Uri builtUri = Uri.parse(USER_BASE_URL).buildUpon()
								  .appendQueryParameter(SSN_PARAM, params[0])
								  .appendQueryParameter(API_KEY_PARAM, apikey)
								  .appendQueryParameter(API_PASS_PARAM, apipass)
								  .build();
				URL url = new URL(builtUri.toString());

				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();

				InputStream inputStream = urlConnection.getInputStream();
				StringBuilder buffer = new StringBuilder();

				if (inputStream != null)
				{
					reader = new BufferedReader(new InputStreamReader(inputStream));

					String line;
					while ((line = reader.readLine()) != null)
					{
						//makes easy to read in logs
						buffer.append(line).append("\n");
					}
					if (buffer.length() != 0)
					{
						jsonStr = buffer.toString();
						Log.v("JSON String: ", jsonStr);
					}
				}
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
			try
			{
				userInfo = new JSONArray(jsonStr);

				return true;
			} catch (JSONException e)
			{
				Log.e("JSON", e.getMessage());
			}

			return false;
		}

		protected void onPostExecute(Boolean success)
		{
			if (success)
			{
				Toast.makeText(getApplicationContext(), "Found User", Toast.LENGTH_LONG).show();
				try
				{
					JSONObject j1 = new JSONObject(userInfo.getJSONObject(0).toString());
					setValues(j1);

				} catch (JSONException e)
				{
					Log.e("JSON ", e.getMessage());
				}

			} else
			{
				Toast.makeText(getApplicationContext(), "Couldn't Find User", Toast.LENGTH_LONG).show();
				loader.setVisibility(View.GONE);
			}

		}
	}

}
