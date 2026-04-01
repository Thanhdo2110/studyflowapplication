package com.example.studyflow.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.studyflow.data.database.entities.ExamEntity;
import com.example.studyflow.data.database.entities.HistoryEntity;
import com.example.studyflow.data.database.entities.PlanEntity;
import com.example.studyflow.data.repository.ExamRepository;
import com.example.studyflow.data.repository.HistoryRepository;
import com.example.studyflow.data.repository.PlanRepository;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private ExamRepository examRepository;
    private PlanRepository planRepository;
    private HistoryRepository historyRepository;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        examRepository = new ExamRepository(application);
        planRepository = new PlanRepository(application);
        historyRepository = new HistoryRepository(application);
    }

    public LiveData<List<ExamEntity>> getAllExams() {
        return examRepository.getAllExams();
    }

    public LiveData<List<PlanEntity>> getTodayPlans() {
        return planRepository.getPlansForToday();
    }

    public void updatePlan(PlanEntity plan) {
        planRepository.update(plan);
        
        // FIX: Định dạng lại tên lịch sử khi hoàn thành ở Trang chủ: VIẾT HOA TOÀN BỘ
        if (plan.isCompleted()) {
            HistoryEntity history = new HistoryEntity(
                "Hoàn thành: " + plan.getTitle().toUpperCase(),
                plan.getDurationMinutes(),
                System.currentTimeMillis()
            );
            historyRepository.insert(history);
        }
    }

    public void deletePlan(PlanEntity plan) {
        planRepository.delete(plan);
    }
}
