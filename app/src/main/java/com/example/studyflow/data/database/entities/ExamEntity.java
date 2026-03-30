package com.example.studyflow.data.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exams")
public class ExamEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private long examDate;
    private String category;
    private int progress;

    public ExamEntity(String name, long examDate, String category, int progress) {
        this.name = name;
        this.examDate = examDate;
        this.category = category;
        this.progress = progress;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getExamDate() { return examDate; }
    public void setExamDate(long examDate) { this.examDate = examDate; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
}
