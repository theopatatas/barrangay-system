package com.example.barangayapp.model;

public class ChangePasswordBody {
    public String currentPassword;
    public String newPassword;

    public ChangePasswordBody(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
}
