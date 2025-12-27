package com.example.nike_fe.data.model;

public class ChatMessage {
    private String message;
    private boolean fromUser;
    private long timestamp;

    public ChatMessage() {
    }

    public ChatMessage(String message, boolean fromUser, long timestamp) {
        this.message = message;
        this.fromUser = fromUser;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isFromUser() {
        return fromUser;
    }

    public void setFromUser(boolean fromUser) {
        this.fromUser = fromUser;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
