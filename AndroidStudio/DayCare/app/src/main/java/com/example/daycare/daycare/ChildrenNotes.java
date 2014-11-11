package com.example.daycare.daycare;

/**
 * Created by Michael on 11/11/2014.
 */
public class ChildrenNotes {
    private int childID, subjectID, noteType;
    private String message;
    private int noteID;

    public ChildrenNotes(String childID, String noteID, String message, String subjectID, String noteType )
    {
        this.childID = Integer.parseInt(childID);
        this.noteID = Integer.parseInt(noteID);
        this.subjectID = Integer.parseInt(subjectID);
        this.noteID = Integer.parseInt(noteID);
        this.noteType = Integer.parseInt(noteType);
        this.message = message;
    }

    public int getChildID() {
        return childID;
    }

    public String getSubject(){
        String [] messageSubject = {"Meal", "Nap", "Accident", "Needs", "Misc"};
        return messageSubject[subjectID];
    }

    public int getNoteType() {
        return noteType;
    }

    public String getMessage() {
        return message;
    }
}
