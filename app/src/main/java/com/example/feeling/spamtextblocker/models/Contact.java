package com.example.feeling.spamtextblocker.models;

import java.util.List;

/**
 * Created by feeling on 3/5/16.
 */
public class Contact {
    private long id;
    private String name;
    private boolean isAllowed;

    public Contact(long id, String name, boolean isAllowed) {
        this.id = id;
        this.name = name;
        this.isAllowed = isAllowed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAllowed() {
        return isAllowed;
    }

    public void setIsAllowed(boolean isAllowed) {
        this.isAllowed = isAllowed;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
