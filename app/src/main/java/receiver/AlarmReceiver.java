package com.hatirlat.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.hatirlat.app.service.AlarmService;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long reminderId = intent.getLongExtra("REMINDER_ID", -1);
        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra("REMINDER_ID", reminderId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}