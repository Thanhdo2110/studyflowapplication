package com.example.studyflow.ui.timetable;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.studyflow.data.database.entities.TimetableEntity;
import com.example.studyflow.data.repository.TimetableRepository;
import java.util.List;

public class TimetableViewModel extends AndroidViewModel {
    private TimetableRepository repository;
    private LiveData<List<TimetableEntity>> allTimetable;

    public TimetableViewModel(@NonNull Application application) {
        super(application);
        repository = new TimetableRepository(application);
        allTimetable = repository.getAllTimetable();
    }

    public LiveData<List<TimetableEntity>> getAllTimetable() {
        return allTimetable;
    }

    public void saveSubject(int day, int period, String subject, String type) {
        TimetableEntity entity = new TimetableEntity(day, period, subject, type);
        // Using insert with REPLACE strategy handles both new and existing entries
        repository.insert(entity);
    }
}
