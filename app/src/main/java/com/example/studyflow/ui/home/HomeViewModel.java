package com.example.studyflow.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.studyflow.data.database.entities.ExamEntity;
import com.example.studyflow.data.database.entities.PlanEntity;
import com.example.studyflow.data.repository.ExamRepository;
import com.example.studyflow.data.repository.PlanRepository;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private ExamRepository examRepository;
    private PlanRepository planRepository;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        examRepository = new ExamRepository(application);
        planRepository = new PlanRepository(application);
    }

    public LiveData<List<ExamEntity>> getAllExams() {
        return examRepository.getAllExams();
    }

    public LiveData<List<PlanEntity>> getTodayPlans() {
        return planRepository.getPlansForToday();
    }

    public void updatePlan(PlanEntity plan) {
        planRepository.update(plan);
    }
}
