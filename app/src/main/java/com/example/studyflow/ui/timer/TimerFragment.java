package com.example.studyflow.ui.timer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
    
    private TextView tvTimerDisplay, tvModeLabel, tvQuoteText, tvQuoteAuthor;
    private LinearProgressIndicator progressIndicator;
    private FloatingActionButton fabPlayPause;
    private View rootLayout;
    private KonfettiView konfettiView;
    private long currentDuration = 0L; 
    private boolean isFinished = false;

    private final Handler quoteHandler = new Handler(Looper.getMainLooper());
    private final Runnable quoteRunnable = new Runnable() {
        @Override
        public void run() {
            updateQuote();
            quoteHandler.postDelayed(this, 15000); 
        }
    };

    private static final List<Quote> QUOTES = Arrays.asList(
        new Quote("“Hành trình vạn dặm bắt đầu từ một bước chân.”", "Lão Tử"),
        new Quote("“Nghị lực và kiên trì sẽ chiến thắng tất cả mọi thứ.”", "Benjamin Franklin"),
        new Quote("“Đầu tư vào tri thức đem lại lợi nhuận cao nhất.”", "Benjamin Franklin"),
        new Quote("“Học không biết chán, dạy người không biết mỏi.”", "Khổng Tử"),
        new Quote("“Thành công là một cuộc hành trình, không phải một điểm đến.”", "Arthur Ashe"),
        new Quote("“Cách tốt nhất để dự đoán tương lai là tạo ra nó.”", "Abraham Lincoln")
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
            // Ngay khi kết nối, lấy lại các giá trị từ Service
            syncWithService();
            observeService();
            checkArgumentsAndSetTimer();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) { isBound = false; }
    };

    private void syncWithService() {
        if (timerService != null) {
            Long remaining = timerService.timeRemaining.getValue();
            if (remaining != null && remaining > 0) {
                currentDuration = remaining;
                if (tvTimerDisplay != null) tvTimerDisplay.setText(formatTime(remaining));
            }
            String mode = timerService.currentMode.getValue();
            if (mode != null && tvModeLabel != null) tvModeLabel.setText(mode);
            
            // Khôi phục trạng thái isFinished nếu thời gian đã về 0
            if (remaining != null && remaining == 0) {
                Long initial = timerService.initialDuration.getValue();
                if (initial != null && initial > 0) {
                    isFinished = true;
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        rootLayout = view.findViewById(R.id.timer_root_layout);
        tvTimerDisplay = view.findViewById(R.id.tv_timer_display);
        tvModeLabel = view.findViewById(R.id.tv_mode_label);
        tvQuoteText = view.findViewById(R.id.tv_quote_text);
        tvQuoteAuthor = view.findViewById(R.id.tv_quote_author);
        konfettiView = view.findViewById(R.id.konfettiView);
        progressIndicator = view.findViewById(R.id.timer_progress);
        fabPlayPause = view.findViewById(R.id.fab_play_pause);

        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        planRepository = new PlanRepository(requireActivity().getApplication());

        if (tvTimerDisplay != null) tvTimerDisplay.setText("00:00");
        if (tvModeLabel != null) tvModeLabel.setText("CHỌN CHẾ ĐỘ");

        updateQuote();

        if (fabPlayPause != null) {
            fabPlayPause.setOnClickListener(v -> {
                if (isBound && timerService != null) {
                    if (Boolean.TRUE.equals(timerService.isRunning.getValue())) {
                        timerService.pauseTimer();
                    } else {
                        if (isFinished) {
                            Toast.makeText(getContext(), "Hãy nhấn Lưu trước khi bắt đầu phiên mới!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Lấy thời gian từ service nếu biến local bị reset
                        long timeToStart = currentDuration;
                        if (timeToStart <= 0 && timerService.timeRemaining.getValue() != null) {
                            timeToStart = timerService.timeRemaining.getValue();
                        }

                        if (timeToStart <= 0) {
                            Toast.makeText(getContext(), "Vui lòng chọn hoặc thiết lập thời gian!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        Intent intent = new Intent(getActivity(), TimerService.class);
                        ContextCompat.startForegroundService(requireContext(), intent);
                        timerService.startTimer(timeToStart, tvModeLabel.getText().toString().toUpperCase());
                    }
                }
            });
        }

        View btnReset = view.findViewById(R.id.btn_reset);
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                stopSuccessSound();
                isFinished = false;
                currentDuration = 0; 
                if (tvTimerDisplay != null) tvTimerDisplay.setText("00:00");
                if (tvModeLabel != null) tvModeLabel.setText("CHỌN CHẾ ĐỘ");
                if (isBound && timerService != null) {
                    timerService.resetTimer(0, "CHỌN CHẾ ĐỘ");
                    updateBackground(Boolean.TRUE.equals(timerService.isGalaxyMode.getValue()), "DEFAULT");
                }
            });
        }

        View btnSave = view.findViewById(R.id.btn_save_session);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                if (isBound && timerService != null) {
                    if (isFinished) {
                        Long initial = timerService.initialDuration.getValue();
                        String modeName = tvModeLabel.getText().toString();
                        if (initial != null && initial > 0) {
                            int elapsedMinutes = (int) (initial / 60000);
                            if (elapsedMinutes == 0) elapsedMinutes = 1;
                            
                            String historyTitle = "HOÀN THÀNH: " + modeName.toUpperCase();
                            historyViewModel.insert(new HistoryEntity(historyTitle, elapsedMinutes, System.currentTimeMillis()));
                            planRepository.markPlanAsCompleted(modeName);
                            Toast.makeText(getContext(), "Đã lưu lịch sử: " + elapsedMinutes + " phút", Toast.LENGTH_SHORT).show();
                            stopSuccessSound();
                            isFinished = false;
                            
                            currentDuration = 0;
                            timerService.resetTimer(0, "CHỌN CHẾ ĐỘ");
                            if (tvTimerDisplay != null) tvTimerDisplay.setText("00:00");
                            if (tvModeLabel != null) tvModeLabel.setText("CHỌN CHẾ ĐỘ");
                            updateBackground(Boolean.TRUE.equals(timerService.isGalaxyMode.getValue()), "DEFAULT");
                        }
                    } else {
                        Toast.makeText(getContext(), "Bạn phải hoàn thành phiên học mới được lưu!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (tvTimerDisplay != null) {
            tvTimerDisplay.setOnClickListener(v -> {
                if (timerService != null && Boolean.TRUE.equals(timerService.isRunning.getValue())) {
                    Toast.makeText(getContext(), "Dừng đồng hồ để nhập thời gian!", Toast.LENGTH_SHORT).show();
                    return;
                }
                showCustomTimerDialog();
            });
        }

        View modePomodoro = view.findViewById(R.id.mode_pomodoro);
        if (modePomodoro != null) {
            modePomodoro.setOnClickListener(v -> {
                if (isBound && timerService != null) {
                    timerService.setGalaxyMode(!Boolean.TRUE.equals(timerService.isGalaxyMode.getValue()));
                }
            });
        }

        View m15 = view.findViewById(R.id.mode_15_min);
        if (m15 != null) m15.setOnClickListener(v -> setMode(900000L, "15 PHÚT"));
        View m30 = view.findViewById(R.id.mode_30_min);
        if (m30 != null) m30.setOnClickListener(v -> setMode(1800000L, "30 PHÚT"));
        View m60 = view.findViewById(R.id.mode_60_min);
        if (m60 != null) m60.setOnClickListener(v -> setMode(3600000L, "1 TIẾNG"));

        return view;
    }

    private void updateQuote() {
        if (tvQuoteText != null && tvQuoteAuthor != null) {
            Quote q = QUOTES.get(new Random().nextInt(QUOTES.size()));
            tvQuoteText.setText(q.text);
            tvQuoteAuthor.setText("— " + q.author);
        }
    }

    private void checkArgumentsAndSetTimer() {
        if (getArguments() != null && isBound && timerService != null) {
            String subject = getArguments().getString("subject_name");
            int minutes = getArguments().getInt("duration_minutes", 0);
            if (subject != null && minutes > 0) {
                setMode(minutes * 60000L, subject.toUpperCase());
                setArguments(null);
            }
        }
    }

    private void showCustomTimerDialog() {
        if (!isAdded()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thiết lập thời gian học");
        final EditText input = new EditText(requireContext());
        input.setHint("Số phút");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(60, 40, 60, 10);
        container.addView(input, params);
        builder.setView(container);
        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            try {
                String val = input.getText().toString().trim();
                if (!val.isEmpty()) {
                    int mins = Integer.parseInt(val);
                    if (mins > 0) setMode(mins * 60000L, "TÙY CHỈNH");
                }
            } catch (Exception e) {}
        });
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        input.requestFocus();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }, 200);
    }

    private void setMode(long duration, String label) {
        isFinished = false;
        currentDuration = duration;
        if (tvModeLabel != null) tvModeLabel.setText(label.toUpperCase());
        if (tvTimerDisplay != null) tvTimerDisplay.setText(formatTime(duration));
        if (isBound) timerService.resetTimer(duration, label.toUpperCase());
    }

    private void observeService() {
        if (timerService == null) return;
        timerService.timeRemaining.observe(getViewLifecycleOwner(), millis -> {
            if (millis != null) {
                if (tvTimerDisplay != null) tvTimerDisplay.setText(formatTime(millis));
                
                // Đồng bộ currentDuration khi đang chạy hoặc khi bị pause
                if (millis > 0) {
                    currentDuration = millis;
                }

                if (millis == 0 && !isFinished) {
                    if (Boolean.FALSE.equals(timerService.isRunning.getValue())) {
                        Long initial = timerService.initialDuration.getValue();
                        if (initial != null && initial > 0) {
                            isFinished = true;
                            updateBackground(Boolean.TRUE.equals(timerService.isGalaxyMode.getValue()), "SUCCESS");
                        }
                    }
                }
                Long initial = timerService.initialDuration.getValue();
                if (initial != null && initial > 0 && progressIndicator != null) 
                    progressIndicator.setProgress((int) ((float) millis / initial * 100));
            }
        });
        timerService.currentMode.observe(getViewLifecycleOwner(), mode -> {
            if (mode != null && tvModeLabel != null) tvModeLabel.setText(mode.toUpperCase());
        });
        timerService.isRunning.observe(getViewLifecycleOwner(), running -> {
            if (fabPlayPause != null) fabPlayPause.setImageResource(Boolean.TRUE.equals(running) ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        });
        timerService.isGalaxyMode.observe(getViewLifecycleOwner(), galaxy -> {
            if (!isFinished) updateBackground(Boolean.TRUE.equals(galaxy), "DEFAULT");
        });
    }

    private void updateBackground(boolean galaxyMode, String type) {
        if (rootLayout == null || !isAdded()) return;
        try {
            if ("SUCCESS".equals(type)) {
                rootLayout.setBackgroundColor(Color.parseColor("#FFF176"));
                if (tvTimerDisplay != null) tvTimerDisplay.setTextColor(Color.parseColor("#BF360C")); 
                showFireworks();
                playSuccessSound();
            } else if (galaxyMode) {
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {Color.parseColor("#020111"), Color.parseColor("#191621")});
                rootLayout.setBackground(gd);
                if (tvTimerDisplay != null) tvTimerDisplay.setTextColor(Color.WHITE);
                if (tvModeLabel != null) tvModeLabel.setTextColor(Color.parseColor("#B39DDB"));
            } else {
                rootLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background));
                if (tvTimerDisplay != null) tvTimerDisplay.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface));
                if (tvModeLabel != null) tvModeLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
            }
        } catch (Exception e) {}
    }

    private void showFireworks() {
        if (konfettiView == null) return;
        EmitterConfig config = new Emitter(5, TimeUnit.SECONDS).perSecond(100);
        konfettiView.start(
            new PartyFactory(config).angle(270).spread(90).setSpeedBetween(15f, 45f).position(new Position.Relative(0.0, 1.0)).build(),
            new PartyFactory(config).angle(270).spread(90).setSpeedBetween(15f, 45f).position(new Position.Relative(1.0, 1.0)).build(),
            new PartyFactory(new Emitter(5, TimeUnit.SECONDS).perSecond(120)).spread(360).setSpeedBetween(15f, 40f).position(new Position.Relative(0.5, 0.4)).build(),
            new PartyFactory(new Emitter(4, TimeUnit.SECONDS).perSecond(100)).spread(360).setSpeedBetween(10f, 30f).position(new Position.Relative(0.2, 0.5)).build(),
            new PartyFactory(new Emitter(4, TimeUnit.SECONDS).perSecond(100)).spread(360).setSpeedBetween(10f, 30f).position(new Position.Relative(0.8, 0.5)).build()
        );
    }

    private void playSuccessSound() {
        if (!isAdded()) return;
        try { 
            ringtone = RingtoneManager.getRingtone(getContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)); 
            if (ringtone != null) ringtone.play(); 
        } catch (Exception e) {}
    }

    private void stopSuccessSound() { 
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop(); 
    }

    private String formatTime(long millis) { 
        return String.format(Locale.getDefault(), "%02d:%02d", (int)(millis/1000)/60, (int)(millis/1000)%60); 
    }

    @Override public void onResume() { 
        super.onResume(); 
        quoteHandler.post(quoteRunnable); 
    }
    
    @Override public void onPause() { 
        super.onPause(); 
        quoteHandler.removeCallbacks(quoteRunnable); 
    }
    
    @Override public void onStart() { 
        super.onStart(); 
        if (getActivity() != null) {
            getActivity().bindService(new Intent(getActivity(), TimerService.class), connection, Context.BIND_AUTO_CREATE); 
        }
    }

    @Override public void onStop() { 
        super.onStop(); 
        if (isBound && getActivity() != null) { 
            getActivity().unbindService(connection); 
            isBound = false; 
        }
    }
}
