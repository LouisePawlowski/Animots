package com.first.animots;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class AppLifecycleObserver implements LifecycleObserver {

    private final Context context;
    public AppLifecycleObserver(Context context) {
        this.context = context.getApplicationContext();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean musicEnabled = prefs.getBoolean("music_enabled", true);
        if (musicEnabled) {
            Intent resumeIntent = new Intent(context, MusicService.class);
            resumeIntent.setAction("RESUME_MUSIC");
            context.startService(resumeIntent);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        Intent pauseIntent = new Intent(context, MusicService.class);
        pauseIntent.setAction("PAUSE_MUSIC");
        context.startService(pauseIntent);
    }
}