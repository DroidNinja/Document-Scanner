package vi.pdfscanner.manager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.TextUtils;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import com.squareup.picasso.PicassoTools;

import vi.pdfscanner.interfaces.Initializer;
import vi.pdfscanner.interfaces.PhotoSavedListener;
import vi.pdfscanner.interfaces.StorageCallback;
import vi.pdfscanner.utils.CropPhotoTask;
import vi.pdfscanner.utils.ManagedTarget;
import vi.pdfscanner.utils.RotatePhotoTask;
import vi.pdfscanner.utils.ScaleTransformation;

public enum ImageManager implements Initializer, StorageCallback {
    i;

    private Context context;
    private Picasso picasso;

    private HashSet<ManagedTarget> targets;
    private Map<String, WeakReference<Bitmap>> bitmapMap;

    @Override
    public void init(Context context) {
        this.context = context;
        this.picasso = Picasso.with(context);
        bitmapMap = new HashMap<>();
        targets = new HashSet<>();
    }

    public void loadPhoto(String path, int width, int height, Target target) {
        File photo = !TextUtils.isEmpty(path) ? new File(path) : null;
        if (path == null) {
            target.onBitmapFailed(null);
        }
        Bitmap bitmap = getBitmap(path);
        if (bitmap != null && !bitmap.isRecycled()) {
            target.onBitmapLoaded(bitmap, Picasso.LoadedFrom.MEMORY);
        } else {
            ManagedTarget managedTarget = new ManagedTarget(target, path, this);
            Picasso.with(context)
                    .load(photo)
                    .skipMemoryCache()
                    .config(Bitmap.Config.ARGB_8888)
                    .transform(new ScaleTransformation(width, height))
                    .into(managedTarget);
        }
    }

    public void cropBitmap(String path, Bitmap croppedBitmap, PhotoSavedListener callback) {
        setBitmap(path, croppedBitmap);
        new CropPhotoTask(path, croppedBitmap, callback).execute();
    }

    public Bitmap rotatePhoto(String path, float angle) {
        Bitmap bitmap = getBitmap(path);
        if (bitmap != null && !bitmap.isRecycled()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        setBitmap(path, bitmap);
        new RotatePhotoTask(path, angle, null).execute();

        return bitmap;
    }

    public Bitmap getBitmap(String path) {
        return bitmapMap.get(path) != null ? bitmapMap.get(path).get() : null;
    }

    @Override
    public void clear() {
        synchronized (bitmapMap) {
            for (WeakReference<Bitmap> reference : bitmapMap.values()) {
                if (reference != null) {
                    Bitmap bitmap = reference.get();
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
            }
            bitmapMap.clear();
        }
        PicassoTools.clearCache(picasso);
    }

    @Override
    public void setBitmap(String path, Bitmap bitmap) {
        bitmapMap.put(path, new WeakReference<>(bitmap));
    }

    @Override
    public void addTarget(ManagedTarget target) {
        removeTarget(target);
        targets.add(target);
    }

    @Override
    public void removeTarget(ManagedTarget target) {
        if (targets.contains(target)) {
            targets.remove(target);
        }
    }

}
