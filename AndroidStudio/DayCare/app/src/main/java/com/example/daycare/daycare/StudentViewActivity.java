package com.example.daycare.daycare;

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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

public class StudentViewActivity extends Activity
{
	private static String[] teacherNames;
	private static JSONArray pInfo;
	private static JSONArray tList;
	private static JSONObject childInfo;
	private final String[] actions = {"Contact Parent", "Add Note", "View Notes", "Move Student"};
	private TextView pView;
	private String JSONString;
	private String apikey, apipass;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = getPreferences();
		apikey = prefs.getString(LoginActivity.PROPERTY_API_KEY, "");
		apipass = prefs.getString(LoginActivity.PROPERTY_API_PASS, "");
		setContentView(R.layout.activity_student_view);
		JSONString = this.getIntent().getStringExtra("JSONString");
		String chosenStudent = this.getIntent().getStringExtra("chosenStudent");
		String teacherString = this.getIntent().getStringExtra("teacherInfo");

		try
		{
			//JSONArray childArray = new JSONArray(chosenStudent);
			childInfo = new JSONObject(chosenStudent);
			//JSONArray teacherArray = new JSONArray(teacherString);
			JSONObject teacherInfo = new JSONObject(teacherString);
			GetParentInfo p = new GetParentInfo();
			p.execute(childInfo.getString("parentID"));
			TextView tView = (TextView) findViewById(R.id.student_name_info);
			tView.setText(childInfo.getString("firstName") + " " + childInfo.getString("lastName"));
			TextView dView = (TextView) findViewById(R.id.dob_info);
			dView.setText(childInfo.getString("dob"));
			TextView teachView = (TextView) findViewById(R.id.teacher_name);
			teachView.setText(teacherInfo.getString("firstName") + " " + teacherInfo.getString("lastName"));
			pView = (TextView) findViewById(R.id.contact_name);

		} catch (JSONException e)
		{
			Log.e("JSON", e.getMessage());
		}

		ListView mListView = (ListView) findViewById(R.id.container);
		GetTeacherList GTL = new GetTeacherList();
		GTL.execute();
		@SuppressWarnings("unchecked") ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, actions);

		//android ic_menu_edit, ic_perm_group_phone_calls.png, ic_menu_myplaces.png
		mListView.setAdapter(adapter);

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				switch (i)
				{
					case 0:
						DialogFragment dgE = new PhoneCallDialog();
						dgE.show(getFragmentManager(), "call");
						break;
					case 1:
						DialogFragment dg2 = new NoteDialogFragment();
						dg2.show(getFragmentManager(), "notes");
						break;
					case 2:
						Intent intent = new Intent(getApplicationContext(), NewsFeedActivity.class);
						intent.putExtra("teacherView", childInfo.toString());
						startActivity(intent);
						break;
					case 3:
						DialogFragment dg1 = new TeacherDialogFragment();
						dg1.show(getFragmentManager(), "teachers");
						break;
					default:
						break;
				}
			}
		});
	}

	@Override
	public void onBackPressed()
	{

		Intent intent = new Intent();
		intent.putExtra("JSONString", JSONString);
		setResult(Activity.RESULT_OK, intent);
		finish();
		super.onBackPressed();
	}

	//    @Override
//    public void onPause()
//    {
//        Intent intent = new Intent();
//        intent.putExtra("JSONString", JSONString);
//        setResult(Activity.RESULT_OK, intent);
//        finish();
//        super.onPause();
//    }
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event)
//    {
//        Toast.makeText(getApplicationContext(), "KEY UP", Toast.LENGTH_LONG).show();
//        Intent intent = new Intent();
//        intent.putExtra("JSONString", JSONString);
//        setResult(Activity.RESULT_OK, intent);
//        finish();
//        super.onKeyUp(keyCode, event);
//        return true;
//    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_student_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.logout_option)
		{
			SharedPreferences prefs = getPreferences();
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(LoginActivity.PROPERTY_API_KEY, "");
			editor.putString(LoginActivity.PROPERTY_API_PASS, "");
			editor.putBoolean(LoginActivity.PROPERTY_API_LOGIN, false);
			editor.apply();
			Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(loginIntent);
			finish();

		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getPreferences()
	{
		return getSharedPreferences(LoginActivity.class.getSimpleName(),
									Context.MODE_PRIVATE);
	}

	void moveTheChildTask(int which)
	{
		try
		{
			MoveChild move = new MoveChild();
			move.execute(tList.getJSONObject(which).getString("userID"), childInfo.getString("childID"));
		} catch (JSONException e)
		{
			Log.e("JSON MOVE", e.getMessage());
		}
	}

	void addNoteAsyncCall(String message, String subjectID)
	{
		AddNote add = new AddNote();
		String notetype = "1";
		String setUpID = "";
		try
		{
			setUpID = "[" + childInfo.getString("childID") + "]";
		} catch (JSONException e)
		{
			Log.e("JSON NOTE ASYNC", e.getMessage());
		}
		add.execute(message, subjectID, notetype, setUpID);
	}

	public static class TeacherDialogFragment extends DialogFragment
	{
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.pick_teacher)
				   .setItems(teacherNames, new DialogInterface.OnClickListener()
							 {
								 public void onClick(DialogInterface dialog, int which)
								 {
									 ((StudentViewActivity) getActivity()).moveTheChildTask(which);
								 }
							 }
				   );
			return builder.create();
		}

	}

	public static class NoteDialogFragment extends DialogFragment
	{

		int noteIDChosen = 4;

		public Dialog onCreateDialog(Bundle savedInstanceState)
		{

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			final EditText message = new EditText(getActivity());
			message.setHint(R.string.enter_message);
			builder.setTitle(R.string.add_note_title)
				   .setSingleChoiceItems(R.array.note_id, noteIDChosen, new DialogInterface.OnClickListener()
				   {
					   @Override
					   public void onClick(DialogInterface dialogInterface, int i)
					   {
						   noteIDChosen = i;
					   }
				   })
				   .setView(message)
				   .setPositiveButton(R.string.save_label, new DialogInterface.OnClickListener()
				   {
					   @Override
					   public void onClick(DialogInterface dialogInterface, int i)
					   {

						   String mContent = message.getText().toString();
						   Log.v("NoteMessage", noteIDChosen + " " + mContent);
						   //submit this as a note to server with noteIDChosen
						   String noteIDSelect = "" + (noteIDChosen + 1);
						   ((StudentViewActivity) getActivity()).addNoteAsyncCall(mContent, noteIDSelect);
					   }
				   })
				   .setNegativeButton(R.string.cancel_label, new DialogInterface.OnClickListener()
				   {
					   @Override
					   public void onClick(DialogInterface dialogInterface, int i)
					   {
						   NoteDialogFragment.this.getDialog().cancel();
					   }
				   });
			return builder.create();
		}
	}

	public static class PhoneCallDialog extends DialogFragment
	{
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			try
			{
				final String phoneNum = pInfo.getJSONObject(0).getString("phone");
				builder.setTitle("Contact Phone Number")
					   .setMessage("You are calling: \n" + pInfo.getJSONObject(0).getString("firstName") +
										   " " + pInfo.getJSONObject(0).getString("lastName") + " at " + phoneNum)
					   .setPositiveButton("Call", new DialogInterface.OnClickListener()
					   {
						   @Override
						   public void onClick(DialogInterface dialogInterface, int i)
						   {

							   String number = "tel:" + phoneNum.trim();
							   Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
							   startActivity(callIntent);
							   getActivity().finish();
						   }
					   });
			} catch (Exception e)
			{
				Log.e("PHONE CALL", e.getMessage());
			}

			return builder.create();
		}

	}

	private class GetParentInfo extends AsyncTask<String, Void, Boolean>
	{

		protected Boolean doInBackground(String... params)
		{

			final String BASE_URL = "http://davisengeler.gwdnow.com/user.php?getaccountbyuserid";
			final String USER_ID = "userid";
			final String API_KEY_PARAM = "apikey";
			final String API_PASS_PARAM = "apipass";
			HttpURLConnection urlConnection = null;
			BufferedReader reader = null;
			String jsonStr = "";

			try
			{
				Uri builtUri = Uri.parse(BASE_URL).buildUpon()
								  .appendQueryParameter(USER_ID, params[0])
								  .appendQueryParameter(API_KEY_PARAM, apikey)
								  .appendQueryParameter(API_PASS_PARAM, apipass)
								  .build();

				Log.v("TEST:   ", builtUri.toString());

				URL url = new URL(builtUri.toString());

				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();

				// Read the input stream into a String
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
						jsonStr += buffer.toString();
					}
				}
			} catch (MalformedURLException e)
			{
				Log.e("URL Error: ", e.getMessage());
			} catch (IOException e)
			{
				Log.e("Connection: ", e.getMessage());
			} finally
			{
				assert urlConnection != null;
				urlConnection.disconnect();
				try
				{
					if (reader != null)
					{
						reader.close();
					}
				} catch (IOException e)
				{
					Log.e("Error closing stream", e.getMessage());
				}
			}

			try
			{

				pInfo = new JSONArray(jsonStr);
				Log.v("ARRAY ", pInfo.toString());

				return true;

			} catch (JSONException e)
			{
				Log.e("JSON Error: ", e.getMessage());
			}

			return false;
		}

		protected void onPostExecute(Boolean success)
		{
			if (success)
			{
				try
				{
					pView.setText(pInfo.getJSONObject(0).getString("firstName") + " " + pInfo.getJSONObject(0).getString("lastName"));

				} catch (JSONException e)
				{
					Log.e("JSON PINFO", e.getMessage());
				}

			} else
			{
				Toast.makeText(getApplicationContext(), "Couldn't Retrieve Parent Info", Toast.LENGTH_LONG).show();
			}

		}

	}

	private class GetTeacherList extends AsyncTask<String, Void, Boolean>
	{

		protected Boolean doInBackground(String... params)
		{

			final String BASE_URL = "http://davisengeler.gwdnow.com/user.php?teacherlist";
			final String API_KEY_PARAM = "apikey";
			final String API_PASS_PARAM = "apipass";

			HttpURLConnection urlConnection = null;
			BufferedReader reader = null;
			String jsonStr = "";

			try
			{
				Uri builtUri = Uri.parse(BASE_URL).buildUpon()
								  .appendQueryParameter(API_KEY_PARAM, apikey)
								  .appendQueryParameter(API_PASS_PARAM, apipass)
								  .build();

				Log.v("TEST:   ", builtUri.toString());

				URL url = new URL(builtUri.toString());

				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();

				// Read the input stream into a String
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
						jsonStr += buffer.toString();
					}
				}
			} catch (MalformedURLException e)
			{
				Log.e("URL Error: ", e.getMessage());
			} catch (IOException e)
			{
				Log.e("Connection: ", e.getMessage());
			} finally
			{
				assert urlConnection != null;
				urlConnection.disconnect();
				try
				{
					if (reader != null)
					{
						reader.close();
					}
				} catch (IOException e)
				{
					Log.e("Error closing stream", e.getMessage());
				}
			}

			try
			{

				tList = new JSONArray(jsonStr);
				Log.v("ARRAY ", tList.toString());

				return true;

			} catch (JSONException e)
			{
				Log.e("JSON Error: ", e.getMessage());
			}

			return false;
		}

		protected void onPostExecute(Boolean success)
		{
			if (success)
			{
				try
				{
					teacherNames = new String[tList.length()];
					for (int i = 0; i < tList.length(); ++i)
					{
						teacherNames[i] = tList.getJSONObject(i).getString("firstName") + " " +
								tList.getJSONObject(i).getString("lastName");
					}
				} catch (JSONException e)
				{
					Log.e("JSON TEACH", e.getMessage());
				}
			}

		}

	}

	private class MoveChild extends AsyncTask<String, Void, String>
	{

		protected String doInBackground(String... params)
		{

			final String BASE_URL = "http://davisengeler.gwdnow.com/child.php?setclass";
			final String TEACH_ID = "teacherid";
			final String CHILD_ID = "childid";
			final String API_KEY_PARAM = "apikey";
			final String API_PASS_PARAM = "apipass";
			HttpURLConnection urlConnection = null;
			BufferedReader reader = null;
			String jsonStr = "";

			try
			{
				Uri builtUri = Uri.parse(BASE_URL).buildUpon()
								  .appendQueryParameter(TEACH_ID, params[0])
								  .appendQueryParameter(CHILD_ID, params[1])
								  .appendQueryParameter(API_KEY_PARAM, apikey)
								  .appendQueryParameter(API_PASS_PARAM, apipass)
								  .build();

				Log.v("TEST:   ", builtUri.toString());

				URL url = new URL(builtUri.toString());

				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();

				// Read the input stream into a String
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
						jsonStr += buffer.toString();
					}
				}
			} catch (MalformedURLException e)
			{
				Log.e("URL Error: ", e.getMessage());
			} catch (IOException e)
			{
				Log.e("Connection: ", e.getMessage());
			} finally
			{
				assert urlConnection != null;
				urlConnection.disconnect();
				try
				{
					if (reader != null)
					{
						reader.close();
					}
				} catch (IOException e)
				{
					Log.e("Error closing stream", e.getMessage());
				}
			}

			return jsonStr;
		}

		protected void onPostExecute(String stmt)
		{

			try
			{
				JSONObject j = new JSONObject(stmt);
				Toast.makeText(getApplicationContext(), j.getString("statusMessage"), Toast.LENGTH_LONG).show();

			} catch (JSONException e)
			{
				Log.e("JSON STMT", e.getMessage());
			}

		}

	}

	private class AddNote extends AsyncTask<String, Void, String>
	{

		protected String doInBackground(String... params)
		{

			final String BASE_URL = "http://davisengeler.gwdnow.com/child.php?addnote";
			final String MESSAGE_ID = "message";
			final String NOTE_ID = "notetype";
			final String SUBJECT_ID = "subjectid";
			final String CHILD_IDS = "children";
			final String API_KEY_PARAM = "apikey";
			final String API_PASS_PARAM = "apipass";
			HttpURLConnection urlConnection = null;
			BufferedReader reader = null;
			String jsonStr = "";

			try
			{
				Uri builtUri = Uri.parse(BASE_URL).buildUpon()
								  .appendQueryParameter(MESSAGE_ID, params[0])
								  .appendQueryParameter(SUBJECT_ID, params[1])
								  .appendQueryParameter(NOTE_ID, params[2])
								  .appendQueryParameter(CHILD_IDS, params[3])
								  .appendQueryParameter(API_KEY_PARAM, apikey)
								  .appendQueryParameter(API_PASS_PARAM, apipass)
								  .build();

				Log.v("TEST:   ", builtUri.toString());

				URL url = new URL(builtUri.toString());

				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();

				// Read the input stream into a String
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
						jsonStr += buffer.toString();
					}
				}
			} catch (MalformedURLException e)
			{
				Log.e("URL Error: ", e.getMessage());
			} catch (IOException e)
			{
				Log.e("Connection: ", e.getMessage());
			} finally
			{
				assert urlConnection != null;
				urlConnection.disconnect();
				try
				{
					if (reader != null)
					{
						reader.close();
					}
				} catch (IOException e)
				{
					Log.e("Error closing stream", e.getMessage());
				}
			}

			return jsonStr;
		}

		protected void onPostExecute(String stmt)
		{

			try
			{
				JSONObject j = new JSONObject(stmt);
				Toast.makeText(getApplicationContext(), j.getString("statusMessage"), Toast.LENGTH_LONG).show();

			} catch (JSONException e)
			{
				Log.e("JSON STMT", e.getMessage());
			}

		}

	}

}
