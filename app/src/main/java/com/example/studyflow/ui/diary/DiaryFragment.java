package com.example.studyflow.ui.diary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyflow.R;
import com.example.studyflow.data.database.entities.DiaryEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class DiaryFragment extends Fragment {

    private DiaryViewModel viewModel;
    private DiaryAdapter adapter;
    private TextInputEditText etContent;
    private MaterialButton btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        etContent = view.findViewById(R.id.et_diary_content);
        btnSave = view.findViewById(R.id.btn_save_diary);
        RecyclerView recyclerView = view.findViewById(R.id.rv_diary);

        viewModel = new ViewModelProvider(this).get(DiaryViewModel.class);
        adapter = new DiaryAdapter(diary -> viewModel.delete(diary));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getAllDiaries().observe(getViewLifecycleOwner(), diaries -> {
            adapter.submitList(diaries);
        });

        btnSave.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();
            if (!content.isEmpty()) {
                DiaryEntity diary = new DiaryEntity(content, System.currentTimeMillis());
                viewModel.insert(diary);
                etContent.setText("");
                Toast.makeText(getContext(), "Đã lưu nhật ký", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
