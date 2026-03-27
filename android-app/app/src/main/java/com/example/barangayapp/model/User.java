package com.example.barangayapp.model;

import java.util.List;

public class User {
    public int id;
    public String fullName;
    public String email;
    public String address;
    public String role;
    public List<DocumentRequest> requests;
    public List<Complaint> complaints;
    public List<NotificationItem> notifications;
}
