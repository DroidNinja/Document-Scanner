package vi.pdfscanner.db;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;

import vi.pdfscanner.db.models.NoteGroup;

/**
 * Created by droidNinja on 19/04/16.
 */
@Database(name = PDFScannerDatabase.NAME, version = PDFScannerDatabase.VERSION)
public class PDFScannerDatabase {

    public static final String NAME = "PDFScannerDatabase";

    public static final int VERSION = 2;
}
