package com.hatirlat.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {

    private Reminder reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        TextView tvTitle = findViewById(R.id.tvAlarmTitle);
        TextView tvDescription = findViewById(R.id.tvAlarmDescription);
        Button btnDismiss = findViewById(R.id.btnDismissAlarm);

        long reminderId = getIntent().getLongExtra("REMINDER_ID", -1);

        if (reminderId != -1) {
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                reminder = db.reminderDao().getReminderById(reminderId);
                if (reminder != null) {
                    runOnUiThread(() -> {
                        tvTitle.setText(reminder.title);
                        tvDescription.setText(reminder.description);
                    });
                }
            }).start();
        }

        btnDismiss.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, AlarmService.class);
            stopService(serviceIntent);
            finish();
        });
    }
}