package com.example.studyflow.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.MainActivity;
import com.example.studyflow.R;
import com.example.studyflow.data.database.entities.ExamEntity;
import com.example.studyflow.data.database.entities.PlanEntity;
import com.example.studyflow.ui.plan.PlanAdapter;
import com.example.studyflow.ui.plan.PlanFragment;
import com.example.studyflow.ui.exam.ExamFragment;
import com.example.studyflow.ui.timetable.TimetableFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private PlanAdapter planAdapter;
    private TextView tvHeroExamName, tvPlanStats, tvNextExamPreview;
    private TextView tvCountdownDays, tvCountdownHours, tvCountdownMinutes;
    private LinearProgressIndicator heroProgress;
    private CircularProgressIndicator cpPlanProgress;
    private MaterialButton btnStartTimer;
    private TextView tvViewAllPlans, btnTimetable;
    private MaterialCardView cardDailyGoal;
    
    private final Handler timerHandler = new Handler();
    private ExamEntity currentUpcomingExam = null;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            updateCountdownUI();
            timerHandler.postDelayed(this, 60000); 
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvHeroExamName = view.findViewById(R.id.tv_hero_exam_name);
        tvCountdownDays = view.findViewById(R.id.tv_countdown_days);
        tvCountdownHours = view.findViewById(R.id.tv_countdown_hours);
        tvCountdownMinutes = view.findViewById(R.id.tv_countdown_minutes);
        tvNextExamPreview = view.findViewById(R.id.tv_next_exam_preview);
        
        tvPlanStats = view.findViewById(R.id.tv_home_plan_stats);
        heroProgress = view.findViewById(R.id.hero_progress);
        cpPlanProgress = view.findViewById(R.id.cp_home_plan_progress);
        btnStartTimer = view.findViewById(R.id.btn_home_start_timer);
        btnTimetable = view.findViewById(R.id.btn_home_timetable);
        tvViewAllPlans = view.findViewById(R.id.tv_home_view_all_plans);
        cardDailyGoal = view.findViewById(R.id.card_daily_goal);

        RecyclerView rvPlans = view.findViewById(R.id.rv_home_plans);
        rvPlans.setLayoutManager(new LinearLayoutManager(getContext()));
        
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        planAdapter = new PlanAdapter(new PlanAdapter.OnPlanClickListener() {
            @Override
            public void onCheckClick(PlanEntity plan) {
                viewModel.updatePlan(plan);
            }

            @Override
            public void onEditClick(PlanEntity plan) {}

            @Override
            public void onDeleteClick(PlanEntity plan) {
                viewModel.deletePlan(plan);
            }

            @Override
            public void onItemClick(PlanEntity plan) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToTimer(plan.getTitle(), plan.getDurationMinutes());
                }
            }
        }, false);
        rvPlans.setAdapter(planAdapter);

        viewModel.getAllExams().observe(getViewLifecycleOwner(), exams -> {
            if (exams != null && !exams.isEmpty()) {
                currentUpcomingExam = exams.get(0);
                updateCountdownUI();
                
                if (exams.size() > 1) {
                    ExamEntity nextExam = exams.get(1);
                    long nextDiff = nextExam.getExamDate() - System.currentTimeMillis();
                    long nextDays = nextDiff / (1000 * 60 * 60 * 24);
                    String previewText = "Tiếp theo: " + nextExam.getName() + " (" + Math.max(0, (int)nextDays) + " ngày)";
                    tvNextExamPreview.setText(previewText);
                    tvNextExamPreview.setVisibility(View.VISIBLE);
                } else {
                    tvNextExamPreview.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getTodayPlans().observe(getViewLifecycleOwner(), plans -> {
            if (plans != null) {
                planAdapter.submitList(plans.size() > 3 ? plans.subList(0, 3) : plans);
                int completed = 0;
                for (PlanEntity p : plans) if (p.isCompleted()) completed++;
                tvPlanStats.setText(completed + "/" + plans.size());
                if (cpPlanProgress != null) {
                    cpPlanProgress.setMax(plans.size() > 0 ? plans.size() : 1);
                    cpPlanProgress.setProgress(completed);
                }
            }
        });

        btnStartTimer.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTimer(null, 0);
            }
        });

        if (btnTimetable != null) {
            btnTimetable.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TimetableFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        tvViewAllPlans.setOnClickListener(v -> navigateToFragment(new PlanFragment(), R.id.nav_plan));
        cardDailyGoal.setOnClickListener(v -> navigateToFragment(new PlanFragment(), R.id.nav_plan));
        view.findViewById(R.id.card_hero_exam).setOnClickListener(v -> navigateToFragment(new ExamFragment(), R.id.nav_exam));

        timerHandler.post(timerRunnable);

        return view;
    }

    private void updateCountdownUI() {
        if (currentUpcomingExam != null) {
            tvHeroExamName.setText(currentUpcomingExam.getName());
            
            long diff = currentUpcomingExam.getExamDate() - System.currentTimeMillis();
            if (diff < 0) diff = 0;

            long days = diff / (1000 * 60 * 60 * 24);
            long hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
            long minutes = (diff % (1000 * 60 * 60)) / (1000 * 60);

            tvCountdownDays.setText(String.format("%02d", days));
            tvCountdownHours.setText(String.format("%02d", hours));
            tvCountdownMinutes.setText(String.format("%02d", minutes));
            
            heroProgress.setProgress(currentUpcomingExam.getProgress());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void navigateToFragment(Fragment fragment, int navId) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.findViewById(R.id.bottom_navigation).post(() -> {
                ((com.google.android.material.bottomnavigation.BottomNavigationView) 
                    activity.findViewById(R.id.bottom_navigation)).setSelectedItemId(navId);
            });
        }
    }
}
