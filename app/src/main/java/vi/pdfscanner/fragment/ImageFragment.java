package vi.pdfscanner.fragment;


import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vi.pdfscanner.R;
import vi.pdfscanner.activity.PreviewActivity;
import vi.pdfscanner.databinding.FragmentImage2Binding;
import vi.pdfscanner.databinding.FragmentPreviewBinding;
import vi.pdfscanner.db.models.Note;
import vi.pdfscanner.db.models.NoteGroup;
import vi.pdfscanner.interfaces.PhotoSavedListener;
import vi.pdfscanner.main.Const;
import vi.pdfscanner.manager.NotificationManager;
import vi.pdfscanner.utils.AppUtility;
import vi.pdfscanner.utils.RotatePhotoTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageFragment extends Fragment {

    private static final long ANIM_DURATION = 600;
    private Note note;

    private int thumbnailTop;
    private int thumbnailLeft;
    private int thumbnailWidth;
    private int thumbnailHeight;
    private int mLeftDelta;
    private int mTopDelta;
    private float mWidthScale;
    private float mHeightScale;
    private FragmentImage2Binding binding;

    public ImageFragment() {
        // Required empty public constructor
    }

    public static ImageFragment newInstance(Note note) {
        ImageFragment fragment = new ImageFragment();
        fragment.note = note;

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentImage2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getActivity().getIntent().getExtras();
        thumbnailTop = bundle.getInt(PreviewActivity.TOP);
        thumbnailLeft = bundle.getInt(PreviewActivity.LEFT);
        thumbnailWidth = bundle.getInt(PreviewActivity.WIDTH);
        thumbnailHeight = bundle.getInt(PreviewActivity.HEIGHT);

        init();
    }

    @OnClick(R.id.share_ib)
    public void onShareButtonClicked()
    {
        AppUtility.shareDocument(getActivity(),note.getImagePath());
    }

    @OnClick(R.id.delete_ib)
    public void onDeleteButtonClicked()
    {
        AppUtility.askAlertDialog(getActivity(), Const.DELETE_ALERT_TITLE, Const.DELETE_ALERT_MESSAGE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NotificationManager.getInstance().raiseNotification(getActivity(), Const.NotificationConst.DELETE_DOCUMENT, note, null);
                getActivity().finish();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    @OnClick(R.id.rotate_right_ib)
    public void onRotateRightButtonClicked()
    {
        rotatePhoto(90);
    }

    @OnClick(R.id.rotate_left_ib)
    public void onRotateLeftButtonClicked()
    {
        rotatePhoto(-90);
    }

    private void rotatePhoto(float angle)
    {
        binding.progress.setVisibility(View.VISIBLE);
        new RotatePhotoTask(note.getImagePath().getPath(), angle, new PhotoSavedListener() {
            @Override
            public void photoSaved(String path, String name) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                if(bitmap!=null) {
                    binding.progress.setVisibility(View.GONE);
                    binding.previewIv.setImageBitmap(bitmap);
                }
            }

            @Override
            public void onNoteGroupSaved(NoteGroup noteGroup) {

            }
        }).execute();
    }

    private void init() {
        Picasso.with(getActivity()).load(note.getImagePath()).into(binding.previewIv);

        ViewTreeObserver observer = binding.previewIv.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                binding.previewIv.getViewTreeObserver().removeOnPreDrawListener(this);

                // Figure out where the thumbnail and full size versions are, relative
                // to the screen and each other
                int[] screenLocation = new int[2];
                binding.previewIv.getLocationOnScreen(screenLocation);
                mLeftDelta = thumbnailLeft - screenLocation[0];
                mTopDelta = thumbnailTop - screenLocation[1];

                // Scale factors to make the large version the same size as the thumbnail
                mWidthScale = (float) thumbnailWidth / binding.previewIv.getWidth();
                mHeightScale = (float) thumbnailHeight / binding.previewIv.getHeight();

                enterAnimation();

                return true;
            }
        });
    }

    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location.
     */
    public void enterAnimation() {

        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        binding.previewIv.setPivotX(0);
        binding.previewIv.setPivotY(0);
        binding.previewIv.setScaleX(mWidthScale);
        binding.previewIv.setScaleY(mHeightScale);
        binding.previewIv.setTranslationX(mLeftDelta);
        binding.previewIv.setTranslationY(mTopDelta);

        // interpolator where the rate of change starts out quickly and then decelerates.
        TimeInterpolator sDecelerator = new DecelerateInterpolator();

        // Animate scale and translation to go from thumbnail to full size
        binding.previewIv.animate().setDuration(ANIM_DURATION).scaleX(1).scaleY(1).
                translationX(0).translationY(0).setInterpolator(sDecelerator);

        // Fade in the black background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(new ColorDrawable(Color.BLACK), "alpha", 0, 255);
        bgAnim.setDuration(ANIM_DURATION);
        bgAnim.start();

    }

    /**
     * The exit animation is basically a reverse of the enter animation.
     * This Animate image back to thumbnail size/location as relieved from bundle.
     *
     * @param endAction This action gets run after the animation completes (this is
     *                  when we actually switch activities)
     */
    public void exitAnimation(final Runnable endAction) {

        TimeInterpolator sInterpolator = new AccelerateInterpolator();
        binding.previewIv.animate().setDuration(ANIM_DURATION).scaleX(mWidthScale).scaleY(mHeightScale).
                translationX(mLeftDelta).translationY(mTopDelta)
                .setInterpolator(sInterpolator).withEndAction(endAction);

        // Fade out background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(new ColorDrawable(Color.WHITE), "alpha", 0);
        bgAnim.setDuration(ANIM_DURATION);
        bgAnim.start();
    }

    public void onBackPressed() {
        exitAnimation(new Runnable() {
            public void run() {
                getActivity().finish();
            }
        });
    }
}
