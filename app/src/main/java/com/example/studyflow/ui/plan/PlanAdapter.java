package com.example.studyflow.ui.plan;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.R;
import com.example.studyflow.data.database.entities.PlanEntity;
import com.google.android.material.button.MaterialButton;

public class PlanAdapter extends ListAdapter<PlanEntity, PlanAdapter.PlanViewHolder> {

    private final OnPlanClickListener listener;

    public interface OnPlanClickListener {
        void onCheckClick(PlanEntity plan);
    }

    public PlanAdapter(OnPlanClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<PlanEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<PlanEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull PlanEntity oldItem, @NonNull PlanEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull PlanEntity oldItem, @NonNull PlanEntity newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getDurationMinutes() == newItem.getDurationMinutes() &&
                    oldItem.isCompleted() == newItem.isCompleted();
        }
    };

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        PlanEntity plan = getItem(position);
        holder.bind(plan, listener);
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvDuration;
        private final MaterialButton btnComplete;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_plan_title);
            tvDuration = itemView.findViewById(R.id.tv_plan_duration);
            btnComplete = itemView.findViewById(R.id.btn_complete_task);
        }

        public void bind(PlanEntity plan, OnPlanClickListener listener) {
            tvTitle.setText(plan.getTitle());
            tvDuration.setText(plan.getDurationMinutes() + " phút");

            if (plan.isCompleted()) {
                btnComplete.setIconResource(R.drawable.ic_done_all);
                btnComplete.setIconTint(ContextCompat.getColorStateList(itemView.getContext(), R.color.primary));
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(0.5f);
            } else {
                btnComplete.setIconResource(R.drawable.ic_check_circle_outline);
                btnComplete.setIconTint(ContextCompat.getColorStateList(itemView.getContext(), R.color.outline_variant));
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setAlpha(1.0f);
            }

            btnComplete.setOnClickListener(v -> listener.onCheckClick(plan));
        }
    }
}
