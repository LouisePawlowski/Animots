package com.first.animots;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private static boolean isPlaying = false;
    private static int currentMusicResId = -1;

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static int getCurrentMusicResId() {
        return currentMusicResId;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("PAUSE_MUSIC".equals(action)) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            } else if ("RESUME_MUSIC".equals(action)) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }

            if (intent.hasExtra("music_res_id")) {
                int musicResId = intent.getIntExtra("music_res_id", -1);
                if (musicResId != -1 && musicResId != currentMusicResId) {
                    playMusic(musicResId);
                }
            }
        }
        return START_STICKY;
    }

    private void playMusic(int resId) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, resId);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        currentMusicResId = resId;
        isPlaying = true;
    }

    @Override
    public void onDestroy() {
        stopAndRelease();
        super.onDestroy();
    }

    private void stopAndRelease() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopAndRelease();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}