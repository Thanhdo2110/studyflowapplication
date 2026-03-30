package com.example.studyflow.ui.timer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.studyflow.R;
import com.example.studyflow.data.database.entities.HistoryEntity;
import com.example.studyflow.service.TimerService;
import com.example.studyflow.ui.history.HistoryViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.Locale;

public class TimerFragment extends Fragment {

    private TimerService timerService;
    private boolean isBound = false;
    private HistoryViewModel historyViewModel;
    
    private TextView tvTimerDisplay, tvModeLabel;
    private LinearProgressIndicator progressIndicator;
    private FloatingActionButton fabPlayPause;
    private View btnReset;
    private MaterialButton btnSaveSession;
    private long currentDuration = 1500000L;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerBinder binder = (TimerService.TimerBinder) service;
            timerService = binder.getService();
            isBound = true;
            if (isAdded() && getView() != null) {
                observeService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        tvTimerDisplay = view.findViewById(R.id.tv_timer_display);
        tvModeLabel = view.findViewById(R.id.tv_mode_label);
        progressIndicator = view.findViewById(R.id.timer_progress);
        fabPlayPause = view.findViewById(R.id.fab_play_pause);
        btnReset = view.findViewById(R.id.btn_reset);
        btnSaveSession = view.findViewById(R.id.btn_save_session);

        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        fabPlayPause.setOnClickListener(v -> {
            if (isBound) {
                Boolean running = timerService.isRunning.getValue();
                if (running != null && running) {
                    timerService.pauseTimer();
                } else {
                    timerService.startTimer(currentDuration, tvModeLabel.getText().toString());
                }
            }
        });

        btnReset.setOnClickListener(v -> {
            if (isBound) timerService.resetTimer(currentDuration, tvModeLabel.getText().toString());
        });

        view.findViewById(R.id.mode_pomodoro).setOnClickListener(v -> setMode(1500000L, "POMODORO"));
        view.findViewById(R.id.mode_short_break).setOnClickListener(v -> setMode(300000L, "SHORT BREAK"));
        view.findViewById(R.id.mode_long_break).setOnClickListener(v -> setMode(900000L, "LONG BREAK"));

        btnSaveSession.setOnClickListener(v -> {
            if (isBound) {
                Long remaining = timerService.timeRemaining.getValue();
                Long initial = timerService.initialDuration.getValue();
                if (remaining != null && initial != null) {
                    int elapsedMinutes = (int) ((initial - remaining) / 60000);
                    if (elapsedMinutes > 0) {
                        HistoryEntity history = new HistoryEntity("Phiên học " + tvModeLabel.getText(), elapsedMinutes, System.currentTimeMillis());
                        historyViewModel.insert(history);
                        Toast.makeText(getContext(), "Đã lưu: " + elapsedMinutes + " phút", Toast.LENGTH_SHORT).show();
                        timerService.resetTimer(initial, tvModeLabel.getText().toString());
                    } else {
                        Toast.makeText(getContext(), "Chưa đủ thời gian để lưu", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    private void setMode(long duration, String label) {
        currentDuration = duration;
        tvModeLabel.setText(label);
        if (isBound) timerService.resetTimer(duration, label);
    }

    private void observeService() {
        if (timerService == null) return;
        
        timerService.timeRemaining.observe(getViewLifecycleOwner(), millis -> {
            if (tvTimerDisplay != null) {
                tvTimerDisplay.setText(formatTime(millis));
            }
            Long initial = timerService.initialDuration.getValue();
            if (initial != null && initial > 0 && progressIndicator != null) {
                int progress = (int) ((float) millis / initial * 100);
                progressIndicator.setProgress(progress);
            }
        });

        timerService.isRunning.observe(getViewLifecycleOwner(), isRunning -> {
            if (fabPlayPause != null) {
                fabPlayPause.setImageResource(isRunning ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
            }
        });

        timerService.currentMode.observe(getViewLifecycleOwner(), mode -> {
            if (mode != null && tvModeLabel != null) {
                tvModeLabel.setText(mode);
                if (mode.equals("POMODORO")) currentDuration = 1500000L;
                else if (mode.equals("SHORT BREAK")) currentDuration = 300000L;
                else if (mode.equals("LONG BREAK")) currentDuration = 900000L;
            }
        });
    }

    private String formatTime(long millis) {
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), TimerService.class);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBound) {
            getActivity().unbindService(connection);
            isBound = false;
        }
    }
}
