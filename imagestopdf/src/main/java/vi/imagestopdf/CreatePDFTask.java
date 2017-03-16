package vi.imagestopdf;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class CreatePDFTask extends AsyncTask<String, Integer, File> {

    private final Context context;
    private final ArrayList<File> files;
    private CreatePDFListener createPDFListener;
    private ProgressDialog progressDialog;

    public CreatePDFTask(Context context, ArrayList<File> files, CreatePDFListener createPDFListener) {
        this.context = context;
        this.files = files;
        this.createPDFListener = createPDFListener;
    }

    @Override
        protected void onPreExecute() {
            super.onPreExecute();


            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Please wait...");
            progressDialog.setMessage("Creating pdf...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(File s) {
            super.onPostExecute(s);
            progressDialog.dismiss();

            if(createPDFListener!=null)
                createPDFListener.onPDFGenerated(s,files.size());
        }

        @Override
        protected File doInBackground(String... params) {

            File outputFile = Utils.getOutputMediaFile(Utils.PDFS_PATH, Utils.getPDFName(files));

            Log.v("stage 1", "store the pdf in sd card");
            //t.append("store the pdf in sd card\n");

            Document document = new Document(PageSize.A4, 38, 38, 50, 38);

            Log.v("stage 2", "Document Created");
            //t.append("Document Created\n");
            Rectangle documentRect = document.getPageSize();

            try {

                PdfWriter.getInstance(document, new FileOutputStream(outputFile.getPath()));

                Log.v("Stage 3", "Pdf writer");
                //  t.append("Pdf writer\n");

                document.open();

                Log.v("Stage 4", "Document opened");
                // t.append("Document opened\n");

                for (int i = 0; i < files.size(); i++) {

                    Bitmap bmp = BitmapFactory.decodeFile(files.get(i).getPath());
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 70, stream);


                    Image image = Image.getInstance(files.get(i).getPath());

                    float scaler = ((document.getPageSize().getWidth()) / bmp.getWidth()) * 100;

                    image.scalePercent(scaler);


                    Log.v("Stage 6", "Image path adding");

                    image.setAbsolutePosition((documentRect.getWidth() - image.getScaledWidth()) / 2, (documentRect.getHeight() - image.getScaledHeight()) / 2);
                    Log.v("Stage 7", "Image Alignments");

//                    image.setBorder(Image.BOX);
//
//                    image.setBorderWidth(15);

                    document.add(image);

                    document.newPage();

                    publishProgress(i);

                }

                Log.v("Stage 8", "Image adding");
                // t.append("Image adding\n");

                document.close();

                Log.v("Stage 7", "Document Closed" + outputFile.getPath());
                //   t.append("Document Closed\n");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            document.close();

            return outputFile;
        }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressDialog.setProgress((values[0]+1)*100/ files.size());
        progressDialog.setTitle("Processing images (" + (values[0]+1) + "/" + files.size() + ")");

    }
}