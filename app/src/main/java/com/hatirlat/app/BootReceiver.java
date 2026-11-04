package com.hatirlat.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Telefon yeniden başladığında alarmları tekrar kur
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(context);
                List<Reminder> reminders = db.reminderDao().getAllReminders();
                for (Reminder reminder : reminders) {
                    if (!reminder.isCompleted && reminder.targetDateTime > System.currentTimeMillis()) {
                        AlarmScheduler.scheduleAlarm(context, reminder);
                    }
                }
            });
        }
    }
}