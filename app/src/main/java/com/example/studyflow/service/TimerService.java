package com.example.studyflow.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
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

    public void startTimer(long duration, String mode) {
        if (countDownTimer != null) countDownTimer.cancel();
        
        _initialDuration.postValue(duration);
        _currentMode.postValue(mode);
        _isRunning.postValue(true);
        
        startForeground(NotificationHelper.TIMER_NOTIFICATION_ID, getNotification("Đang tập trung: " + mode));

        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                _timeRemaining.postValue(millisUntilFinished);
                updateNotification(mode + " - Còn lại: " + formatTime(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                _isRunning.postValue(false);
                _timeRemaining.postValue(0L);
                stopForeground(true);
                NotificationHelper.showTimerFinishedNotification(getApplicationContext(), "Hoàn thành!", "Bạn đã hoàn thành phiên " + mode);
                stopSelf();
            }
        }.start();
    }

    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        _isRunning.postValue(false);
        stopForeground(true);
    }

    public void resetTimer(long duration, String mode) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        _isRunning.postValue(false);
        _initialDuration.postValue(duration);
        _timeRemaining.postValue(duration);
        _currentMode.postValue(mode);
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
