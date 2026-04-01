package com.example.studyflow.ui.timer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.studyflow.R;
import com.example.studyflow.data.database.entities.HistoryEntity;
import com.example.studyflow.data.repository.PlanRepository;
import com.example.studyflow.service.TimerService;
import com.example.studyflow.ui.history.HistoryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class TimerFragment extends Fragment {

    private TimerService timerService;
    private boolean isBound = false;
    private HistoryViewModel historyViewModel;
    private PlanRepository planRepository;
    private Ringtone ringtone;
    
    private TextView tvTimerDisplay, tvModeLabel;
    private LinearProgressIndicator progressIndicator;
    private FloatingActionButton fabPlayPause;
    private View rootLayout;
    private KonfettiView konfettiView;
    private long currentDuration = 1500000L; 
    private boolean isFinished = false;

    private static final List<Quote> QUOTES = Arrays.asList(
        new Quote("“Hành trình vạn dặm bắt đầu từ một bước chân.”", "Lão Tử"),
        new Quote("“Nghị lực và kiên trì sẽ chiến thắng tất cả mọi thứ.”", "Benjamin Franklin"),
        new Quote("“Đầu tư vào tri thức đem lại lợi nhuận cao nhất.”", "Benjamin Franklin"),
        new Quote("“Học không biết chán, dạy người không biết mỏi.”", "Khổng Tử")
    );

    private static class Quote {
        final String text; final String author;
        Quote(String text, String author) { this.text = text; this.author = author; }
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            timerService = ((TimerService.TimerBinder) service).getService();
            isBound = true;
            observeService();
            checkArgumentsAndSetTimer();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) { isBound = false; }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        rootLayout = view.findViewById(R.id.timer_root_layout);
        tvTimerDisplay = view.findViewById(R.id.tv_timer_display);
        tvModeLabel = view.findViewById(R.id.tv_mode_label);
        konfettiView = view.findViewById(R.id.konfettiView);
        progressIndicator = view.findViewById(R.id.timer_progress);
        fabPlayPause = view.findViewById(R.id.fab_play_pause);

        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        planRepository = new PlanRepository(requireActivity().getApplication());

        showRandomQuote(view);

        if (getArguments() != null) {
            String subject = getArguments().getString("subject_name");
            int minutes = getArguments().getInt("duration_minutes", 0);
            if (minutes > 0) {
                currentDuration = minutes * 60000L;
                tvTimerDisplay.setText(formatTime(currentDuration));
                if (subject != null) tvModeLabel.setText(subject.toUpperCase());
            }
        }

        fabPlayPause.setOnClickListener(v -> {
            if (isBound && timerService != null) {
                if (Boolean.TRUE.equals(timerService.isRunning.getValue())) {
                    timerService.pauseTimer();
                } else {
                    if (isFinished) {
                        Toast.makeText(getContext(), "Hãy nhấn Xong trước khi bắt đầu phiên mới!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(getActivity(), TimerService.class);
                    ContextCompat.startForegroundService(requireContext(), intent);
                    timerService.startTimer(currentDuration, tvModeLabel.getText().toString());
                }
            }
        });

        view.findViewById(R.id.btn_reset).setOnClickListener(v -> {
            stopSuccessSound();
            isFinished = false;
            if (isBound && timerService != null) {
                timerService.resetTimer(currentDuration, tvModeLabel.getText().toString());
                updateBackground(Boolean.TRUE.equals(timerService.isGalaxyMode.getValue()), "DEFAULT");
            }
        });

        view.findViewById(R.id.btn_save_session).setOnClickListener(v -> {
            if (isBound && timerService != null) {
                if (isFinished) {
                    Long initial = timerService.initialDuration.getValue();
                    String modeName = tvModeLabel.getText().toString();
                    if (initial != null && initial > 0) {
                        int elapsedMinutes = (int) (initial / 60000);
                        if (elapsedMinutes == 0) elapsedMinutes = 1;
                        
                        // Định dạng lại tên lưu lịch sử: Viết hoa toàn bộ
                        String saveName = modeName.replace("TÙY CHỈNH", "TUY CHINH").toUpperCase();
                        HistoryEntity history = new HistoryEntity("Hoàn thành: " + saveName, elapsedMinutes, System.currentTimeMillis());
                        
                        historyViewModel.insert(history);
                        planRepository.markPlanAsCompleted(modeName);
                        Toast.makeText(getContext(), "Đã lưu lịch sử: " + elapsedMinutes + " phút", Toast.LENGTH_SHORT).show();
                        stopSuccessSound();
                        isFinished = false;
                        timerService.resetTimer(currentDuration, modeName);
                        updateBackground(Boolean.TRUE.equals(timerService.isGalaxyMode.getValue()), "DEFAULT");
                    }
                } else {
                    Toast.makeText(getContext(), "Bạn cần hoàn thành hết thời gian mới có thể nhấn Xong!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvTimerDisplay.setOnClickListener(v -> {
            if (isBound && timerService != null && !Boolean.TRUE.equals(timerService.isRunning.getValue())) {
                if (isFinished) return;
                showCustomTimerDialog();
            }
        });

        view.findViewById(R.id.mode_pomodoro).setOnClickListener(v -> {
            if (isBound && timerService != null) {
                timerService.setGalaxyMode(!Boolean.TRUE.equals(timerService.isGalaxyMode.getValue()));
            }
        });

        view.findViewById(R.id.mode_15_min).setOnClickListener(v -> setMode(900000L, "15 PHÚT"));
        view.findViewById(R.id.mode_30_min).setOnClickListener(v -> setMode(1800000L, "30 PHÚT"));
        view.findViewById(R.id.mode_60_min).setOnClickListener(v -> setMode(3600000L, "1 TIẾNG"));

        return view;
    }

    private void checkArgumentsAndSetTimer() {
        if (getArguments() != null && isBound && timerService != null) {
            String subject = getArguments().getString("subject_name");
            int minutes = getArguments().getInt("duration_minutes", 0);
            if (subject != null && minutes > 0) {
                setMode(minutes * 60000L, subject.toUpperCase());
                getArguments().clear();
            }
        }
    }

    private void showCustomTimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thiết lập thời gian học");
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        final EditText inputMinutes = new EditText(requireContext());
        inputMinutes.setHint("Số phút (vd: 25, 45, 60...)");
        inputMinutes.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputMinutes);
        builder.setView(layout);
        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            try {
                String minutesStr = inputMinutes.getText().toString().trim();
                if (!minutesStr.isEmpty()) {
                    int minutes = Integer.parseInt(minutesStr);
                    if (minutes > 0 && minutes <= 999) setMode(minutes * 60000L, "TÙY CHỈNH");
                }
            } catch (Exception e) {}
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void setMode(long duration, String label) {
        if (isFinished) return;
        currentDuration = duration;
        tvModeLabel.setText(label);
        tvTimerDisplay.setText(formatTime(duration));
        if (isBound) timerService.resetTimer(duration, label);
    }

    private void observeService() {
        timerService.timeRemaining.observe(getViewLifecycleOwner(), millis -> {
            tvTimerDisplay.setText(formatTime(millis));
            if (millis != null && millis == 0 && !isFinished) {
                Boolean isRunning = timerService.isRunning.getValue();
                if (isRunning != null && !isRunning) {
                    Long initial = timerService.initialDuration.getValue();
                    if (initial != null && initial > 0) {
                        isFinished = true;
                        updateBackground(Boolean.TRUE.equals(timerService.isGalaxyMode.getValue()), "SUCCESS");
                    }
                }
            }
            Long initial = timerService.initialDuration.getValue();
            if (initial != null && initial > 0) progressIndicator.setProgress((int) ((float) millis / initial * 100));
        });
        timerService.isRunning.observe(getViewLifecycleOwner(), running -> fabPlayPause.setImageResource(Boolean.TRUE.equals(running) ? R.drawable.ic_pause : R.drawable.ic_play_arrow));
        timerService.isGalaxyMode.observe(getViewLifecycleOwner(), galaxy -> { if(!isFinished) updateBackground(Boolean.TRUE.equals(galaxy), "DEFAULT"); });
    }

    private void updateBackground(boolean galaxyMode, String type) {
        if (rootLayout == null) return;
        if ("SUCCESS".equals(type)) {
            rootLayout.setBackgroundColor(Color.parseColor("#FFF176"));
            tvTimerDisplay.setTextColor(Color.parseColor("#BF360C")); 
            showFireworks();
            playSuccessSound();
        } else if (galaxyMode) {
            rootLayout.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {Color.parseColor("#020111"), Color.parseColor("#191621")}));
            tvTimerDisplay.setTextColor(Color.WHITE);
            tvModeLabel.setTextColor(Color.parseColor("#B39DDB"));
        } else {
            rootLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background));
            tvTimerDisplay.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface));
            tvModeLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        }
    }

    private void showFireworks() {
        if (konfettiView == null) return;
        konfettiView.start(new PartyFactory(new Emitter(5, TimeUnit.SECONDS).perSecond(100)).angle(270).spread(90).setSpeedBetween(10f, 40f).position(new Position.Relative(0.5, 0.5)).build());
    }

    private void playSuccessSound() {
        try { ringtone = RingtoneManager.getRingtone(getContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)); if (ringtone != null) ringtone.play(); } catch (Exception e) {}
    }

    private void stopSuccessSound() { if (ringtone != null && ringtone.isPlaying()) ringtone.stop(); }

    private void showRandomQuote(View v) {
        Quote q = QUOTES.get(new Random().nextInt(QUOTES.size()));
        ((TextView)v.findViewById(R.id.tv_quote_text)).setText(q.text);
        ((TextView)v.findViewById(R.id.tv_quote_author)).setText("— " + q.author);
    }

    private String formatTime(long millis) { return String.format(Locale.getDefault(), "%02d:%02d", (int)(millis/1000)/60, (int)(millis/1000)%60); }

    @Override
    public void onStart() { super.onStart(); getActivity().bindService(new Intent(getActivity(), TimerService.class), connection, Context.BIND_AUTO_CREATE); }

    @Override
    public void onStop() { super.onStop(); if (isBound) { getActivity().unbindService(connection); isBound = false; } }
}
