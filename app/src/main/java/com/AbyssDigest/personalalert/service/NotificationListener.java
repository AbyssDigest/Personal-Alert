package com.AbyssDigest.personalalert.service;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.AbyssDigest.personalalert.database.AppDatabase;
import com.AbyssDigest.personalalert.database.Alert;
import android.content.SharedPreferences;
import java.util.List;
import android.util.Log;

public class NotificationListener extends NotificationListenerService {

    private AppDatabase db;
    private AlertManager alertManager;

    @Override
    public void onCreate() {
//        Log.d("AlertAdapter", "NotificationListener: create"  );
        super.onCreate();
        db = AppDatabase.getDatabase(getApplicationContext());
        alertManager = new AlertManager(getApplicationContext());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        new Thread(() -> {
            List<Alert> alerts = db.alertDao().getAll();
            Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;
            String title = extras.getString(Notification.EXTRA_TITLE);
            String text = extras.getString(Notification.EXTRA_TEXT);

//            Log.d("AlertAdapter", "title: " + title );
//            Log.d("AlertAdapter", "text: " + text );

            if (text != null) {
                String lowerCaseText = text.toLowerCase();
                for (Alert alert : alerts) {
                    String[] keywords = alert.keywords.toLowerCase().split(",");
                    for (String keyword : keywords) {
                        if (lowerCaseText.contains(keyword.trim())) {
                            alertManager.triggerAlert(alert.sound, alert.flashlight);
                            SharedPreferences prefs = getSharedPreferences("PersonalAlert", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("last_alert_name", alert.name);
                            editor.putLong("last_alert_time", System.currentTimeMillis());
                            editor.apply();
                            break;
                        }
                    }
                }
            }
        }).start();
    }
}
