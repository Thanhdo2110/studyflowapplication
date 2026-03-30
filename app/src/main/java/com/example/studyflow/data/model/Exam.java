package com.example.studyflow.data.model;

import java.util.Date;

public class Exam {
    private int id;
    private String name;
    private long date;
    private String category;
    private int progress;

    public Exam(int id, String name, long date, String category, int progress) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.category = category;
        this.progress = progress;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public long getDate() { return date; }
    public String getCategory() { return category; }
    public int getProgress() { return progress; }
    
    public int getDaysRemaining() {
        long diff = date - System.currentTimeMillis();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }
}
