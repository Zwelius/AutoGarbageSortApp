package com.example.autogarbagesortapp.ui.slideshow;

public class GameRecord {
    private String team1Name;
    private String team2Name;
    private int team1Score;
    private int team2Score;
    private String timestamp;
    private String timerSet;

    public GameRecord(String team1Name, int team1Score, String team2Name, int team2Score, String timestamp, String timerSet) {
        this.team1Name = team1Name;
        this.team1Score = team1Score;
        this.team2Name = team2Name;
        this.team2Score = team2Score;
        this.timestamp = timestamp;
        this.timerSet = timerSet;
    }

    public String getTeam1Name() {
        return team1Name;
    }

    public String getTeam2Name() {
        return team2Name;
    }

    public int getTeam1Score() {
        return team1Score;
    }

    public int getTeam2Score() {
        return team2Score;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public String getTimerSet() {
        return timerSet;
    }
}