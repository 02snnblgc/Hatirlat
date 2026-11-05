package com.hatirlat.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long reminderId = intent.getLongExtra("REMINDER_ID", -1);

        Intent activityIntent = new Intent(context, AlarmActivity.class);
        activityIntent.putExtra("REMINDER_ID", reminderId);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(activityIntent);

        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra("REMINDER_ID", reminderId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
