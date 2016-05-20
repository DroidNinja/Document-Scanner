package vi.pdfscanner.db;

import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import java.util.Date;
import java.util.List;
import java.util.Timer;

import timber.log.Timber;
import vi.pdfscanner.db.models.Note;
import vi.pdfscanner.db.models.NoteGroup;
import vi.pdfscanner.db.models.NoteGroup_Table;
import vi.pdfscanner.db.models.Note_Table;

/**
 * Created by droidNinja on 19/04/16.
 */
public class DBManager {
    private static final String TAG = DBManager.class.getSimpleName();
    private static DBManager ourInstance = new DBManager();

    public static DBManager getInstance() {
        return ourInstance;
    }

    private DBManager() {
    }

    public NoteGroup createNoteGroup(String noteImageName)
    {
        NoteGroup noteGroup = new NoteGroup();
        noteGroup.name = "New Group";
        noteGroup.type = "Document";
        noteGroup.save();

        noteGroup = insertNote(noteGroup, noteImageName);
        return noteGroup;
    }

    public NoteGroup insertNote(NoteGroup noteGroup, String noteImageName)
    {
        if(noteGroup.id>0)
        {
            Note note = new Note();
            note.name = noteImageName;
            note.createdAt = new Date();
            note.associateNoteGroup(noteGroup);
            note.save();

            return getNoteGroup(noteGroup.id);
        }
        return null;
    }

    public List<NoteGroup> getAllNoteGroups()
    {
        List<NoteGroup> noteGroups = new Select().from(NoteGroup.class).queryList();
        for (NoteGroup notegroup :
                noteGroups) {
            notegroup.notes = notegroup.getNotes();
            Timber.i("size" + notegroup.notes.size(), notegroup.notes.size());
        }
        return noteGroups;
    }

    public NoteGroup getNoteGroup(int id)
    {
        NoteGroup noteGroup = SQLite.select()
                .from(NoteGroup.class)
                .where(NoteGroup_Table.id.eq(id))
                .querySingle();
        noteGroup.notes = noteGroup.getNotes();
        return noteGroup;
    }

    public void deleteNoteGroup(int id)
    {
        SQLite.delete(Note.class)
                .where(Note_Table.noteGroupId.eq(id))
                .query();
        SQLite.delete(NoteGroup.class)
                .where(NoteGroup_Table.id.eq(id))
                .query();
    }

    public void deleteNote(int id)
    {
        SQLite.delete(Note.class)
                .where(Note_Table.id.eq(id))
                .query();

    }

    public void updateNoteGroupName(int id, String name)
    {
        SQLite.update(NoteGroup.class)
                .set(NoteGroup_Table.name.eq(name))
                .where(NoteGroup_Table.id.eq(id))
                .query();
    }
}
