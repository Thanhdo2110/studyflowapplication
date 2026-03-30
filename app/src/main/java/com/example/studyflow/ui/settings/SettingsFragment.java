package com.example.studyflow.ui.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.studyflow.R;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private TextView tvName, tvEmail, tvDisplayName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        tvName = view.findViewById(R.id.tv_settings_name);
        tvEmail = view.findViewById(R.id.tv_settings_email);
        tvDisplayName = view.findViewById(R.id.tv_settings_display_name);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        viewModel.userName.observe(getViewLifecycleOwner(), name -> {
            if (tvName != null) tvName.setText(name);
            if (tvDisplayName != null) tvDisplayName.setText(name);
        });

        viewModel.userEmail.observe(getViewLifecycleOwner(), email -> {
            if (tvEmail != null) tvEmail.setText(email);
        });

        view.findViewById(R.id.icon_name).setOnClickListener(v -> showEditDialog("Đổi tên", viewModel.userName.getValue(), true));
        view.findViewById(R.id.icon_email).setOnClickListener(v -> showEditDialog("Đổi Email", viewModel.userEmail.getValue(), false));

        return view;
    }

    private void showEditDialog(String title, String currentValue, boolean isName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);

        final EditText input = new EditText(getContext());
        input.setText(currentValue);
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newValue = input.getText().toString().trim();
            if (!newValue.isEmpty()) {
                if (isName) {
                    viewModel.saveUserName(newValue);
                } else {
                    viewModel.saveUserEmail(newValue);
                }
                Toast.makeText(getContext(), "Đã cập nhật", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
