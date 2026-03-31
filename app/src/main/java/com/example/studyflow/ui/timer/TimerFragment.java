package com.example.studyflow.ui.timer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.example.studyflow.service.TimerService;
import com.example.studyflow.ui.history.HistoryViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class TimerFragment extends Fragment {

    private TimerService timerService;
    private boolean isBound = false;
    private HistoryViewModel historyViewModel;
    
    private TextView tvTimerDisplay, tvModeLabel, tvLabelStudyMode, tvLabelReset, tvLabelPause;
    private TextView tvQuoteText, tvQuoteAuthor;
    private LinearProgressIndicator progressIndicator;
    private FloatingActionButton fabPlayPause;
    private View btnReset, btnSaveSession;
    private View rootLayout;
    private KonfettiView konfettiView;
    private long currentDuration = 0L;
    private boolean isGalaxyMode = false;
    private boolean isFinished = false;

    private static final List<Quote> QUOTES = Arrays.asList(
        new Quote("“Hành trình vạn dặm bắt đầu từ một bước chân.”", "Lão Tử"),
        new Quote("“Thành công không phải là cuối cùng, thất bại không phải là tử địa: lòng can đảm đi tiếp mới quan trọng.”", "Winston Churchill"),
        new Quote("“Cách tốt nhất để dự đoán tương lai là tạo ra nó.”", "Abraham Lincoln"),
        new Quote("“Đừng đợi thời điểm hoàn hảo, hãy nắm lấy thời điểm và biến nó thành hoàn hảo.”", "Khuyết danh"),
        new Quote("“Nghị lực và kiên trì sẽ chiến thắng tất cả mọi thứ.”", "Benjamin Franklin"),
        new Quote("“Học tập là hạt giống của kiến thức, kiến thức là hạt giống của hạnh phúc.”", "Ngạn ngữ Gruzia"),
        new Quote("“Bất cứ khi nào bạn thấy một doanh nghiệp thành công, ai đó đã từng đưa ra một quyết định dũng cảm.”", "Peter Drucker"),
        new Quote("“Nếu bạn không thể bay, hãy chạy. Nếu không thể chạy, hãy đi bộ. Nếu không thể đi bộ, hãy bò.”", "Martin Luther King Jr."),
        new Quote("“Mọi công việc khó khăn đều có thành quả.”", "Châm ngôn 14:23"),
        new Quote("“Chỉ những người dám thất bại lớn mới có thể đạt được thành công lớn.”", "Robert F. Kennedy")
    );

    private static class Quote {
        String text;
        String author;
        Quote(String text, String author) {
            this.text = text;
            this.author = author;
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerBinder binder = (TimerService.TimerBinder) service;
            timerService = binder.getService();
            isBound = true;
            if (isAdded() && getView() != null) {
                observeService();
                updateBackground(isGalaxyMode ? "POMODORO" : "DEFAULT");
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

        rootLayout = view.findViewById(R.id.timer_root_layout);
        tvTimerDisplay = view.findViewById(R.id.tv_timer_display);
        tvModeLabel = view.findViewById(R.id.tv_mode_label);
        tvLabelStudyMode = view.findViewById(R.id.tv_label_study_mode);
        tvLabelReset = view.findViewById(R.id.tv_label_reset);
        tvLabelPause = view.findViewById(R.id.tv_label_pause);
        tvQuoteText = view.findViewById(R.id.tv_quote_text);
        tvQuoteAuthor = view.findViewById(R.id.tv_quote_author);
        konfettiView = view.findViewById(R.id.konfettiView);
        
        progressIndicator = view.findViewById(R.id.timer_progress);
        fabPlayPause = view.findViewById(R.id.fab_play_pause);
        btnReset = view.findViewById(R.id.btn_reset);
        btnSaveSession = view.findViewById(R.id.btn_save_session);

        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        showRandomQuote();

        fabPlayPause.setOnClickListener(v -> {
            if (isBound) {
                if (currentDuration <= 0) {
                    Toast.makeText(getContext(), "Hãy chọn thời gian trước", Toast.LENGTH_SHORT).show();
                    return;
                }
                isFinished = false;
                Boolean running = timerService.isRunning.getValue();
                if (running != null && running) {
                    timerService.pauseTimer();
                    showRandomQuote();
                } else {
                    timerService.startTimer(currentDuration, tvModeLabel.getText().toString());
                }
            }
        });

        btnReset.setOnClickListener(v -> {
            isFinished = false;
            if (isBound) timerService.resetTimer(currentDuration, tvModeLabel.getText().toString());
            updateBackground(isGalaxyMode ? "POMODORO" : "DEFAULT");
            showRandomQuote();
        });

        tvTimerDisplay.setOnClickListener(v -> showCustomTimerDialog());

        view.findViewById(R.id.mode_pomodoro).setOnClickListener(v -> {
            if (isFinished) return; 
            isGalaxyMode = !isGalaxyMode;
            updateBackground(isGalaxyMode ? "POMODORO" : "DEFAULT");
            String msg = isGalaxyMode ? "Đã bật không gian Galaxy" : "Đã tắt không gian Galaxy";
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.mode_15_min).setOnClickListener(v -> setMode(900000L, "15 PHÚT"));
        view.findViewById(R.id.mode_30_min).setOnClickListener(v -> setMode(1800000L, "30 PHÚT"));
        view.findViewById(R.id.mode_60_min).setOnClickListener(v -> setMode(3600000L, "1 TIẾNG"));

        btnSaveSession.setOnClickListener(v -> {
            if (isBound) {
                Long remaining = timerService.timeRemaining.getValue();
                Long initial = timerService.initialDuration.getValue();
                if (remaining != null && initial != null) {
                    int elapsedMinutes = (int) ((initial - remaining) / 60000);
                    if (elapsedMinutes > 0) {
                        HistoryEntity history = new HistoryEntity("Phiên học " + tvModeLabel.getText(), elapsedMinutes, System.currentTimeMillis());
                        historyViewModel.insert(history);
                        Toast.makeText(getContext(), "Đã lưu buổi học!", Toast.LENGTH_SHORT).show();
                        timerService.resetTimer(initial, tvModeLabel.getText().toString());
                        showRandomQuote();
                    } else {
                        Toast.makeText(getContext(), "Chưa đủ thời gian để lưu", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    private void showRandomQuote() {
        if (tvQuoteText != null && tvQuoteAuthor != null) {
            Quote quote = QUOTES.get(new Random().nextInt(QUOTES.size()));
            tvQuoteText.setText(quote.text);
            tvQuoteAuthor.setText("— " + quote.author);
        }
    }

    private void showCustomTimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Đặt thời gian (phút)");
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            try {
                int minutes = Integer.parseInt(input.getText().toString());
                if (minutes > 0) setMode(minutes * 60000L, "TÙY CHỈNH");
            } catch (Exception e) { }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void setMode(long duration, String label) {
        isFinished = false;
        currentDuration = duration;
        tvModeLabel.setText(label);
        updateBackground(isGalaxyMode ? "POMODORO" : "DEFAULT");
        if (isBound) timerService.resetTimer(duration, label);
        showRandomQuote();
    }

    private void updateBackground(String mode) {
        if (rootLayout == null || getContext() == null) return;
        
        if ("SUCCESS".equals(mode)) {
            GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {Color.parseColor("#FFF176"), Color.parseColor("#FFD54F")}
            );
            rootLayout.setBackground(gd);
            setUIColors(Color.parseColor("#BF360C"), false); 
            showFireworks();
        } else if ("POMODORO".equals(mode)) {
            GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {Color.parseColor("#020111"), Color.parseColor("#191621"), Color.parseColor("#20202c")}
            );
            rootLayout.setBackground(gd);
            setUIColors(Color.WHITE, true);
        } else {
            rootLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background));
            setUIColors(ContextCompat.getColor(requireContext(), R.color.on_surface), false);
        }
    }

    private void showFireworks() {
        EmitterConfig emitterConfig = new Emitter(5, java.util.concurrent.TimeUnit.SECONDS).perSecond(30);
        Party party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeedBetween(10f, 40f)
                .position(new Position.Relative(0.0, 0.5))
                .sizes(new Size(12, 5f, 0.2f))
                .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                .build();
        
        konfettiView.start(party);
        
        Party partyRight = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeedBetween(10f, 40f)
                .position(new Position.Relative(1.0, 0.5))
                .sizes(new Size(12, 5f, 0.2f))
                .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                .build();
        
        konfettiView.start(partyRight);
    }

    private void setUIColors(int color, boolean isDarkBg) {
        tvTimerDisplay.setTextColor(color);
        tvLabelStudyMode.setTextColor(color);
        tvLabelReset.setTextColor(color);
        tvLabelPause.setTextColor(color);
        tvModeLabel.setTextColor(isDarkBg ? Color.parseColor("#B39DDB") : ContextCompat.getColor(requireContext(), R.color.primary));
        
        if (isDarkBg) {
            progressIndicator.setTrackColor(Color.parseColor("#33FFFFFF"));
            progressIndicator.setIndicatorColor(Color.parseColor("#E040FB"));
        } else {
            progressIndicator.setTrackColor(ContextCompat.getColor(requireContext(), R.color.surface_container_high));
            progressIndicator.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.primary));
        }
    }

    private void observeService() {
        if (timerService == null) return;
        
        timerService.timeRemaining.observe(getViewLifecycleOwner(), millis -> {
            if (tvTimerDisplay != null) tvTimerDisplay.setText(formatTime(millis));
            
            if (millis == 0 && currentDuration > 0 && !isFinished) {
                Boolean running = timerService.isRunning.getValue();
                if (running != null && !running) {
                    isFinished = true;
                    updateBackground("SUCCESS");
                    Toast.makeText(getContext(), "🎉 Xuất sắc! Bạn đã hoàn thành mục tiêu!", Toast.LENGTH_LONG).show();
                }
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
