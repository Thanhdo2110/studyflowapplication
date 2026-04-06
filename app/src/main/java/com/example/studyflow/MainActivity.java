package com.example.studyflow;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.studyflow.service.DailyReminderReceiver;
import com.example.studyflow.ui.AboutFragment;
import com.example.studyflow.ui.diary.DiaryFragment;
import com.example.studyflow.ui.exam.ExamFragment;
import com.example.studyflow.ui.history.HistoryFragment;
import com.example.studyflow.ui.home.HomeFragment;
import com.example.studyflow.ui.plan.PlanFragment;
import com.example.studyflow.ui.timer.TimerFragment;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityAd";
    private BottomNavigationView bottomNav;
    private DrawerLayout drawerLayout;
    private TextView tvToolbarTitle;
    private AdView adView;
    private InterstitialAd mInterstitialAd;
    
    // Quản lý tần suất quảng cáo
    private int actionCount = 0;
    private static final int ACTION_THRESHOLD = 5; // Hiện sau mỗi 5 lần click
    private long lastAdShowTime = 0;
    private static final long AD_COOLDOWN_MS = 120000; // Cách nhau ít nhất 2 phút (120000ms)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Khởi tạo Mobile Ads SDK
        MobileAds.initialize(this, initializationStatus -> {
            Log.d(TAG, "AdMob SDK Initialized");
            loadInterstitialAd();
        });

        initAdmob();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        drawerLayout = findViewById(R.id.drawer_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            View appBar = findViewById(R.id.toolbar).getParent() instanceof View ? (View) findViewById(R.id.toolbar).getParent() : findViewById(R.id.toolbar);
            appBar.setPadding(0, systemBars.top, 0, 0);
            findViewById(R.id.bottom_navigation).setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        ImageView btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = null;
            String title = "";

            if (id == R.id.nav_diary) {
                fragment = new DiaryFragment();
                title = "Nhật ký học tập";
            } else if (id == R.id.nav_about) {
                fragment = new AboutFragment();
                title = "Về StudyFlow";
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                tvToolbarTitle.setText(title);
                checkAndShowAd(); // Kiểm tra tần suất trước khi hiện
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        bottomNav = findViewById(R.id.bottom_navigation);
        setupBottomNavListener();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        DailyReminderReceiver.schedule(this);
    }

    private void initAdmob() {
        adView = findViewById(R.id.adView);
        if (adView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.interstitial_full_screen), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                mInterstitialAd = null;
                                lastAdShowTime = System.currentTimeMillis(); // Cập nhật thời điểm hiện ad
                                loadInterstitialAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                mInterstitialAd = null;
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    private void checkAndShowAd() {
        actionCount++;
        long currentTime = System.currentTimeMillis();
        
        // Điều kiện hiện: Đã qua 5 lần click VÀ cách lần cuối ít nhất 2 phút
        if (actionCount >= ACTION_THRESHOLD && (currentTime - lastAdShowTime) >= AD_COOLDOWN_MS) {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(this);
                actionCount = 0; // Reset bộ đếm
            }
        }
    }

    private void setupBottomNavListener() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;
            tvToolbarTitle.setText(R.string.app_name);

            if (itemId == R.id.nav_home) selectedFragment = new HomeFragment();
            else if (itemId == R.id.nav_timer) selectedFragment = new TimerFragment();
            else if (itemId == R.id.nav_plan) selectedFragment = new PlanFragment();
            else if (itemId == R.id.nav_exam) selectedFragment = new ExamFragment();
            else if (itemId == R.id.nav_history) selectedFragment = new HistoryFragment();

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                checkAndShowAd(); // Kiểm tra tần suất trước khi hiện
            }
            return true;
        });
    }

    public void navigateToTimer(String subject, int duration) {
        TimerFragment timerFragment = new TimerFragment();
        Bundle args = new Bundle();
        args.putString("subject_name", subject);
        args.putInt("duration_minutes", duration);
        timerFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, timerFragment)
                .commit();
        
        bottomNav.setOnItemSelectedListener(null);
        bottomNav.setSelectedItemId(R.id.nav_timer);
        setupBottomNavListener();
    }

    @Override
    public void onPause() {
        if (adView != null) adView.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) adView.resume();
    }

    @Override
    public void onDestroy() {
        if (adView != null) adView.destroy();
        super.onDestroy();
    }
}
