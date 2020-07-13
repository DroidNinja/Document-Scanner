package vi.pdfscanner.activity;

import android.app.Activity;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import org.parceler.Parcels;

import vi.pdfscanner.R;
import vi.pdfscanner.db.models.NoteGroup;
import vi.pdfscanner.fragment.PreviewFragment;

public class PreviewActivity extends AppCompatActivity {


    public static final String POSITION = "position";
    public static final String LEFT = "left";
    public static final String TOP = "TOP";
    public static final String WIDTH = "WIDTH";
    public static final String HEIGHT = "HEIGHT";
    private PreviewFragment previewFragment;
    private NoteGroup mNoteGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_photo);

        init();
    }

    public static void startPreviewActivity(NoteGroup mNoteGroup, int position, Activity startingActivity, int[] screenLocation, int width, int height)
    {
        Intent intent = new Intent(startingActivity, PreviewActivity.class);
        intent.putExtra(NoteGroup.class.getSimpleName(), Parcels.wrap(mNoteGroup));
        intent.putExtra(PreviewActivity.POSITION, position);
        intent.putExtra(PreviewActivity.LEFT, screenLocation[0]);
        intent.putExtra(PreviewActivity.TOP, screenLocation[1]);
        intent.putExtra(PreviewActivity.WIDTH, width);
        intent.putExtra(PreviewActivity.HEIGHT, height);
        startingActivity.startActivity(intent);
    }

    private void init() {
        mNoteGroup = Parcels.unwrap(getIntent().getParcelableExtra(NoteGroup.class.getSimpleName()));
        if(mNoteGroup==null)
            finish();

        int position = getIntent().getIntExtra(POSITION,0);

        if (previewFragment == null) {
            previewFragment = PreviewFragment.newInstance(mNoteGroup, position);
            setFragment(previewFragment, PreviewFragment.class.getSimpleName());
        } else {
            previewFragment.setNoteGroup(mNoteGroup, position);
        }
    }

    private void setFragment(Fragment fragment, String name) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_content, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if(previewFragment!=null)
            previewFragment.onBackPressed();
    }
}
