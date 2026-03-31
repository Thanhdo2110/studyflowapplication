package com.example.studyflow.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.studyflow.R;

public class NotificationHelper {
    public static final String CHANNEL_ID = "study_flow_channel";
    public static final int TIMER_NOTIFICATION_ID = 1;
    public static final int FINISHED_NOTIFICATION_ID = 2;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Study Flow Notifications";
            String description = "Channel for timer and exam reminders";
            // Chỉnh lên IMPORTANCE_DEFAULT để có âm thanh cho thông báo nhắc nhở
            int importance = NotificationManager.IMPORTANCE_DEFAULT; 
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void showTimerFinishedNotification(Context context, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(FINISHED_NOTIFICATION_ID, builder.build());
        }
    }
}
