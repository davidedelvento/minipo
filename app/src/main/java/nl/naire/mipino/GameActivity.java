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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import nl.naire.mipino.R;

public class GameActivity extends AppCompatActivity {
    private int currentIndex = -1;
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
    private MidiNumber midiNotes;
    private GameSettings gameSettings;
    private GameState gameState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        noteTextView = (TextView)findViewById(R.id.noteTextView);
        scoreTotalTime = (TextView)findViewById(R.id.score_total_time);
        scoreTime = (TextView)findViewById(R.id.score_time);
        scoreNote = (TextView)findViewById(R.id.score_note);
        scoreScore = (TextView)findViewById(R.id.score_score);
        scoreLastScore = (TextView)findViewById(R.id.score_last_score);
        scoreHighScore = (TextView)findViewById(R.id.score_high_score);
        nextButton = (Button)findViewById(R.id.next_button);
        startButton = (Button)findViewById(R.id.start_button);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        gameSettings = new GameSettings(prefs);

        if(savedInstanceState != null) currentIndex = savedInstanceState.getInt("currentIndex", -1);
        if(currentIndex == -1) currentIndex = gameSettings.random();

        gameState = new GameState(gameSettings.getDuration(), gameSettings.size());
        gameState.newNote();
        displayGameState();

        timer = new Timer();
        timer.scheduleAtFixedRate(gameStateTimerTask, 200, 200);

        midiNotes = new MidiNumber((MidiManager)getSystemService(MIDI_SERVICE));
        midiNotes.registerListener(midiNumberListener);
        midiNotes.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        midiNotes.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear_high_score) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("currentIndex", currentIndex);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentIndex = savedInstanceState.getInt("currentIndex", gameSettings.random());
    }

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
        noteTextView.setText(gameSettings.get(currentIndex).resource);
    }

    private void displayGameStateUpdating() {
        if(gameState.isRunning()) {
            scoreTime.setText(secondsToString(gameState.timeRemaining()));
        }
        else {
            scoreTime.setText(secondsToString(gameState.timeEllapsed()));
        }

        if(gameState.isRunning() && gameState.timeRemaining() == 0) {
            startButton.setText("Done");
            nextButton.setEnabled(false);
        }
        else if(gameState.isRunning()) {
            startButton.setText("Stop");
            nextButton.setEnabled(false);
        }
        else {
            startButton.setText("Start");
            nextButton.setEnabled(true);
        }
    }

    private TimerTask gameStateTimerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayGameStateUpdating();
                }
            });
        }
    };

    private final MidiNumber.Listener midiNumberListener = new MidiNumber.Listener() {
        @Override
        public void onConnectedChanged(final boolean connected, final String name) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    if (connected) {
                        toolbar.setLogo(android.R.drawable.presence_online);
                        toolbar.setTitle("MiPiNo - Connected " + name);
                    } else {
                        toolbar.setLogo(android.R.drawable.presence_invisible);
                        toolbar.setTitle("MiPiNo - Disconnected");
                    }
                }
            });
        }

        @Override
        public void onNumber(final int number) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    String text = "Key #" + String.valueOf(number);
                    Toast.makeText(GameActivity.this, text, Toast.LENGTH_SHORT).show();
                    if (number == gameSettings.get(currentIndex).number) {
                        gameState.correct();
                        gameState.newNote();
                        currentIndex = gameSettings.random(currentIndex);
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
        currentIndex = gameSettings.random(currentIndex);
        displayGameState();
    }

    public void startButtonPressed(View view) {
        if(gameState.isRunning()) {
            gameState.stop();
            gameState.clear();
            gameState.newNote();
            currentIndex = gameSettings.random();
            displayGameState();
        }
        else {
            gameState.start();
            gameState.newNote();
            currentIndex = gameSettings.random();
            displayGameState();
        }
    }

    public void onClearScoreClicked(MenuItem item) {
        gameState.clear();
        gameState.newNote();
        displayGameState();
    }

    public void onSettingsClicked(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
