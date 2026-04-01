package com.example.studyflow.ui.history;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.studyflow.data.database.entities.HistoryEntity;
import com.example.studyflow.data.database.entities.PlanEntity;
import com.example.studyflow.data.repository.HistoryRepository;
import com.example.studyflow.data.repository.PlanRepository;
import com.example.studyflow.utils.SharedPrefsHelper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private final HistoryRepository repository;
    private final PlanRepository planRepository;
    private final LiveData<List<HistoryEntity>> allHistory;
    private final LiveData<List<PlanEntity>> weeklyPlans;
    private final MutableLiveData<Integer> selectedDayIndex = new MutableLiveData<>(-1);

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new HistoryRepository(application);
        planRepository = new PlanRepository(application);
        allHistory = repository.getAllHistory();
        
        Calendar start = getStartOfWeek(0);
        Calendar end = getEndOfWeek(0);
        weeklyPlans = planRepository.getPlansForWeek(start.getTimeInMillis(), end.getTimeInMillis());
    }

    public LiveData<Integer> getWeeklyTotalMinutes() {
        return Transformations.map(allHistory, list -> {
            int total = 0;
            if (list == null) return total;
            Calendar start = getStartOfWeek(0);
            Calendar end = getEndOfWeek(0);
            for (HistoryEntity h : list) {
                if (h.getCompletedTimestamp() >= start.getTimeInMillis() && h.getCompletedTimestamp() <= end.getTimeInMillis()) {
                    total += h.getDurationMinutes();
                }
            }
            return total;
        });
    }

    public LiveData<String> getMaxSessionTime() {
        return Transformations.map(allHistory, list -> {
            int max = 0;
            if (list != null) {
                Calendar start = getStartOfWeek(0);
                for (HistoryEntity h : list) {
                    if (h.getCompletedTimestamp() >= start.getTimeInMillis()) {
                        if (h.getDurationMinutes() > max) max = h.getDurationMinutes();
                    }
                }
            }
            return (max / 60) + "h " + (max % 60) + "m";
        });
    }

    public LiveData<String> getTrend() {
        return Transformations.map(allHistory, list -> {
            int thisWeek = 0, lastWeek = 0;
            if (list == null) return "0%";
            
            long sw0 = getStartOfWeek(0).getTimeInMillis();
            long ew0 = getEndOfWeek(0).getTimeInMillis();
            long sw1 = getStartOfWeek(-1).getTimeInMillis();
            long ew1 = getEndOfWeek(-1).getTimeInMillis();

            for (HistoryEntity h : list) {
                if (h.getCompletedTimestamp() >= sw0 && h.getCompletedTimestamp() <= ew0) thisWeek += h.getDurationMinutes();
                else if (h.getCompletedTimestamp() >= sw1 && h.getCompletedTimestamp() <= ew1) lastWeek += h.getDurationMinutes();
            }
            if (lastWeek == 0) return thisWeek > 0 ? "+100%" : "0%";
            int percent = (int) (((float)(thisWeek - lastWeek) / lastWeek) * 100);
            return (percent >= 0 ? "+" : "") + percent + "%";
        });
    }

    public LiveData<String> getWeeklyGoalProgress() {
        MediatorLiveData<String> result = new MediatorLiveData<>();
        result.addSource(weeklyPlans, plans -> {
            result.setValue(calculateGoalProgress(plans));
        });
        return result;
    }

    private String calculateGoalProgress(List<PlanEntity> plans) {
        if (plans == null || plans.isEmpty()) {
            return "0%";
        }
        int totalPlans = plans.size();
        int completedPlans = 0;
        for (PlanEntity plan : plans) {
            if (plan.isCompleted()) {
                completedPlans++;
            }
        }
        int progress = (int) (((float) completedPlans / totalPlans) * 100);
        return progress + "%";
    }

    public LiveData<float[]> getWeeklyData() {
        return Transformations.map(allHistory, historyList -> {
            float[] weekStats = new float[7];
            if (historyList == null) return weekStats;
            Calendar start = getStartOfWeek(0);
            Calendar end = getEndOfWeek(0);
            Calendar cal = Calendar.getInstance();
            for (HistoryEntity h : historyList) {
                if (h.getCompletedTimestamp() >= start.getTimeInMillis() && h.getCompletedTimestamp() <= end.getTimeInMillis()) {
                    cal.setTimeInMillis(h.getCompletedTimestamp());
                    int day = cal.get(Calendar.DAY_OF_WEEK);
                    int index = (day == Calendar.SUNDAY) ? 6 : day - 2;
                    if (index >= 0 && index < 7) weekStats[index] += h.getDurationMinutes();
                }
            }
            return weekStats;
        });
    }

    private Calendar getStartOfWeek(int offsetWeeks) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, offsetWeeks);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private Calendar getEndOfWeek(int offsetWeeks) {
        Calendar cal = getStartOfWeek(offsetWeeks);
        cal.add(Calendar.DAY_OF_YEAR, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal;
    }

    public LiveData<List<HistoryEntity>> getFilteredHistory() {
        return Transformations.switchMap(selectedDayIndex, index -> {
            if (index == -1) return allHistory;
            return Transformations.map(allHistory, list -> {
                List<HistoryEntity> filtered = new ArrayList<>();
                if (list == null) return filtered;
                Calendar start = getStartOfWeek(0);
                start.add(Calendar.DAY_OF_YEAR, index);
                Calendar cal = Calendar.getInstance();
                for (HistoryEntity h : list) {
                    cal.setTimeInMillis(h.getCompletedTimestamp());
                    if (cal.get(Calendar.YEAR) == start.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == start.get(Calendar.DAY_OF_YEAR)) {
                        filtered.add(h);
                    }
                }
                return filtered;
            });
        });
    }

    public void setSelectedDayIndex(int index) { selectedDayIndex.setValue(index); }
    public LiveData<Integer> getSelectedDayIndex() { return selectedDayIndex; }

    public void insert(HistoryEntity history) {
        repository.insert(history);
    }
}
