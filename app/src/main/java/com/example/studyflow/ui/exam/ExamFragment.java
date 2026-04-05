package com.example.studyflow.ui.exam;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.R;
import com.example.studyflow.data.database.entities.ExamEntity;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ExamFragment extends Fragment implements ExamAdapter.OnExamClickListener {

    private ExamViewModel viewModel;
    private ExamAdapter adapter;
    private Calendar calendar = Calendar.getInstance();
    private EditText etName, etDate;
    private MaterialButton btnSave;
    private ExamEntity editingExam = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exam, container, false);

        etName = view.findViewById(R.id.et_exam_name);
        etDate = view.findViewById(R.id.et_exam_date);
        btnSave = view.findViewById(R.id.btn_save_exam);

        RecyclerView recyclerView = view.findViewById(R.id.rv_exams);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExamAdapter(this);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ExamViewModel.class);
        viewModel.getAllExams().observe(getViewLifecycleOwner(), exams -> {
            // Ép buộc cập nhật danh sách bằng cách tạo list mới
            adapter.submitList(new ArrayList<>(exams));
        });

        etDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên kỳ thi", Toast.LENGTH_SHORT).show();
                return;
            }
            
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            if (editingExam == null) {
                ExamEntity exam = new ExamEntity(name, calendar.getTimeInMillis(), "Chung", 0);
                viewModel.insert(exam);
            } else {
                ExamEntity updatedExam = new ExamEntity(name, calendar.getTimeInMillis(), 
                        editingExam.getCategory(), editingExam.getProgress());
                updatedExam.setId(editingExam.getId());
                
                viewModel.update(updatedExam);
                editingExam = null;
                btnSave.setText("Lưu");
            }
            
            etName.setText("");
            etDate.setText("");
            calendar = Calendar.getInstance();
        });

        return view;
    }

    private void showDatePicker() {
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etDate.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onEditClick(ExamEntity exam) {
        editingExam = exam;
        etName.setText(exam.getName());
        calendar.setTimeInMillis(exam.getExamDate());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etDate.setText(sdf.format(calendar.getTime()));
        btnSave.setText("Cập nhật");
    }

    @Override
    public void onDeleteClick(ExamEntity exam) {
        viewModel.delete(exam);
        if (editingExam != null && editingExam.getId() == exam.getId()) {
            editingExam = null;
            etName.setText("");
            etDate.setText("");
            btnSave.setText("Lưu");
        }
    }
}
