package com.example.daycare.daycare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Michael on 11/11/2014.
 */
@SuppressWarnings("DefaultFileTemplate")
class ChildrenNotesAdapter extends ArrayAdapter<ChildrenNotes>
{

	public ChildrenNotesAdapter(Context context, ArrayList<ChildrenNotes> notes)
	{
		super(context, 0, notes);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ChildrenNotes notes = getItem(position);

		if (convertView == null)
		{
			convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
		}

		TextView tv1 = (TextView) convertView.findViewById(android.R.id.text1);
		TextView tv2 = (TextView) convertView.findViewById(android.R.id.text2);

		tv1.setText(notes.getSubject() + " (" + notes.getChildName() + ")");
		tv2.setText(notes.getMessage());

		return convertView;

	}

}
