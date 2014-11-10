package com.example.daycare.daycare;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;


public class AdminAddNote extends Activity {
    EditText mEditText;
    RadioButton radioButton1, radioButton2, radioButton3, radioButton4, radioButton5;
    int noteIndex;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_note);
        Resources res = getResources();
        final String [] notes = res.getStringArray(R.array.note_id);
        radioButton1 = (RadioButton) findViewById(R.id.radio_1);
        radioButton2 = (RadioButton) findViewById(R.id.radio_2);
        radioButton3 = (RadioButton) findViewById(R.id.radio_3);
        radioButton4 = (RadioButton) findViewById(R.id.radio_4);
        radioButton5 = (RadioButton) findViewById(R.id.radio_5);
        radioButton1.setText(notes[0]);
        radioButton2.setText(notes[1]);
        radioButton3.setText(notes[2]);
        radioButton4.setText(notes[3]);
        radioButton5.setText(notes[4]);
        radioButton1.isChecked();
        mEditText = (EditText) findViewById(R.id.note_message_text);

        button = (Button) findViewById(R.id.addNoteButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mEditText.getText().toString();
                Log.v("Note: ", message + " " + notes[noteIndex]);
            }
        });




    }


    public void onRadioButtonClicked(View view)
    {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_1:
                if (checked)
                {
                    noteIndex = 0;
                }
                break;
            case R.id.radio_2:
                if (checked)
                {
                    noteIndex = 1;
                }
                break;
            case R.id.radio_3:
                if (checked)
                {
                    noteIndex = 2;
                }
                break;
            case R.id.radio_4:
                if (checked)
                {
                    noteIndex = 3;
                }
                break;
            case R.id.radio_5:
                if (checked)
                {
                    noteIndex = 4;
                }
                break;
            default:
                break;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_admin_add_note, menu);
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
}
