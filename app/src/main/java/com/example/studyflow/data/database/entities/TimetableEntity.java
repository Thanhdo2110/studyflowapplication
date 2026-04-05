package com.example.studyflow.data.database.entities;

import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "timetable", primaryKeys = {"dayOfWeek", "period"})
public class TimetableEntity {
    private int dayOfWeek; // 2 to 7 (Thứ 2 - Thứ 7)
    private int period;    // 1 to 13 (Giá trị thực trong DB)
    private String subject;
    private String type;   // "MORNING", "AFTERNOON", "EVENING"

    public TimetableEntity(int dayOfWeek, int period, String subject, String type) {
        this.dayOfWeek = dayOfWeek;
        this.period = period;
        this.subject = subject;
        this.type = type;
    }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public int getPeriod() { return period; }
    public void setPeriod(int period) { this.period = period; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
