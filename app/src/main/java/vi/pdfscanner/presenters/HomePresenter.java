package vi.pdfscanner.presenters;

import android.app.Activity;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

import vi.pdfscanner.R;
import vi.pdfscanner.activity.callbacks.HomeView;
import vi.pdfscanner.db.DBManager;
import vi.pdfscanner.db.models.NoteGroup;

/**
 * Created by droidNinja on 20/04/16.
 */
public class HomePresenter implements Presenter<HomeView> {

    private HomeView homeView;

    @Override
    public void attachView(HomeView view) {
        this.homeView = view;
    }

    @Override
    public void detachView() {
        this.homeView = null;
    }

    public void loadNoteGroups()
    {
        List<NoteGroup> noteGroups = DBManager.getInstance().getAllNoteGroups();

        if(noteGroups==null || noteGroups.size()==0)
            homeView.showEmptyMessage();
        else
            homeView.loadNoteGroups(noteGroups);
    }

    public void showRenameDialog(final NoteGroup noteGroup)
    {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(homeView.getContext());
        LayoutInflater inflater = ((Activity)homeView.getContext()).getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.rename_alert_layout, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Rename Document");
        dialogBuilder.setPositiveButton("Done", null);
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final EditText editText = (EditText) dialogView.findViewById(R.id.rename_et);

        final AlertDialog alertDialog = dialogBuilder.create();

        final View.OnClickListener myListener = new View.OnClickListener(){
            public void onClick(View v){
                String text = editText.getText().toString().trim();
                if(!TextUtils.isEmpty(text)) {
                    DBManager.getInstance().updateNoteGroupName(noteGroup.id, text);
                    loadNoteGroups();
                    alertDialog.dismiss();
                }
                else
                    editText.setError("Please enter a valid name!");
            }
        };



        alertDialog.setOnShowListener(new DialogInterface.OnShowListener(){

        public void onShow(DialogInterface dialog){
            Button b = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            b.setOnClickListener(myListener);
        }

    });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
               dialog.dismiss();
            }
        });

        alertDialog.show();
    }
}
