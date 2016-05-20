package vi.pdfscanner.interfaces;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by droidNinja on 15/04/16.
 */
public interface ScanListener {
    void onRotateLeftClicked();

    void onRotateRightClicked();

    void onBackClicked();

    void onOkButtonClicked(Bitmap bitmap);
}
