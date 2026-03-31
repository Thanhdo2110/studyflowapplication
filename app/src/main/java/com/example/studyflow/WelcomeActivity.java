package com.example.studyflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kích hoạt lại tính năng chỉ hiện một lần
        SharedPreferences prefs = getSharedPreferences("StudyFlowPrefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);
        if (!isFirstRun) {
            startMainActivity();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load logo StudyFlow
        ImageView ivLogo = findViewById(R.id.iv_logo_main);
        Glide.with(this)
                .load("https://lh3.googleusercontent.com/aida-public/AB6AXuAZe737H11z7v2qX2W_vL-87YpGndkGzU-8oYvO8-eY8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8-e8")
                .into(ivLogo);

        // Load ảnh minh họa
        ImageView ivHero = findViewById(R.id.iv_hero);
        Glide.with(this)
                .load("https://lh3.googleusercontent.com/aida-public/AB6AXuALwah5azj5IxJnFliDHE8Qc_0GwzW2kZhozoBLyxxi-nOkHLGNGuDBIQTkxHOQ6y3Aa7tsCjiA9W1HpyAJY7B5R2ruq6VrbSzGx47lSgn8O5nOGJ2yxD78IirHnwxNMUjFfpyuv2U4TZ8si58Hg2Vdkg_4qm8Cz0wNzrx2o_43Rel8nPRzKwnkMTR4cI41oiJC4cBEABFptm8ZNgKA4VnxeG6VeV3VOuMjfS4GbNUER9I3Qzc44Eiry87in25UgrmxjjjUB4TU2BI")
                .centerCrop()
                .into(ivHero);

        MaterialButton btnGetStarted = findViewById(R.id.btn_get_started);
        btnGetStarted.setOnClickListener(v -> {
            // Lưu trạng thái đã xem màn hình chào mừng
            prefs.edit().putBoolean("isFirstRun", false).apply();
            startMainActivity();
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
