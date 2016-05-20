/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Zillow
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package vi.pdfscanner.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import vi.pdfscanner.db.DBManager;
import vi.pdfscanner.db.models.NoteGroup;
import vi.pdfscanner.interfaces.PhotoSavedListener;
import vi.pdfscanner.main.CameraConst;


public class SavingBitmapTask extends AsyncTask<Void, Void, Void> {

    private Bitmap bitmap;
    private String path;
    private PhotoSavedListener callback;
    private NoteGroup noteGroup;

    public SavingBitmapTask(NoteGroup noteGroup, Bitmap bitmap, String path, PhotoSavedListener callback) {
        this.bitmap = bitmap;
        this.path = path;
        this.callback = callback;
        this.noteGroup = noteGroup;
    }

    @Override
    protected Void doInBackground(Void... params) {
        FileOutputStream fos = null;
        try {
            File file = new File(path);
            fos = new FileOutputStream(file);

            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, CameraConst.COMPRESS_QUALITY, fos);
            }

            if(noteGroup!=null) {
                noteGroup = DBManager.getInstance().insertNote(noteGroup, file.getName());
            }
            else
            {
                noteGroup = DBManager.getInstance().createNoteGroup(file.getName());
            }

        } catch (FileNotFoundException e) {
            Timber.e(e, "File not found: " + e.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Timber.e(e, e.getMessage());
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (callback != null) {
            callback.photoSaved(path, null);
            callback.onNoteGroupSaved(noteGroup);
        }
    }
}
