package com.example.studyflow.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static String formatDayMonthYear(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", new Locale("vi", "VN"));
        return sdf.format(new Date(timestamp));
    }

    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatElapsedTime(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }
}
