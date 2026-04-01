package com.example.studyflow.ui.exam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.R;
import com.example.studyflow.data.database.entities.ExamEntity;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExamAdapter extends ListAdapter<ExamEntity, ExamAdapter.ExamViewHolder> {

    private final OnExamClickListener listener;

    public interface OnExamClickListener {
        void onEditClick(ExamEntity exam);
        void onDeleteClick(ExamEntity exam);
    }

    public ExamAdapter(OnExamClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ExamEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<ExamEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull ExamEntity oldItem, @NonNull ExamEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ExamEntity oldItem, @NonNull ExamEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getExamDate() == newItem.getExamDate() &&
                    oldItem.getProgress() == newItem.getProgress();
        }
    };

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        ExamEntity exam = getItem(position);
        holder.bind(exam, listener);
    }

    static class ExamViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvDate, tvDays;
        private final CircularProgressIndicator progressIndicator;
        private final ImageView ivMore;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_exam_name);
            tvDate = itemView.findViewById(R.id.tv_exam_date);
            tvDays = itemView.findViewById(R.id.tv_days_remaining);
            progressIndicator = itemView.findViewById(R.id.progress_exam);
            ivMore = itemView.findViewById(R.id.iv_exam_more);
        }

        public void bind(ExamEntity exam, OnExamClickListener listener) {
            tvName.setText(exam.getName());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", new Locale("vi", "VN"));
            tvDate.setText(sdf.format(new Date(exam.getExamDate())));
            
            // Logic tính ngày chính xác (đã chuẩn hóa 00:00:00)
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0); today.set(Calendar.SECOND, 0); today.set(Calendar.MILLISECOND, 0);

            Calendar examDate = Calendar.getInstance();
            examDate.setTimeInMillis(exam.getExamDate());
            examDate.set(Calendar.HOUR_OF_DAY, 0); examDate.set(Calendar.MINUTE, 0); examDate.set(Calendar.SECOND, 0); examDate.set(Calendar.MILLISECOND, 0);

            long diff = examDate.getTimeInMillis() - today.getTimeInMillis();
            int days = (int) (diff / (1000 * 60 * 60 * 24));

            tvDays.setText(String.valueOf(Math.max(0, days)));
            progressIndicator.setProgress(exam.getProgress());

            if (ivMore != null) {
                ivMore.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(v.getContext(), v);
                    popup.getMenuInflater().inflate(R.menu.menu_item_options, popup.getMenu());

                    if (popup.getMenu().findItem(R.id.action_edit) != null) {
                        popup.getMenu().findItem(R.id.action_edit).setVisible(false);
                    }

                    popup.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_delete) {
                            if (listener != null) listener.onDeleteClick(exam);
                            return true;
                        }
                        return false;
                    });
                    popup.show();
                });
            }
        }
    }
}
