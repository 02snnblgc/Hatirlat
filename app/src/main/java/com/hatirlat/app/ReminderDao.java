package com.hatirlat.app;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ReminderDao {

    @Insert
    long insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminders ORDER BY targetDateTime ASC")
    List<Reminder> getAllReminders();

    @Query("SELECT * FROM reminders WHERE id = :id")
    Reminder getReminderById(long id);

    @Query("SELECT * FROM reminders WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY targetDateTime ASC")
    List<Reminder> searchReminders(String query);
}
