
package vi.pdfscanner.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import vi.pdfscanner.interfaces.StorageCallback;

public class ManagedTarget implements Target {

    private Target target;
    private String path;
    private StorageCallback callback;

    public ManagedTarget(Target target, String path, StorageCallback callback) {
        this.target = target;
        this.path = path;
        this.callback = callback;
        callback.addTarget(this);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        target.onBitmapLoaded(bitmap, from);
        callback.setBitmap(path, bitmap);
        callback.removeTarget(this);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        target.onBitmapFailed(errorDrawable);
        callback.removeTarget(this);
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        target.onPrepareLoad(placeHolderDrawable);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        ManagedTarget other = (ManagedTarget) o;
        return other.path.equals(path);
    }

}
