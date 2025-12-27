package com.example.nike_fe.data.model;

import java.util.List;

public class ChatResponse {
    private String message;
    private String sessionId;
    private List<RetrievedDocument> sources;

    public ChatResponse() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<RetrievedDocument> getSources() {
        return sources;
    }

    public void setSources(List<RetrievedDocument> sources) {
        this.sources = sources;
    }
}
