package com.example.daycare.daycare;

/**
 * Created by Michael on 11/11/2014.
 */
@SuppressWarnings("DefaultFileTemplate")
class ChildrenNotes
{
	private final int subjectID;
	private final int noteType;
	private final String message;
	private final String childID;
	private String childName;

	public ChildrenNotes(String childID, String message, String subjectID, String noteType)
	{
		this.childID = childID;
		this.subjectID = Integer.parseInt(subjectID);
		this.noteType = Integer.parseInt(noteType);
		this.message = message;
	}

	public String getChildID()
	{
		return childID;
	}

	public String getSubject()
	{
		String[] messageSubject = {"Meal", "Nap", "Accident", "Needs", "Misc"};
		return messageSubject[subjectID - 1];
	}

	public int getNoteType()
	{
		return noteType;
	}

	public String getMessage()
	{
		return message;
	}

	public String getChildName()
	{
		return childName;
	}

	public void setChildName(String firstName, String lastName)
	{
		childName = firstName + " " + lastName;
	}

}
