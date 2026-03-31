package com.example.studyflow.ui.document;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.MainActivity;
import com.example.studyflow.R;
import com.example.studyflow.data.model.Document;
import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {
    private List<Document> documents;
    private List<Document> filteredList;

    public DocumentAdapter(List<Document> documents) {
        this.documents = documents;
        this.filteredList = new ArrayList<>(documents);
    }

    public void filter(String query, String subject) {
        filteredList.clear();
        for (Document doc : documents) {
            boolean matchesQuery = query.isEmpty() || doc.getTitle().toLowerCase().contains(query.toLowerCase());
            boolean matchesSubject = subject.equals("Tất cả") || doc.getTitle().contains(subject);
            
            if (matchesQuery && matchesSubject) {
                filteredList.add(doc);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Document doc = filteredList.get(position);
        holder.tvName.setText(doc.getTitle());
        holder.tvInfo.setText("Năm: " + doc.getYear() + " | Loại: " + doc.getType());
        
        holder.itemView.setOnClickListener(v -> {
            String url = doc.getUrl();
            if (url != null && !url.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                v.getContext().startActivity(intent);
            } else {
                Toast.makeText(v.getContext(), "Chưa có liên kết cho đề thi này", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnStudyNow.setOnClickListener(v -> {
            if (v.getContext() instanceof MainActivity) {
                MainActivity activity = (MainActivity) v.getContext();
                // Tách tên môn học từ title (ví dụ: "Đề thi thử Toán...")
                String subject = "Môn học";
                if (doc.getTitle().contains("Toán")) subject = "Toán học";
                else if (doc.getTitle().contains("Văn")) subject = "Ngữ văn";
                else if (doc.getTitle().contains("Anh")) subject = "Tiếng Anh";
                else if (doc.getTitle().contains("Lý")) subject = "Vật lý";
                else if (doc.getTitle().contains("Hóa")) subject = "Hóa học";
                
                activity.navigateToTimer(subject, 90);
                Toast.makeText(v.getContext(), "Bắt đầu làm bài: " + subject, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo;
        View btnStudyNow;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_doc_name);
            tvInfo = itemView.findViewById(R.id.tv_doc_info);
            btnStudyNow = itemView.findViewById(R.id.btn_study_now);
        }
    }
}
