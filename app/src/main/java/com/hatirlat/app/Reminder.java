package com.hatirlat.app;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders")
public class Reminder {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public String description;
    public String category; // Fatura, Özel Gün, Etkinlik vb.
    public long targetDateTime; // Milisaniye cinsinden hedef tarih
    public String repeatType; // NONE, DAILY, WEEKLY_DAYS, MONTHLY_5TH, YEARLY
    public String repeatDays; // Örn: "1,3,5" (Pazartesi, Çarşamba, Cuma)
    public boolean notify1DayBefore;
    public boolean notify1HourBefore;
    public boolean notifyAtTime;
    public String ringtoneUri;
    public boolean isCompleted;
    public int themeColorIndex; // 0-4 pastel renkler

    public Reminder() {
        this.notifyAtTime = true;
        this.isCompleted = false;
        this.themeColorIndex = 0;
    }
}
