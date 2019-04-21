package nl.naire.mipino;

import android.media.midi.MidiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Random;

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
    private MidiNumber midiNotes;
    private GameSettings gameSettings;
    private GameState gameState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        noteTextView = (TextView)findViewById(R.id.noteTextView);
        scoreTotalTime = (TextView)findViewById(R.id.score_total_time);
        scoreTime = (TextView)findViewById(R.id.score_time);
        scoreNote = (TextView)findViewById(R.id.score_note);
        scoreScore = (TextView)findViewById(R.id.score_score);
        scoreLastScore = (TextView)findViewById(R.id.score_last_score);
        scoreHighScore = (TextView)findViewById(R.id.score_high_score);

        gameSettings = new GameSettings();
        gameSettings.setDuration(120);
        gameSettings.add(GameSettings.Group.CMajor);
        gameSettings.add(GameSettings.Range.Treble_C4B4);

        gameState = new GameState(gameSettings.size(), gameSettings.getDuration());
        gameState.newNote();
        displayGameState();

        if(savedInstanceState != null) currentIndex = savedInstanceState.getInt("currentIndex", -1);
        if(currentIndex == -1) currentIndex = gameSettings.random();
        noteTextView.setText(gameSettings.get(currentIndex).resource);

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
        if(gameState.isRunning()) {
            scoreTime.setText(secondsToString(gameState.timeRemaining()));
        }
        else {
            scoreTime.setText(secondsToString(gameState.timeEllapsed()));
        }
        scoreNote.setText(String.valueOf(gameState.getNoteScore()));
    }

    private MidiNumber.Listener midiNumberListener = new MidiNumber.Listener() {
        @Override
        public void onConnectedChanged(boolean connected, String name) {
            String text;
            if(connected) text = "Connected: " + name;
            else text = "Disconnected";
            Toast.makeText(GameActivity.this, text, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onNumber(final int number) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    String text = "Key #" + String.valueOf(number);
                    Toast.makeText(GameActivity.this, text, Toast.LENGTH_SHORT).show();
                    if(number == gameSettings.get(currentIndex).number) {
                        currentIndex = gameSettings.random(currentIndex);
                        noteTextView.setText(gameSettings.get(currentIndex).resource);
                    }
                }
            });
        }
    };

    public void nextButtonPressed(View view) {
        currentIndex = gameSettings.random(currentIndex);
        noteTextView.setText(gameSettings.get(currentIndex).resource);
    }
}
