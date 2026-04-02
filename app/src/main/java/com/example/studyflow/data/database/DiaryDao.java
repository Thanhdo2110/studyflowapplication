package com.example.studyflow.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studyflow.data.database.entities.DiaryEntity;

import java.util.List;

@Dao
public interface DiaryDao {
    @Query("SELECT * FROM diary ORDER BY timestamp DESC")
    LiveData<List<DiaryEntity>> getAllDiaries();

    @Insert
    void insert(DiaryEntity diary);

    @Update
    void update(DiaryEntity diary);

    @Delete
    void delete(DiaryEntity diary);
}
