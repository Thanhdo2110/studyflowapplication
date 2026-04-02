package com.example.studyflow.data.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "diary")
public class DiaryEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String content;
    private long timestamp;

    public DiaryEntity(String content, long timestamp) {
        this.content = content;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
