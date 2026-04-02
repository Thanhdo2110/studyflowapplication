package com.example.studyflow.ui.diary;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.studyflow.data.database.entities.DiaryEntity;
import com.example.studyflow.data.repository.DiaryRepository;

import java.util.List;

public class DiaryViewModel extends AndroidViewModel {
    private DiaryRepository repository;
    private LiveData<List<DiaryEntity>> allDiaries;

    public DiaryViewModel(@NonNull Application application) {
        super(application);
        repository = new DiaryRepository(application);
        allDiaries = repository.getAllDiaries();
    }

    public LiveData<List<DiaryEntity>> getAllDiaries() {
        return allDiaries;
    }

    public void insert(DiaryEntity diary) {
        repository.insert(diary);
    }

    public void update(DiaryEntity diary) {
        repository.update(diary);
    }

    public void delete(DiaryEntity diary) {
        repository.delete(diary);
    }
}
