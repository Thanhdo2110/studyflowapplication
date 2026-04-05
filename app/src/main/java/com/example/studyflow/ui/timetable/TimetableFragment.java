package com.example.studyflow.ui.timetable;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.R;

public class TimetableFragment extends Fragment {

    private TimetableViewModel viewModel;
    private TimetableAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rv_timetable);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new TimetableAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(TimetableViewModel.class);
        viewModel.getAllTimetable().observe(getViewLifecycleOwner(), list -> {
            adapter.setTimetableData(list);
        });

        adapter.setOnItemClickListener((dayOfWeek, period, currentSubject, type) -> {
            showEditDialog(dayOfWeek, period, currentSubject, type);
        });

        view.findViewById(R.id.btn_back).setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void showEditDialog(int dayOfWeek, int period, String currentSubject, String type) {
        String dayName = "Thứ " + dayOfWeek;
        String sectionName = type.equals("MORNING") ? "Sáng" : (type.equals("AFTERNOON") ? "Chiều" : "Tối");
        int displayNumber = (period <= 5) ? period : (period <= 10 ? period - 5 : period - 10);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        
        // Custom Title View
        TextView titleView = new TextView(getContext());
        titleView.setText(dayName + " - " + sectionName + " - Tiết " + displayNumber);
        titleView.setPadding(60, 40, 60, 0);
        titleView.setTextSize(18);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        builder.setCustomTitle(titleView);

        final EditText input = new EditText(getContext());
        input.setText(currentSubject);
        input.setHint("Nhập môn học (Toán, Văn...)");
        
        // Padding for the EditText container
        android.widget.FrameLayout container = new android.widget.FrameLayout(getContext());
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 60;
        params.rightMargin = 60;
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newSubject = input.getText().toString().trim();
            viewModel.saveSubject(dayOfWeek, period, newSubject, type);
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
