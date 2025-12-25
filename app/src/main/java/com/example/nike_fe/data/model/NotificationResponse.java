package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * NotificationResponse - Response chứa danh sách thông báo và phân trang
 */
public class NotificationResponse {
    
    @SerializedName("content")
    private List<Notification> content;
    
    @SerializedName("totalElements")
    private Long totalElements;
    
    @SerializedName("totalPages")
    private Integer totalPages;
    
    @SerializedName("size")
    private Integer size;
    
    @SerializedName("number")
    private Integer number;
    
    @SerializedName("first")
    private Boolean first;
    
    @SerializedName("last")
    private Boolean last;

    // Constructors
    public NotificationResponse() {}

    // Getters and Setters
    public List<Notification> getContent() {
        return content;
    }

    public void setContent(List<Notification> content) {
        this.content = content;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Boolean getFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    public Boolean getLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }
}
