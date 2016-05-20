package vi.pdfscanner.activity.adapters;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Helper class to keep track of the checked items.
 */
public class MultiSelector {
    private static final String CHECKED_STATES = "checked_states";
    private RecyclerView recyclerView;

    public MultiSelector(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    private ParcelableSparseBooleanArray checkedItems = new ParcelableSparseBooleanArray();


    public void onSaveInstanceState(Bundle state) {
        if (state != null) {
            state.putParcelable(CHECKED_STATES, checkedItems);
        }
    }

    public void onRestoreInstanceState(Bundle state) {
        if (state != null) {
            checkedItems = state.getParcelable(CHECKED_STATES);
        }
    }

    public void checkView(View view, int position) {
        boolean isChecked = isChecked(position);
        onChecked(position, !isChecked);
        view.setActivated(!isChecked);
    }

    public boolean isChecked(int position) {
        return checkedItems.get(position, false);
    }

    private void onChecked(int position, boolean isChecked) {
        if (isChecked) {
            checkedItems.put(position, true);
        } else {
            checkedItems.delete(position);
        }
    }

    public ParcelableSparseBooleanArray getCheckedItems()
    {
        return checkedItems;
    }

    public int getCount() {
        return checkedItems.size();
    }

    public void clearAll() {
        for (int i = 0; i < checkedItems.size(); i++) {
            int position = checkedItems.keyAt(i);
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
            if (viewHolder != null) {
                viewHolder.itemView.setActivated(false);
            }
        }
        checkedItems.clear();
    }
}