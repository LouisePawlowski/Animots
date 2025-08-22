package com.first.animots;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.first.animots.databinding.ActivityFinishBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FinishActivity extends AppCompatActivity {

    ActivityFinishBinding binding;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFinishBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        binding.settingsButton.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(FinishActivity.this, SettingsActivity.class);
            settingsIntent.putExtra("origin", "finish");
            startActivity(settingsIntent);
        });

        startMusicIfNeeded(R.raw.home_music);

        int scoreCenti = getIntent().getIntExtra("score", 0);
        int gameLevel = getIntent().getIntExtra("gameLevel", 1);
        long endTimestamp = getIntent().getLongExtra("date", 0);
        int bestScoreCenti = prefs.getInt("best_score", -999999);
        int bestScoreCentiLevel = prefs.getInt("best_score_level" + gameLevel, -999999);

        String pointsStr = String.format(Locale.getDefault(), "%.2f", scoreCenti/100.0) + " points";
        binding.score.setText(pointsStr);

        if (scoreCenti > bestScoreCenti) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("best_score", scoreCenti);
            editor.putInt("best_score_level" + gameLevel, scoreCenti);
            editor.apply();
            popupAnimation(true);
        } else if (scoreCenti > bestScoreCentiLevel) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("best_score_level" + gameLevel, scoreCenti);
            editor.apply();
            popupAnimation(false);
        }

        Spinner spinnerPlayers = binding.spinnerPlayers;
        Button saveButton = binding.saveButton;

        AppDatabase db = AppDatabase.getInstance(this);
        ScoreDao scoreDao = db.scoreDao();

        new Thread(() -> {
            List<String> playerNames = scoreDao.getAllPlayerNames();
            playerNames.add("Nouveau joueur");
            if (playerNames.size() == 1) {
                playerNames.add(0, "Inconnu");
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        R.layout.spinner_item,
                        playerNames
                );
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinnerPlayers.setAdapter(adapter);
            });
        }).start();

        spinnerPlayers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                if ("Nouveau joueur".equals(selected)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FinishActivity.this);
                    builder.setTitle("Entrez le nom du joueur");

                    final EditText input = new EditText(FinishActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
                    input.setHint("Nom du joueur");
                    builder.setView(input);

                    builder.setPositiveButton("OK", (dialog, which) -> {
                        String newName = input.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            new Thread(() -> {
                                List<String> updatedNames = scoreDao.getAllPlayerNames();
                                updatedNames.add("Nouveau joueur");

                                if (!updatedNames.contains(newName)) {
                                    updatedNames.add(0, newName);
                                }

                                runOnUiThread(() -> {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                            FinishActivity.this,
                                            android.R.layout.simple_spinner_item,
                                            updatedNames
                                    );
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spinnerPlayers.setAdapter(adapter);
                                    spinnerPlayers.setSelection(updatedNames.indexOf(newName));
                                });
                            }).start();
                        } else {
                            spinnerPlayers.setSelection(0);
                        }
                    });
                    builder.setNegativeButton("Annuler", (dialog, which) -> {dialog.cancel();spinnerPlayers.setSelection(0);});

                    builder.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        saveButton.setOnClickListener(v -> {
            String selectedPlayer = (String) spinnerPlayers.getSelectedItem();
            if ("Nouveau joueur".equals(selectedPlayer)) return;

            saveButton.setEnabled(false);

            new Thread(() -> {
                Score newScore = new Score(capitalizeFirstLetter(selectedPlayer), scoreCenti, endTimestamp, gameLevel);
                db.scoreDao().insertScore(newScore);
            }).start();
            Toast.makeText(getApplicationContext(), "Enregistré", Toast.LENGTH_SHORT).show();
            handleExit();
        });

        binding.homeButton.setOnClickListener(v -> {
            handleExit();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("best_score", bestScoreCenti);
            editor.putInt("best_score_level" + gameLevel, bestScoreCentiLevel);
            editor.apply();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleExit();
            }
        });
    }

    private void popupAnimation(boolean allBest) {

        TextView scorePopup = binding.bestScorePopup;
        scorePopup.setVisibility(View.VISIBLE);
        scorePopup.setAlpha(1f);
        scorePopup.setScaleX(1f);
        scorePopup.setScaleY(1f);
        scorePopup.setTranslationY(100f);
        if (allBest) {
            scorePopup.setText("Nouveau Meilleur Score !");
        } else {
            scorePopup.setText("Nouveau record pour ce niveau !");
        }
        scorePopup.animate()
                .scaleX(1.5f).scaleY(1.5f)
                .translationYBy(-100f)
                .alpha(0f)
                .setDuration(3000)
                .withEndAction(() -> {
                    scorePopup.setVisibility(View.GONE);
                    scorePopup.setAlpha(1f);
                    scorePopup.setScaleX(1f);
                    scorePopup.setScaleY(1f);
                    scorePopup.setTranslationY(0f);
                })
                .start();
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
        Intent backActivityIntent = new Intent(FinishActivity.this, MainActivity.class);
        backActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(backActivityIntent);
        finish();

    }

    private static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}
