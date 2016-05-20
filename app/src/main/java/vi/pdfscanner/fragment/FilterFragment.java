package vi.pdfscanner.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import vi.pdfscanner.R;
import vi.pdfscanner.interfaces.ScanListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import vi.pdfscanner.main.ScannerEngine;
import vi.pdfscanner.views.PinchImageView;

public class FilterFragment extends BaseFragment {

    private Bitmap original;

    @Bind(R.id.photo)
    PinchImageView imageView;

    private ScanListener scanListener;

    @Bind(R.id.original_ib)
    TextView originalTextView;

    @Bind(R.id.magic_ib)
    TextView magicTextView;

    @Bind(R.id.gray_mode_ib)
    TextView grayTextView;

    @Bind(R.id.bw_mode_ib)
    TextView bwTextView;

    @Bind(R.id.progress)
    ProgressBar progressBar;
    private Bitmap transformed;

    public static FilterFragment newInstance(Bitmap bitmap) {
        FilterFragment fragment = new FilterFragment();
        fragment.original = bitmap;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_layout, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (original != null && !original.isRecycled()) {
            imageView.setImageBitmap(original);
            this.transformed = original;
        } else {
            imageView.setImageResource(R.drawable.no_image);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ScanListener) {
            scanListener = (ScanListener) activity;
        } else {
            throw new RuntimeException(activity.getClass().getName() + " must implement " + ScanListener.class.getName());
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.original = bitmap;
        imageView.setImageBitmap(bitmap);
    }

    @OnClick(R.id.ok_ib)
    public void onOKClicked(View view) {
        progressBar.setVisibility(View.VISIBLE);
        scanListener.onOkButtonClicked(transformed);
    }

    public void hideProgressBar()
    {
        progressBar.setVisibility(View.GONE);
    }

    @OnClick(R.id.rotate_right_ib)
    public void onRotateRightClicked(View view) {
       transformed = rotateBitmap(transformed,90);
        original = rotateBitmap(original,90);
        if(transformed!=null)
            imageView.setImageBitmap(transformed);
    }

    @OnClick(R.id.rotate_left_ib)
    public void onRotateLeftClicked(View view) {
        transformed = rotateBitmap(transformed,-90);
        original = rotateBitmap(original,-90);
        if(transformed!=null)
            imageView.setImageBitmap(transformed);
    }

    @OnClick(R.id.back_ib)
    public void onBackButtonClicked(View view) {
        scanListener.onBackClicked();
    }

    @OnClick(R.id.original_ib)
    public void onOrginalModeClicked(View view) {
        originalTextView.setSelected(true);
        magicTextView.setSelected(false);
        grayTextView.setSelected(false);
        bwTextView.setSelected(false);

        transformed = original;
        imageView.setImageBitmap(original);
    }

    @OnClick(R.id.magic_ib)
    public void onMagicModeClicked(View view) {
        originalTextView.setSelected(false);
        magicTextView.setSelected(true);
        grayTextView.setSelected(false);
        bwTextView.setSelected(false);

        new FilterImageTask(ScannerEngine.MAGIC_MODE).execute();
    }

    @OnClick(R.id.gray_mode_ib)
    public void onGrayModeClicked(View view) {
        originalTextView.setSelected(false);
        magicTextView.setSelected(false);
        grayTextView.setSelected(true);
        bwTextView.setSelected(false);

        new FilterImageTask(ScannerEngine.GRAY_MODE).execute();
    }

    @OnClick(R.id.bw_mode_ib)
    public void onBWModeClicked(View view) {
        originalTextView.setSelected(false);
        magicTextView.setSelected(false);
        grayTextView.setSelected(false);
        bwTextView.setSelected(true);

        new FilterImageTask(ScannerEngine.BW_MODE).execute();
    }

    private class FilterImageTask extends AsyncTask<Void, Void, Bitmap> {

        private int filterMode;
        public FilterImageTask(int mode) {
            this.filterMode = mode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            switch (filterMode)
            {
                case ScannerEngine.MAGIC_MODE:
                    transformed = ScannerEngine.getInstance().getMagicColorBitmap(original);
                    break;

                case ScannerEngine.GRAY_MODE:
                    transformed = ScannerEngine.getInstance().getGrayBitmap(original);
                    break;

                case ScannerEngine.BW_MODE:
                    transformed = ScannerEngine.getInstance().getBWBitmap(original);
                    break;
            }

            return transformed;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            progressBar.setVisibility(View.GONE);
            imageView.setImageBitmap(bitmap);
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int angle)
    {
        if (bitmap != null && !bitmap.isRecycled()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bitmap;
        }
        return null;
    }

}
