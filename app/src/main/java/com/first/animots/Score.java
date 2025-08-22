package com.first.animots;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scores")
public class Score {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String playerName;
    public int score;
    public long date;
    public int gameLevel;

    public Score(String playerName, int score, long date, int gameLevel) {
        this.playerName = playerName;
        this.score = score;
        this.date = date;
        this.gameLevel = gameLevel;
    }
}

