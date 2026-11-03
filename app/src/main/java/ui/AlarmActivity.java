package com.hatirlat.app.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hatirlat.app.R;
import com.hatirlat.app.data.AppDatabase;
import com.hatirlat.app.data.Reminder;
import com.hatirlat.app.service.AlarmService;
import com.hatirlat.app.utils.AlarmScheduler;

public class AlarmActivity extends AppCompatActivity {

    private Reminder reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_alarm);

        long reminderId = getIntent().getLongExtra("REMINDER_ID", -1);
        reminder = AppDatabase.getInstance(this).reminderDao().getById(reminderId);

        TextView tvTitle = findViewById(R.id.tvAlarmTitle);
        TextView tvDescription = findViewById(R.id.tvAlarmDescription);
        Button btnStop = findViewById(R.id.btnStopAlarm);
        Button btnSnooze = findViewById(R.id.btnSnoozeAlarm);

        if (reminder != null) {
            tvTitle.setText(reminder.title);
            tvDescription.setText(reminder.description);
        }

        btnStop.setOnClickListener(v -> stopAlarmAndClose());

        btnSnooze.setOnClickListener(v -> {
            snoozeAlarm(10); // 10 dakika ertele
            stopAlarmAndClose();
        });
    }

    private void snoozeAlarm(int minutes) {
        if (reminder != null) {
            reminder.targetDateTime = System.currentTimeMillis() + (minutes * 60 * 1000);
            AppDatabase.getInstance(this).reminderDao().update(reminder);
            AlarmScheduler.schedule(this, reminder);
        }
    }

    private void stopAlarmAndClose() {
        Intent serviceIntent = new Intent(this, AlarmService.class);
        stopService(serviceIntent);

        if (reminder != null && !"NONE".equals(reminder.repeatType)) {
            reminder.targetDateTime = AlarmScheduler.calculateNextRepeat(reminder);
            AppDatabase.getInstance(this).reminderDao().update(reminder);
            AlarmScheduler.schedule(this, reminder);
        }
        finish();
    }
}