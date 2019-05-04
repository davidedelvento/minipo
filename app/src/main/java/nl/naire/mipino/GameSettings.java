package nl.naire.mipino;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GameSettings {
    class NoteInfo {
        public final int resource;
        public final int number;
        final int letter;

        NoteInfo(int resource, int number, int letter) {
            this.resource = resource;
            this.number = number;
            this.letter = letter;
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

    private void clear() {
        ranges.clear();
        groups.clear();
        setup();
    }

    private void add(Range range) {
        ranges.add(range);
        setup();
    }

    private void add(Group group) {
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
        return notes.size();
    }

    public NoteInfo get(int index) {
        return notes.get(index);
    }

    public int random() {
        if (size() == 0) throw new IllegalStateException("No notes configured");
        return random.nextInt(notes.size());
    }

    public int random(int omitIndex) {
        int index = random.nextInt(notes.size() - 1);
        if (index >= omitIndex) index++;
        return index;
    }

    public int getDuration() {
        return duration;
    }

    private void setDuration(int duration) {
        this.duration = duration;
    }

    private void setup() {
        notes.clear();

        /* CMajor */
        if (groups.contains(Group.CMajor)) {

            if (ranges.contains(Range.Bass_D2B2)) {
                notes.addAll(Arrays.asList(
                        new NoteInfo(R.string.noteg_d2, 38, R.string.note_d),
                        new NoteInfo(R.string.noteg_e2, 40, R.string.note_e),
                        new NoteInfo(R.string.noteg_f2, 41, R.string.note_f),
                        new NoteInfo(R.string.noteg_g2, 43, R.string.note_g),
                        new NoteInfo(R.string.noteg_a2, 45, R.string.note_a),
                        new NoteInfo(R.string.noteg_b2, 47, R.string.note_b))
                );
            }

            if (ranges.contains(Range.Bass_C3C4)) {
                notes.addAll(Arrays.asList(
                        new NoteInfo(R.string.noteg_c3, 48, R.string.note_c),
                        new NoteInfo(R.string.noteg_d3, 50, R.string.note_d),
                        new NoteInfo(R.string.noteg_e3, 52, R.string.note_e),
                        new NoteInfo(R.string.noteg_f3, 53, R.string.note_f),
                        new NoteInfo(R.string.noteg_g3, 55, R.string.note_g),
                        new NoteInfo(R.string.noteg_a3, 57, R.string.note_a),
                        new NoteInfo(R.string.noteg_b3, 59, R.string.note_b),
                        new NoteInfo(R.string.noteg_c4, 60, R.string.note_c))
                );
            }

            if (ranges.contains(Range.Treble_C4B4)) {
                notes.addAll(Arrays.asList(
                        new NoteInfo(R.string.notef_c4, 60, R.string.note_c),
                        new NoteInfo(R.string.notef_d4, 62, R.string.note_d),
                        new NoteInfo(R.string.notef_e4, 64, R.string.note_e),
                        new NoteInfo(R.string.notef_f4, 65, R.string.note_f),
                        new NoteInfo(R.string.notef_g4, 67, R.string.note_g),
                        new NoteInfo(R.string.notef_a4, 69, R.string.note_a),
                        new NoteInfo(R.string.notef_b4, 71, R.string.note_b))
                );
            }

            if (ranges.contains(Range.Treble_C5B5)) {
                notes.addAll(Arrays.asList(
                        new NoteInfo(R.string.notef_c5, 72, R.string.note_c),
                        new NoteInfo(R.string.notef_d5, 74, R.string.note_d),
                        new NoteInfo(R.string.notef_e5, 76, R.string.note_e),
                        new NoteInfo(R.string.notef_f5, 77, R.string.note_f),
                        new NoteInfo(R.string.notef_g5, 79, R.string.note_g),
                        new NoteInfo(R.string.notef_a5, 81, R.string.note_a),
                        new NoteInfo(R.string.notef_b5, 83, R.string.note_b))
                );
            }

        }
    }

    private final Random random = new Random(System.currentTimeMillis());
    private final Set<Range> ranges = new HashSet<>();
    private final Set<Group> groups = new HashSet<>();
    private int duration;
    private final transient ArrayList<NoteInfo> notes = new ArrayList<>();
}
