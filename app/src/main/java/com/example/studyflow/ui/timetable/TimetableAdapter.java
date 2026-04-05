package com.example.studyflow.ui.timetable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.R;
import com.example.studyflow.data.database.entities.TimetableEntity;
import java.util.ArrayList;
import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SECTION = 0;
    private static final int TYPE_ROW = 1;

    private List<TimetableEntity> data = new ArrayList<>();
    private final List<Object> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onSubjectClick(int dayOfWeek, int period, String currentSubject, String type);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TimetableAdapter() {
        setupItems();
    }

    private void setupItems() {
        items.clear();
        items.add("BUỔI SÁNG");
        for (int i = 1; i <= 5; i++) items.add(new PeriodRow(i, "MORNING"));
        items.add("BUỔI CHIỀU");
        for (int i = 1; i <= 5; i++) items.add(new PeriodRow(i + 5, "AFTERNOON"));
        items.add("BUỔI TỐI");
        for (int i = 1; i <= 3; i++) items.add(new PeriodRow(i + 10, "EVENING"));
    }

    public void setTimetableData(List<TimetableEntity> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_SECTION : TYPE_ROW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SECTION) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timetable_section, parent, false);
            return new SectionViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timetable_row, parent, false);
            return new RowViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SectionViewHolder) {
            ((SectionViewHolder) holder).tvTitle.setText((String) items.get(position));
        } else if (holder instanceof RowViewHolder) {
            PeriodRow row = (PeriodRow) items.get(position);
            RowViewHolder vh = (RowViewHolder) holder;

            // Hiển thị số tiết (1, 2, 3...)
            int displayNumber = row.period > 10 ? row.period - 10 : (row.period > 5 ? row.period - 5 : row.period);
            vh.tvPeriod.setText(String.valueOf(displayNumber));

            // Xóa dữ liệu cũ
            vh.tvMon.setText(""); vh.tvTue.setText(""); vh.tvWed.setText("");
            vh.tvThu.setText(""); vh.tvFri.setText(""); vh.tvSat.setText("");

            // Điền môn học từ database
            for (TimetableEntity entity : data) {
                if (entity.getPeriod() == row.period) {
                    switch (entity.getDayOfWeek()) {
                        case 2: vh.tvMon.setText(entity.getSubject()); break;
                        case 3: vh.tvTue.setText(entity.getSubject()); break;
                        case 4: vh.tvWed.setText(entity.getSubject()); break;
                        case 5: vh.tvThu.setText(entity.getSubject()); break;
                        case 6: vh.tvFri.setText(entity.getSubject()); break;
                        case 7: vh.tvSat.setText(entity.getSubject()); break;
                    }
                }
            }

            // Click listener
            setupCellClick(vh.tvMon, 2, row.period, row.type);
            setupCellClick(vh.tvTue, 3, row.period, row.type);
            setupCellClick(vh.tvWed, 4, row.period, row.type);
            setupCellClick(vh.tvThu, 5, row.period, row.type);
            setupCellClick(vh.tvFri, 6, row.period, row.type);
            setupCellClick(vh.tvSat, 7, row.period, row.type);
        }
    }

    private void setupCellClick(TextView tv, int day, int period, String type) {
        tv.setOnClickListener(v -> {
            if (listener != null) listener.onSubjectClick(day, period, tv.getText().toString(), type);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        SectionViewHolder(View v) { super(v); tvTitle = v.findViewById(R.id.tv_section_title); }
    }

    static class RowViewHolder extends RecyclerView.ViewHolder {
        TextView tvPeriod, tvMon, tvTue, tvWed, tvThu, tvFri, tvSat;
        RowViewHolder(View v) {
            super(v);
            tvPeriod = v.findViewById(R.id.tv_period_number);
            tvMon = v.findViewById(R.id.tv_mon); tvTue = v.findViewById(R.id.tv_tue);
            tvWed = v.findViewById(R.id.tv_wed); tvThu = v.findViewById(R.id.tv_thu);
            tvFri = v.findViewById(R.id.tv_fri); tvSat = v.findViewById(R.id.tv_sat);
        }
    }

    static class PeriodRow {
        int period; String type;
        PeriodRow(int p, String t) { this.period = p; this.type = t; }
    }
}
