package com.AbyssDigest.personalalert;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.AbyssDigest.personalalert.database.Alert;
import com.AbyssDigest.personalalert.database.AppDatabase;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddEditAlertActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText keywordsEditText;
    private CheckBox flashlightCheckBox;
    private Button soundButton;
    private TextView soundTextView;
    private Button saveButton;

    private AppDatabase db;
    private Alert alert;
    private String selectedSound;
    private MediaPlayer mediaPlayer;

    private static final int RINGTONE_PICKER_REQUEST_CODE = 999;

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

        soundButton.setOnClickListener(v -> showSoundSourceDialog());

        saveButton.setOnClickListener(v -> saveAlert());
        int alertId = getIntent().getIntExtra("alert_id", -1);
        if (alertId != -1) {
            loadAlert(alertId);
        }
    }

    private void showSoundSourceDialog() {
        final CharSequence[] options = {"App Sounds", "System Ringtones"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Sound Source");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showAssetSoundSelectionDialog();
            } else {
                showRingtonePicker();
            }
        });
        builder.show();
    }

    private void showAssetSoundSelectionDialog() {
        List<String> soundList = getSoundsFromAssets();
        final CharSequence[] soundItems = soundList.toArray(new CharSequence[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Sound");
        builder.setItems(soundItems, (dialog, which) -> {
            selectedSound = soundItems[which].toString();
            soundTextView.setText(selectedSound);
            playSound(selectedSound);
        });
        builder.show();
    }

    private void showRingtonePicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        startActivityForResult(intent, RINGTONE_PICKER_REQUEST_CODE);
    }

    private List<String> getSoundsFromAssets() {
        List<String> sounds = new ArrayList<>();
        try {
            String[] soundFiles = getAssets().list("sounds");
            if (soundFiles != null) {
                for (String file : soundFiles) {
                    sounds.add(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sounds;
    }

    private void playSound(String soundIdentifier) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release();
            mediaPlayer = null;
        });

        try {
            if (soundIdentifier.startsWith("content://")) {
                Uri soundUri = Uri.parse(soundIdentifier);
                mediaPlayer.setDataSource(this, soundUri);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } else {
                AssetFileDescriptor afd = getAssets().openFd("sounds/" + soundIdentifier);
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RINGTONE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                selectedSound = uri.toString();
                Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
                String name = ringtone.getTitle(this);
                soundTextView.setText(name);
                playSound(selectedSound);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
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
                    selectedSound = alert.sound;
                    if (selectedSound.startsWith("content://")) {
                        Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(selectedSound));
                        String name = ringtone.getTitle(this);
                        soundTextView.setText(name);
                    } else {
                        soundTextView.setText(selectedSound);
                    }
                }
            });
        }).start();
    }

    private void saveAlert() {
        new Thread(() -> {
            if (alert == null) {
                alert = new Alert();
                alert.isActive = true;
            }
            alert.name = nameEditText.getText().toString();
            alert.keywords = keywordsEditText.getText().toString();
            alert.flashlight = flashlightCheckBox.isChecked();
            if (selectedSound != null) {
                alert.sound = selectedSound;
            }

            if (alert.id == 0) {
                alert.order = db.alertDao().getAll().size();
                db.alertDao().insert(alert);
            } else {
                db.alertDao().update(alert);
            }
            finish();
        }).start();
    }
}
