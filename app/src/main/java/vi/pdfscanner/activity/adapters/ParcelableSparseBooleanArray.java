package vi.pdfscanner.activity.adapters;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

/**
 * Custom SpareBooleanArray that can be treated as a Parcelable.
 */
public class ParcelableSparseBooleanArray extends SparseBooleanArray implements Parcelable {

    public ParcelableSparseBooleanArray() {
        super();
    }

    private ParcelableSparseBooleanArray(Parcel source) {
        int size = source.readInt();

        for (int i = 0; i < size; i++) {
            put(source.readInt(), (Boolean) source.readValue(null));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(size());

        for (int i = 0; i < size(); i++) {
            dest.writeInt(keyAt(i));
            dest.writeValue(valueAt(i));
        }
    }

    public static Parcelable.Creator<ParcelableSparseBooleanArray> CREATOR = new Parcelable.Creator<ParcelableSparseBooleanArray>() {
        @Override
        public ParcelableSparseBooleanArray createFromParcel(Parcel source) {
            return new ParcelableSparseBooleanArray(source);
        }

        @Override
        public ParcelableSparseBooleanArray[] newArray(int size) {
            return new ParcelableSparseBooleanArray[size];
        }
    };
}