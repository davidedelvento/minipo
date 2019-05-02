package nl.naire.mipino;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import nl.naire.mipino.R;

public class GameActivity extends AppCompatActivity {
    private int currentNoteIndex = -1;
    private TextView noteTextView;
    private TextView scoreTotalTime;
    private TextView scoreTime;
    private TextView scoreNote;
    private TextView scoreScore;
    private TextView scoreLastScore;
    private TextView scoreHighScore;
    private Toolbar toolbar;
    private Button nextButton;
    private Button startButton;
    private Timer timer;
    private Timer noteLetterTimer;
    private MidiNumber midiNotes;
    private GameSettings gameSettings;
    private GameState gameState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        noteTextView = findViewById(R.id.noteTextView);
        scoreTotalTime = findViewById(R.id.score_total_time);
        scoreTime = findViewById(R.id.score_time);
        scoreNote = findViewById(R.id.score_note);
        scoreScore = findViewById(R.id.score_score);
        scoreLastScore = findViewById(R.id.score_last_score);
        scoreHighScore = findViewById(R.id.score_high_score);
        nextButton = findViewById(R.id.next_button);
        startButton = findViewById(R.id.start_button);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        gameSettings = new GameSettings(prefs);

        if(savedInstanceState == null) {
            currentNoteIndex = gameSettings.random();
            gameState = new GameState(this);
            gameState.setup(prefs, gameSettings.getDuration(), gameSettings.size());
            gameState.newNote();
            displayGameState();
        }

        prefs.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

        midiNotes = new MidiNumber(this, (MidiManager)getSystemService(MIDI_SERVICE));
        midiNotes.registerListener(midiNumberListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayGameStateUpdating();
                    }
                });
            }
        }, 200, 200);
        midiNotes.connect();
    }

    @Override
    protected void onPause() {
        midiNotes.disconnect();
        timer.cancel();
        if(noteLetterTimer != null) noteLetterTimer.cancel();
        noteLetterTimer = null;


        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("currentNoteIndex", currentNoteIndex);
        savedInstanceState.putSerializable("gameState", gameState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        gameState = (GameState)savedInstanceState.getSerializable("gameState");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        gameState.setup(prefs, gameSettings.getDuration(), gameSettings.size());
        gameState.newNote();

        currentNoteIndex = savedInstanceState.getInt("currentNoteIndex", gameSettings.random());
        if(currentNoteIndex >= gameSettings.size()) currentNoteIndex = gameSettings.random();

        displayGameState();
    }

    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            gameSettings = new GameSettings(sharedPreferences);
            gameState.setup(sharedPreferences, gameSettings.getDuration(), gameSettings.size());
            gameState.newNote();
            currentNoteIndex = gameSettings.random();
            displayGameState();
        }
    };

    private String secondsToString(int seconds) {
        int minutes = seconds / 60;
        seconds = seconds - minutes*60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void displayGameState() {
        scoreTotalTime.setText(secondsToString(gameSettings.getDuration()));
        scoreNote.setText(String.valueOf(gameState.getNoteScore()));
        scoreScore.setText(gameState.getScore());
        scoreLastScore.setText(gameState.getLastScore());
        scoreHighScore.setText(gameState.getHighScore());
        if(gameState.isRunning() && gameState.timeRemaining() == 0) {
            noteTextView.setText(R.string.notef_none);
        }
        else {
            noteTextView.setText(gameSettings.get(currentNoteIndex).resource);
        }
    }

    private void displayGameStateUpdating() {
        if(gameState.isRunning()) {
            scoreTime.setText(secondsToString(gameState.timeRemaining()));
        }
        else {
            scoreTime.setText(secondsToString(gameState.timeEllapsed()));
        }

        if(gameState.isRunning() && gameState.timeRemaining() == 0) {
            startButton.setText(R.string.done);
            noteTextView.setText(R.string.notef_none);
            nextButton.setEnabled(false);
        }
        else if(gameState.isRunning()) {
            startButton.setText(R.string.stop);
            nextButton.setEnabled(false);
        }
        else {
            startButton.setText(R.string.start);
            nextButton.setEnabled(true);
        }
    }

    private final MidiNumber.Listener midiNumberListener = new MidiNumber.Listener() {
        @Override
        public void onConnectedChanged(final boolean connected, final String name) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    if (connected) {
                        toolbar.setLogo(android.R.drawable.presence_online);
                        toolbar.setTitle(getString(R.string.mipino_connected) + name);
                    } else {
                        toolbar.setLogo(android.R.drawable.presence_invisible);
                        toolbar.setTitle(R.string.mipino_disconnected);
                    }
                }
            });
        }

        @Override
        public void onNumber(final int number) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(gameState.isRunning() && gameState.timeRemaining() == 0) return;

                    if (number == gameSettings.get(currentNoteIndex).number) {
                        gameState.correct();
                        gameState.newNote();
                        currentNoteIndex = gameSettings.random(currentNoteIndex);
                        displayGameState();
                    } else {
                        gameState.incorrect();
                        displayGameState();
                    }
                }
            });
        }
    };

    public void nextButtonPressed(View view) {
        gameState.incorrect();
        gameState.newNote();
        currentNoteIndex = gameSettings.random(currentNoteIndex);
        displayGameState();
    }

    public void startButtonPressed(View view) {
        if(gameState.isRunning()) {
            gameState.stop(PreferenceManager.getDefaultSharedPreferences(this));
            gameState.clear();
            gameState.newNote();
            currentNoteIndex = gameSettings.random();
            displayGameState();
        }
        else {
            gameState.start();
            gameState.newNote();
            currentNoteIndex = gameSettings.random();
            displayGameState();
        }
    }

    public void onClearScoreClicked(MenuItem item) {
        gameState.clear();
        gameState.newNote();
        displayGameState();
    }

    public void onClearHighScoreClicked(MenuItem item) {
        gameState.clearHighScore(PreferenceManager.getDefaultSharedPreferences(this));
    }

    public void onSettingsClicked(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
