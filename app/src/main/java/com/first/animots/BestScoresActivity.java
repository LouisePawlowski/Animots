package com.first.animots;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.first.animots.databinding.ActivityBestscoresBinding;
import com.first.animots.databinding.ActivityFinishBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BestScoresActivity extends AppCompatActivity {

    ActivityBestscoresBinding binding;
    SharedPreferences prefs;
    private String selectedPlayer = "Tous";
    private String selectedMode = "Tous";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBestscoresBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.textTopScores.setLineSpacing(40, 1f);

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        binding.exitButton.setOnClickListener(v -> handleExit());

        binding.settingsButton.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(BestScoresActivity.this, SettingsActivity.class);
            settingsIntent.putExtra("origin", "finish");
            startActivity(settingsIntent);
        });

        startMusicIfNeeded(R.raw.home_music);

        AppDatabase db = AppDatabase.getInstance(this);

        new Thread(() -> {
            ScoreDao scoreDao = db.scoreDao();

            //scoreDao.deleteAllScores();

            List<Score> top10 = scoreDao.getTop15();
            List<String> playerNames = new ArrayList<>();
            playerNames.add("Tous");
            playerNames.addAll(scoreDao.getAllPlayerNames());
            List<String> gameModes = new ArrayList<>();
            gameModes.add("Tous");
            gameModes.add("Apprentis");
            gameModes.add("Courageux");
            gameModes.add("Pros");

            runOnUiThread(() -> {
                afficherScores(top10);

                ArrayAdapter<String> adapterLevel = new ArrayAdapter<>(
                        this,
                        R.layout.spinner_item,
                        gameModes
                );
                adapterLevel.setDropDownViewResource(R.layout.spinner_dropdown_item);

                ArrayAdapter<String> adapterPlayer = new ArrayAdapter<>(
                        this,
                        R.layout.spinner_item,
                        playerNames
                );
                adapterPlayer.setDropDownViewResource(R.layout.spinner_dropdown_item);
                binding.spinnerPlayers.setAdapter(adapterPlayer);
                binding.spinnerGameMode.setAdapter(adapterLevel);

                binding.spinnerGameMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedMode = gameModes.get(position);
                        refreshScores(scoreDao);
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });

                binding.spinnerPlayers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedPlayer = playerNames.get(position);
                        refreshScores(scoreDao);
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });
            });
        }).start();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleExit();
            }
        });
    }

    private void afficherScores(List<Score> scores) {
        if (scores.isEmpty()) {
            binding.textTopScores.setText("Aucun score trouvé.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        int rank = 0;
        double previousScore = Double.MIN_VALUE;

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        for (int i = 0; i < scores.size(); i++) {
            Score s = scores.get(i);

            if (s.score != previousScore) {
                rank = i + 1;
            }

            String dateStr = sdf.format(new Date(s.date));
            sb.append(String.format(
                    Locale.getDefault(),
                    "%2d. %-10s %-8s %8.2f\n",
                    rank, s.playerName, dateStr, s.score/100.0
            ));

            previousScore = s.score;
        }

        binding.textTopScores.setTypeface(Typeface.MONOSPACE);
        binding.textTopScores.setText(sb.toString());
    }

    private void startMusicIfNeeded(int musicResId) {
        boolean musicEnabled = prefs.getBoolean("music_enabled", true);
        if (musicEnabled) {
            Intent musicIntent = new Intent(this, MusicService.class);
            musicIntent.putExtra("music_res_id", musicResId);
            startService(musicIntent);
        }
    }

    private void handleExit() {
        Intent backActivityIntent = new Intent(BestScoresActivity.this, MainActivity.class);
        backActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(backActivityIntent);
        finish();
    }

    private void refreshScores(ScoreDao scoreDao) {
        new Thread(() -> {
            List<Score> scoresToShow;

            if (selectedPlayer.equals("Tous") && selectedMode.equals("Tous")) {
                scoresToShow = scoreDao.getTop15();
            } else if (!selectedPlayer.equals("Tous") && selectedMode.equals("Tous")) {
                scoresToShow = scoreDao.getScoresByPlayer(selectedPlayer);
            } else if (selectedPlayer.equals("Tous") && !selectedMode.equals("Tous")) {
                scoresToShow = scoreDao.getScoresByLevel(getLevelFromMode(selectedMode));
            } else {
                scoresToShow = scoreDao.getScoresByLevelAndPlayer(
                        selectedPlayer,
                        getLevelFromMode(selectedMode)
                );
            }

            runOnUiThread(() -> afficherScores(scoresToShow));
        }).start();
    }

    private int getLevelFromMode(String mode) {
        switch (mode) {
            case "Apprentis": return 1;
            case "Courageux": return 2;
            case "Pros": return 3;
            default: return 0;
        }
    }
}