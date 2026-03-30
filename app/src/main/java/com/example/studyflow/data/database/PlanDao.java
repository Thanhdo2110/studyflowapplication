package com.example.studyflow.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studyflow.data.database.entities.PlanEntity;

import java.util.List;

@Dao
public interface PlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PlanEntity plan);

    @Update
    void update(PlanEntity plan);

    @Delete
    void delete(PlanEntity plan);

    @Query("SELECT * FROM plans WHERE date >= :startOfDay AND date <= :endOfDay ORDER BY isCompleted ASC, id DESC")
    LiveData<List<PlanEntity>> getPlansForDay(long startOfDay, long endOfDay);

    @Query("SELECT * FROM plans ORDER BY date DESC")
    LiveData<List<PlanEntity>> getAllPlans();
}
