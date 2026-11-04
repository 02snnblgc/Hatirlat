package com.hatirlat.app.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hatirlat.app.R;
import com.hatirlat.app.data.AppDatabase;
import com.hatirlat.app.data.Reminder;
import com.hatirlat.app.utils.AlarmScheduler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReminderEditActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etCategory;
    private TextView tvSelectedDate;
    private CheckBox cb1Day, cb1Hour, cbAtTime;
    private Spinner spinnerRepeat;
    private Button btnSelectRingtone, btnSave;

    private Calendar targetCalendar = Calendar.getInstance();
    private String selectedRingtoneUri = "";
    private long reminderId = -1;
    private Reminder existingReminder;
    private static final int RINGTONE_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_edit);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etCategory = findViewById(R.id.etCategory);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        cb1Day = findViewById(R.id.cb1DayBefore);
        cb1Hour = findViewById(R.id.cb1HourBefore);
        cbAtTime = findViewById(R.id.cbAtTime);
        spinnerRepeat = findViewById(R.id.spinnerRepeat);
        btnSelectRingtone = findViewById(R.id.btnSelectRingtone);
        btnSave = findViewById(R.id.btnSaveReminder);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.repeat_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeat.setAdapter(adapter);

        reminderId = getIntent().getLongExtra("REMINDER_ID", -1);
        if (reminderId != -1) {
            existingReminder = AppDatabase.getInstance(this).reminderDao().getById(reminderId);
            if (existingReminder != null) {
                etTitle.setText(existingReminder.title);
                etDescription.setText(existingReminder.description);
                etCategory.setText(existingReminder.category);
                targetCalendar.setTimeInMillis(existingReminder.targetDateTime);
                cb1Day.setChecked(existingReminder.notify1DayBefore);
                cb1Hour.setChecked(existingReminder.notify1HourBefore);
                cbAtTime.setChecked(existingReminder.notifyAtTime);
                selectedRingtoneUri = existingReminder.ringtoneUri;
            }
        }

        updateDateText();

        tvSelectedDate.setOnClickListener(v -> showDateTimePicker());

        btnSelectRingtone.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Alarm Melodisi Seç");
            startActivityForResult(intent, RINGTONE_REQUEST_CODE);
        });

        btnSave.setOnClickListener(v -> saveReminder());
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            targetCalendar.set(Calendar.YEAR, year);
            targetCalendar.set(Calendar.MONTH, month);
            targetCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                targetCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                targetCalendar.set(Calendar.MINUTE, minute);
                targetCalendar.set(Calendar.SECOND, 0);
                updateDateText();
            }, targetCalendar.get(Calendar.HOUR_OF_DAY), targetCalendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();

        }, targetCalendar.get(Calendar.YEAR), targetCalendar.get(Calendar.MONTH), targetCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        tvSelectedDate.setText(sdf.format(targetCalendar.getTime()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RINGTONE_REQUEST_CODE && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                selectedRingtoneUri = uri.toString();
                Toast.makeText(this, "Melodi seçildi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveReminder() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            etTitle.setError("Başlık boş olamaz");
            return;
        }

        Reminder reminder = existingReminder != null ? existingReminder : new Reminder();
        reminder.title = title;
        reminder.description = etDescription.getText().toString().trim();
        reminder.category = etCategory.getText().toString().trim();
        reminder.targetDateTime = targetCalendar.getTimeInMillis();
        reminder.notify1DayBefore = cb1Day.isChecked();
        reminder.notify1HourBefore = cb1Hour.isChecked();
        reminder.notifyAtTime = cbAtTime.isChecked();
        reminder.ringtoneUri = selectedRingtoneUri;

        int repeatPos = spinnerRepeat.getSelectedItemPosition();
        switch (repeatPos) {
            case 1: reminder.repeatType = "DAILY"; break;
            case 2: reminder.repeatType = "MONTHLY_5TH"; break;
            case 3: reminder.repeatType = "YEARLY"; break;
            default: reminder.repeatType = "NONE"; break;
        }

        AppDatabase db = AppDatabase.getInstance(this);
        if (existingReminder != null) {
            db.reminderDao().update(reminder);
            AlarmScheduler.cancel(this, reminder);
        } else {
            long newId = db.reminderDao().insert(reminder);
            reminder.id = newId;
        }

        AlarmScheduler.schedule(this, reminder);
        finish();
    }
}