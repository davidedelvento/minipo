package nl.naire.mipino;

import android.content.SharedPreferences;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GameSettings {
    class NoteInfo {
        public int resource;
        public int number;

        public NoteInfo(int resource, int number) {
            this.resource = resource;
            this.number = number;
        }
    }

    enum Range {
        Treble_C4B4,
        Treble_C5B5,
        Bass_C3B3,
        Bass_D2B2
    }

    enum Group {
        CMajor,
        Flat,
        Sharp
    }

    public GameSettings(SharedPreferences prefs) {
        clear();

        if(prefs.getBoolean("treble_c4b4", true)) add(Range.Treble_C4B4);
        if(prefs.getBoolean("treble_c5b5", false)) add(Range.Treble_C5B5);
        if(prefs.getBoolean("base_c3b3", false)) add(Range.Bass_C3B3);
        if(prefs.getBoolean("base_d2b2", false)) add(Range.Bass_D2B2);
        if(prefs.getBoolean("group_c_major", true)) add(Group.CMajor);
        if(prefs.getBoolean("group_flat", false)) add(Group.Flat);
        if(prefs.getBoolean("group_sharp", false)) add(Group.Sharp);
        setDuration(Integer.parseInt(prefs.getString("game_duration", "120")));
        if(size() == 0) {
            add(Range.Treble_C4B4);
        }
        if(size() == 0) {
            add(Group.CMajor);
        }
    }

    public void clear() {
        ranges.clear();
        groups.clear();
        setup();
    }

    public void add(Range range) {
        ranges.add(range);
        setup();
    }

    public void add(Group group) {
        groups.add(group);
        setup();
    }

    public void remove(Range range) {
        ranges.remove(range);
        setup();
    }

    public void remove(Group group) {
        groups.remove(group);
        setup();
    }

    public int size() {
        return notes.size() / 5;
    }

    public NoteInfo get(int index) {
        return notes.get(index);
    }

    public int random() {
        if (size() == 0) throw new IllegalStateException("No notes configured");
        return random.nextInt(size());
    }

    public int random(int omitIndex) {
        int index = random.nextInt(notes.size() - 1);
        if (index >= omitIndex) index++;
        return index;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    private void setup() {
        notes.clear();

        /* CMajor */
        if (groups.contains(Group.CMajor)) {

            if (ranges.contains(Range.Treble_C4B4)) {
                notes.addAll(Arrays.asList(
                        new NoteInfo(R.string.note_c4, 60),
                        new NoteInfo(R.string.note_d4, 62),
                        new NoteInfo(R.string.note_e4, 64),
                        new NoteInfo(R.string.note_f4, 65),
                        new NoteInfo(R.string.note_g4, 67),
                        new NoteInfo(R.string.note_a4, 69),
                        new NoteInfo(R.string.note_b4, 71))
                );
            }

            if (ranges.contains(Range.Treble_C5B5)) {
                notes.addAll(Arrays.asList(
                        new NoteInfo(R.string.note_c5, 72),
                        new NoteInfo(R.string.note_d5, 74),
                        new NoteInfo(R.string.note_e5, 76),
                        new NoteInfo(R.string.note_f5, 77),
                        new NoteInfo(R.string.note_g5, 79),
                        new NoteInfo(R.string.note_a5, 81),
                        new NoteInfo(R.string.note_b5, 83))
                );
            }

        }
    }

    private Random random = new Random();
    private Set<Range> ranges = new HashSet<Range>();
    private Set<Group> groups = new HashSet<Group>();
    private int duration;
    private transient ArrayList<NoteInfo> notes = new ArrayList<NoteInfo>();
}
