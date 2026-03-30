package com.example.studyflow.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.studyflow.data.database.entities.HistoryEntity;

import java.util.List;

@Dao
public interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HistoryEntity history);

    @Query("SELECT * FROM history ORDER BY completedTimestamp DESC")
    LiveData<List<HistoryEntity>> getAllHistory();

    @Query("SELECT SUM(durationMinutes) FROM history")
    LiveData<Integer> getTotalMinutes();
}
