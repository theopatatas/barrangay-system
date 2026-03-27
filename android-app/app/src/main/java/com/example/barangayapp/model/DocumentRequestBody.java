package com.example.barangayapp.model;

public class DocumentRequestBody {
    public String doc;
    public String date;
    public String time;

    public DocumentRequestBody(String doc, String date, String time) {
        this.doc = doc;
        this.date = date;
        this.time = time;
    }
}
