package com.example.studyflow.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.R;

public class HistoryFragment extends Fragment {

    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;
    private TextView tvTotalTime;
    private View[] bars = new View[7];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        tvTotalTime = view.findViewById(R.id.tv_total_time);
        
        bars[0] = view.findViewById(R.id.bar_1);
        bars[1] = view.findViewById(R.id.bar_2);
        bars[2] = view.findViewById(R.id.bar_3);
        bars[3] = view.findViewById(R.id.bar_4);
        bars[4] = view.findViewById(R.id.bar_5);
        bars[5] = view.findViewById(R.id.bar_6);
        bars[6] = view.findViewById(R.id.bar_7);

        RecyclerView recyclerView = view.findViewById(R.id.rv_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        
        viewModel.getAllHistory().observe(getViewLifecycleOwner(), historyList -> {
            adapter.submitList(historyList);
        });

        viewModel.getTotalMinutes().observe(getViewLifecycleOwner(), totalMinutes -> {
            if (totalMinutes != null) {
                int hours = totalMinutes / 60;
                int minutes = totalMinutes % 60;
                tvTotalTime.setText(hours + "h " + minutes + "m");
            } else {
                tvTotalTime.setText("0h 0m");
            }
        });

        viewModel.getWeeklyData().observe(getViewLifecycleOwner(), data -> {
            updateChart(data);
        });

        return view;
    }

    private void updateChart(float[] data) {
        float max = 0;
        for (float val : data) if (val > max) max = val;
        if (max == 0) max = 1; // Tránh chia cho 0

        int maxHeightPx = 400; // Chiều cao tối đa của bar trong chart container

        for (int i = 0; i < 7; i++) {
            float heightPercent = data[i] / max;
            int finalHeight = (int) (heightPercent * maxHeightPx);
            
            ViewGroup.LayoutParams params = bars[i].getLayoutParams();
            params.height = Math.max(20, finalHeight); // Tối thiểu 20px để bar không biến mất
            bars[i].setLayoutParams(params);
        }
    }
}
