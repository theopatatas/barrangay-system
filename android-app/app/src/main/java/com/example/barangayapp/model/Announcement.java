package com.example.barangayapp.model;

public class Announcement {
    public int id;
    public String title;
    public String content;
    public String description;
    public String created_at;

    public String body() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        return content == null ? "" : content;
    }
}
