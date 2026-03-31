package com.example.studyflow.ui.history;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.studyflow.data.database.entities.HistoryEntity;
import com.example.studyflow.data.repository.HistoryRepository;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private HistoryRepository repository;
    private final LiveData<List<HistoryEntity>> allHistory;
    private final LiveData<Integer> totalMinutes;
    private final MutableLiveData<Integer> selectedDayIndex = new MutableLiveData<>(-1);

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new HistoryRepository(application);
        allHistory = repository.getAllHistory();
        totalMinutes = repository.getTotalMinutes();
    }

    public LiveData<List<HistoryEntity>> getAllHistory() {
        return allHistory;
    }

    public LiveData<Integer> getTotalMinutes() {
        return totalMinutes;
    }

    public LiveData<Integer> getWeeklyTotalMinutes() {
        return Transformations.map(allHistory, list -> {
            int total = 0;
            if (list == null) return total;
            
            Calendar start = getStartOfWeek();
            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_YEAR, 6);
            end.set(Calendar.HOUR_OF_DAY, 23);
            end.set(Calendar.MINUTE, 59);

            for (HistoryEntity h : list) {
                if (h.getCompletedTimestamp() >= start.getTimeInMillis() && 
                    h.getCompletedTimestamp() <= end.getTimeInMillis()) {
                    total += h.getDurationMinutes();
                }
            }
            return total;
        });
    }

    public void setSelectedDayIndex(int index) {
        selectedDayIndex.setValue(index);
    }

    public LiveData<Integer> getSelectedDayIndex() {
        return selectedDayIndex;
    }

    public LiveData<List<HistoryEntity>> getFilteredHistory() {
        return Transformations.switchMap(selectedDayIndex, index -> {
            if (index == -1) return allHistory;
            
            return Transformations.map(allHistory, list -> {
                List<HistoryEntity> filtered = new ArrayList<>();
                if (list == null) return filtered;

                Calendar calStartOfWeek = getStartOfWeek();
                Calendar targetDate = (Calendar) calStartOfWeek.clone();
                targetDate.add(Calendar.DAY_OF_YEAR, index);
                
                Calendar calHistory = Calendar.getInstance();
                for (HistoryEntity h : list) {
                    calHistory.setTimeInMillis(h.getCompletedTimestamp());
                    if (isSameDay(calHistory, targetDate)) {
                        filtered.add(h);
                    }
                }
                return filtered;
            });
        });
    }

    public LiveData<float[]> getWeeklyData() {
        return Transformations.map(allHistory, historyList -> {
            float[] weekStats = new float[7];
            if (historyList == null) return weekStats;

            Calendar calStartOfWeek = getStartOfWeek();
            Calendar calEndOfWeek = (Calendar) calStartOfWeek.clone();
            calEndOfWeek.add(Calendar.DAY_OF_YEAR, 6);
            calEndOfWeek.set(Calendar.HOUR_OF_DAY, 23);
            calEndOfWeek.set(Calendar.MINUTE, 59);

            Calendar calHistory = Calendar.getInstance();
            for (HistoryEntity history : historyList) {
                long timestamp = history.getCompletedTimestamp();
                if (timestamp >= calStartOfWeek.getTimeInMillis() && timestamp <= calEndOfWeek.getTimeInMillis()) {
                    calHistory.setTimeInMillis(timestamp);
                    int dayOfWeek = calHistory.get(Calendar.DAY_OF_WEEK);
                    int index = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 2;
                    if (index >= 0 && index < 7) {
                        weekStats[index] += history.getDurationMinutes();
                    }
                }
            }
            return weekStats;
        });
    }

    private Calendar getStartOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public void insert(HistoryEntity history) {
        repository.insert(history);
    }
}
