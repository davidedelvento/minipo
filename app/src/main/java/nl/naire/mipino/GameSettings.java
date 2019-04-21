package nl.naire.mipino;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GameSettings implements Serializable {
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
        Bass_BD2B2
    }

    enum Group {
        CMajor,
        //Flat,
        //Sharp
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
        return notes.size();
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

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        setup();
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

        }
    }

    private Random random = new Random();
    private Set<Range> ranges = new HashSet<Range>();
    private Set<Group> groups = new HashSet<Group>();
    private int duration;
    private transient ArrayList<NoteInfo> notes = new ArrayList<NoteInfo>();
}
