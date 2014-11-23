package com.example.daycare.daycare;

/**
 * Created by Michael on 11/11/2014.
 */
public class ChildrenNotes {
    private int subjectID, noteType;
    private String message;
    private int noteID;
    private String childName, childID;

    public ChildrenNotes(String childID, String noteID, String message, String subjectID, String noteType )
    {
        this.childID = childID;
        this.noteID = Integer.parseInt(noteID);
        this.subjectID = Integer.parseInt(subjectID);
        this.noteID = Integer.parseInt(noteID);
        this.noteType = Integer.parseInt(noteType);
        this.message = message;
    }

    public String getChildID() {
        return childID;
    }

    public String getSubject(){
        String [] messageSubject = {"Meal", "Nap", "Accident", "Needs", "Misc"};
        return messageSubject[subjectID-1];
    }

    public int getNoteType() {
        return noteType;
    }

    public String getMessage() {
        return message;
    }

    public String getChildName() { return childName;}

    public void setChildName(String firstName, String lastName)
    {
        childName = firstName + " " + lastName;
    }

}
