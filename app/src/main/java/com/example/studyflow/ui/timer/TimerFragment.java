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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
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

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class TimerFragment extends Fragment {

    private static final String TAG = "TimerFragment";
    private TimerService timerService;
    private boolean isBound = false;
    private HistoryViewModel historyViewModel;
    private PlanRepository planRepository;
    private Ringtone ringtone;
    
    private TextView tvTimerDisplay, tvModeLabel, tvLabelStudyMode, tvLabelReset, tvLabelPause;
    private TextView tvQuoteText, tvQuoteAuthor;
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
            showRandomQuote();
            quoteHandler.postDelayed(this, 30000);
        }
    };

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
        new Quote("“Chỉ những người dám thất bại lớn mới có thể đạt được thành công lớn.”", "Robert F. Kennedy"),
        new Quote("“Thanh xuân giống như một cơn mưa rào, dù cho bạn từng bị cảm lạnh vì tắm mưa thì bạn vẫn muốn được đắm mình trong nó lần nữa.”", "Cửu Bả Đao"),
        new Quote("“Trên con đường thành công không có dấu chân của kẻ lười biếng.”", "Lỗ Tấn"),
        new Quote("“Nếu bạn ngủ ngay lúc này, bạn sẽ nằm mơ. Nếu bạn học ngay lúc này, bạn sẽ giải thích được giấc mơ.”", "Ngạn ngữ Harvard"),
        new Quote("“Học tập là cuốn hộ chiếu cho tương lai, vì ngày mai thuộc về những người chuẩn bị cho nó từ hôm nay.”", "Malcolm X"),
        new Quote("“Hãy học khi người khác đang ngủ, lao động khi người khác còn lười nhác, chuẩn bị khi người khác đang chơi bời.”", "William Arthur Ward"),
        new Quote("“Giáo dục là vũ khí mạnh nhất mà người ta có thể sử dụng để thay đổi cả thế giới.”", "Nelson Mandela"),
        new Quote("“Sự học như đi thuyền trên dòng nước ngược, không tiến ắt sẽ lùi.”", "Ngạn ngữ Trung Hoa"),
        new Quote("“Rễ của giáo dục thì đắng, nhưng quả của nó thì ngọt.”", "Aristotle"),
        new Quote("“Thiên tài 1% là cảm hứng và 99% là mồ hôi.”", "Thomas Edison"),
        new Quote("“Đừng bao giờ ngừng học hỏi vì cuộc đời không bao giờ ngừng dạy bảo.”", "Khuyết danh"),
        new Quote("“Hãy sống như thể ngày mai bạn sẽ chết, hãy học như thể bạn sẽ sống mãi mãi.”", "Mahatma Gandhi"),
        new Quote("“Đừng xấu hổ khi không biết, chỉ xấu hổ khi không học.”", "Ngạn ngữ Nga"),
        new Quote("“Đầu tư vào tri thức đem lại lợi nhuận cao nhất.”", "Benjamin Franklin"),
        new Quote("“Học không biết chán, dạy người không biết mỏi.”", "Khổng Tử"),
        new Quote("“Tuổi trẻ không có lý tưởng giống như buổi sáng không có mặt trời.”", "Belinsky"),
        new Quote("“Mục tiêu của giáo dục là soi sáng tâm hồn con người.”", "M.P. Mason"),
        new Quote("“Học tập là một kho báu sẽ đi theo chủ nhân của nó tới mọi nơi.”", "Ngạn ngữ Trung Hoa"),
        new Quote("“Trí tuệ con người trưởng thành trong tĩnh lặng, còn tính cách trưởng thành trong b bão táp.”", "Goethe"),
        new Quote("“Đọc một cuốn sách hay cũng giống như trò chuyện với một bộ óc tuyệt vời nhất.”", "René Descartes"),
        new Quote("“Trường học chỉ cho ta chìa khóa tri thức, học trong cuộc sống là công việc cả đời.”", "Bill Gates"),
        new Quote("“Những gì chúng ta biết hôm nay sẽ lỗi thời vào ngày mai. Đừng ngừng học hỏi.”", "Dorothy Billington"),
        new Quote("“Tri thức là sức mạnh.”", "Francis Bacon"),
        new Quote("“Không có kho báu nào quý giá bằng tri thức.”", "Ali ibn Abi Talib"),
        new Quote("“Thành công là một cuộc hành trình chứ không phải là điểm đến.”", "Arthur Ashe"),
        new Quote("“Học, học nữa, học mãi.”", "Lenin"),
        new Quote("“Nghệ thuật dạy học chính là nghệ thuật giúp ai đó khám phá ra điều gì đó.”", "Mark Van Doren"),
        new Quote("“Người thầy cầm tay, mở ra trí óc và chạm đến trái tim.”", "Khuyết danh"),
        new Quote("“Cuốn sách tốt nhất cho bạn là cuốn sách nói với bạn nhiều nhất khi bạn đọc nó.”", "Ralph Waldo Emerson"),
        new Quote("“Thời gian bạn dùng để học tập hôm nay chính là số tiền bạn đếm được trong tương lai.”", "Khuyết danh"),
        new Quote("“Đại học không phải con đường duy nhất, nhưng là con đường ngắn nhất dẫn tới thành công.”", "Khuyết danh")
    );

    private static class Quote {
        final String text;
        final String author;
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
                checkArgumentsAndSetTimer();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            timerService = null;
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
        View btnReset = view.findViewById(R.id.btn_reset);
        View btnSaveSession = view.findViewById(R.id.btn_save_session);

        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        planRepository = new PlanRepository(requireActivity().getApplication());

        showRandomQuote();

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
                Boolean running = timerService.isRunning.getValue();
                if (Boolean.TRUE.equals(running)) {
                    timerService.pauseTimer();
                } else {
                    if (isFinished) {
                        Toast.makeText(getContext(), "Vui lòng nhấn Hoàn thành trước khi bắt đầu phiên mới!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Long timeRemaining = timerService.timeRemaining.getValue();
                    if (currentDuration <= 0 && (timeRemaining == null || timeRemaining <= 0)) {
                        Toast.makeText(getContext(), "Hãy chọn thời gian trước", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateBackground(Boolean.TRUE.equals(timerService.isGalaxyMode.getValue()), "DEFAULT");
                    Intent intent = new Intent(getActivity(), TimerService.class);
                    ContextCompat.startForegroundService(requireContext(), intent);
                    long durationToStart = (currentDuration > 0) ? currentDuration : (timeRemaining != null ? timeRemaining : 0);
                    timerService.startTimer(durationToStart, tvModeLabel.getText().toString());
                    currentDuration = 0;
                }
            }
        });

        btnReset.setOnClickListener(v -> {
            stopSuccessSound();
            if (isBound && timerService != null) {
                isFinished = false;
                Long initial = timerService.initialDuration.getValue();
                long duration = (currentDuration > 0) ? currentDuration : (initial != null ? initial : 0);
                timerService.resetTimer(duration, tvModeLabel.getText().toString());
                updateBackground(Boolean.TRUE.equals(timerService.isGalaxyMode.getValue()), "DEFAULT");
            }
            showRandomQuote();
        });

        tvTimerDisplay.setOnClickListener(v -> {
            if (isFinished) {
                Toast.makeText(getContext(), "Vui lòng nhấn Hoàn thành trước khi đặt thời gian mới!", Toast.LENGTH_SHORT).show();
                return;
            }
            showCustomTimerDialog();
        });

        view.findViewById(R.id.mode_pomodoro).setOnClickListener(v -> {
            if (isFinished) return; 
            if (isBound && timerService != null) {
                boolean newMode = !Boolean.TRUE.equals(timerService.isGalaxyMode.getValue());
                timerService.setGalaxyMode(newMode);
                Toast.makeText(getContext(), newMode ? "Đã bật không gian Galaxy" : "Đã tắt không gian Galaxy", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.mode_15_min).setOnClickListener(v -> setMode(900000L, "15 PHÚT"));
        view.findViewById(R.id.mode_30_min).setOnClickListener(v -> setMode(1800000L, "30 PHÚT"));
        view.findViewById(R.id.mode_60_min).setOnClickListener(v -> setMode(3600000L, "1 TIẾNG"));

        btnSaveSession.setOnClickListener(v -> {
            stopSuccessSound();
            if (isBound && timerService != null) {
                Long remaining = timerService.timeRemaining.getValue();
                Long initial = timerService.initialDuration.getValue();
                String mode = timerService.currentMode.getValue();
                if (remaining != null && initial != null) {
                    int elapsedMinutes = (int) ((initial - remaining) / 60000);
                    if (elapsedMinutes > 0 || isFinished) {
                        HistoryEntity history = new HistoryEntity("Học " + mode, 
                                Math.max(1, elapsedMinutes), System.currentTimeMillis());
                        historyViewModel.insert(history);

                        if (mode != null && !mode.isEmpty() && !mode.equals("CHỌN CHẾ ĐỘ") && !mode.equals("TÙY CHỈNH")) {
                            planRepository.markPlanAsCompleted(mode);
                        }

                        Toast.makeText(getContext(), "Đã lưu buổi học!", Toast.LENGTH_SHORT).show();
                        
                        isFinished = false;
                        timerService.resetTimer(initial, mode);
                        updateBackground(Boolean.TRUE.equals(timerService.isGalaxyMode.getValue()), "DEFAULT");
                    } else {
                        Toast.makeText(getContext(), "Chưa đủ thời gian để lưu", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

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

    private void playSuccessSound() {
        try {
            stopSuccessSound();
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            ringtone = RingtoneManager.getRingtone(requireContext().getApplicationContext(), notification);
            if (ringtone != null) {
                ringtone.play();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing sound", e);
        }
    }

    private void stopSuccessSound() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    private void showRandomQuote() {
        if (tvQuoteText != null && tvQuoteAuthor != null && isAdded()) {
            Quote quote = QUOTES.get(new Random().nextInt(QUOTES.size()));
            tvQuoteText.setText(quote.text);
            tvQuoteAuthor.setText(getString(R.string.quote_author_format, quote.author));
        }
    }

    private void showCustomTimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thiết lập phiên học");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputSubject = new EditText(requireContext());
        inputSubject.setHint("Nhập môn học (vd: Toán, Lý, Anh...)");
        inputSubject.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(inputSubject);

        final EditText inputMinutes = new EditText(requireContext());
        inputMinutes.setHint("Nhập số phút học");
        inputMinutes.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputMinutes);

        builder.setView(layout);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            try {
                String subject = inputSubject.getText().toString().trim();
                String minutesStr = inputMinutes.getText().toString().trim();
                
                if (subject.isEmpty()) subject = "TÙY CHỈNH";
                int minutes = Integer.parseInt(minutesStr);
                
                if (minutes > 0) {
                    setMode(minutes * 60000L, subject.toUpperCase());
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Vui lòng nhập số phút hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void setMode(long duration, String label) {
        if (isFinished) {
            Toast.makeText(getContext(), "Vui lòng nhấn Hoàn thành trước khi chọn phiên mới!", Toast.LENGTH_SHORT).show();
            return;
        }
        currentDuration = duration;
        tvModeLabel.setText(label);
        if (tvTimerDisplay != null) tvTimerDisplay.setText(formatTime(duration));
        if (isBound && timerService != null) {
            timerService.resetTimer(duration, label);
            updateBackground(Boolean.TRUE.equals(timerService.isGalaxyMode.getValue()), "DEFAULT");
        }
    }

    private void updateBackground(boolean galaxyMode, String type) {
        if (rootLayout == null || !isAdded()) return;
        
        if ("SUCCESS".equals(type)) {
            GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {Color.parseColor("#FFF176"), Color.parseColor("#FFD54F")}
            );
            rootLayout.setBackground(gd);
            setUIColors(Color.parseColor("#BF360C"), false); 
            showFireworks();
            playSuccessSound();
        } else if (galaxyMode) {
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
        if (konfettiView == null) return;
        EmitterConfig emitterConfig = new Emitter(5, TimeUnit.SECONDS).perSecond(100);
        Party partyLeft = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeedBetween(10f, 40f)
                .position(new Position.Relative(0.0, 0.5))
                .sizes(new Size(12, 5f, 0.2f))
                .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                .build();
        Party partyRight = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeedBetween(10f, 40f)
                .position(new Position.Relative(1.0, 0.5))
                .sizes(new Size(12, 5f, 0.2f))
                .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                .build();
        konfettiView.start(partyLeft);
        konfettiView.start(partyRight);
    }

    private void setUIColors(int color, boolean isDarkBg) {
        if (!isAdded()) return;
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
            
            if (millis == 0 && !isFinished && Boolean.TRUE.equals(timerService.isRunning.getValue()) == false) {
                Long initial = timerService.initialDuration.getValue();
                if (initial != null && initial > 0) {
                    isFinished = true;
                    updateBackground(Boolean.TRUE.equals(timerService.isGalaxyMode.getValue()), "SUCCESS");
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
                fabPlayPause.setImageResource(Boolean.TRUE.equals(isRunning) ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
            }
        });

        timerService.isGalaxyMode.observe(getViewLifecycleOwner(), galaxy -> {
            if (!isFinished) {
                updateBackground(Boolean.TRUE.equals(galaxy), "DEFAULT");
            }
        });

        timerService.currentMode.observe(getViewLifecycleOwner(), mode -> {
            if (mode != null && tvModeLabel != null) tvModeLabel.setText(mode);
        });
    }

    private String formatTime(long millis) {
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    public void onResume() {
        super.onResume();
        quoteHandler.postDelayed(quoteRunnable, 30000);
    }

    @Override
    public void onPause() {
        super.onPause();
        quoteHandler.removeCallbacks(quoteRunnable);
        stopSuccessSound();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), TimerService.class);
            getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBound && getActivity() != null) {
            getActivity().unbindService(connection);
            isBound = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopSuccessSound();
        rootLayout = null;
        tvTimerDisplay = null;
        tvModeLabel = null;
        tvLabelStudyMode = null;
        tvLabelReset = null;
        tvLabelPause = null;
        tvQuoteText = null;
        tvQuoteAuthor = null;
        konfettiView = null;
        progressIndicator = null;
        fabPlayPause = null;
    }
}
