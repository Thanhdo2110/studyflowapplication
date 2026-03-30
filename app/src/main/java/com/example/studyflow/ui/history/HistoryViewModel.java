package com.example.studyflow.ui.history;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.example.studyflow.data.database.entities.HistoryEntity;
import com.example.studyflow.data.repository.HistoryRepository;
import java.util.Calendar;
import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private HistoryRepository repository;
    private final LiveData<List<HistoryEntity>> allHistory;
    private final LiveData<Integer> totalMinutes;

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

    public LiveData<float[]> getWeeklyData() {
        return Transformations.map(allHistory, historyList -> {
            float[] dayMinutes = new float[7];
            if (historyList == null) return dayMinutes;

            Calendar calNow = Calendar.getInstance();
            // Đặt về cuối ngày hôm nay
            calNow.set(Calendar.HOUR_OF_DAY, 23);
            calNow.set(Calendar.MINUTE, 59);
            calNow.set(Calendar.SECOND, 59);
            calNow.set(Calendar.MILLISECOND, 999);

            Calendar calHistory = Calendar.getInstance();

            for (HistoryEntity history : historyList) {
                calHistory.setTimeInMillis(history.getCompletedTimestamp());
                
                // Tính số ngày chênh lệch dựa trên lịch (không dùng phép chia mili giây đơn thuần)
                int diffDays = getDiffDays(calHistory, calNow);
                
                if (diffDays >= 0 && diffDays < 7) {
                    // Index 6 là hôm nay, index 0 là 6 ngày trước
                    dayMinutes[6 - diffDays] += history.getDurationMinutes();
                }
            }
            return dayMinutes;
        });
    }

    private int getDiffDays(Calendar start, Calendar end) {
        Calendar s = (Calendar) start.clone();
        Calendar e = (Calendar) end.clone();
        s.set(Calendar.HOUR_OF_DAY, 0); s.set(Calendar.MINUTE, 0); s.set(Calendar.SECOND, 0); s.set(Calendar.MILLISECOND, 0);
        e.set(Calendar.HOUR_OF_DAY, 0); e.set(Calendar.MINUTE, 0); e.set(Calendar.SECOND, 0); e.set(Calendar.MILLISECOND, 0);
        
        long diff = e.getTimeInMillis() - s.getTimeInMillis();
        return (int) (diff / (24 * 60 * 60 * 1000));
    }

    public void insert(HistoryEntity history) {
        repository.insert(history);
    }
}
