package vi.pdfscanner.main;

import android.os.Environment;

import java.io.File;

public final class Const {

    public static final boolean DEBUG = true;
    public static final String DELETE_ALERT_TITLE = "Delete Document";
    public static final String DELETE_ALERT_MESSAGE = "Are you sure you want to delete?";

    public static final class FOLDERS {

        private static final String ROOT = File.separator + ".DocScanner";

        private static final String CROP = ROOT + File.separator + "CroppedImages";

        private static final String SD_CARD_PATH = Environment.getExternalStorageDirectory().getPath();

        public static final String PATH = SD_CARD_PATH + ROOT;

        public static final String CROP_IMAGE_PATH = SD_CARD_PATH + CROP;

    }

    public static final class NotificationConst {

        public static final String DELETE_DOCUMENT = "DELETE_DOCUMENT";
    }

}
