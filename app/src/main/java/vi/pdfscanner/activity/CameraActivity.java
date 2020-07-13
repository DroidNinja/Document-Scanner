
package vi.pdfscanner.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

import vi.pdfscanner.R;
import vi.pdfscanner.databinding.ActivityWithFragmentBinding;
import vi.pdfscanner.db.models.NoteGroup;
import vi.pdfscanner.interfaces.CameraParamsChangedListener;
import vi.pdfscanner.interfaces.KeyEventsListener;
import vi.pdfscanner.interfaces.PhotoSavedListener;
import vi.pdfscanner.interfaces.PhotoTakenCallback;
import vi.pdfscanner.interfaces.RawPhotoTakenCallback;
import vi.pdfscanner.main.CameraConst;

import vi.pdfscanner.camera.fragments.CameraFragment;
import vi.pdfscanner.main.Const;
import vi.pdfscanner.manager.ImageManager;
import vi.pdfscanner.manager.SharedPrefManager;
import timber.log.Timber;
import vi.pdfscanner.utils.PhotoUtil;
import vi.pdfscanner.utils.SavingPhotoTask;
import vi.pdfscanner.utils.TransformAndSaveTask;
import vi.pdfscanner.views.RevealBackgroundView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends BaseActivity implements RevealBackgroundView.OnStateChangeListener, PhotoTakenCallback, PhotoSavedListener, RawPhotoTakenCallback, CameraParamsChangedListener {

    public static final String PATH = "path";
    public static final String USE_FRONT_CAMERA = "use_front_camera";
    public static final String OPEN_PHOTO_PREVIEW = "open_photo_preview";
    public static final String LAYOUT_ID = "layout_id";
    public static final String CAPTURE_MODE = "CAPTURE_MODE";

    private static final String IMG_PREFIX = "IMG_";
    private static final String IMG_POSTFIX = ".jpg";
    private static final String TIME_FORMAT = "yyyyMMdd_HHmmss";

    public static final int CAMERA_REQUEST_CODE = 0x9812;

    private KeyEventsListener keyEventsListener;
    private PhotoSavedListener photoSavedListener;

    private String path;
    private boolean openPreview;

    private boolean saving;

    private int captureMode = CameraConst.CAPTURE_SINGLE_MODE;
    private NoteGroup mNoteGroup;
    private CameraFragment fragment;
    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";

    ActivityWithFragmentBinding binding;
    
    public static void startCameraFromLocation(int[] startingLocation, Activity startingActivity, NoteGroup mNoteGroup) {
        Intent intent = new Intent(startingActivity, CameraActivity.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        intent.putExtra(CameraActivity.PATH, Const.FOLDERS.PATH);
        intent.putExtra(CameraActivity.CAPTURE_MODE, CameraConst.CAPTURE_SINGLE_MODE);
        intent.putExtra(CameraActivity.USE_FRONT_CAMERA, false);
        if(mNoteGroup!=null)
            intent.putExtra(NoteGroup.class.getSimpleName(), Parcels.wrap(mNoteGroup));

        startingActivity.startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWithFragmentBinding.inflate(getLayoutInflater());
        hideActionBar();
        setContentView(binding.getRoot());


        setupRevealBackground(savedInstanceState);

        if (TextUtils.isEmpty(path = getIntent().getStringExtra(PATH))) {
            path = Environment.getExternalStorageDirectory().getPath();
        }
        openPreview = getIntent().getBooleanExtra(OPEN_PHOTO_PREVIEW, SharedPrefManager.i.isOpenPhotoPreview());
        if (openPreview != SharedPrefManager.i.isOpenPhotoPreview()) {
            SharedPrefManager.i.setOpenPhotoPreview(openPreview);
        }

        captureMode = getIntent().getIntExtra(CAPTURE_MODE, -1);
        if(captureMode==-1)
            captureMode = CameraConst.CAPTURE_SINGLE_MODE;

        boolean useFrontCamera = getIntent().getBooleanExtra(USE_FRONT_CAMERA, SharedPrefManager.i.useFrontCamera());
        if (useFrontCamera != SharedPrefManager.i.useFrontCamera()) {
            SharedPrefManager.i.setUseFrontCamera(useFrontCamera);
        }

        mNoteGroup = Parcels.unwrap(getIntent().getParcelableExtra(NoteGroup.class.getSimpleName()));

        init();
    }

    private void setupRevealBackground(Bundle savedInstanceState) {
        binding.vRevealBackground.setFillPaintColor(getResources().getColor(R.color.colorAccent));
        binding.vRevealBackground.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
            binding.vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    binding.vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                    binding.vRevealBackground.startFromLocation(startingLocation);
                    return true;
                }
            });
        } else {
            binding.vRevealBackground.setToFinishedFrame();
        }
    }

    private void init() {

        int layoutId = getIntent().getIntExtra(LAYOUT_ID, -1);
        if (layoutId > 0) {
            fragment = CameraFragment.newInstance(layoutId, this, createCameraParams());
        } else {
            fragment = CameraFragment.newInstance(this, createCameraParams());
        }
        fragment.setParamsChangedListener(this);
        keyEventsListener = fragment;
        photoSavedListener = fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_content, fragment)
                .commit();
    }

    private Bundle createCameraParams() {
        Bundle bundle = new Bundle();

        bundle.putInt(CameraFragment.RATIO, SharedPrefManager.i.getCameraRatio());
        bundle.putInt(CameraFragment.FLASH_MODE, SharedPrefManager.i.getCameraFlashMode());
        bundle.putInt(CameraFragment.HDR_MODE, SharedPrefManager.i.isHDR());
        bundle.putInt(CameraFragment.QUALITY, 1); //medium quality
        bundle.putInt(CameraFragment.FOCUS_MODE, SharedPrefManager.i.getCameraFocusMode());
        bundle.putBoolean(CameraFragment.FRONT_CAMERA, SharedPrefManager.i.useFrontCamera());
        bundle.putInt(CameraFragment.CAPTURE_MODE, captureMode);

        return bundle;
    }

    private String createName() {
        String timeStamp = new SimpleDateFormat(TIME_FORMAT).format(new Date());
        return IMG_PREFIX + timeStamp + IMG_POSTFIX;
    }

    @Override
    public void photoTaken(byte[] data, int orientation) {
        savePhoto(data, createName(), path, orientation);
    }

    @Override
    public void rawPhotoTaken(byte[] data) {
        Timber.d("rawPhotoTaken: data[%1d]", data.length);
    }

    private void savePhoto(byte[] data, String name, String path, int orientation) {
        saving = true;
        new SavingPhotoTask(data, name, path, orientation, this).execute();
    }

    @Override
    public void photoSaved(String path, String name) {
        saving = false;
//        Toast.makeText(this, "Photo " + name + " saved", Toast.LENGTH_SHORT).show();
        Timber.d("Photo " + name + " saved");
        if (CameraConst.DEBUG) {
            printExifOrientation(path);
        }
        if (captureMode==CameraConst.CAPTURE_SINGLE_MODE) {
            if(fragment!=null)
                fragment.hideProcessingDialog();

            openPreview(path, name);
        }
        else
        {
            saveTransformedImage(path,name);
        }

        if (photoSavedListener != null) {
            photoSavedListener.photoSaved(path, name);
        }
    }

    @Override
    public void onNoteGroupSaved(NoteGroup noteGroup) {

    }

    private void saveTransformedImage(final String path, final String name) {
        Target loadingTarget = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                new TransformAndSaveTask(mNoteGroup,name,bitmap,  new PhotoSavedListener() {
                    String croppedPath ="";
                    @Override
                    public void photoSaved(String path, String name) {
//                        Toast.makeText(CameraActivity.this, "Photo " + name + " saved1", Toast.LENGTH_SHORT).show();
                        croppedPath = path;
                    }

                    @Override
                    public void onNoteGroupSaved(NoteGroup noteGroup) {
                        mNoteGroup = noteGroup;
                        if(fragment!=null)
                            fragment.setPreviewImage(croppedPath, noteGroup);
                    }
                }).execute();

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }

        };

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ImageManager.i.loadPhoto(path, metrics.widthPixels, metrics.heightPixels, loadingTarget);
    }

    private void openPreview(String path, String name) {
        Intent intent = new Intent(this, ScannerActivity.class);
        intent.putExtra(BaseScannerActivity.EXTRAS.PATH, path);
        intent.putExtra(BaseScannerActivity.EXTRAS.NAME, name);
        intent.putExtra(BaseScannerActivity.EXTRAS.FROM_CAMERA, true);
        if(mNoteGroup!=null)
            intent.putExtra(NoteGroup.class.getSimpleName(), Parcels.wrap(mNoteGroup));

        startActivityForResult(intent, BaseScannerActivity.EXTRAS.REQUEST_PHOTO_EDIT);
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BaseScannerActivity.EXTRAS.REQUEST_PHOTO_EDIT) {
            switch (resultCode) {
                case BaseScannerActivity.EXTRAS.RESULT_DELETED:
                    String path = data.getStringExtra(BaseScannerActivity.EXTRAS.PATH);
                    PhotoUtil.deletePhoto(path);
                    break;
                case RESULT_OK:
                    mNoteGroup = Parcels.unwrap(data.getParcelableExtra(NoteGroup.class.getSimpleName()));
                    if (mNoteGroup != null) {
                        Intent intent = new Intent();
                        intent.putExtra(NoteGroup.class.getSimpleName(), Parcels.wrap(mNoteGroup));
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    break;

            }
        }
    }

    private void printExifOrientation(String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Timber.d("Orientation: " + orientation);
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                keyEventsListener.zoomIn();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                keyEventsListener.zoomOut();
                return true;
            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                return true;
            case KeyEvent.KEYCODE_CAMERA:
                keyEventsListener.takePhoto();
                return true;
        }
        return false;
    }

    @Override
    public void onQualityChanged(int id) {
        SharedPrefManager.i.setCameraQuality(id);
    }

    @Override
    public void onRatioChanged(int id) {
        SharedPrefManager.i.setCameraRatio(id);
    }

    @Override
    public void onFlashModeChanged(int id) {
        SharedPrefManager.i.setCameraFlashMode(id);
    }

    @Override
    public void onHDRChanged(int id) {
        SharedPrefManager.i.setHDRMode(id);
    }

    @Override
    public void onFocusModeChanged(int id) {
        SharedPrefManager.i.setCameraFocusMode(id);
    }

    @Override
    public void onCaptureModeChanged(int mode) {
        captureMode = mode;
    }

    @Override
    public void onBackPressed() {
        if (!saving) {
            super.onBackPressed();
        }
    }

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            binding.fragmentContent.setVisibility(View.VISIBLE);

        } else {
            binding.fragmentContent.setVisibility(View.INVISIBLE);
        }
    }
}
