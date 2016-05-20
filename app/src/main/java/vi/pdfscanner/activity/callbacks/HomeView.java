package vi.pdfscanner.activity.callbacks;

import java.util.List;

import vi.pdfscanner.db.models.NoteGroup;

/**
 * Created by droidNinja on 20/04/16.
 */
public interface HomeView extends BaseView{
    void loadNoteGroups(List<NoteGroup> noteGroups);

    void showEmptyMessage();
}
