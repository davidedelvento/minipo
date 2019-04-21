package nl.naire.mipino;

import java.io.Serializable;

public class GameState implements Serializable {
    GameState(int duration, int numberOfNotes) {
        this.duration = duration;
        this.numberOfNotes = numberOfNotes;
        clear();
    }

    public void clear() {
        startTime = System.nanoTime();
        notes = 0;
        correct = 0;
        score = 0;
        noteScore = 0;
    }

    public void clearHighScore() {
        highScore = "No high score";
    }

    public void start() {
        running = true;
        clear();
    }

    public void stop() {
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

        score += noteScore;
        notes++;
        if(noteScore > 0) {
            correct++;
        }
    }

    public void incorrect() {
        noteScore = 0;
    }

    public int getNoteScore() {
        return noteScore;
    }

    public int getMaxNoteScore() {
        return numberOfNotes / 5;
    }

    public String getScore() {
        return String.format("%d (%d/%d)", score, notes, correct);
    }

    public String getLastScore() {
        return lastScore;
    }

    public String getHighScore() {
        return highScore;
    }

    private boolean running = false;
    private int duration;
    private long startTime = System.nanoTime();
    private int numberOfNotes;
    private int notes;
    private int correct;
    private int score;
    private int noteScore;
    private String lastScore = "No last score";
    private String highScore = "No high score";
}
