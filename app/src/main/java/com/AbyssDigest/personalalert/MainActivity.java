package com.AbyssDigest.personalalert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;
import android.os.Handler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.ItemTouchHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.AbyssDigest.personalalert.database.AppDatabase;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AlertAdapter alertAdapter;
    private AppDatabase db;
    private TextView statusTextView;

    private TextView lastAlertTextView;
    private TextView lastAlertTextTextView;
    private Handler handler = new Handler();
    private Runnable updateLastAlertRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.d("AlertAdapter", "Main: create"  );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDatabase(getApplicationContext());

        statusTextView = findViewById(R.id.statusTextView);
        recyclerView = findViewById(R.id.recyclerView);
        lastAlertTextView = findViewById(R.id.lastAlertTextView);
        lastAlertTextTextView = findViewById(R.id.lastAlertTextTextView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AddEditAlertActivity.class));
        });

        loadAlerts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlerts();
        updateStatus();
        updateLastAlert();
    }

    private void updateLastAlert() {
        if (updateLastAlertRunnable != null) {
            handler.removeCallbacks(updateLastAlertRunnable);
        }

        updateLastAlertRunnable = new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences("PersonalAlert", MODE_PRIVATE);
                long lastAlertTime = prefs.getLong("last_alert_time", 0);
                String lastAlertName = prefs.getString("last_alert_name", "");
                String lastAlertText = prefs.getString("last_alert_text", "");

                if (lastAlertTime > 0 && !lastAlertName.isEmpty()) {
                    long timeDifference = System.currentTimeMillis() - lastAlertTime;
                    long minutes = timeDifference / (1000 * 60);

                    if (minutes < 10) {
                        lastAlertTextView.setText("Last Alert: " + lastAlertName + " (" + minutes + "m ago)");
                        lastAlertTextTextView.setText(lastAlertText);
                        lastAlertTextTextView.setVisibility(View.VISIBLE);
                    } else {
                        lastAlertTextView.setText("Last Alert: None");
                        lastAlertTextTextView.setVisibility(View.GONE);
                    }
                } else {
                    lastAlertTextView.setText("Last Alert: None");
                    lastAlertTextTextView.setVisibility(View.GONE);
                }
                handler.postDelayed(this, 60000); // Update every minute
            }
        };
        handler.post(updateLastAlertRunnable);
    }

    private void updateStatus() {
        if (isNotificationServiceEnabled()) {
            statusTextView.setText("Status: Active");
        } else {
            statusTextView.setText("Status: Inactive. Please grant notification access.");
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void loadAlerts() {
        new Thread(() -> {
            List<com.AbyssDigest.personalalert.database.Alert> alerts = db.alertDao().getAll();
            runOnUiThread(() -> {
                if (alertAdapter == null) {
                    alertAdapter = new AlertAdapter(alerts);
                    recyclerView.setAdapter(alertAdapter);
                    ItemTouchHelper.Callback callback = new ReorderCallback(alertAdapter);
                    ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                    touchHelper.attachToRecyclerView(recyclerView);
                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(alertAdapter));
                    itemTouchHelper.attachToRecyclerView(recyclerView);
                } else {
                    alertAdapter.setAlerts(alerts);
                }
            });
        }).start();
    }
}
