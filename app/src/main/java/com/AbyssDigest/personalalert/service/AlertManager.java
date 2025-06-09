package com.AbyssDigest.personalalert.service;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import java.io.IOException;

public class AlertManager {

    private Context context;
    private CameraManager cameraManager;
    private String cameraId;

    public AlertManager(Context context) {
        this.context = context;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            this.cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void triggerAlert(String sound, boolean flashlight) {
        if (sound != null && !sound.isEmpty()) {
            playSound(sound);
        }

        if (flashlight) {
            strobeFlashlight(5, 100);
        }
    }

    private void playSound(String soundIdentifier) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release();
        });

        try {
            if (soundIdentifier.startsWith("content://")) {
                // It's a ringtone
                Uri soundUri = Uri.parse(soundIdentifier);
                mediaPlayer.setDataSource(context, soundUri);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } else {
                // It's an asset
                AssetFileDescriptor afd = context.getAssets().openFd("sounds/" + soundIdentifier);
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            mediaPlayer.release();
        }
    }

    private void strobeFlashlight(int count, int delay) {
        new Thread(() -> {
            for (int i = 0; i < count * 2; i++) {
                try {
                    cameraManager.setTorchMode(cameraId, i % 2 == 0);
                    Thread.sleep(delay);
                } catch (CameraAccessException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void release() {
        // No longer needed as MediaPlayer is released on completion
    }
}
