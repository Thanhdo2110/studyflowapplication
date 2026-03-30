package com.example.studyflow.ui.plan;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.studyflow.data.database.entities.PlanEntity;
import com.example.studyflow.data.repository.PlanRepository;
import java.util.List;

public class PlanViewModel extends AndroidViewModel {
    private PlanRepository repository;
    private final LiveData<List<PlanEntity>> todayPlans;

    public PlanViewModel(@NonNull Application application) {
        super(application);
        repository = new PlanRepository(application);
        todayPlans = repository.getPlansForToday();
    }

    public LiveData<List<PlanEntity>> getTodayPlans() {
        return todayPlans;
    }

    public void insert(PlanEntity plan) {
        repository.insert(plan);
    }

    public void update(PlanEntity plan) {
        repository.update(plan);
    }

    public void delete(PlanEntity plan) {
        repository.delete(plan);
    }
}
