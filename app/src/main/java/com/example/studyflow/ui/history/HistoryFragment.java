package com.example.studyflow.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.R;
import java.util.Calendar;

public class HistoryFragment extends Fragment {

    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;
    private TextView tvTotalTime, tvRecentTitle, tvTotalLabel;
    private View[] barContainers = new View[7];
    private View[] barViews = new View[7];
    private TextView[] dayLabels = new TextView[7];
    private TextView[] barTimeLabels = new TextView[7];
    private String[] daysFull = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        tvTotalTime = view.findViewById(R.id.tv_total_time);
        tvTotalLabel = view.findViewById(R.id.tv_total_label);
        tvRecentTitle = view.findViewById(R.id.tv_recent_history_title);
        
        if (tvTotalLabel != null) {
            tvTotalLabel.setText("TỔNG THỜI GIAN TUẦN NÀY");
        }
        
        setupChartViews(view);

        RecyclerView recyclerView = view.findViewById(R.id.rv_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        
        viewModel.getFilteredHistory().observe(getViewLifecycleOwner(), historyList -> {
            adapter.submitList(historyList);
        });

        viewModel.getWeeklyTotalMinutes().observe(getViewLifecycleOwner(), totalMinutes -> {
            if (totalMinutes != null) {
                int hours = totalMinutes / 60;
                int minutes = totalMinutes % 60;
                tvTotalTime.setText(hours + "h " + minutes + "m");
            } else {
                tvTotalTime.setText("0h 0m");
            }
        });

        viewModel.getWeeklyData().observe(getViewLifecycleOwner(), data -> {
            updateChart(data, viewModel.getSelectedDayIndex().getValue());
        });

        viewModel.getSelectedDayIndex().observe(getViewLifecycleOwner(), index -> {
            if (index != null && index != -1) {
                tvRecentTitle.setText("Phiên học " + daysFull[index]);
            } else {
                tvRecentTitle.setText("Tất cả phiên học");
            }
            float[] data = viewModel.getWeeklyData().getValue();
            if (data != null) updateChart(data, index);
        });

        return view;
    }

    private void setupChartViews(View root) {
        int[] containerIds = {
            R.id.bar_container_1, R.id.bar_container_2, R.id.bar_container_3,
            R.id.bar_container_4, R.id.bar_container_5, R.id.bar_container_6, R.id.bar_container_7
        };
        String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

        for (int i = 0; i < 7; i++) {
            final int index = i;
            barContainers[i] = root.findViewById(containerIds[i]);
            barViews[i] = barContainers[i].findViewById(R.id.bar_view);
            dayLabels[i] = barContainers[i].findViewById(R.id.tv_day_label);
            barTimeLabels[i] = barContainers[i].findViewById(R.id.tv_bar_time);
            
            dayLabels[i].setText(days[i]);

            barContainers[i].setOnClickListener(v -> {
                if (viewModel.getSelectedDayIndex().getValue() != null && 
                    viewModel.getSelectedDayIndex().getValue() == index) {
                    viewModel.setSelectedDayIndex(-1);
                } else {
                    viewModel.setSelectedDayIndex(index);
                }
            });
        }
    }

    private void updateChart(float[] data, Integer selectedIndex) {
        float max = 0;
        for (float val : data) if (val > max) max = val;
        if (max == 0) max = 60;

        int maxHeightPx = (int) (140 * getResources().getDisplayMetrics().density);
        
        for (int i = 0; i < 7; i++) {
            float heightPercent = data[i] / max;
            int finalHeight = (int) (heightPercent * maxHeightPx);
            
            ViewGroup.LayoutParams params = barViews[i].getLayoutParams();
            params.height = Math.max((int)(8 * getResources().getDisplayMetrics().density), finalHeight);
            barViews[i].setLayoutParams(params);

            if (data[i] > 0) {
                int h = (int) data[i] / 60;
                int m = (int) data[i] % 60;
                String timeStr = h > 0 ? h + "h" + m + "m" : m + "m";
                barTimeLabels[i].setText(timeStr);
                barTimeLabels[i].setVisibility(View.VISIBLE);
            } else {
                barTimeLabels[i].setVisibility(View.INVISIBLE);
            }

            if (selectedIndex != null && selectedIndex == i) {
                // Sử dụng Drawable Gradient nổi bật cho cột được chọn
                barViews[i].setBackgroundResource(R.drawable.bg_chart_bar_selected);
                dayLabels[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
                barTimeLabels[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
                barTimeLabels[i].setAlpha(1.0f);
            } else {
                // Sử dụng Drawable Gradient nhẹ cho cột bình thường
                barViews[i].setBackgroundResource(R.drawable.bg_chart_bar_default);
                dayLabels[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface_variant));
                barTimeLabels[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface_variant));
                barTimeLabels[i].setAlpha(0.6f);
            }
        }
    }
}
