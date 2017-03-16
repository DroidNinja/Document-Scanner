package vi.imagestopdf;

import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by droidNinja on 25/07/16.
 */
public class Utils {
    private static final String IMAGE_TO_PDF_MODULE = "IMAGE_TO_PDF_MODULE";

    public static final String PDFS_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + ".DocScanner" + File.separator + "PDFs";

    public static File getOutputMediaFile(String path, String name) {
        // To be safe, we should check that the SDCard is mounted
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(IMAGE_TO_PDF_MODULE,"External storage " + Environment.getExternalStorageState());
            return null;
        }

        File dir = new File(path);
        // Create the storage directory if it doesn't exist
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(IMAGE_TO_PDF_MODULE,"Failed to create directory");
                return null;
            }
        }

        return new File(dir.getPath() + File.separator + name);
    }

    public static String getPDFName(ArrayList<File> files)
    {
        String fileName  = "";
        for (int i = 0; i < files.size(); i++) {
            fileName += "_" + files.get(i).getName();
        }
        Log.i("name",fileName);
        String md5 = getMd5(fileName);
        return "PDF_"+ md5 + ".pdf";
    }

    public static File getPDFFileFromName(String pdfName)
    {
        return new File(Utils.PDFS_PATH + File.separator + pdfName);
    }

    public static String getMd5(String text)
    {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        md.update(text.getBytes());

        byte byteData[] = md.digest();

        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
