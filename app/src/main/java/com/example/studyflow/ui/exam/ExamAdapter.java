package com.example.studyflow.ui.exam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.R;
import com.example.studyflow.data.database.entities.ExamEntity;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExamAdapter extends ListAdapter<ExamEntity, ExamAdapter.ExamViewHolder> {

    public ExamAdapter() {
        super(DIFF_CALLBACK);
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
        holder.bind(exam);
    }

    static class ExamViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvDate, tvDays, tvDaysLabel;
        private final CircularProgressIndicator progressIndicator;
        private final ImageView ivIcon;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_exam_name);
            tvDate = itemView.findViewById(R.id.tv_exam_date);
            tvDays = itemView.findViewById(R.id.tv_days_remaining);
            tvDaysLabel = itemView.findViewById(R.id.tv_days_label);
            progressIndicator = itemView.findViewById(R.id.progress_exam);
            ivIcon = itemView.findViewById(R.id.iv_exam_icon);
        }

        public void bind(ExamEntity exam) {
            tvName.setText(exam.getName());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", new Locale("vi", "VN"));
            tvDate.setText(sdf.format(new Date(exam.getExamDate())));
            
            long diff = exam.getExamDate() - System.currentTimeMillis();
            long days = diff / (1000 * 60 * 60 * 24);
            tvDays.setText(String.valueOf(Math.max(0, days)));
            
            progressIndicator.setProgress(exam.getProgress());
            
            // Customize icon/color based on category if needed
        }
    }
}
