package com.example.nike_fe.data.model;

public class ChatMessage {
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String content;
    private String sentAt;
    private boolean isRead;
    private String type;
    
    // Old fields for backward compatibility
    private String message;
    private boolean fromUser;
    private long timestamp;

    public ChatMessage() {
    }

    public ChatMessage(String message, boolean fromUser, long timestamp) {
        this.message = message;
        this.content = message;
        this.fromUser = fromUser;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getContent() {
        return content != null ? content : message;
    }

    public void setContent(String content) {
        this.content = content;
        this.message = content;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return content != null ? content : message;
    }

    public void setMessage(String message) {
        this.message = message;
        this.content = message;
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
