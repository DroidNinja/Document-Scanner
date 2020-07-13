package vi.pdfscanner.activity.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import vi.pdfscanner.R;
import vi.pdfscanner.db.DBManager;
import vi.pdfscanner.db.models.NoteGroup;
import vi.pdfscanner.manager.ImageManager;
import vi.pdfscanner.utils.DeletePhotoTask;

/**
 * Created by droidNinja on 20/04/16.
 */
public class NoteGroupAdapter extends RecyclerView.Adapter<NoteGroupAdapter.NoteGroupViewHolder>{

    private final Context context;
    private final MultiSelector multiSelector;
    private List<NoteGroup> noteGroups;
    private Callback callback;
    private boolean isMultipleChoiceMode;

    public NoteGroupAdapter(Context context, MultiSelector multiSelector) {
        this.context = context;
        this.noteGroups = Collections.emptyList();
        this.multiSelector = multiSelector;
    }

    public void deleteItems(ParcelableSparseBooleanArray checkItems) {
        for(int x = noteGroups.size() - 1; x >= 0; x--)
        {
            if(checkItems.get(x, false)) {
                DBManager.getInstance().deleteNoteGroup(noteGroups.get(x).id);
                new DeletePhotoTask(noteGroups.get(x)).execute();
                noteGroups.remove(x);
            }
        }
        notifyDataSetChanged();
    }

    public void setNormalChoiceMode() {
        this.isMultipleChoiceMode = false;
        notifyDataSetChanged();
    }

    public NoteGroup getCheckedNoteGroup()
    {
        ParcelableSparseBooleanArray checkItems = multiSelector.getCheckedItems();
        for(int x = noteGroups.size() - 1; x >= 0; x--)
        {
            if(checkItems.get(x, false)) {
                return  noteGroups.get(x);
            }
        }
        return null;
    }

    public ArrayList<Uri> getCheckedNoteGroups() {
        ArrayList<Uri> checkedUriItems = new ArrayList<>();
        ParcelableSparseBooleanArray checkItems = multiSelector.getCheckedItems();
        for(int x = noteGroups.size() - 1; x >= 0; x--)
        {
            if(checkItems.get(x, false)) {
                NoteGroup noteGroup = noteGroups.get(x);
                for(int index=0;index<noteGroup.notes.size();index++)
                {
                    checkedUriItems.add(noteGroup.notes.get(index).getImagePath());
                }

            }
        }
        return checkedUriItems;
    }

    public interface Callback {
        void onItemClick(View view, int position, NoteGroup noteGroup);
        void onItemLongClick(View view, int position);
    }

    @Override
    public NoteGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notegroup_layout, null);
        NoteGroupViewHolder rcv = new NoteGroupViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final NoteGroupViewHolder holder, int position) {
        final NoteGroup noteGroup = noteGroups.get(position);
        holder.noteGroupName.setText(noteGroup.name);
        holder.numOfNotes.setText(noteGroup.notes.size() + " Docs");

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onItemClick(v, holder.getAdapterPosition(), noteGroup);
                }
                if(isMultipleChoiceMode)
                    holder.checkBox.setChecked(!holder.checkBox.isChecked());
            }
        });

        holder.rootView.setLongClickable(true);
        holder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(callback!=null) {
                    callback.onItemLongClick(v, holder.getAdapterPosition());
                    isMultipleChoiceMode = true;
                    notifyDataSetChanged();
                }
                return true;
            }
        });
        holder.itemView.setActivated(multiSelector.isChecked(position));

        if(isMultipleChoiceMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(multiSelector.isChecked(position));
        }
        else
            holder.checkBox.setVisibility(View.GONE);

//        holder.imageView.setImageURI(noteGroup.notes.get(noteGroup.notes.size()-1).getImagePath());
        setImageView(holder.imageView,noteGroup);
    }

    private void setImageView(final ImageView imageView, NoteGroup noteGroup) {
        Target loadingTarget = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                imageView.setImageResource(R.drawable.placeholder);
            }

        };

        if(noteGroup.notes.size()>0)
            ImageManager.i.loadPhoto(noteGroup.notes.get(noteGroup.notes.size()-1).getImagePath().getPath(), 400, 600, loadingTarget);
        else
            imageView.setImageResource(R.drawable.placeholder);
    }

    public void setNoteGroups(List<NoteGroup> noteGroups) {
        this.noteGroups = noteGroups;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public int getItemCount() {
        return noteGroups.size();
    }

    public static class NoteGroupViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.noteGroup_iv)
        public ImageView imageView;

        @Bind(R.id.noteGroupName_tv)
        public TextView noteGroupName;

        @Bind(R.id.numOfNotes_tv)
        public TextView numOfNotes;

        @Bind(R.id.root_layout)
        public View rootView;

        @Bind(R.id.isSelected_cb)
        public CheckBox checkBox;

        public NoteGroupViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
