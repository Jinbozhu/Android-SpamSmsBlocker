package com.example.feeling.smstest;

/*
Copyright 2014 Scott Logic Ltd

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/

import java.util.Date;

/**
 * Created by sdavies on 09/01/2014.
 */
public class Message {
    private String content;
    private String sender;
    private String recipient;
    private long time;
    private boolean isRead;
    private boolean isSpam;

    public Message(String content, String sender, String recipient, long time, boolean isRead, boolean isSpam) {
        this.content = content;
        this.sender = sender;
        this.recipient = recipient;
        this.time = time;
        this.isRead = isRead;
        this.isSpam = isSpam;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        recipient = recipient;
    }

    public long getTime() {
        return time;
    }

    public void setTime(Date time) {
        time = time;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public boolean isSpam() {
        return isSpam;
    }

    public void setIsSpam(boolean isSpam) {
        this.isSpam = isSpam;
    }

    @Override
    public String toString() {
        return getContent() + "  -  " + String.valueOf(getTime());
    }
}
