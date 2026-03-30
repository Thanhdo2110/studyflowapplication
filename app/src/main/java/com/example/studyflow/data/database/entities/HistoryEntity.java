package com.example.studyflow.data.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
public class HistoryEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String taskTitle;
    private int durationMinutes;
    private long completedTimestamp;

    public HistoryEntity(String taskTitle, int durationMinutes, long completedTimestamp) {
        this.taskTitle = taskTitle;
        this.durationMinutes = durationMinutes;
        this.completedTimestamp = completedTimestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public long getCompletedTimestamp() { return completedTimestamp; }
    public void setCompletedTimestamp(long completedTimestamp) { this.completedTimestamp = completedTimestamp; }
}
