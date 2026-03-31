package com.example.studyflow.ui.home;

import android.os.Bundle;
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
import com.example.studyflow.R;
import com.example.studyflow.data.database.entities.ExamEntity;
import com.example.studyflow.data.database.entities.PlanEntity;
import com.example.studyflow.ui.plan.PlanAdapter;
import com.example.studyflow.ui.timer.TimerFragment;
import com.example.studyflow.ui.plan.PlanFragment;
import com.example.studyflow.ui.exam.ExamFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private PlanAdapter planAdapter;
    private TextView tvHeroExamName, tvHeroDays, tvPlanStats, tvNextExamPreview;
    private LinearProgressIndicator heroProgress;
    private CircularProgressIndicator cpPlanProgress;
    private MaterialButton btnStartTimer;
    private TextView tvViewAllPlans;
    private MaterialCardView cardDailyGoal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvHeroExamName = view.findViewById(R.id.tv_hero_exam_name);
        tvHeroDays = view.findViewById(R.id.tv_hero_days);
        tvPlanStats = view.findViewById(R.id.tv_home_plan_stats);
        tvNextExamPreview = view.findViewById(R.id.tv_next_exam_preview);
        heroProgress = view.findViewById(R.id.hero_progress);
        cpPlanProgress = view.findViewById(R.id.cp_home_plan_progress);
        btnStartTimer = view.findViewById(R.id.btn_home_start_timer);
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
            public void onEditClick(PlanEntity plan) {
            }

            @Override
            public void onDeleteClick(PlanEntity plan) {
            }
        }, false);
        rvPlans.setAdapter(planAdapter);

        viewModel.getAllExams().observe(getViewLifecycleOwner(), exams -> {
            if (exams != null && !exams.isEmpty()) {
                ExamEntity upcoming = exams.get(0);
                tvHeroExamName.setText(upcoming.getName());
                long diff = upcoming.getExamDate() - System.currentTimeMillis();
                long days = diff / (1000 * 60 * 60 * 24);
                tvHeroDays.setText(String.valueOf(Math.max(0, (int)days)));
                heroProgress.setProgress(upcoming.getProgress());

                if (exams.size() > 1 && tvNextExamPreview != null) {
                    ExamEntity nextExam = exams.get(1);
                    long nextDiff = nextExam.getExamDate() - System.currentTimeMillis();
                    long nextDays = nextDiff / (1000 * 60 * 60 * 24);
                    String previewText = "Tiếp theo: " + nextExam.getName() + " (" + Math.max(0, (int)nextDays) + " ngày)";
                    tvNextExamPreview.setText(previewText);
                    tvNextExamPreview.setVisibility(View.VISIBLE);
                } else if (tvNextExamPreview != null) {
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
                
                if (cpPlanProgress != null && plans.size() > 0) {
                    cpPlanProgress.setMax(plans.size());
                    cpPlanProgress.setProgress(completed);
                }
            }
        });

        btnStartTimer.setOnClickListener(v -> navigateToFragment(new TimerFragment(), R.id.nav_timer));
        tvViewAllPlans.setOnClickListener(v -> navigateToFragment(new PlanFragment(), R.id.nav_plan));
        cardDailyGoal.setOnClickListener(v -> navigateToFragment(new PlanFragment(), R.id.nav_plan));
        view.findViewById(R.id.card_hero_exam).setOnClickListener(v -> navigateToFragment(new ExamFragment(), R.id.nav_exam));

        return view;
    }

    private void navigateToFragment(Fragment fragment, int navId) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setReorderingAllowed(true)
                .commit();
        
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(navId);
        }
    }
}
