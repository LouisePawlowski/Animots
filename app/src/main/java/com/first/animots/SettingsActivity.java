package com.first.animots;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.first.animots.databinding.ActivityFinishBinding;
import com.first.animots.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String origin = getIntent().getStringExtra("origin");

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        boolean isMusicEnabled = prefs.getBoolean("music_enabled", true);
        binding.musicSwitch.setChecked(isMusicEnabled);
        binding.musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            changeMusicStatus(isChecked, origin);
        });

        boolean isSoundEnabled = prefs.getBoolean("sound_enabled", true);
        binding.soundSwitch.setChecked(isSoundEnabled);
        binding.soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            changeSoundsStatus(isChecked);
        });

        boolean isRandomEnabled = prefs.getBoolean("random_enabled", false);
        binding.randomSwitch.setChecked(isRandomEnabled);
        binding.randomSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            changeRandomStatus(isChecked);
        });

        binding.backButton.setOnClickListener(v -> finish());


        binding.resetButton.setOnClickListener(v -> {
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Réinitialiser les scores")
                    .setMessage("Voulez-vous vraiment supprimer tous les scores enregistrés ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                        prefs.edit().clear().apply();
                        AppDatabase db = AppDatabase.getInstance(this);

                        new Thread(() -> {
                            ScoreDao scoreDao = db.scoreDao();
                            scoreDao.deleteAllScores();
                        }).start();

                        Toast.makeText(this, "Scores réinitialisés", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });

    }

    private void changeRandomStatus(boolean isChecked) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("random_enabled", isChecked);
        editor.apply();
    }

    private void changeSoundsStatus(boolean isChecked) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("sound_enabled", isChecked);
        editor.apply();
    }

    private void changeMusicStatus(boolean isChecked, String origin) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("music_enabled", isChecked);
        editor.apply();

        if (isChecked) {
            Intent musicIntent = new Intent(this, MusicService.class);
            if ("game".equals(origin)) {
                musicIntent.putExtra("music_res_id", R.raw.game_music);
            } else {
                musicIntent.putExtra("music_res_id", R.raw.home_music);
            }
            startService(musicIntent);
        } else {
            stopService(new Intent(this, MusicService.class));
        }
    }
}