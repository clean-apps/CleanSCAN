package com.babanomania.pdfscanner.persistance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveAll(List<Document> documents);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(Document document);

    @Query("SELECT * FROM Document ORDER BY documentId DESC")
    LiveData<List<Document>> findAll();

    @Query("SELECT * FROM Document WHERE name like :text ORDER BY documentId DESC")
    LiveData<List<Document>> search( String text );

    @Update
    void update( Document document );

    @Delete
    void delete( Document document );
}
