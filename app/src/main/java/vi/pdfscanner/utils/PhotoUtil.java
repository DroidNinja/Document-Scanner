package vi.pdfscanner.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import java.io.File;

import vi.pdfscanner.db.models.NoteGroup;

public class PhotoUtil {

    public static void deletePhoto(String path) {
        File file = new File(path);
        file.delete();
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static int getScreenWidth(Context context)
    {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

    public static int getScreenHeight(Context context)
    {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    public static void deleteNoteGroup(NoteGroup noteGroup) {
        for(int index=0;index<noteGroup.notes.size();index++)
            PhotoUtil.deletePhoto(noteGroup.notes.get(index).getImagePath().getPath());
    }
}
