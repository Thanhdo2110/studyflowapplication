package com.example.studyflow.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studyflow.data.database.entities.TimetableEntity;

import java.util.List;

@Dao
public interface TimetableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TimetableEntity timetable);

    @Update
    void update(TimetableEntity timetable);

    @Query("SELECT * FROM timetable ORDER BY dayOfWeek, period")
    LiveData<List<TimetableEntity>> getAllTimetable();

    @Query("SELECT * FROM timetable WHERE dayOfWeek = :day AND period = :period LIMIT 1")
    TimetableEntity getTimetableBySlot(int day, int period);

    @Query("DELETE FROM timetable")
    void deleteAll();
}
