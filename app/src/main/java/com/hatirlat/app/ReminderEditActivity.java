package com.hatirlat.app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class ReminderEditActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private Spinner spinnerCategory, spinnerRepeat;
    private CheckBox cbNotify1Day, cbNotify1Hour, cbNotifyAtTime;
    private Button btnSave;
    
    private long selectedDateTime = 0;
    private long reminderId = -1;
    private AppDatabase db;
    private Reminder existingReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_edit);

        db = AppDatabase.getInstance(this);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerRepeat = findViewById(R.id.spinnerRepeat);
        cbNotify1Day = findViewById(R.id.cbNotify1Day);
        cbNotify1Hour = findViewById(R.id.cbNotify1Hour);
        cbNotifyAtTime = findViewById(R.id.cbNotifyAtTime);
        btnSave = findViewById(R.id.btnSave);

        reminderId = getIntent().getLongExtra("REMINDER_ID", -1);
        if (reminderId != -1) {
            setTitle("Hatırlatmayı Düzenle");
            loadReminderData();
        } else {
            setTitle("Yeni Hatırlatma");
            selectedDateTime = System.currentTimeMillis() + 60000; // Varsayılan 1 dakika sonra
        }

        btnSave.setOnClickListener(v -> saveReminder());
    }

    private void loadReminderData() {
        new Thread(() -> {
            existingReminder = db.reminderDao().getReminderById(reminderId);
            if (existingReminder != null) {
                runOnUiThread(() -> {
                    etTitle.setText(existingReminder.title);
                    etDescription.setText(existingReminder.description);
                    selectedDateTime = existingReminder.targetDateTime;
                    cbNotify1Day.setChecked(existingReminder.notify1DayBefore);
                    cbNotify1Hour.setChecked(existingReminder.notify1HourBefore);
                    cbNotifyAtTime.setChecked(existingReminder.notifyAtTime);
                });
            }
        }).start();
    }

    private void saveReminder() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Lütfen bir başlık girin", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            Reminder reminder;
            if (reminderId != -1 && existingReminder != null) {
                reminder = existingReminder;
                reminder.title = title;
                reminder.description = description;
                reminder.targetDateTime = selectedDateTime;
                reminder.notify1DayBefore = cbNotify1Day.isChecked();
                reminder.notify1HourBefore = cbNotify1Hour.isChecked();
                reminder.notifyAtTime = cbNotifyAtTime.isChecked();
                db.reminderDao().update(reminder);
            } else {
                reminder = new Reminder();
                reminder.title = title;
                reminder.description = description;
                reminder.targetDateTime = selectedDateTime;
                reminder.notify1DayBefore = cbNotify1Day.isChecked();
                reminder.notify1HourBefore = cbNotify1Hour.isChecked();
                reminder.notifyAtTime = cbNotifyAtTime.isChecked();
                long newId = db.reminderDao().insert(reminder);
                reminder.id = newId;
            }

            // Alarmı kur
            if (!reminder.isCompleted && reminder.targetDateTime > System.currentTimeMillis()) {
                AlarmScheduler.scheduleAlarm(this, reminder);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Hatırlatma kaydedildi", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
