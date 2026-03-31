package com.example.studyflow.ui.plan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.MainActivity;
import com.example.studyflow.R;
import com.example.studyflow.data.database.entities.PlanEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.List;

public class PlanFragment extends Fragment implements PlanAdapter.OnPlanClickListener {

    private PlanViewModel viewModel;
    private PlanAdapter adapter;
    private EditText etTitle, etDuration;
    private MaterialButton btnSave;
    private LinearProgressIndicator progressIndicator;
    private TextView tvProgressLabel;
    private PlanEntity editingPlan = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan, container, false);

        etTitle = view.findViewById(R.id.et_plan_title);
        etDuration = view.findViewById(R.id.et_plan_duration);
        btnSave = view.findViewById(R.id.btn_save_plan);
        progressIndicator = view.findViewById(R.id.plan_progress);
        tvProgressLabel = view.findViewById(R.id.tv_progress_label);

        RecyclerView recyclerView = view.findViewById(R.id.rv_plans);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PlanAdapter(this);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(PlanViewModel.class);
        viewModel.getTodayPlans().observe(getViewLifecycleOwner(), plans -> {
            adapter.submitList(plans);
            updateProgress(plans);
        });

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String durationStr = etDuration.getText().toString().trim();

            if (title.isEmpty() || durationStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int duration = Integer.parseInt(durationStr);
                if (duration <= 0) {
                    Toast.makeText(getContext(), "Thời lượng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (editingPlan != null) {
                    editingPlan.setTitle(title);
                    editingPlan.setDurationMinutes(duration);
                    viewModel.update(editingPlan);
                    editingPlan = null;
                    btnSave.setText("Thêm kế hoạch");
                } else {
                    PlanEntity plan = new PlanEntity(title, duration, false, System.currentTimeMillis());
                    viewModel.insert(plan);
                }

                etTitle.setText("");
                etDuration.setText("");
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Thời lượng không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void updateProgress(List<PlanEntity> plans) {
        if (plans == null || plans.isEmpty()) {
            if (progressIndicator != null) progressIndicator.setProgress(0);
            if (tvProgressLabel != null) tvProgressLabel.setText("0/0 tasks completed");
            return;
        }

        int completed = 0;
        for (PlanEntity plan : plans) {
            if (plan.isCompleted()) completed++;
        }

        int progress = (int) ((float) completed / plans.size() * 100);
        if (progressIndicator != null) progressIndicator.setProgress(progress);
        if (tvProgressLabel != null) tvProgressLabel.setText(completed + "/" + plans.size() + " tasks completed");
    }

    @Override
    public void onCheckClick(PlanEntity plan) {
        viewModel.update(plan);
    }

    @Override
    public void onEditClick(PlanEntity plan) {
    }

    @Override
    public void onDeleteClick(PlanEntity plan) {
        viewModel.delete(plan);
        Toast.makeText(getContext(), "Đã xóa kế hoạch", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(PlanEntity plan) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToTimer(plan.getTitle(), plan.getDurationMinutes());
        }
    }
}
