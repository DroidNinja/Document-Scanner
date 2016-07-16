package vi.pdfscanner.main;

import android.graphics.Bitmap;

/**
 * Created by droidNinja on 18/04/16.
 */
public class ScannerEngine {
    private static ScannerEngine ourInstance = new ScannerEngine();

    public static final int GRAY_MODE = 0x1;
    public static final int MAGIC_MODE = 0x2;
    public static final int BW_MODE = 0x3;

    public static ScannerEngine getInstance() {
        return ourInstance;
    }

    private ScannerEngine() {
    }

    public native Bitmap getScannedBitmap(Bitmap bitmap, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4);

    public native Bitmap getGrayBitmap(Bitmap bitmap);

    public native Bitmap getMagicColorBitmap(Bitmap bitmap);

    public native Bitmap getBWBitmap(Bitmap bitmap);

    public native float[] getPoints(Bitmap bitmap);

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("Scanner");
    }
}
