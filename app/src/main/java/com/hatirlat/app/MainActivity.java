package com.hatirlat.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private AppDatabase db;
    private Button btnExportJson, btnImportJson;
    private SearchView searchView;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Veritabanı bağlantısı
        db = AppDatabase.getInstance(this);

        // Arayüz bileşenlerinin tanımlanması
        recyclerView = findViewById(R.id.recyclerView);
        btnExportJson = findViewById(R.id.btnExportJson);
        btnImportJson = findViewById(R.id.btnImportJson);
        searchView = findViewById(R.id.searchView);
        fabAdd = findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 1. Özellik: JSON Yedekleme ve Dışa Aktarma
        btnExportJson.setOnClickListener(v -> JsonBackupUtil.exportToJson(this));

        // 2. Özellik: JSON Geri Yükleme ve İçe Aktarma
        btnImportJson.setOnClickListener(v -> {
            JsonBackupUtil.importFromJson(this);
            // Geri yükleme sonrası listeyi tazele
            loadReminders();
        });

        // Yeni Hatırlatma Ekleme Ekranına Geçiş
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReminderEditActivity.class);
            startActivity(intent);
        });

        // 3. Özellik: Anlık Arama ve Filtreleme (SearchView)
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

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders(); // Ekrandan dönüldüğünde veya uygulama açıldığında listeyi yenile
    }

    // Veritabanındaki tüm hatırlatmaları yükle ve adaptöre bağla
    private void loadReminders() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Reminder> list = db.reminderDao().getAllReminders();
            runOnUiThread(() -> setupAdapter(list));
        });
    }

    // Arama kelimesine göre filtreleme yap
    private void filterReminders(String query) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Reminder> list;
            if (query == null || query.trim().isEmpty()) {
                list = db.reminderDao().getAllReminders();
            } else {
                list = db.reminderDao().searchReminders("%" + query + "%");
            }
            runOnUiThread(() -> {
                if (adapter != null) {
                    adapter.updateList(list);
                } else {
                    setupAdapter(list);
                }
            });
        });
    }

    // RecyclerView adaptörünü kur ve tıklama/checkbox olaylarını yönet
    private void setupAdapter(List<Reminder> reminders) {
        adapter = new ReminderAdapter(reminders, reminder -> {
            // Liste elemanına tıklandığında düzenleme ekranına git
            Intent intent = new Intent(MainActivity.this, ReminderEditActivity.class);
            intent.putExtra("REMINDER_ID", reminder.getId());
            startActivity(intent);
        }, (reminder, isChecked) -> {
            // Checkbox işaretlendiğinde veritabanında tamamlandı durumunu güncelle
            reminder.setCompleted(isChecked);
            Executors.newSingleThreadExecutor().execute(() -> {
                db.reminderDao().update(reminder);
            });
        });
        recyclerView.setAdapter(adapter);
    }
}