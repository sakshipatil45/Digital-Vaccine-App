package com.example.digitalvaccineapp.shared;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotificationPrefs {
    private static final String PREF_NAME = "notifications_prefs";
    private static final String KEY_REMINDERS = "reminders_json";

    public static void saveReminder(Context context, String title, String message, String date) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_REMINDERS, "[]");

        try {
            JSONArray array = new JSONArray(json);
            JSONObject obj = new JSONObject();
            obj.put("id", UUID.randomUUID().toString());
            obj.put("title", title);
            obj.put("message", message);
            obj.put("date", date);

            // Add the new reminder to the top
            JSONArray newArray = new JSONArray();
            newArray.put(obj);
            for (int i = 0; i < array.length(); i++) {
                newArray.put(array.getJSONObject(i));
            }

            prefs.edit().putString(KEY_REMINDERS, newArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<NotificationItem> getReminders(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_REMINDERS, "[]");
        List<NotificationItem> list = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                list.add(new NotificationItem(
                        obj.optString("id"),
                        obj.optString("title"),
                        obj.optString("message"),
                        obj.optString("date")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }
}
