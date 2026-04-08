package com.example.studyflow.ui.exam;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.studyflow.data.database.entities.ExamEntity;
import com.example.studyflow.data.repository.ExamRepository;
import java.util.List;

public class ExamViewModel extends AndroidViewModel {
    private ExamRepository repository;

    public ExamViewModel(@NonNull Application application) {
        super(application);
        repository = new ExamRepository(application);
    }

    public LiveData<List<ExamEntity>> getAllExamsOrdered() {
        return repository.getAllExamsOrdered(System.currentTimeMillis());
    }

    public void insert(ExamEntity exam) {
        repository.insert(exam);
    }

    public void update(ExamEntity exam) {
        repository.update(exam);
    }

    public void delete(ExamEntity exam) {
        repository.delete(exam);
    }
}
