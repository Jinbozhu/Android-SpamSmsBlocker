package com.example.feeling.spamtextblocker.models;

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
    private long id;
    private String sender;
    private String content;
    private String recipient;
    private long time;
    private boolean isDelivered;
    private boolean isRead;
    private boolean isSpam;

    public Message(long id, String sender, String content, String recipient, long time,
                   boolean isDelivered, boolean isRead, boolean isSpam) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.recipient = recipient;
        this.time = time;
        this.isDelivered = isDelivered;
        this.isRead = isRead;
        this.isSpam = isSpam;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setIsDelivered(boolean isDelivered) {
        this.isDelivered = isDelivered;
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
