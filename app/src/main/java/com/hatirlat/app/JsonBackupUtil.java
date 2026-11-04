package com.hatirlat.app;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonBackupUtil {

    public static String exportToJson(Context context, List<Reminder> reminders) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Reminder r : reminders) {
                JSONObject obj = new JSONObject();
                obj.put("id", r.id);
                obj.put("title", r.title);
                obj.put("description", r.description);
                obj.put("category", r.category);
                obj.put("targetDateTime", r.targetDateTime);
                obj.put("repeatType", r.repeatType);
                obj.put("isCompleted", r.isCompleted);
                jsonArray.put(obj);
            }
            return jsonArray.toString(4);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean importFromJson(Context context, String jsonString) {
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            AppDatabase db = AppDatabase.getInstance(context);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Reminder r = new Reminder();
                r.title = obj.optString("title");
                r.description = obj.optString("description");
                r.category = obj.optString("category", "Genel");
                r.targetDateTime = obj.optLong("targetDateTime", System.currentTimeMillis());
                r.repeatType = obj.optString("repeatType", "NONE");
                r.isCompleted = obj.optBoolean("isCompleted", false);

                db.reminderDao().insert(r);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
