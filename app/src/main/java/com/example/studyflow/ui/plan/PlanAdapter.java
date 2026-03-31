package com.example.studyflow.ui.plan;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.R;
import com.example.studyflow.data.database.entities.PlanEntity;
import com.google.android.material.button.MaterialButton;

public class PlanAdapter extends ListAdapter<PlanEntity, PlanAdapter.PlanViewHolder> {

    private final OnPlanClickListener listener;
    private final boolean showOptions;

    public interface OnPlanClickListener {
        void onCheckClick(PlanEntity plan);
        void onEditClick(PlanEntity plan);
        void onDeleteClick(PlanEntity plan);
        void onItemClick(PlanEntity plan); // Thêm sự kiện click vào item
    }

    public PlanAdapter(OnPlanClickListener listener) {
        this(listener, true);
    }

    public PlanAdapter(OnPlanClickListener listener, boolean showOptions) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.showOptions = showOptions;
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
        holder.bind(plan, listener, showOptions);
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvDuration;
        private final MaterialButton btnComplete;
        private final ImageView ivMore;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_plan_title);
            tvDuration = itemView.findViewById(R.id.tv_plan_duration);
            btnComplete = itemView.findViewById(R.id.btn_complete_task);
            ivMore = itemView.findViewById(R.id.iv_plan_more);
        }

        public void bind(PlanEntity plan, OnPlanClickListener listener, boolean showOptions) {
            updateUI(plan);

            // Click vào toàn bộ item để hẹn giờ
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(plan);
            });

            ivMore.setVisibility(showOptions ? View.VISIBLE : View.GONE);

            btnComplete.setOnClickListener(v -> {
                plan.setCompleted(!plan.isCompleted());
                updateUI(plan);
                if (listener != null) listener.onCheckClick(plan);
            });

            if (showOptions) {
                ivMore.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(v.getContext(), v);
                    popup.getMenuInflater().inflate(R.menu.menu_item_options, popup.getMenu());
                    popup.getMenu().findItem(R.id.action_edit).setVisible(false);

                    if (plan.isCompleted()) {
                        popup.getMenu().findItem(R.id.action_delete).setVisible(false);
                    }

                    popup.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_delete) {
                            if (listener != null) listener.onDeleteClick(plan);
                            return true;
                        }
                        return false;
                    });
                    
                    if (popup.getMenu().findItem(R.id.action_delete).isVisible()) {
                        popup.show();
                    }
                });
            }
        }

        private void updateUI(PlanEntity plan) {
            tvTitle.setText(plan.getTitle());
            tvDuration.setText(plan.getDurationMinutes() + " phút");

            if (plan.isCompleted()) {
                btnComplete.setIconResource(R.drawable.ic_check_circle);
                btnComplete.setIconTint(ContextCompat.getColorStateList(itemView.getContext(), R.color.primary));
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(0.5f);
            } else {
                btnComplete.setIconResource(R.drawable.ic_check_circle_outline);
                btnComplete.setIconTint(ContextCompat.getColorStateList(itemView.getContext(), R.color.outline_variant));
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setAlpha(1.0f);
            }
        }
    }
}
