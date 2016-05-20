package vi.pdfscanner.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

import vi.pdfscanner.interfaces.PhotoSavedListener;
import vi.pdfscanner.main.CameraConst;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;
import vi.pdfscanner.manager.ImageManager;

public class RotatePhotoTask extends AsyncTask<Void, Void, Void> {

    private String path;
    private float angle;
    private PhotoSavedListener callback;

    public RotatePhotoTask(String path, float angle, PhotoSavedListener callback) {
        this.path = path;
        this.angle = angle;
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Bitmap bitmap = BitmapFactory.decodeFile(path); // todo NPE
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(path));

            bitmap.compress(Bitmap.CompressFormat.JPEG, CameraConst.COMPRESS_QUALITY, fos);

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
        ImageManager.i.setBitmap(path,bitmap);
        bitmap.recycle();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (callback != null) {
            callback.photoSaved(path, null);
        }
    }
}
