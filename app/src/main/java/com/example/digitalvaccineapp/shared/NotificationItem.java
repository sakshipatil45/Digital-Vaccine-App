package com.example.digitalvaccineapp.shared;

public class NotificationItem {
    private String id;
    private String title;
    private String message;
    private String date;

    public NotificationItem(String id, String title, String message, String date) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.date = date;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getDate() { return date; }
}
