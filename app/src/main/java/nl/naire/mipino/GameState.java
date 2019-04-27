package nl.naire.mipino;

import android.content.SharedPreferences;
import android.os.Bundle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Scanner;

public class GameState implements Serializable {
    GameState() {
    }

    public void setup(SharedPreferences prefs, int duration, int numberOfNotes) {
        this.lastScore = prefs.getString("last_score", "No last score");
        this.highScore = prefs.getString("high_score", "No high score");

        if(this.duration != duration || this.numberOfNotes != numberOfNotes) {
            this.duration = duration;
            this.numberOfNotes = numberOfNotes;
            clear();
        }

    }

    public void clear() {
        startTime = System.nanoTime();
        notes = 0;
        correct = 0;
        summedNotes = 0;
        noteScore = 0;
    }

    public void clearHighScore(SharedPreferences prefs) {
        highScore = "No high score";
        prefs.edit().putString("high_score", highScore).apply();
    }

    public void start() {
        running = true;
        clear();
    }

    public void stop(SharedPreferences prefs) {
        if(timeRemaining() == 0) {
            SharedPreferences.Editor editor = prefs.edit();

            lastScore = getScore();
            editor.putString("last_score", lastScore);

            if(parseScore(lastScore) > parseScore(highScore)) {
                highScore = lastScore;
                editor.putString("high_score", highScore);
            }

            editor.apply();
        }
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public int timeEllapsed() {
        return (int)((System.nanoTime() - startTime) / 1e9);
    }

    public int timeRemaining() {
        return Math.max(0, duration - timeEllapsed());
    }

    public void newNote() {
        noteScore = getMaxNoteScore();
    }

    public void correct() {
        if(running && timeEllapsed() > duration) return;

        summedNotes += noteScore;
        notes++;
        if(noteScore > 0) {
            correct++;
        }
    }

    public void incorrect() {
        noteScore = 0;
        notes++;
    }

    public int getNoteScore() {
        return noteScore;
    }

    public int getMaxNoteScore() {
        return numberOfNotes / 5;
    }

    public String getScore() {
        int score;
        if(notes > 0) {
            score = summedNotes * correct / notes;
        }
        else {
            score = 0;
        }
        return String.format("%d (%d/%d)", score, correct, notes);
    }

    public String getLastScore() {
        return lastScore;
    }

    public String getHighScore() {
        return highScore;
    }

    private int parseScore(String score) {
        Scanner scanner = new Scanner(score);
        if(scanner.hasNextInt()) return scanner.nextInt();
        else return 0;
    }

    private boolean running = false;
    private int duration = -1;
    private long startTime = System.nanoTime();
    private int numberOfNotes = -1;
    private int notes;
    private int correct;
    private int summedNotes;
    private int noteScore;
    private String lastScore;
    private String highScore;
}
