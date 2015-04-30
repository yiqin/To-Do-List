package com.apress.gerber.reminders;

/**
 * Created by Clifton
 * Copyright 12/26/2014.
 */
public class Reminder {

    private int mId;
    private String mContent;

    // The boolean value doesn't work here.
    private int mImportant;

    public Reminder(int id, String content, int important) {
        mId = id;
        mImportant = important;
        mContent = content;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getImportant() {
        return mImportant;
    }

    public void setImportant(int important) {
        mImportant = important;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }
}
