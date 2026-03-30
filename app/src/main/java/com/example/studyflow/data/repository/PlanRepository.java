package com.example.studyflow.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.studyflow.data.database.AppDatabase;
import com.example.studyflow.data.database.PlanDao;
import com.example.studyflow.data.database.entities.PlanEntity;
import java.util.List;
import java.util.Calendar;

public class PlanRepository {
    private PlanDao planDao;

    public PlanRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        planDao = db.planDao();
    }

    public LiveData<List<PlanEntity>> getPlansForToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endOfDay = calendar.getTimeInMillis();

        return planDao.getPlansForDay(startOfDay, endOfDay);
    }

    public void insert(PlanEntity plan) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            planDao.insert(plan);
        });
    }

    public void update(PlanEntity plan) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            planDao.update(plan);
        });
    }

    public void delete(PlanEntity plan) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            planDao.delete(plan);
        });
    }
}
