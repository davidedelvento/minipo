package nl.naire.mipino;

import android.content.SharedPreferences;

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
        Bass_C3C4,
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
        if(prefs.getBoolean("base_c3c4", false)) add(Range.Bass_C3C4);
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

            if (ranges.contains(Range.Bass_D2B2)) {
                notes.addAll(Arrays.asList(
                        new NoteInfo(R.string.noteg_d2, 38),
                        new NoteInfo(R.string.noteg_e2, 40),
                        new NoteInfo(R.string.noteg_f2, 41),
                        new NoteInfo(R.string.noteg_g2, 43),
                        new NoteInfo(R.string.noteg_a2, 45),
                        new NoteInfo(R.string.noteg_b2, 47))
                );
            }

            if (ranges.contains(Range.Bass_C3C4)) {
                notes.addAll(Arrays.asList(
                        new NoteInfo(R.string.noteg_c3, 48),
                        new NoteInfo(R.string.noteg_d3, 50),
                        new NoteInfo(R.string.noteg_e3, 52),
                        new NoteInfo(R.string.noteg_f3, 53),
                        new NoteInfo(R.string.noteg_g3, 55),
                        new NoteInfo(R.string.noteg_a3, 57),
                        new NoteInfo(R.string.noteg_b3, 59),
                        new NoteInfo(R.string.noteg_c4, 60))
                );
            }

            if (ranges.contains(Range.Treble_C4B4)) {
                notes.addAll(Arrays.asList(
                        new NoteInfo(R.string.notef_c4, 60),
                        new NoteInfo(R.string.notef_d4, 62),
                        new NoteInfo(R.string.notef_e4, 64),
                        new NoteInfo(R.string.notef_f4, 65),
                        new NoteInfo(R.string.notef_g4, 67),
                        new NoteInfo(R.string.notef_a4, 69),
                        new NoteInfo(R.string.notef_b4, 71))
                );
            }

            if (ranges.contains(Range.Treble_C5B5)) {
                notes.addAll(Arrays.asList(
                        new NoteInfo(R.string.notef_c5, 72),
                        new NoteInfo(R.string.notef_d5, 74),
                        new NoteInfo(R.string.notef_e5, 76),
                        new NoteInfo(R.string.notef_f5, 77),
                        new NoteInfo(R.string.notef_g5, 79),
                        new NoteInfo(R.string.notef_a5, 81),
                        new NoteInfo(R.string.notef_b5, 83))
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
