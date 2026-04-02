package com.example.studyflow.ui.diary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyflow.R;
import com.example.studyflow.data.database.entities.DiaryEntity;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiaryAdapter extends ListAdapter<DiaryEntity, DiaryAdapter.DiaryViewHolder> {

    private OnDiaryClickListener listener;

    public interface OnDiaryClickListener {
        void onDeleteClick(DiaryEntity diary);
    }

    public DiaryAdapter(OnDiaryClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<DiaryEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<DiaryEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryEntity oldItem, @NonNull DiaryEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryEntity oldItem, @NonNull DiaryEntity newItem) {
            return oldItem.getContent().equals(newItem.getContent()) &&
                    oldItem.getTimestamp() == newItem.getTimestamp();
        }
    };

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate, tvContent;
        private MaterialButton btnDelete;
        private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_diary_date);
            tvContent = itemView.findViewById(R.id.tv_diary_content);
            btnDelete = itemView.findViewById(R.id.btn_delete_diary);
        }

        public void bind(DiaryEntity diary, OnDiaryClickListener listener) {
            tvDate.setText(sdf.format(new Date(diary.getTimestamp())));
            tvContent.setText(diary.getContent());
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(diary));
        }
    }
}
