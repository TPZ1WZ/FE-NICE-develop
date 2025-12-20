package com.example.nike_fe.ui.notification;

public class NotificationItem {
    private String title;
    private String message;
    private String time;
    private String type; // order, promotion, delivery, system
    private boolean isRead;

    public NotificationItem(String title, String message, String time, String type, boolean isRead) {
        this.title = title;
        this.message = message;
        this.time = time;
        this.type = type;
        this.isRead = isRead;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
