package com.hatirlat.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.hatirlat.app.data.AppDatabase;
import com.hatirlat.app.data.Reminder;
import com.hatirlat.app.utils.AlarmScheduler;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || 
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            
            AppDatabase db = AppDatabase.getInstance(context);
            List<Reminder> activeReminders = db.reminderDao().getActiveReminders();
            for (Reminder r : activeReminders) {
                AlarmScheduler.schedule(context, r);
            }
        }
    }
}