package com.hatirlat.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hatirlat.app.R;
import com.hatirlat.app.data.AppDatabase;
import com.hatirlat.app.data.Reminder;
import com.hatirlat.app.utils.AlarmScheduler;
import com.hatirlat.app.utils.JsonBackupUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ReminderAdapter.OnReminderClickListener {

    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private AppDatabase database;
    private static final int PICK_BACKUP_FILE = 100;
    private static final int CREATE_BACKUP_FILE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = AppDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recyclerViewReminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabAdd = findViewById(R.id.fabAddReminder);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReminderEditActivity.class);
            startActivity(intent);
        });

        checkBatteryOptimizations();
        loadReminders("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders("");
    }

    private void loadReminders(String query) {
        List<Reminder> list;
        if (query.isEmpty()) {
            list = database.reminderDao().getAllReminders();
        } else {
            list = database.reminderDao().searchReminders(query);
        }
        adapter = new ReminderAdapter(list, this);
        recyclerView.setAdapter(adapter);
    }

    private void checkBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadReminders(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadReminders(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "hatirlat_yedek.json");
            startActivityForResult(intent, CREATE_BACKUP_FILE);
            return true;
        } else if (id == R.id.action_import) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            startActivityForResult(intent, PICK_BACKUP_FILE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == CREATE_BACKUP_FILE && uri != null) {
                try {
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    if (JsonBackupUtil.exportToJson(this, os)) {
                        Toast.makeText(this, "Yedekleme başarılı!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == PICK_BACKUP_FILE && uri != null) {
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    if (JsonBackupUtil.importFromJson(this, is)) {
                        loadReminders("");
                        Toast.makeText(this, "Geri yükleme başarılı!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onReminderClick(Reminder reminder) {
        Intent intent = new Intent(this, ReminderEditActivity.class);
        intent.putExtra("REMINDER_ID", reminder.id);
        startActivity(intent);
    }

    @Override
    public void onCompleteClick(Reminder reminder) {
        reminder.isCompleted = !reminder.isCompleted;
        database.reminderDao().update(reminder);
        if (reminder.isCompleted) {
            AlarmScheduler.cancel(this, reminder);
        } else {
            AlarmScheduler.schedule(this, reminder);
        }
        loadReminders("");
    }
}