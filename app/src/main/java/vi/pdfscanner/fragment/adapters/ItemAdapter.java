package vi.pdfscanner.fragment.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import vi.pdfscanner.R;
import vi.pdfscanner.utils.BottomSheetModel;


public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
 
    private List<BottomSheetModel> mItems;
    private ItemListener mListener;
 
    public ItemAdapter(List<BottomSheetModel> items, ItemListener listener) {
        mItems = items;
        mListener = listener;
    }
 
    public void setListener(ItemListener listener) {
        mListener = listener;
    }
 
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_share_dialog, parent, false));
    }
 
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(mItems.get(position));
    }
 
    @Override
    public int getItemCount() {
        return mItems.size();
    }
 
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
 
        public ImageView imageView;
        public TextView textView;
        public BottomSheetModel item;
 
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            textView = (TextView) itemView.findViewById(R.id.textView);
        }
 
        public void setData(BottomSheetModel item) {
            this.item = item;
            imageView.setImageResource(item.drawable);
            textView.setText(item.title);
        }
 
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(item);
            }
        }
    }
 
    public interface ItemListener {
        void onItemClick(BottomSheetModel item);
    }
}