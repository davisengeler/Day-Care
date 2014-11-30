package com.example.daycare.daycare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

// This activity provides a starting point which can be called by third party NFC-trigger apps.
// When started, it immediately starts the login Activity, passing in an intent extra letting the
// app know it was launched via NFC scan. After the user is logged in via API key/pass or
// email/password, IF the logged in account is a parent account, the child sign in/out screen is
// immediately displayed.
public class NFC extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent parentIntent = new Intent(getApplicationContext(), LoginActivity.class);
		parentIntent.putExtra("nfcStart", true);
		startActivity(parentIntent);
		finish();
		setContentView(R.layout.activity_nfc);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_nfc, menu);
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
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
