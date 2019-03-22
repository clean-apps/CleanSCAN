package com.babanomania.pdfscanner.persistance;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Document.class}, version = 1, exportSchema = false)
public abstract class DocumentDatabase extends RoomDatabase {

    private static DocumentDatabase INSTANCE;

    public abstract DocumentDao documentDao();

    private static final Object sLock = new Object();

    public static DocumentDatabase getInstance( Context context ) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                                context.getApplicationContext(),
                                DocumentDatabase.class,
                            "documents.db"
                        )
                        .allowMainThreadQueries()
                        .build();
            }
            return INSTANCE;
        }
    }

}
