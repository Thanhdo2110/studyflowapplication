package com.example.studyflow.ui.timer;

import android.os.CountDownTimer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Locale;

public class TimerViewModel extends ViewModel {

    private MutableLiveData<Long> _timeRemaining = new MutableLiveData<>(1500000L); // 25 mins
    public LiveData<Long> timeRemaining = _timeRemaining;

    private MutableLiveData<Boolean> _isRunning = new MutableLiveData<>(false);
    public LiveData<Boolean> isRunning = _isRunning;

    private MutableLiveData<Integer> _progress = new MutableLiveData<>(100);
    public LiveData<Integer> progress = _progress;

    private CountDownTimer countDownTimer;
    private long totalTime = 1500000L;

    public void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(_timeRemaining.getValue(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                _timeRemaining.setValue(millisUntilFinished);
                int prog = (int) ((float) millisUntilFinished / totalTime * 100);
                _progress.setValue(prog);
            }

            @Override
            public void onFinish() {
                _isRunning.setValue(false);
                _timeRemaining.setValue(0L);
                _progress.setValue(0);
            }
        }.start();

        _isRunning.setValue(true);
    }

    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        _isRunning.setValue(false);
    }

    public void resetTimer(long timeMillis) {
        pauseTimer();
        totalTime = timeMillis;
        _timeRemaining.setValue(timeMillis);
        _progress.setValue(100);
    }

    public String formatTime(long millis) {
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
