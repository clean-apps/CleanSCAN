package com.babanomania.pdfscanner.persistance;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.sql.Timestamp;

@Entity
public class Document {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    private int documentId;

    @NonNull
    private String name;

    @NonNull
    private String category;

    @NonNull
    private String path;

    private String scanned;

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public void setCategory(@NonNull String category) {
        this.category = category;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    public String getScanned() {
        return scanned;
    }

    public void setScanned(String scanned) {
        this.scanned = scanned;
    }

}
