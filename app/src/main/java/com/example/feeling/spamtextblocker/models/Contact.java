package com.example.feeling.spamtextblocker.models;

import java.util.List;

/**
 * Created by feeling on 3/5/16.
 */
public class Contact {
    private String name;
    private List<String> numbers;
    private boolean isAllowed;

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

    public List<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }
}
