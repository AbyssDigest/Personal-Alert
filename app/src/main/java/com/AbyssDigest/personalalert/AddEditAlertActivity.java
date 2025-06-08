package com.AbyssDigest.personalalert;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.media.Ringtone;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.AbyssDigest.personalalert.database.Alert;
import com.AbyssDigest.personalalert.database.AppDatabase;

public class AddEditAlertActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText keywordsEditText;
    private CheckBox flashlightCheckBox;
    private Button soundButton;
    private TextView soundTextView;
    private Button saveButton;

    private AppDatabase db;
    private Alert alert;
    private Uri selectedSoundUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_alert);

        db = AppDatabase.getDatabase(getApplicationContext());

        nameEditText = findViewById(R.id.nameEditText);
        keywordsEditText = findViewById(R.id.keywordsEditText);
        flashlightCheckBox = findViewById(R.id.flashlightCheckBox);
        soundButton = findViewById(R.id.soundButton);
        soundTextView = findViewById(R.id.soundTextView);
        saveButton = findViewById(R.id.saveButton);

        soundButton.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
            startActivityForResult(intent, 999);
        });

        saveButton.setOnClickListener(v -> saveAlert());
        int alertId = getIntent().getIntExtra("alert_id", -1);
        if (alertId != -1) {
            loadAlert(alertId);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999 && resultCode == RESULT_OK) {
            selectedSoundUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (selectedSoundUri != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(this, selectedSoundUri);
                String name = ringtone.getTitle(this);
                if (name != null && name.toLowerCase().contains("unknown")) {
                    name = selectedSoundUri.getLastPathSegment();
                    if (name != null && name.contains(".")) {
                        name = name.substring(0, name.lastIndexOf('.'));
                    }
                }
                soundTextView.setText(name);
            }
        }
    }

    private void loadAlert(int alertId) {
        new Thread(() -> {
            alert = db.alertDao().getById(alertId);
            runOnUiThread(() -> {
                nameEditText.setText(alert.name);
                keywordsEditText.setText(alert.keywords);
                flashlightCheckBox.setChecked(alert.flashlight);
                if (alert.sound != null && !alert.sound.isEmpty()) {
                    selectedSoundUri = Uri.parse(alert.sound);
                    Ringtone ringtone = RingtoneManager.getRingtone(this, selectedSoundUri);
                    String name = ringtone.getTitle(this);
                    if (name != null && name.toLowerCase().contains("unknown")) {
                        name = selectedSoundUri.getLastPathSegment();
                        if (name != null && name.contains(".")) {
                            name = name.substring(0, name.lastIndexOf('.'));
                        }
                    }
                    soundTextView.setText(name);
                }
            });
        }).start();
    }

    private void saveAlert() {
        new Thread(() -> {
            if (alert == null) {
                alert = new Alert();
            }
            alert.name = nameEditText.getText().toString();
            alert.keywords = keywordsEditText.getText().toString();
            alert.flashlight = flashlightCheckBox.isChecked();
            if (selectedSoundUri != null) {
                alert.sound = selectedSoundUri.toString();
            }

            if (alert.id == 0) {
                db.alertDao().insert(alert);
            } else {
                db.alertDao().update(alert);
            }
            finish();
        }).start();
    }
}
