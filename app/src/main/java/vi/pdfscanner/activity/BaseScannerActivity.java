package vi.pdfscanner.activity;


import android.graphics.BitmapFactory;
import androidx.fragment.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.View;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

import vi.pdfscanner.databinding.ActivityBasePhotoBinding;
import vi.pdfscanner.db.models.NoteGroup;
import vi.pdfscanner.interfaces.PhotoSavedListener;
import vi.pdfscanner.manager.ImageManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import vi.pdfscanner.R;
import vi.pdfscanner.utils.RotatePhotoTask;

public abstract class BaseScannerActivity extends BaseActivity {

    protected String path;
    protected String name;
    protected Bitmap bitmap;

    private NoteGroup mNoteGroup;
    private ActivityBasePhotoBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBasePhotoBinding.inflate(getLayoutInflater());
        showActionBar();
        showBack();
        setContentView(R.layout.activity_base_photo);
        ButterKnife.bind(this);

        if (getIntent().hasExtra(EXTRAS.PATH)) {
            path = getIntent().getStringExtra(EXTRAS.PATH);
        } else {
            throw new RuntimeException("There is no path to image in extras");
        }
        if (getIntent().hasExtra(EXTRAS.NAME)) {
            name = getIntent().getStringExtra(EXTRAS.NAME);
        } else {
            throw new RuntimeException("There is no image name in extras");
        }

        mNoteGroup = Parcels.unwrap(getIntent().getParcelableExtra(NoteGroup.class.getSimpleName()));

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bitmap == null || bitmap.isRecycled()) {
            loadPhoto();
        }
    }

    protected NoteGroup getNoteGroupFromIntent()
    {
        return mNoteGroup;
    }

    protected abstract void showPhoto(Bitmap bitmap);

    protected void rotatePhoto(float angle) {

            new RotatePhotoTask(path, angle, new PhotoSavedListener() {
                @Override
                public void photoSaved(String path, String name) {
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    if(bitmap!=null) {
                        showPhoto(bitmap);
                        setResult(EXTRAS.RESULT_EDITED, setIntentData());
                    }
                }

                @Override
                public void onNoteGroupSaved(NoteGroup noteGroup) {

                }
            }).execute();
    }

    protected void deletePhoto() {
        setResult(EXTRAS.RESULT_DELETED, setIntentData());
        finish();
    }

    protected void loadPhoto() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ImageManager.i.loadPhoto(path, metrics.widthPixels, metrics.heightPixels, loadingTarget);
    }

    protected void setFragment(Fragment fragment, String name) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_content, fragment);
        fragmentTransaction.commit();
    }

    private Target loadingTarget = new Target() {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            binding.progress.setVisibility(View.GONE);
            BaseScannerActivity.this.bitmap = bitmap;
            showPhoto(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            binding.progress.setVisibility(View.GONE);
            bitmap = null;
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            binding.progress.setVisibility(View.VISIBLE);
        }

    };

    protected Intent setIntentData() {
        return setIntentData(null);
    }

    protected Intent setIntentData(Intent intent) {
        if (intent == null) {
            intent = new Intent();
        }
        intent.putExtra(EXTRAS.PATH, path);
        intent.putExtra(EXTRAS.NAME, name);
        return intent;
    }

    public static final class EXTRAS {

        public static final String PATH = "path";

        public static final String NAME = "name";

        public static final String FROM_CAMERA = "from_camera";

        public static final int REQUEST_PHOTO_EDIT = 7338;

        public static final int RESULT_EDITED = 338;

        public static final int RESULT_DELETED = 3583;

        public static final String CROPPED_PATH = "CROPPED_PATH";

        public static final int FILTER_REQUEST_CODE = 3422;

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EXTRAS.FILTER_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    mNoteGroup = Parcels.unwrap(data.getParcelableExtra(NoteGroup.class.getSimpleName()));
                    if (mNoteGroup != null) {
                        setResult(mNoteGroup);
                        finish();
                    }
                }
                break;
        }
    }

    protected void setResult(NoteGroup noteGroup)
    {
        Intent intent = new Intent();
        intent.putExtra(NoteGroup.class.getSimpleName(), Parcels.wrap(noteGroup));
        setResult(RESULT_OK, intent);
    }
}
