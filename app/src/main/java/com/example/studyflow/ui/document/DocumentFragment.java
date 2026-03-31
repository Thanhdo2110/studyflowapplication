package com.example.studyflow.ui.document;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.R;
import com.example.studyflow.data.model.Document;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class DocumentFragment extends Fragment {
    private String type;
    private String title;
    private DocumentAdapter adapter;
    private String currentSearch = "";
    private String currentSubject = "Tất cả";

    public static DocumentFragment newInstance(String type, String title) {
        DocumentFragment fragment = new DocumentFragment();
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString("type");
            title = getArguments().getString("title");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_document, container, false);
        
        TextView tvTitle = view.findViewById(R.id.tv_doc_title);
        tvTitle.setText(title);

        RecyclerView rv = view.findViewById(R.id.rv_documents);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        
        List<Document> docs = generateMockData(type);
        adapter = new DocumentAdapter(docs);
        rv.setAdapter(adapter);

        // Xử lý Tìm kiếm
        EditText etSearch = view.findViewById(R.id.et_search_docs);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString();
                adapter.filter(currentSearch, currentSubject);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý Lọc theo môn học
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_subjects);
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = view.findViewById(checkedId);
            if (chip != null) {
                currentSubject = chip.getText().toString();
                adapter.filter(currentSearch, currentSubject);
            }
        });

        return view;
    }

    private List<Document> generateMockData(String type) {
        List<Document> list = new ArrayList<>();
        String[] subjects = {"Toán", "Văn", "Anh", "Lý", "Hóa", "Sinh", "Sử", "Địa"};
        String defaultUrl = "https://vnexpress.net/giao-duc/tuyen-sinh/de-thi-dap-an";
        
        int count = 0;
        for (int year = 2025; year >= 2015; year--) {
            for (String sub : subjects) {
                if (count >= 100) break;
                String customUrl = defaultUrl;
                if (type.equals("THPT") && sub.equals("Toán") && year == 2024) {
                    customUrl = "https://vnexpress.net/de-thi-toan-tot-nghiep-thpt-2024-4762553.html";
                }
                list.add(new Document("Đề thi thử " + sub + " " + type + " " + year, String.valueOf(year), type, customUrl));
                count++;
            }
            if (count >= 100) break;
        }
        return list;
    }
}
