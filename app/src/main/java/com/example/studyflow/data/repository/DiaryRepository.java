package com.example.studyflow.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.studyflow.data.database.AppDatabase;
import com.example.studyflow.data.database.DiaryDao;
import com.example.studyflow.data.database.entities.DiaryEntity;

import java.util.List;

public class DiaryRepository {
    private DiaryDao diaryDao;
    private LiveData<List<DiaryEntity>> allDiaries;

    public DiaryRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        diaryDao = db.diaryDao();
        allDiaries = diaryDao.getAllDiaries();
    }

    public LiveData<List<DiaryEntity>> getAllDiaries() {
        return allDiaries;
    }

    public void insert(DiaryEntity diary) {
        AppDatabase.databaseWriteExecutor.execute(() -> diaryDao.insert(diary));
    }

    public void update(DiaryEntity diary) {
        AppDatabase.databaseWriteExecutor.execute(() -> diaryDao.update(diary));
    }

    public void delete(DiaryEntity diary) {
        AppDatabase.databaseWriteExecutor.execute(() -> diaryDao.delete(diary));
    }
}
