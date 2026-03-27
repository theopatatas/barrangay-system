package com.example.barangayapp.model;

public class RegisterRequest {
    public String name;
    public String email;
    public String password;
    public String address;

    public RegisterRequest(String name, String email, String password, String address) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = address;
    }
}
