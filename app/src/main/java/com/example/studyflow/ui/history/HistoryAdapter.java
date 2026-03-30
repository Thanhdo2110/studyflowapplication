package com.example.studyflow.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.R;
import com.example.studyflow.data.database.entities.HistoryEntity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryAdapter extends ListAdapter<HistoryEntity, HistoryAdapter.HistoryViewHolder> {

    public HistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<HistoryEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<HistoryEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull HistoryEntity oldItem, @NonNull HistoryEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull HistoryEntity oldItem, @NonNull HistoryEntity newItem) {
            return oldItem.getTaskTitle().equals(newItem.getTaskTitle()) &&
                    oldItem.getDurationMinutes() == newItem.getDurationMinutes() &&
                    oldItem.getCompletedTimestamp() == newItem.getCompletedTimestamp();
        }
    };

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryEntity history = getItem(position);
        holder.bind(history);
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvDate, tvDuration;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_history_title);
            tvDate = itemView.findViewById(R.id.tv_history_date);
            tvDuration = itemView.findViewById(R.id.tv_history_duration);
        }

        public void bind(HistoryEntity history) {
            tvTitle.setText(history.getTaskTitle());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy • hh:mm a", Locale.getDefault());
            tvDate.setText(sdf.format(new Date(history.getCompletedTimestamp())));
            
            int hours = history.getDurationMinutes() / 60;
            int minutes = history.getDurationMinutes() % 60;
            if (hours > 0) {
                tvDuration.setText(hours + "h " + minutes + "m");
            } else {
                tvDuration.setText(minutes + "m");
            }
        }
    }
}
