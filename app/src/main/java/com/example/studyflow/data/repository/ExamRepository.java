package com.example.studyflow.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.studyflow.data.database.AppDatabase;
import com.example.studyflow.data.database.ExamDao;
import com.example.studyflow.data.database.entities.ExamEntity;
import java.util.List;

public class ExamRepository {
    private ExamDao examDao;

    public ExamRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        examDao = db.examDao();
    }

    public LiveData<List<ExamEntity>> getAllExams() {
        return examDao.getAllExams();
    }

    public LiveData<List<ExamEntity>> getAllExamsOrdered(long currentTime) {
        return examDao.getAllExamsOrdered(currentTime);
    }

    public void insert(ExamEntity exam) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            examDao.insert(exam);
        });
    }

    public void update(ExamEntity exam) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            examDao.update(exam);
        });
    }

    public void delete(ExamEntity exam) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            examDao.delete(exam);
        });
    }
}
