package vi.pdfscanner.db.migration;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;

import vi.pdfscanner.db.PDFScannerDatabase;
import vi.pdfscanner.db.models.NoteGroup;

@Migration(version = 2, database = PDFScannerDatabase.class)
public class Migration2 extends AlterTableMigration<NoteGroup> {

        public Migration2() {
            super(NoteGroup.class);
        }

        @Override
        public void onPreMigrate() {
            super.onPreMigrate();
            addColumn(SQLiteType.TEXT, "pdfPath");
            addColumn(SQLiteType.INTEGER,"numOfImagesInPDF");
        }
    }