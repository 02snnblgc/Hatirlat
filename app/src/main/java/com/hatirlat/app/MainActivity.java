package com.hatirlat.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private AppDatabase db;
    private SearchView searchView;
    private FloatingActionButton fabAdd;
    private final List<Reminder> reminderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        fabAdd = findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReminderAdapter(reminderList, new ReminderAdapter.OnReminderClickListener() {
            @Override
            public void onReminderClick(Reminder reminder) {
                Intent intent = new Intent(MainActivity.this, ReminderEditActivity.class);
                intent.putExtra("REMINDER_ID", reminder.id);
                startActivity(intent);
            }

            @Override
            public void onCompleteClick(Reminder reminder) {
                reminder.isCompleted = !reminder.isCompleted;
                Executors.newSingleThreadExecutor().execute(() -> {
                    db.reminderDao().update(reminder);
                    if (reminder.isCompleted) {
                        AlarmScheduler.cancelAlarm(MainActivity.this, reminder);
                    } else {
                        AlarmScheduler.scheduleAlarm(MainActivity.this, reminder);
                    }
                    loadReminders();
                });
            }
        });

        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReminderEditActivity.class);
            startActivity(intent);
        });

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filterReminders(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterReminders(newText);
                    return true;
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
    }

    private void loadReminders() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Reminder> reminders = db.reminderDao().getAllReminders();
            runOnUiThread(() -> {
                if (adapter != null) {
                    adapter.updateList(reminders);
                }
            });
        });
    }

    private void filterReminders(String query) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Reminder> filtered;
            if (query == null || query.trim().isEmpty()) {
                filtered = db.reminderDao().getAllReminders();
            } else {
                filtered = db.reminderDao().searchReminders(query.trim());
            }
            runOnUiThread(() -> {
                if (adapter != null) {
                    adapter.updateList(filtered);
                }
            });
        });
    }
}
