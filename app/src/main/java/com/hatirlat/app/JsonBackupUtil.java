package com.hatirlat.app;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hatirlat.app.data.AppDatabase;
import com.hatirlat.app.data.Reminder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.List;

public class JsonBackupUtil {

    public static boolean exportToJson(Context context, OutputStream outputStream) {
        try {
            List<Reminder> list = AppDatabase.getInstance(context).reminderDao().getAllReminders();
            Gson gson = new Gson();
            String json = gson.toJson(list);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            writer.write(json);
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean importFromJson(Context context, InputStream inputStream) {
        try {
            InputStreamReader reader = new InputStreamReader(inputStream);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Reminder>>() {}.getType();
            List<Reminder> list = gson.fromJson(reader, listType);
            reader.close();

            if (list != null) {
                AppDatabase db = AppDatabase.getInstance(context);
                for (Reminder r : list) {
                    db.reminderDao().insert(r);
                    AlarmScheduler.schedule(context, r);
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
