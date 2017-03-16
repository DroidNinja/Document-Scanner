package vi.pdfscanner.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;
import vi.pdfscanner.db.DBManager;
import vi.pdfscanner.db.models.NoteGroup;
import vi.pdfscanner.interfaces.DeletePhotoListener;
import vi.pdfscanner.interfaces.PhotoSavedListener;
import vi.pdfscanner.main.CameraConst;
import vi.pdfscanner.manager.ImageManager;

public class DeletePhotoTask extends AsyncTask<Void, Void, Void> {

    private final NoteGroup noteGroup;

    public DeletePhotoTask(NoteGroup noteGroup) {
        this.noteGroup = noteGroup;
    }

    @Override
    protected Void doInBackground(Void... params) {
        PhotoUtil.deleteNoteGroup(noteGroup);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
