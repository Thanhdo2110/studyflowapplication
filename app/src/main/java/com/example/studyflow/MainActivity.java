package com.example.studyflow;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.studyflow.ui.exam.ExamFragment;
import com.example.studyflow.ui.history.HistoryFragment;
import com.example.studyflow.ui.home.HomeFragment;
import com.example.studyflow.ui.plan.PlanFragment;
import com.example.studyflow.ui.settings.SettingsFragment;
import com.example.studyflow.ui.timer.TimerFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Cấu hình thanh trạng thái để icon hệ thống (pin, sóng) dễ nhìn trên nền sáng
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // Xử lý Insets để Toolbar không bị dính vào Status Bar và BottomNav không bị dính vào Navigation Bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load profile image using Glide
        ImageView ivProfile = findViewById(R.id.iv_main_profile);
        Glide.with(this)
                .load("https://lh3.googleusercontent.com/aida-public/AB6AXuA5WBBHPLP6pZxfNRyBWZzWxcyX0C1y-6rAAu5cUjpLr7G3BrHJrqVo-wamqqIlHXz0Y_WpEwfjvE8HBJcStgS3SmsuWPIqvw-WaO96rPHDqdBcPpYQmk4HEJ8Y6dePBXDZxz84PX00Mxq-0AnLzCj10g6hkIB3kKnr9I0aYd_oe1LEajFTZuNDvcnRWHn7ULlcfiWm0EgpS1exZA65bUT2bSw2fMBcfGcMjblslPuiFCqHww8RINaAMq9enqV_bNbHOeJnZ-n9m8s")
                .placeholder(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(ivProfile);

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_timer) {
                selectedFragment = new TimerFragment();
            } else if (itemId == R.id.nav_plan) {
                selectedFragment = new PlanFragment();
            } else if (itemId == R.id.nav_exam) {
                selectedFragment = new ExamFragment();
            } else if (itemId == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }
}
