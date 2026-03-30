package com.example.studyflow.data.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "plans")
public class PlanEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private int durationMinutes;
    private boolean isCompleted;
    private long date;

    public PlanEntity(String title, int durationMinutes, boolean isCompleted, long date) {
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.isCompleted = isCompleted;
        this.date = date;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }
}
