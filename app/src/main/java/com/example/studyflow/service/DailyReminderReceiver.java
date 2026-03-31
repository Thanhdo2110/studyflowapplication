package com.example.studyflow.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.studyflow.R;
import com.example.studyflow.data.database.AppDatabase;
import com.example.studyflow.data.database.ExamDao;
import com.example.studyflow.data.database.entities.ExamEntity;
import com.example.studyflow.utils.NotificationHelper;
import java.util.Calendar;
import java.util.List;

public class DailyReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "DailyReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Tái đặt lịch cho ngày tiếp theo
        schedule(context);

        // Nếu là sự kiện báo thức (không phải chỉ là khởi động máy), thì hiện thông báo
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            showDailyNotification(context);
        }
    }

    private void showDailyNotification(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        ExamDao examDao = db.examDao();

        new Thread(() -> {
            try {
                List<ExamEntity> exams = examDao.getAllExamsSync();
                if (exams != null && !exams.isEmpty()) {
                    ExamEntity upcoming = exams.get(0);
                    long diff = upcoming.getExamDate() - System.currentTimeMillis();
                    long days = diff / (1000 * 60 * 60 * 24);
                    
                    if (days >= 0) {
                        String title = "StudyFlow: " + upcoming.getName();
                        String message = getMotivationalQuote(days);
                        showNotification(context, title, message);
                    } else {
                        // Nếu kỳ thi đã qua, hiện thông báo mặc định
                        showNotification(context, "StudyFlow", "Bắt đầu ngày mới đầy năng lượng cùng StudyFlow nhé!");
                    }
                } else {
                    // Hiển thị thông báo mặc định nếu không có kỳ thi nào để kiểm tra
                    showNotification(context, "StudyFlow", "Đừng quên lập kế hoạch học tập cho hôm nay nhé!");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error showing notification", e);
            }
        }).start();
    }

    public static void schedule(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, DailyReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 26);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Nếu thời điểm đã qua, hẹn cho ngày mai
        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
            Log.d(TAG, "Alarm scheduled for: " + calendar.getTime().toString());
        } catch (SecurityException e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.e(TAG, "SecurityException while scheduling alarm", e);
        }
    }

    private String getMotivationalQuote(long days) {
        if (days > 30) {
            return "Còn " + days + " ngày nữa. Hãy bắt đầu xây dựng nền tảng vững chắc từ hôm nay!";
        } else if (days > 14) {
            return "Chỉ còn " + days + " ngày. Tốc độ và sự kiên trì là chìa khóa thành công lúc này.";
        } else if (days > 7) {
            return "Còn " + days + " ngày! Thời gian vàng để bứt phá. Đừng để lãng phí bất kỳ phút giây nào.";
        } else if (days > 3) {
            return "CÁCH KỲ THI " + days + " NGÀY! Tập trung tối đa, lược bỏ mọi thứ không cần thiết. Bạn làm được!";
        } else if (days > 1) {
            return "CHỈ CÒN " + days + " NGÀY CUỐI CÙNG! Giữ vững tinh thần thép và ôn tập những trọng tâm cuối.";
        } else if (days == 1) {
            return "NGÀY MAI LÀ KỲ THI! Nghỉ ngơi đủ, tự tin vào những gì mình đã nỗ lực. Chiến thắng đang chờ bạn!";
        } else {
            return "Hôm nay là ngày thi! Bình tĩnh, tự tin và tỏa sáng hết mình nhé!";
        }
    }

    private void showNotification(Context context, String title, String message) {
        NotificationHelper.createNotificationChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_event)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1001, builder.build());
        }
    }
}
