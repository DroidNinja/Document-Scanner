package vi.pdfscanner.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vi.pdfscanner.R;
import vi.pdfscanner.db.models.Note;

/**
 * Created by droidNinja on 19/04/16.
 */
public class AppUtility {
    /**
     * Create a File for saving an image
     */
    public static File getOutputMediaFile(String path, String name) {
        // To be safe, we should check that the SDCard is mounted
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Timber.e("External storage " + Environment.getExternalStorageState());
            return null;
        }

        File dir = new File(path);
        // Create the storage directory if it doesn't exist
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Timber.e("Failed to create directory");
                return null;
            }
        }

        return new File(dir.getPath() + File.separator + name);
    }

    public static void showErrorDialog(Context context, String errorMessage)
    {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        builder.setTitle("Oops!");
        builder.setMessage(errorMessage);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static void shareDocuments(Context context, ArrayList<Uri> uris)
    {
        Intent shareIntent = new Intent();
         shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
//        shareIntent.setAction(Intent.ACTION_SEND);
        // shareIntent.setType("image/jpeg");
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uris);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));

        context.startActivity(Intent.createChooser(shareIntent, "Share notes.."));
    }

    public static void shareDocument(Context context, Uri uri)
    {
        Intent shareIntent = new Intent();
//        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.setAction(Intent.ACTION_SEND);
        // shareIntent.setType("image/jpeg");
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));

        context.startActivity(Intent.createChooser(shareIntent, "Share notes.."));
    }

    public static void askAlertDialog(Context context,String title,String message, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener)
    {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", positiveListener)
                .setNegativeButton("No", negativeListener)
                .show();
    }

    public static void rateOnPlayStore(Context context)
    {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, " Unable to find Play Store", Toast.LENGTH_LONG).show();
        }
    }

    public static ArrayList<Uri> getUrisFromNotes(List<Note> notes)
    {
        ArrayList<Uri> uris = new ArrayList<>();
        for (int index = 0; index < notes.size(); index++) {
            uris.add(notes.get(index).getImagePath());
        }

        return uris;
    }
}
