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

    // Logic: Ngày chưa tới (còn đếm) xếp trên, ngày đã qua xếp dưới cùng.
    // Sắp xếp theo ngày gần nhất lên đầu cho những ngày chưa tới.
    @Query("SELECT * FROM exams ORDER BY CASE WHEN examDate >= :currentTime THEN 0 ELSE 1 END ASC, examDate ASC")
    LiveData<List<ExamEntity>> getAllExamsOrdered(long currentTime);

    @Query("SELECT * FROM exams ORDER BY examDate ASC")
    LiveData<List<ExamEntity>> getAllExams();

    @Query("SELECT * FROM exams ORDER BY examDate ASC")
    List<ExamEntity> getAllExamsSync();

    @Query("SELECT * FROM exams WHERE id = :id")
    LiveData<ExamEntity> getExamById(int id);
}
