package vi.pdfscanner.db.models;

import android.net.Uri;
import android.os.Parcelable;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import org.parceler.Parcel;
import org.parceler.Transient;

import java.io.File;
import java.util.Date;

import vi.pdfscanner.db.PDFScannerDatabase;
import vi.pdfscanner.main.Const;

/**
 * Created by droidNinja on 19/04/16.
 */
@Table(database = PDFScannerDatabase.class)
@Parcel(analyze={Note.class})
public class Note extends BaseModel{

    @Column
    @PrimaryKey(autoincrement = true)
    public int id;

    @Column
    public String name;

    @Column
    public Date createdAt;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "noteGroupId", columnType = Integer.class, foreignKeyColumnName = "id")}, saveForeignKeyModel = false, onDelete = ForeignKeyAction.CASCADE, onUpdate = ForeignKeyAction.CASCADE)
    @Transient
    ForeignKeyContainer<NoteGroup> noteGroupForeignKeyContainer;

    public void associateNoteGroup(NoteGroup noteGroup) {
        noteGroupForeignKeyContainer = FlowManager.getContainerAdapter(NoteGroup.class).toForeignKeyContainer(noteGroup);
    }

    public Uri getImagePath()
    {
        Uri uri = Uri.fromFile(new File(Const.FOLDERS.CROP_IMAGE_PATH + File.separator + name));
        return uri;
    }
}