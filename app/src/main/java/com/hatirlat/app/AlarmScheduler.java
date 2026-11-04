package com.hatirlat.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.hatirlat.app.data.Reminder;
import com.hatirlat.app.receiver.AlarmReceiver;

import java.util.Calendar;

public class AlarmScheduler {

    public static void schedule(Context context, Reminder reminder) {
        if (reminder.isCompleted) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long now = System.currentTimeMillis();

        // Çoklu hatırlatma zamanları (Olay anı, 1 saat önce, 1 gün önce)
        if (reminder.notifyAtTime && reminder.targetDateTime > now) {
            setExactAlarm(context, alarmManager, reminder, reminder.targetDateTime, 0);
        }
        if (reminder.notify1HourBefore && (reminder.targetDateTime - 3600000) > now) {
            setExactAlarm(context, alarmManager, reminder, reminder.targetDateTime - 3600000, 1);
        }
        if (reminder.notify1DayBefore && (reminder.targetDateTime - 86400000) > now) {
            setExactAlarm(context, alarmManager, reminder, reminder.targetDateTime - 86400000, 2);
        }
    }

    private static void setExactAlarm(Context context, AlarmManager alarmManager, Reminder reminder, long triggerTime, int offsetType) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("REMINDER_ID", reminder.id);
        intent.putExtra("OFFSET_TYPE", offsetType);

        int requestCode = (int) (reminder.id * 10 + offsetType);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    public static void cancel(Context context, Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        for (int offsetType = 0; offsetType < 3; offsetType++) {
            Intent intent = new Intent(context, AlarmReceiver.class);
            int requestCode = (int) (reminder.id * 10 + offsetType);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    public static long calculateNextRepeat(Reminder reminder) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(reminder.targetDateTime);

        switch (reminder.repeatType) {
            case "DAILY":
                cal.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case "MONTHLY_5TH":
                cal.add(Calendar.MONTH, 1);
                cal.set(Calendar.DAY_OF_MONTH, 5);
                break;
            case "YEARLY":
                cal.add(Calendar.YEAR, 1);
                break;
        }
        return cal.getTimeInMillis();
    }
}
