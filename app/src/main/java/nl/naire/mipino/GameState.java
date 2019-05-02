package nl.naire.mipino;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.Serializable;
import java.util.Scanner;

public class GameState implements Serializable {
    private final String noHighScore;
    private final String noLastScore;

    GameState(Context context) {
        noHighScore = context.getString(R.string.no_high_score);
        noLastScore = context.getString(R.string.no_last_score);
    }

    public void setup(SharedPreferences prefs, int duration, int numberOfNotes) {
        this.lastScore = prefs.getString("last_score", noLastScore);
        this.highScore = prefs.getString("high_score", noHighScore);

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
        prefs.edit().putString("high_score", noHighScore).apply();
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
