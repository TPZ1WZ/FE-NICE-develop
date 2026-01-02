package com.example.nike_fe.ui.notification;

public class NotificationItem {
    private Long id;
    private String title;
    private String message;
    private String time;
    private String type; // order, promotion, delivery, system
    private boolean isRead;

    private java.util.Map<String, Object> data;

    public NotificationItem(Long id, String title, String message, String time, String type, boolean isRead) {
        this(id, title, message, time, type, isRead, null);
    }

    public NotificationItem(Long id, String title, String message, String time, String type, boolean isRead,
            java.util.Map<String, Object> data) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.time = time;
        this.type = type;
        this.isRead = isRead;
        this.data = data;
    }

    public java.util.Map<String, Object> getData() {
        return data;
    }

    public void setData(java.util.Map<String, Object> data) {
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
