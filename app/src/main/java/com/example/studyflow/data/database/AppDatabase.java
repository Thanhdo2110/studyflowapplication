package com.example.studyflow.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.studyflow.data.database.entities.ExamEntity;
import com.example.studyflow.data.database.entities.PlanEntity;
import com.example.studyflow.data.database.entities.HistoryEntity;
import com.example.studyflow.data.database.entities.DiaryEntity;
import com.example.studyflow.data.database.entities.TimetableEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ExamEntity.class, PlanEntity.class, HistoryEntity.class, DiaryEntity.class, TimetableEntity.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ExamDao examDao();
    public abstract PlanDao planDao();
    public abstract HistoryDao historyDao();
    public abstract DiaryDao diaryDao();
    public abstract TimetableDao timetableDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "studyflow_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
