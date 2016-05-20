package vi.pdfscanner.activity.adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import vi.pdfscanner.db.models.Note;
import vi.pdfscanner.db.models.NoteGroup;
import vi.pdfscanner.manager.ImageManager;
import vi.pdfscanner.utils.PhotoUtil;

/**
 * Created by droidNinja on 20/04/16.
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>{

    private final MultiSelector multiSelector;
    private List<Note> notes;
    private Callback callback;
    private boolean isMultipleChoiceMode;

    public interface Callback {
        void onItemClick(View view, int position, Note note);
        void onItemLongClick(View view, int position);
    }

    public NoteAdapter(List<Note> notes, MultiSelector multiSelector)
    {
        this.notes = notes;
        this.multiSelector = multiSelector;
    }

    public void deleteItems(ParcelableSparseBooleanArray checkItems) {
        for(int x = notes.size() - 1; x >= 0; x--)
        {
            if(checkItems.get(x, false)) {
                DBManager.getInstance().deleteNote(notes.get(x).id);
                PhotoUtil.deletePhoto(notes.get(x).getImagePath().getPath());
                notes.remove(x);
            }
        }
        notifyDataSetChanged();
    }

    public void deleteItem(Note note)
    {
        for(int x = notes.size() - 1; x >= 0; x--)
        {
            if(notes.get(x).id==note.id) {
                DBManager.getInstance().deleteNote(notes.get(x).id);
                PhotoUtil.deletePhoto(notes.get(x).getImagePath().getPath());
                notes.remove(x);
            }
        }
        notifyDataSetChanged();
    }

    public ArrayList<Uri> getCheckedNotes() {
        ArrayList<Uri> checkedUriItems = new ArrayList<>();
        ParcelableSparseBooleanArray checkItems = multiSelector.getCheckedItems();
        for(int x = notes.size() - 1; x >= 0; x--)
        {
            if(checkItems.get(x, false)) {
                    checkedUriItems.add(notes.get(x).getImagePath());
            }
        }
        return checkedUriItems;
    }

    public void setNormalChoiceMode() {
        this.isMultipleChoiceMode = false;
        notifyDataSetChanged();
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_layout, null);
        NoteViewHolder rcv = new NoteViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final NoteViewHolder holder, final int position) {
        final Note note = notes.get(position);
        holder.NoteName.setText("Scan " + (position+1));

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onItemClick(v, holder.getAdapterPosition(), note);
                }
                if (isMultipleChoiceMode) {
                    holder.checkBox.setVisibility(View.VISIBLE);
                    holder.checkBox.setChecked(!holder.checkBox.isChecked());
                }
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

        setImageView(holder.imageView,note);
    }

    private void setImageView(final ImageView imageView, Note note) {
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

        ImageManager.i.loadPhoto(note.getImagePath().getPath(), 400, 600, loadingTarget);
    }

    public void setNotes(List<Note> Notes) {
        this.notes = Notes;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.note_iv)
        public ImageView imageView;

        @Bind(R.id.noteName_tv)
        public TextView NoteName;

        @Bind(R.id.root_layout)
        public View rootView;

        @Bind(R.id.isSelected_cb)
        public CheckBox checkBox;

        public NoteViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
