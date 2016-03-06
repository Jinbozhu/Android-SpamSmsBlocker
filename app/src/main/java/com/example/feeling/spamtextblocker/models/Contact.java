package com.example.feeling.spamtextblocker.models;

/**
 * Created by feeling on 3/5/16.
 */
public class Contact {
    private String name;
    private String number;
    private boolean isAllowed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean isAllowed() {
        return isAllowed;
    }

    public void setIsAllowed(boolean isAllowed) {
        this.isAllowed = isAllowed;
    }
}
