package com.example.daycare.daycare;

/**
 * Created by Michael on 11/11/2014.
 */
public class ChildrenNotes {
    private int childID, subjectID, noteType;
    private String message;
    private int noteID;

    public ChildrenNotes(int childID, int noteID, String message, int subjectID, int noteType )
    {
        this.childID = childID;
        this.noteID = noteID;
        this.subjectID = subjectID;
        this.noteID = noteID;
        this.noteType = noteType;
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
