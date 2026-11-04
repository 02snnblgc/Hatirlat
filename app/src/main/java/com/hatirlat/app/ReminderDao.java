package com.hatirlat.app;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    Reminder getById(long id);

    @Query("SELECT * FROM reminders ORDER BY targetDateTime ASC")
    List<Reminder> getAllReminders();

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY targetDateTime ASC")
    List<Reminder> getActiveReminders();

    @Query("SELECT * FROM reminders WHERE title LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%' ORDER BY targetDateTime ASC")
    List<Reminder> searchReminders(String searchQuery);
}
