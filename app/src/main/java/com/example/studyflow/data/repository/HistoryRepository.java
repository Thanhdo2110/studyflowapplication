package com.example.studyflow.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.studyflow.data.database.AppDatabase;
import com.example.studyflow.data.database.HistoryDao;
import com.example.studyflow.data.database.entities.HistoryEntity;
import java.util.List;

public class HistoryRepository {
    private HistoryDao historyDao;
    private LiveData<List<HistoryEntity>> allHistory;

    public HistoryRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        historyDao = db.historyDao();
        allHistory = historyDao.getAllHistory();
    }

    public LiveData<List<HistoryEntity>> getAllHistory() {
        return allHistory;
    }

    public LiveData<Integer> getTotalMinutes() {
        return historyDao.getTotalMinutes();
    }

    public void insert(HistoryEntity history) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            historyDao.insert(history);
        });
    }
}
