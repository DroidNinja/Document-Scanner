package vi.pdfscanner.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;


import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import vi.pdfscanner.R;
import vi.pdfscanner.activity.adapters.MultiSelector;
import vi.pdfscanner.activity.adapters.NoteGroupAdapter;
import vi.pdfscanner.activity.adapters.ParcelableSparseBooleanArray;
import vi.pdfscanner.activity.callbacks.HomeView;
import vi.pdfscanner.databinding.ActivityMainBinding;
import vi.pdfscanner.db.models.NoteGroup;
import vi.pdfscanner.main.Const;
import vi.pdfscanner.presenters.HomePresenter;
import vi.pdfscanner.utils.AppUtility;
import vi.pdfscanner.utils.ItemOffsetDecoration;

public class HomeActivity extends BaseActivity implements HomeView{

    HomePresenter homePresenter;

    public static final String IS_IN_ACTION_MODE = "IS_IN_ACTION_MODE";
    private MultiSelector multiSelector;
    private ActionMode actionMode;
    ActivityMainBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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

    private void updateActionModeTitle() {
        actionMode.setTitle(String.valueOf(multiSelector.getCount()));
    }

    private void startActionMode() {
        actionMode = startSupportActionMode(actionModeCallback);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        multiSelector.onSaveInstanceState(outState);
        outState.putBoolean(IS_IN_ACTION_MODE, actionMode != null);
    }

    private void init() {
        homePresenter = new HomePresenter();
        homePresenter.attachView(this);

        setUpNoteGroupList();


        homePresenter.loadNoteGroups();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setUpNoteGroupList() {
        multiSelector = new MultiSelector(binding.noteGroupRv);

        binding.noteGroupRv.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.noteGroupRv.setLayoutManager(gridLayoutManager);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.item_offset);
        binding.noteGroupRv.addItemDecoration(itemDecoration);

        NoteGroupAdapter adapter = new NoteGroupAdapter(this, multiSelector);
        adapter.setCallback(new NoteGroupAdapter.Callback() {
            @Override
            public void onItemClick(View view, int position, NoteGroup noteGroup) {
                if (isMultiSelectionEnabled()) {
                    multiSelector.checkView(view, position);
                    updateActionModeTitle();
                    if(multiSelector.getCount()>1)
                        showEditActionMode(false);
                    else
                        showEditActionMode(true);
                }
                else
                    openNoteGroupActivity(noteGroup);
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
        binding.noteGroupRv.setAdapter(adapter);
    }

    private void showEditActionMode(boolean b) {
        if(actionMode!=null)
        {
            MenuItem menuItem = actionMode.getMenu().findItem(R.id.edit);
            if(menuItem!=null)
                menuItem.setVisible(b);
        }
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
                case R.id.edit:
                    onEditOptionClicked();
                    mode.finish();
                    return true;

                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
//            multiSelector.clearAll();

            NoteGroupAdapter adapter = (NoteGroupAdapter) binding.noteGroupRv.getAdapter();
            if(adapter!=null)
                adapter.setNormalChoiceMode();
        }
    };

    private void onEditOptionClicked() {
        NoteGroupAdapter adapter = (NoteGroupAdapter) binding.noteGroupRv.getAdapter();
        if(adapter!=null)
        {
            NoteGroup noteGroup  = adapter.getCheckedNoteGroup();
            if(noteGroup!=null) {
                homePresenter.showRenameDialog(noteGroup);
            }
        }
    }

    private void onShareOptionClicked() {
        NoteGroupAdapter adapter = (NoteGroupAdapter) binding.noteGroupRv.getAdapter();
        if(adapter!=null)
        {
            AppUtility.shareDocuments(this,adapter.getCheckedNoteGroups());
        }
    }

    private void onDeleteOptionClicked() {
        final ParcelableSparseBooleanArray checkItems = multiSelector.getCheckedItems();
        AppUtility.askAlertDialog(this, Const.DELETE_ALERT_TITLE, Const.DELETE_ALERT_MESSAGE, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        NoteGroupAdapter adapter = (NoteGroupAdapter) binding.noteGroupRv.getAdapter();
                        if (adapter != null) {
                            adapter.deleteItems(checkItems);
                            homePresenter.loadNoteGroups();
                        }
                        multiSelector.clearAll();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    private void openNoteGroupActivity(NoteGroup noteGroup) {
        Intent intent = new Intent(this, NoteGroupActivity.class);
        intent.putExtra(NoteGroup.class.getSimpleName(), Parcels.wrap(noteGroup));
        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
    }

    @Override
    protected void onDestroy() {
        homePresenter.detachView();
        super.onDestroy();
    }

    public void onCameraClicked(View view) {
        int[] startingLocation = new int[2];
        view.getLocationOnScreen(startingLocation);
        startingLocation[0] += view.getWidth() / 2;
        CameraActivity.startCameraFromLocation(startingLocation, this, null);
        overridePendingTransition(0, 0);
    }

    @Override
    public void loadNoteGroups(List<NoteGroup> noteGroups) {
        NoteGroupAdapter adapter = (NoteGroupAdapter) binding.noteGroupRv.getAdapter();
        adapter.setNoteGroups(noteGroups);
        adapter.notifyDataSetChanged();
        binding.noteGroupRv.requestFocus();

        binding.noteGroupRv.setVisibility(View.VISIBLE);
        binding.emptyView.setVisibility(View.GONE);
        binding.progress.setVisibility(View.GONE);
    }

    @Override
    public void showEmptyMessage() {
        binding.noteGroupRv.setVisibility(View.GONE);
        binding.emptyView.setVisibility(View.VISIBLE);
        binding.progress.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(homePresenter!=null)
            homePresenter.loadNoteGroups();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.context_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    public void onImportGalleryClicked(MenuItem item) {
        selectImageFromGallery(null);
    }

    public void onRateUsClicked(MenuItem item) {
        AppUtility.rateOnPlayStore(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
    }

    @Override
    public void onBackPressed() {
        AppUtility.askAlertDialog(this, "Don't forget to rate us.", "Press YES to rate or NO to exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppUtility.rateOnPlayStore(HomeActivity.this);
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                });
    }
}
