package com.first.animots;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.first.animots.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        startMusicIfNeeded(R.raw.home_music);

        binding.level1Button.setOnClickListener(v -> {
            Intent activity1Intent = new Intent(MainActivity.this, GameActivity.class);
            activity1Intent.putExtra("difficulty", "level1");
            startActivity(activity1Intent);
            finish();
        });

        binding.level2Button.setOnClickListener(v -> {
            Intent activity2Intent = new Intent(MainActivity.this, GameActivity.class);
            activity2Intent.putExtra("difficulty", "level2");
            startActivity(activity2Intent);
            finish();
        });

        binding.level3Button.setOnClickListener(v -> {
            Intent activity3Intent = new Intent(MainActivity.this, GameActivity.class);
            activity3Intent.putExtra("difficulty", "level3");
            startActivity(activity3Intent);
            finish();
        });

        binding.settingsButton.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            settingsIntent.putExtra("origin", "menu");
            startActivity(settingsIntent);
        });

        binding.scoresButton.setOnClickListener(v -> {
            Intent scoresIntent = new Intent(MainActivity.this, BestScoresActivity.class);
            startActivity(scoresIntent);
        });
    }
    private void startMusicIfNeeded(int musicResId) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean musicEnabled = prefs.getBoolean("music_enabled", true);

        if (musicEnabled) {
            Intent musicIntent = new Intent(this, MusicService.class);
            musicIntent.putExtra("music_res_id", musicResId);
            startService(musicIntent);
        }
    }
}