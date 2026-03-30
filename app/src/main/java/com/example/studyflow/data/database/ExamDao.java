package com.example.studyflow.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studyflow.data.database.entities.ExamEntity;

import java.util.List;

@Dao
public interface ExamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExamEntity exam);

    @Update
    void update(ExamEntity exam);

    @Delete
    void delete(ExamEntity exam);

    @Query("SELECT * FROM exams ORDER BY examDate ASC")
    LiveData<List<ExamEntity>> getAllExams();

    @Query("SELECT * FROM exams WHERE id = :id")
    LiveData<ExamEntity> getExamById(int id);
}
