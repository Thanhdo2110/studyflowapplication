package com.example.studyflow.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.studyflow.MainActivity;
import com.example.studyflow.R;
import com.example.studyflow.utils.NotificationHelper;

public class TimerService extends Service {

    private final IBinder binder = new TimerBinder();
    
    private MutableLiveData<Long> _timeRemaining = new MutableLiveData<>(0L);
    public LiveData<Long> timeRemaining = _timeRemaining;
    
    private MutableLiveData<Boolean> _isRunning = new MutableLiveData<>(false);
    public LiveData<Boolean> isRunning = _isRunning;

    private MutableLiveData<Long> _initialDuration = new MutableLiveData<>(0L);
    public LiveData<Long> initialDuration = _initialDuration;

    private MutableLiveData<String> _currentMode = new MutableLiveData<>("CHỌN CHẾ ĐỘ");
    public LiveData<String> currentMode = _currentMode;

    private MutableLiveData<Boolean> _isGalaxyMode = new MutableLiveData<>(false);
    public LiveData<Boolean> isGalaxyMode = _isGalaxyMode;
    
    private CountDownTimer countDownTimer;

    public class TimerBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createNotificationChannel(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void setGalaxyMode(boolean enabled) {
        _isGalaxyMode.postValue(enabled);
    }

    private void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public void startTimer(long duration, String mode) {
        if (countDownTimer != null) countDownTimer.cancel();
        
        // Sử dụng logic lấy giá trị hiện tại chắc chắn hơn
        Long currentRemaining = _timeRemaining.getValue();
        if (currentRemaining == null || currentRemaining <= 0) {
            _initialDuration.setValue(duration);
            _timeRemaining.setValue(duration);
            currentRemaining = duration;
        }
        
        _currentMode.setValue(mode);
        _isRunning.setValue(true);
        
        startForeground(NotificationHelper.TIMER_NOTIFICATION_ID, getNotification("Đang tập trung: " + mode));

        final long timerToStart = currentRemaining;
        countDownTimer = new CountDownTimer(timerToStart, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                _timeRemaining.postValue(millisUntilFinished);
                updateNotification(mode + " - Còn lại: " + formatTime(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                runOnUiThread(() -> {
                    _isRunning.setValue(false);
                    _timeRemaining.setValue(0L);
                    stopForeground(true);
                    NotificationHelper.showTimerFinishedNotification(getApplicationContext(), "Hoàn thành!", "Bạn đã hoàn thành phiên " + mode);
                    stopSelf();
                });
            }
        }.start();
    }

    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        _isRunning.setValue(false);
        updateNotification("Đang tạm dừng: " + _currentMode.getValue());
    }

    public void resetTimer(long duration, String mode) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        _isRunning.setValue(false);
        _initialDuration.setValue(duration);
        _timeRemaining.setValue(duration);
        _currentMode.setValue(mode);
        stopForeground(true);
    }

    private Notification getNotification(String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                .setContentTitle("Study Flow")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();
    }

    private void updateNotification(String content) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NotificationHelper.TIMER_NOTIFICATION_ID, getNotification(content));
        }
    }

    private String formatTime(long millis) {
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
