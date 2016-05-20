package vi.pdfscanner.activity;

import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.parceler.Parcels;

import java.util.List;
import java.util.Observable;

import butterknife.Bind;
import butterknife.ButterKnife;
import vi.pdfscanner.R;
import vi.pdfscanner.activity.adapters.MultiSelector;
import vi.pdfscanner.activity.adapters.NoteAdapter;
import vi.pdfscanner.activity.adapters.NoteGroupAdapter;
import vi.pdfscanner.activity.adapters.ParcelableSparseBooleanArray;
import vi.pdfscanner.db.DBManager;
import vi.pdfscanner.db.models.Note;
import vi.pdfscanner.db.models.NoteGroup;
import vi.pdfscanner.main.Const;
import vi.pdfscanner.manager.NotificationManager;
import vi.pdfscanner.manager.NotificationModel;
import vi.pdfscanner.manager.NotificationObserver;
import vi.pdfscanner.utils.AppUtility;
import vi.pdfscanner.utils.ItemOffsetDecoration;

public class NoteGroupActivity extends AppCompatActivity implements NotificationObserver {

    @Bind(R.id.noteGroup_rv)
    RecyclerView noteRecyclerView;

    @Bind(R.id.toolbar)
    Toolbar toolbar;


    private NoteGroup mNoteGroup;
    private MultiSelector multiSelector;

    public static final String IS_IN_ACTION_MODE = "IS_IN_ACTION_MODE";
    private ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_group);
        ButterKnife.bind(this);
        init();
        if (savedInstanceState != null) {
            restoreSavedState(savedInstanceState);
        }
    }

    private void restoreSavedState(Bundle savedInstanceState) {
        // Restores the checked states
        multiSelector.onRestoreInstanceState(savedInstanceState);

        // Restore the action mode
        boolean isInActionMode = savedInstanceState.getBoolean(IS_IN_ACTION_MODE);
        if (isInActionMode) {
            startActionMode();
            updateActionModeTitle();
        }
    }

    private void init() {
        registerNotifications();
        mNoteGroup = Parcels.unwrap(getIntent().getParcelableExtra(NoteGroup.class.getSimpleName()));

        multiSelector = new MultiSelector(noteRecyclerView);
        if(mNoteGroup!=null && mNoteGroup.notes.size()>0) {
            setUpNoteList(mNoteGroup.notes);
            setToolbar(mNoteGroup);
        }else
            finish();
    }

    private void updateActionModeTitle() {
        actionMode.setTitle(String.valueOf(multiSelector.getCount()));
    }

    private void startActionMode() {
//        toolbar.setVisibility(View.GONE);
        actionMode = startSupportActionMode(actionModeCallback);
    }

    private boolean isMultiSelectionEnabled() {
        return actionMode != null;
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    onDeleteOptionClicked();
                    mode.finish();
                    return true;
                case R.id.share:
                    onShareOptionClicked();
                    mode.finish();
                    return true;
                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
//            toolbar.setVisibility(View.VISIBLE);
            actionMode = null;
//            multiSelector.clearAll();

            NoteAdapter adapter = (NoteAdapter) noteRecyclerView.getAdapter();
            if(adapter!=null)
                adapter.setNormalChoiceMode();
        }
    };

    private void onShareOptionClicked() {
        NoteAdapter adapter = (NoteAdapter) noteRecyclerView.getAdapter();
        if(adapter!=null)
        {
            AppUtility.shareDocuments(this,adapter.getCheckedNotes());
        }
    }

    private void onDeleteOptionClicked() {
        AppUtility.askAlertDialog(this, Const.DELETE_ALERT_TITLE, Const.DELETE_ALERT_MESSAGE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ParcelableSparseBooleanArray checkItems = multiSelector.getCheckedItems();
                NoteAdapter adapter = (NoteAdapter) noteRecyclerView.getAdapter();
                if (adapter != null) {
                    adapter.deleteItems(checkItems);
                }

                if(mNoteGroup.getNotes().size()==0) {
                    DBManager.getInstance().deleteNoteGroup(mNoteGroup.id);
                    finish();
                }

                multiSelector.clearAll();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    private void setToolbar(NoteGroup mNoteGroup) {
        setSupportActionBar(toolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(mNoteGroup.name);
    }

    private void setUpNoteList(List<Note> notes) {
        noteRecyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        noteRecyclerView.setLayoutManager(gridLayoutManager);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.item_offset);
        noteRecyclerView.addItemDecoration(itemDecoration);

        NoteAdapter adapter = new NoteAdapter(notes, multiSelector);
        adapter.setCallback(new NoteAdapter.Callback() {
            @Override
            public void onItemClick(View view, int position, Note note) {
                if (isMultiSelectionEnabled()) {
                    multiSelector.checkView(view, position);
                    updateActionModeTitle();
                }
                else
                    openPreviewActivity(view, mNoteGroup, position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelectionEnabled()) {
                    startActionMode();
                }
                multiSelector.checkView(view, position);
                updateActionModeTitle();
            }
        });
        noteRecyclerView.setAdapter(adapter);

    }

    private void openPreviewActivity(View view, NoteGroup mNoteGroup, int position) {
        int[] screenLocation = new int[2];
        ImageView imageView = (ImageView) view.findViewById(R.id.note_iv);
        imageView.getLocationOnScreen(screenLocation);
        PreviewActivity.startPreviewActivity(mNoteGroup, position, this, screenLocation, imageView.getWidth(), imageView.getHeight());
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    @Override
    public void registerNotifications() {
        NotificationManager.getInstance().registerNotification(Const.NotificationConst.DELETE_DOCUMENT, this);
    }

    @Override
    public void deRegisterNotifications() {
        NotificationManager.getInstance().deRegisterNotification(Const.NotificationConst.DELETE_DOCUMENT, this);
    }

    @Override
    public void update(Observable observable, Object data) {
        NotificationModel notificationModel = (NotificationModel) data;
        switch (notificationModel.notificationName)
        {
            case Const.NotificationConst.DELETE_DOCUMENT:
                onDeleteDocument(notificationModel);
                break;
        }
    }

    private void onDeleteDocument(NotificationModel notificationModel) {
        Note note = (Note) notificationModel.request;
        if(note!=null)
        {
            NoteAdapter adapter = (NoteAdapter) noteRecyclerView.getAdapter();
            if(adapter!=null)
            {
                adapter.deleteItem(note);
            }
        }
    }

    public void onCameraClicked(View view) {
        int[] startingLocation = new int[2];
        view.getLocationOnScreen(startingLocation);
        startingLocation[0] += view.getWidth() / 2;
        CameraActivity.startCameraFromLocation(startingLocation, this, mNoteGroup);
        overridePendingTransition(0, 0);
    }
}
