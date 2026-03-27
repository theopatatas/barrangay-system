package com.example.barangayapp.model;

public class UpdateProfileBody {
    public String fullName;
    public String email;
    public String address;

    public UpdateProfileBody(String fullName, String email, String address) {
        this.fullName = fullName;
        this.email = email;
        this.address = address;
    }
}
