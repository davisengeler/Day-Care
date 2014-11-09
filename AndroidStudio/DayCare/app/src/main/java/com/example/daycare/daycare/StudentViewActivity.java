package com.example.daycare.daycare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
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


public class StudentViewActivity extends Activity {
    protected String [] actions = {"Contact Parent", "Add Note", "Move Student"};
    static final String [] teachers = {"Jane Smith", "John Doe", "Davis Engeler", "John Sloan", "Michael Hetzel"};
    private ListView mListView;
    private TextView tView, dView, teachView, pView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_view);

        String chosenStudent = this.getIntent().getStringExtra("chosenStudent");
        tView = (TextView) findViewById(R.id.student_name_info);
        tView.setText(chosenStudent);
        dView = (TextView) findViewById(R.id.dob_info);
        dView.setText("03/13/1990");
        teachView = (TextView) findViewById(R.id.teacher_name);
        teachView.setText(teachers[0]);
        pView = (TextView) findViewById(R.id.contact_name);
        pView.setText("Parent Name");
        mListView = (ListView) findViewById(R.id.container);

        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, actions);

        //android ic_menu_edit, ic_perm_group_phone_calls.png, ic_menu_myplaces.png
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i)
                {
                    case 0:
                        //for testing purposes
                        DialogFragment dgE = new ErrorMessageDialog();
                        dgE.show(getFragmentManager(), "error");
                        break;
                    case 1:
                        DialogFragment dg2 = new NoteDialogFragment();
                        dg2.show(getFragmentManager(), "notes");
                        break;
                    case 2:
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_student_view, menu);
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
    public static class TeacherDialogFragment extends DialogFragment
    {
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.pick_teacher)
                    .setItems(teachers, new DialogInterface.OnClickListener()
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

    public static class NoteDialogFragment extends DialogFragment
    {
        String[] noteID = {"Meal", "Nap", "Accident", "Needs", "Misc"};
        int noteIDChosen =4;
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final EditText message = new EditText(getActivity());
            message.setHint("Enter Message Here");
            message.setInputType(41); //check to see if this works
            builder.setTitle(R.string.add_note_title)
                    .setSingleChoiceItems(noteID, noteIDChosen, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            noteIDChosen = i;
                        }
                    })
                    .setView(message)
                    .setPositiveButton(R.string.save_label, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            String mContent = message.getText().toString();
                            Log.v("NoteMessage", noteIDChosen + " " + mContent);
                            //submit this as a note to server with noteIDChosen
                        }
                    })
                    .setNegativeButton(R.string.cancel_label, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            NoteDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }
    }

    public static class ErrorMessageDialog extends DialogFragment
    {
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Must select a note type")
                    .setMessage("Please select the type of note")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert);
            return builder.create();
        }


    }
}
