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

import java.util.Random;

import nl.naire.mipino.R;

public class MainActivity extends AppCompatActivity {
    static class NoteInfo {
        NoteInfo(int resource, int number) {
            this.resource = resource;
            this.number = number;
        }
        public int resource;
        public int number;
    }

    private Random random = new Random();
    private static NoteInfo[] notes = {
        new NoteInfo(R.string.note_c4, 60),
        new NoteInfo(R.string.note_d4, 62),
        new NoteInfo(R.string.note_e4, 64),
        new NoteInfo(R.string.note_f4, 65),
        new NoteInfo(R.string.note_g4, 67),
        new NoteInfo(R.string.note_a4, 69),
        new NoteInfo(R.string.note_b4, 71),
    };
    private int currentIndex = -1;
    private TextView noteTextView;
    private MidiNumber midiNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        noteTextView = (TextView)findViewById(R.id.noteTextView);

        if(savedInstanceState != null) currentIndex = savedInstanceState.getInt("currentIndex", -1);
        if(currentIndex == -1) currentIndex = randomIndex();
        noteTextView.setText(notes[currentIndex].resource);

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
        if (id == R.id.action_settings) {
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
        currentIndex = savedInstanceState.getInt("currentIndex", randomIndex());
    }

    private MidiNumber.Listener midiNumberListener = new MidiNumber.Listener() {
        @Override
        public void onConnectedChanged(boolean connected, String name) {
            String text;
            if(connected) text = "Connected: " + name;
            else text = "Disconnected";
            Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onNumber(final int number) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    String text = "Key #" + String.valueOf(number);
                    Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
                    if(number == notes[currentIndex].number) {
                        currentIndex = randomIndex(currentIndex);
                        noteTextView.setText(notes[currentIndex].resource);
                    }
                }
            });
        }
    };

    public void nextButtonPressed(View view) {
        currentIndex = randomIndex(currentIndex);
        noteTextView.setText(notes[currentIndex].resource);
    }

    int randomIndex() {
        int index = random.nextInt(notes.length);
        return index;
    }

    int randomIndex(int skipNote) {
        int index = random.nextInt(notes.length-1);
        if(index >= skipNote) index++;
        return index;
    }

}
