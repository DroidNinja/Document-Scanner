package vi.pdfscanner.fragment.adapters;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import vi.pdfscanner.db.models.Note;
import vi.pdfscanner.fragment.ImageFragment;

public class NotesPagerAdapter extends FragmentPagerAdapter {

    private final List<Note> notes;

    public NotesPagerAdapter(FragmentManager fragmentManager, List<Note> notes) {
            super(fragmentManager);
            this.notes = notes;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return notes.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return ImageFragment.newInstance(notes.get(position));
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Scan " + (position+1);
        }

    }