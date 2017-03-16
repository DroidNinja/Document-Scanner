package vi.imagestopdf;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by droidNinja on 25/07/16.
 */
public class PDFEngine {
    private static PDFEngine ourInstance = new PDFEngine();

    public static PDFEngine getInstance() {
        return ourInstance;
    }

    private PDFEngine() {
    }

    public void createPDF(Context context,ArrayList<File> files, CreatePDFListener createPDFListener)
    {
        new CreatePDFTask(context, files, createPDFListener).execute();
    }

    public void openPDF(Context context, File pdfFile)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(pdfFile), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }

    public boolean checkIfPDFExists(ArrayList<File> files, String pdfFileName)
    {
        if(pdfFileName!=null && pdfFileName.equals(Utils.getPDFName(files)))
        {
            File pdfFile = new File(Utils.PDFS_PATH + File.separator + pdfFileName);
            return pdfFile.exists();
        }

        return false;
    }

    public void sharePDF(Context context, File pdfFile)
    {
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);

        if(pdfFile.exists()) {
            intentShareFile.setType("application/pdf");
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Check out our app: https://goo.gl/FXwAmD");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse(pdfFile.getPath()));
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Shared via Document Scanner");

            context.startActivity(Intent.createChooser(intentShareFile, "Share PDF"));
        }
    }
}
