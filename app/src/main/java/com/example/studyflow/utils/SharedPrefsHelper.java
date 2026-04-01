package com.example.studyflow.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsHelper {
    private static final String PREF_NAME = "study_flow_prefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_WEEKLY_GOAL = "weekly_goal_minutes";

    private final SharedPreferences prefs;

    public SharedPrefsHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Minh Quân");
    }

    public void setUserEmail(String email) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "quan.m@university.edu");
    }

    public void setDarkMode(boolean isEnabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, isEnabled).apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setWeeklyGoal(int minutes) {
        prefs.edit().putInt(KEY_WEEKLY_GOAL, minutes).apply();
    }

    public int getWeeklyGoal() {
        // Mặc định là 10 giờ (600 phút) nếu chưa thiết lập
        return prefs.getInt(KEY_WEEKLY_GOAL, 600);
    }
}
