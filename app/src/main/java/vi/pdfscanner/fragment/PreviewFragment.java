package vi.pdfscanner.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import vi.pdfscanner.R;
import vi.pdfscanner.activity.PreviewActivity;
import vi.pdfscanner.db.models.Note;
import vi.pdfscanner.db.models.NoteGroup;
import vi.pdfscanner.fragment.adapters.NotesPagerAdapter;

public class PreviewFragment extends BaseFragment {

    @Bind(R.id.photo_vp)
    ViewPager viewPager;

    private NoteGroup noteGroup;
    private int position;

    public static PreviewFragment newInstance(NoteGroup noteGroup, int position) {
        PreviewFragment fragment = new PreviewFragment();
        fragment.noteGroup = noteGroup;
        fragment.position = position;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
    }

    private void init() {
        NotesPagerAdapter notesPagerAdapter = new NotesPagerAdapter(getChildFragmentManager(),noteGroup.notes);
        viewPager.setAdapter(notesPagerAdapter);
        viewPager.setCurrentItem(position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    public void setNoteGroup(NoteGroup mNoteGroup, int position) {
        this.noteGroup = mNoteGroup;
        this.position = position;
    }

    public void onBackPressed() {
        Fragment page = getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.photo_vp + ":" + viewPager.getCurrentItem());
        // based on the current position you can then cast the page to the correct
        // class and call the method:
        if (page != null) {
            ((ImageFragment)page).onBackPressed();
        }
    }
}
