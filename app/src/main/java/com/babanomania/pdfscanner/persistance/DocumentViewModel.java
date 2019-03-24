package com.babanomania.pdfscanner.persistance;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DocumentViewModel extends AndroidViewModel {

    private DocumentDao dao;
    private ExecutorService executorService;

    public DocumentViewModel(@NonNull Application application) {
        super(application);
        dao = DocumentDatabase.getInstance(application).documentDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Document>> getAllDocuments() {
        return dao.findAll();
    }

    public LiveData<List<Document>> search( String text ) {
        return dao.search(text);
    }

    public void saveDocument(final Document document) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                dao.save(document);
            }
        });
    }

    public void updateDocument(final Document document) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                dao.update(document);
            }
        });
    }

    public void deleteDocument(final Document document) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                dao.delete(document);
            }
        });
    }
}
