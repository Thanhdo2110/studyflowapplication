package com.example.studyflow.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.studyflow.data.database.AppDatabase;
import com.example.studyflow.data.database.TimetableDao;
import com.example.studyflow.data.database.entities.TimetableEntity;
import java.util.List;

public class TimetableRepository {
    private TimetableDao timetableDao;
    private LiveData<List<TimetableEntity>> allTimetable;

    public TimetableRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        timetableDao = db.timetableDao();
        allTimetable = timetableDao.getAllTimetable();
    }

    public LiveData<List<TimetableEntity>> getAllTimetable() {
        return allTimetable;
    }

    public void insert(TimetableEntity entity) {
        AppDatabase.databaseWriteExecutor.execute(() -> timetableDao.insert(entity));
    }

    public void update(TimetableEntity entity) {
        AppDatabase.databaseWriteExecutor.execute(() -> timetableDao.update(entity));
    }
}
