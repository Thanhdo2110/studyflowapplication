package com.example.studyflow.data.model;

public class Document {
    private String title;
    private String year;
    private String type; // THPT, THCS, DH
    private String url;

    public Document(String title, String year, String type, String url) {
        this.title = title;
        this.year = year;
        this.type = type;
        this.url = url;
    }

    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getType() { return type; }
    public String getUrl() { return url; }
}
