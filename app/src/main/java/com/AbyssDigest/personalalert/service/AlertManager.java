package com.AbyssDigest.personalalert.service;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
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
    private MediaPlayer mediaPlayer;

    public AlertManager(Context context) {
        this.context = context;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            this.cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void triggerAlert(String soundUri, boolean flashlight) {
        if (soundUri != null && !soundUri.isEmpty()) {
            playSound(Uri.parse(soundUri));
        } else {
            playSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        if (flashlight) {
            strobeFlashlight(5, 100);
        }
    }

    private void playSound(Uri soundUri) {

        try {
//            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(this.context, soundUri);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        if (mediaPlayer != null) {
//            mediaPlayer.release();
//        }
//        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setAudioAttributes(
//                new AudioAttributes.Builder()
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .setUsage(AudioAttributes.USAGE_MEDIA)
//                        .build()
//        );
//        mediaPlayer.setOnCompletionListener(mp -> {
//            mp.release();
//            mediaPlayer = null;
//        });
//        try {
//            mediaPlayer.setDataSource(this.context , soundUri);
//            mediaPlayer.prepare();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        mediaPlayer.start();
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
}
