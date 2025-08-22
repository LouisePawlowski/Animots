package com.first.animots;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.first.animots.databinding.ActivityGamelevel1Binding;
import com.first.animots.databinding.ActivityGamelevel2Binding;
import com.first.animots.databinding.ActivityGamelevel3Binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class GameActivity extends AppCompatActivity {
    List<ClickableArea> areas = new ArrayList<>();
    List<Animal> animals = new ArrayList<>();
    List<Integer> numbers = new ArrayList<>();
    TextView timerText;
    TextView scorePopup;
    TextView questionText;
    TextView pointsDisplay;
    CountDownTimer timer;
    int durationInSeconds = 30;
    int rabbitPosition;
    int points = 0;
    ViewBinding binding;

    private SoundPool soundPool;

    {
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .build();
    }

    private int failClickSoundId;
    private int successClickSoundId;
    private int gameLevel = 0;
    SharedPreferences prefs;
    long startTime;
    private CountDownTimer scoreTimer;
    PieTimerView pieTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String layoutChoice = getIntent().getStringExtra("difficulty");

        if ("level2".equals(layoutChoice)) {
            gameLevel = 2;
            ActivityGamelevel2Binding bindingLevel2 = ActivityGamelevel2Binding.inflate(getLayoutInflater());
            binding = bindingLevel2;
            setContentView(bindingLevel2.getRoot());
        } else if ("level3".equals(layoutChoice)) {
            gameLevel = 3;
            ActivityGamelevel3Binding bindingLevel3 = ActivityGamelevel3Binding.inflate(getLayoutInflater());
            binding = bindingLevel3;
            setContentView(bindingLevel3.getRoot());
        } else {
            gameLevel = 1;
            ActivityGamelevel1Binding bindingLevel1 = ActivityGamelevel1Binding.inflate(getLayoutInflater());
            binding = bindingLevel1;
            setContentView(bindingLevel1.getRoot());
        }

        for (int i = 0; i < 17; i++) {
            numbers.add(i);
        }

        failClickSoundId = soundPool.load(this, R.raw.fail_click, 1);
        successClickSoundId = soundPool.load(this, R.raw.success_click, 1);

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        startMusicIfNeeded(R.raw.game_music);
        initializeClickableAreas();

        animals = Animal.loadAnimalData(this);

        if (gameLevel == 2) {
            ActivityGamelevel2Binding binding2 = (ActivityGamelevel2Binding) binding;
            timerText = binding2.timerText;
            scorePopup = binding2.scorePopup;
            questionText = binding2.questionText;
            pointsDisplay = binding2.pointsDisplay;
            pieTimer = binding2.pieTimer;
            setupListeners(binding2.exitButton, binding2.settingsButton);
            for (int i = 0; i < areas.size(); i++) {
                areas.get(i).getLayout().setTag(i);
                areas.get(i).getLayout().setOnClickListener(this::reactToClick);
            }
        } else if (gameLevel == 3) {
            ActivityGamelevel3Binding binding3 = (ActivityGamelevel3Binding) binding;
            timerText = binding3.timerText;
            scorePopup = binding3.scorePopup;
            questionText = binding3.questionText;
            pointsDisplay = binding3.pointsDisplay;
            pieTimer = binding3.pieTimer;
            setupListeners(binding3.exitButton, binding3.settingsButton);
            for (ClickableArea area : areas) {
                area.getTextView().setOnClickListener(this::reactToClick);
            }
        } else {
            ActivityGamelevel1Binding binding1 = (ActivityGamelevel1Binding) binding;
            timerText = binding1.timerText;
            scorePopup = binding1.scorePopup;
            questionText = binding1.questionText;
            pointsDisplay = binding1.pointsDisplay;
            pieTimer = binding1.pieTimer;
            setupListeners(binding1.exitButton, binding1.settingsButton);
            for (ClickableArea area : areas) {
                area.getLayout().setOnClickListener(this::reactToClick);
            }
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleExit();
            }
        });

        initializeTimer();
        moveTheAnimal(prefs.getBoolean("random_enabled", false));



    }

    private void setupListeners(Button exitButton, ImageButton settingsButton) {
        exitButton.setOnClickListener(v -> handleExit());
        settingsButton.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(GameActivity.this, SettingsActivity.class);
            settingsIntent.putExtra("origin", "game");
            startActivity(settingsIntent);
        });
    }

    private void initializeClickableAreas() {
        for (int i = 0; i < 6; i++) {
            String layoutIdName = "area" + i;
            if (gameLevel == 3) {
                int textId = getResources().getIdentifier(layoutIdName, "id", getPackageName());
                TextView text = findViewById(textId);
                areas.add(new ClickableArea(null, text, null));
            } else {
                int layoutId = getResources().getIdentifier(layoutIdName, "id", getPackageName());
                LinearLayout layout = findViewById(layoutId);
                ImageView img = (ImageView) layout.getChildAt(0);
                TextView text = (TextView) layout.getChildAt(1);
                areas.add(new ClickableArea(layout, text, img));
            }
        }
    }

    private void initializeTimer() {
        long timerDuration = TimeUnit.SECONDS.toMillis(durationInSeconds);
        long tickInterval = 1000;

        timer = new CountDownTimer(timerDuration, tickInterval) {
            public void onTick(long millisUntilFinished) {
                long totalSeconds = millisUntilFinished / 1000;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;

                @SuppressLint("DefaultLocale") String timeFormatted = String.format("%d:%02d", minutes, seconds);
                timerText.setText(timeFormatted);

                if (totalSeconds <= 5) {
                    timerText.setTextColor(Color.RED);
                    ScaleAnimation scaleAnimation = new ScaleAnimation(
                            1.0f, 1.5f,
                            1.0f, 1.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f
                    );
                    scaleAnimation.setDuration(300);
                    timerText.startAnimation(scaleAnimation);
                } else {
                    timerText.setTextColor(Color.BLACK);
                }
            }

            public void onFinish() {
                timerText.setText("0:00");
                Intent intent = new Intent(GameActivity.this, FinishActivity.class);
                intent.putExtra("score", points);
                intent.putExtra("gameLevel", gameLevel);
                intent.putExtra("date", System.currentTimeMillis());
                startActivity(intent);
                finish();
            }
        }.start();
    }

    private void reactToClick(View view) {
        if (gameLevel == 2) {
            int clickedIndex = (int) view.getTag();
            if (clickedIndex == rabbitPosition) {
                successReaction();
            } else {
                Animal animal = animals.get(numbers.get(clickedIndex));
                String nom = animal.getNom();
                int drawableID = getResources().getIdentifier(removeAccents(nom), "drawable", getPackageName());
                areas.get(clickedIndex).getImageView().setImageResource(drawableID);
                defeatReaction();
            }
        } else if (gameLevel == 3) {
            TextView winningZone = areas.get(rabbitPosition).getTextView();
            if (view == winningZone) {
                successReaction();
            } else {
                defeatReaction();
            }
        } else {
            LinearLayout winningZone = areas.get(rabbitPosition).getLayout();
            if (view == winningZone) {
                successReaction();
            } else {
                defeatReaction();
            }
        }
    }

    private void defeatReaction() {
        Log.i("ANIMOTS", "Failure");
        points -= 200;
        String pointsStr = String.format(Locale.getDefault(), "%.2f", points/100.0) + " points";
        pointsDisplay.setText(pointsStr);
        if (prefs.getBoolean("sound_enabled", true)) {
            soundPool.play(failClickSoundId, 1f, 1f, 0, 0, 1f);
        }
        popupAnimation(false, "-2.00");
    }

    private void successReaction() {
        Log.i("ANIMOTS", "Success");
        if (scoreTimer != null) {
            scoreTimer.cancel();
        }
        points += pieTimer.getScoreCenti();
        String pointsStr = String.format(Locale.getDefault(), "%.2f", points/100.0) + " points";
        pointsDisplay.setText(pointsStr);
        if (prefs.getBoolean("sound_enabled", true)) {
            soundPool.play(successClickSoundId, 1f, 1f, 0, 0, 1f);
        }
        popupAnimation(true, pieTimer.getScoreAsString());
        moveTheAnimal(prefs.getBoolean("random_enabled", false));
    }

    private void popupAnimation(Boolean found, String scoreStr) {
        if (found == true) {
            scorePopup.setText("+" + scoreStr);
            scorePopup.setTextColor(Color.parseColor("#8AEF00"));
        } else {
            scorePopup.setText(String.format(Locale.getDefault(), "%.2f", -2.0 ));
            scorePopup.setTextColor(Color.parseColor("#FF1313"));
        }

        scorePopup.setVisibility(View.VISIBLE);
        scorePopup.setAlpha(1f);
        scorePopup.setScaleX(1f);
        scorePopup.setScaleY(1f);
        scorePopup.setTranslationY(100f);

        scorePopup.animate()
                .scaleX(1.5f).scaleY(1.5f)
                .translationYBy(-100f)
                .alpha(0f)
                .setDuration(1300)
                .withEndAction(() -> {
                    scorePopup.setVisibility(View.GONE);
                    scorePopup.setAlpha(1f);
                    scorePopup.setScaleX(1f);
                    scorePopup.setScaleY(1f);
                    scorePopup.setTranslationY(0f);
                })
                .start();
    }

    @SuppressLint("SetTextI18n")
    private void moveTheAnimal(boolean randomizedAnimal) {
        if (randomizedAnimal && numbers.size() == animals.size() - 1) {
            numbers.add(17);
        } else if (!randomizedAnimal && numbers.size() == animals.size()) {
            numbers.remove(Integer.valueOf(animals.size() - 1));
            questionText.setText("Où est le lapin ?");
        }
        Collections.shuffle(numbers);

        Random random = new Random();
        rabbitPosition = random.nextInt(6);
        for (int i = 0; i < areas.size(); i++) {
            TextView text = areas.get(i).getTextView();
            ImageView img = areas.get(i).getImageView();
            if (i == rabbitPosition && !randomizedAnimal) {
                text.setText("Lapin");
                if (gameLevel == 1) {
                    img.setImageResource(R.drawable.lapin);
                } else if (gameLevel == 2) {
                    img.setImageResource(R.drawable.loupe);
                }
            } else {
                Animal animal = animals.get(numbers.get(i));
                String nom = animal.getNom();
                text.setText(capitalizeFirstLetter(nom));
                if (gameLevel == 1) {
                    int drawableID = getResources().getIdentifier(removeAccents(nom), "drawable", getPackageName());
                    img.setImageResource(drawableID);
                } else if (gameLevel == 2) {
                    img.setImageResource(R.drawable.loupe);
                }
                if (i == rabbitPosition) {
                    String pronoun = animal.getPronoun();
                    questionText.setText("Où est " + pronoun + nom + " ?");
                }
            }
        }
        startDecreasingScore();
    }

    private static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private static String removeAccents(String input) {
        if (input == null) return null;
        return input.replace('é', 'e')
                .replace('è', 'e')
                .replace('ê', 'e')
                .replace('ë', 'e');
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
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
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        Intent backActivityIntent = new Intent(GameActivity.this, MainActivity.class);
        backActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(backActivityIntent);
        finish();
    }

    private void startDecreasingScore() {
        startTime = System.currentTimeMillis();

        if (scoreTimer != null) {
            scoreTimer.cancel();
        }

        pieTimer.setProgressFromScore(500);

        scoreTimer = new CountDownTimer(4000, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                long elapsed = System.currentTimeMillis() - startTime;

                int scoreCenti;
                if (elapsed < 200) {
                    scoreCenti = 500;
                } else {
                    scoreCenti = (int) Math.round(500 - (elapsed - 200) / 10.0);
                    if (scoreCenti < 100) scoreCenti = 100;
                }
                pieTimer.setProgressFromScore(scoreCenti);
            }

            @Override
            public void onFinish() {
                pieTimer.setProgressFromScore(100);
            }
        };
        scoreTimer.start();
    }
}