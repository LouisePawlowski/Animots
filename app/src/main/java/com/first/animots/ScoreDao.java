package com.first.animots;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ScoreDao {

    @Query("SELECT * FROM scores ORDER BY score DESC, date ASC LIMIT 15")
    List<Score> getTop15();

    @Query("SELECT * FROM scores WHERE playerName = :name ORDER BY score DESC, date ASC LIMIT 15")
    List<Score> getScoresByPlayer(String name);

    @Query("SELECT * FROM scores WHERE gameLevel = :level ORDER BY score DESC, date ASC LIMIT 15")
    List<Score> getScoresByLevel(int level);

    @Query("SELECT * FROM scores WHERE playerName = :name AND gameLevel = :level ORDER BY score DESC, date ASC LIMIT 15")
    List<Score> getScoresByLevelAndPlayer(String name, int level);

    @Query("SELECT DISTINCT playerName FROM scores ORDER BY playerName ASC")
    List<String> getAllPlayerNames();

    @Query("DELETE FROM scores")
    void deleteAllScores();

    @Insert
    void insertScore(Score score);
}
