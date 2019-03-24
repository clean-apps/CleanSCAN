package com.babanomania.pdfscanner;

import android.app.Activity;
import android.app.SearchManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.babanomania.pdfscanner.persistance.DocumentViewModel;
import com.babanomania.pdfscanner.persistance.Document;
import com.babanomania.pdfscanner.utils.DialogUtil;
import com.babanomania.pdfscanner.utils.DialogUtilCallback;
import com.babanomania.pdfscanner.fileView.FLAdapter;
import com.babanomania.pdfscanner.utils.FileIOUtils;
import com.babanomania.pdfscanner.utils.FileWritingCallback;
import com.babanomania.pdfscanner.utils.PDFWriterUtil;
import com.babanomania.pdfscanner.utils.PermissionUtil;
import com.babanomania.pdfscanner.utils.UIUtil;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FLAdapter fileAdapter;
    private final Context c = this;
    private List<Uri> scannedBitmaps = new ArrayList<>();

    private DocumentViewModel viewModel;

    private String searchText = "";
    LiveData<List<Document>> liveData;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.rw);

        UIUtil.setLightNavigationBar( recyclerView, this );
        PermissionUtil.ask(this);

        final String baseStorageDirectory =  getApplicationContext().getString( R.string.base_storage_path);
        FileIOUtils.mkdir( baseStorageDirectory );

        final String baseStagingDirectory =  getApplicationContext().getString( R.string.base_staging_path);
        FileIOUtils.mkdir( baseStagingDirectory );

        viewModel = ViewModelProviders.of(this).get(DocumentViewModel.class);

        fileAdapter = new FLAdapter( viewModel, this);
        recyclerView.setAdapter( fileAdapter );

        liveData = viewModel.getAllDocuments();
        liveData.observe(this, new Observer<List<Document>>() {
                    @Override
                    public void onChanged(@Nullable List<Document> documents) {
                        fileAdapter.setData(documents);
                    }
                });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.default_menu, menu);
        return true;
    }

    public void goToSearch(MenuItem mi){
        Intent intent = new Intent(this, SearchableActivity.class);
        startActivityForResult(intent, 0);
    }

    public void openCamera(View v){
        scannedBitmaps.clear();
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
        startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE);
    }

    public void openGallery(View v){
        scannedBitmaps.clear();
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_MEDIA);
        startActivityForResult(intent, ScanConstants.PICKFILE_REQUEST_CODE);
    }

    private void saveBitmap( final Bitmap bitmap, final boolean addMore ){

            final String baseDirectory =  getApplicationContext().getString( addMore ? R.string.base_staging_path : R.string.base_storage_path);
            final File sd = Environment.getExternalStorageDirectory();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
            final String timestamp = simpleDateFormat.format( new Date() );

            if( addMore ){

                try {

                    String filename = "SCANNED_STG_" + timestamp + ".png";
                    FileIOUtils.writeFile(baseDirectory, filename, new FileWritingCallback() {
                        @Override
                        public void write(FileOutputStream out) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                        }
                    });

                    bitmap.recycle();
                    System.gc();

                }catch(IOException ioe){
                    ioe.printStackTrace();
                }

            } else {

                DialogUtil.askUserFilaname( c, null, null, new DialogUtilCallback() {

                    @Override
                    public void onSave(String textValue, String category) {

                        try {

                            final PDFWriterUtil pdfWriter = new PDFWriterUtil();

                            String stagingDirPath = getApplicationContext().getString( R.string.base_staging_path );

                            List<File> stagingFiles = FileIOUtils.getAllFiles( stagingDirPath );
                            for ( File stagedFile : stagingFiles ) {
                                pdfWriter.addFile( stagedFile );
                            }

                            pdfWriter.addBitmap(bitmap);

                            String filename = "SCANNED_" + timestamp + ".pdf";
                            FileIOUtils.writeFile( baseDirectory, filename, new FileWritingCallback() {
                                @Override
                                public void write(FileOutputStream out) {
                                    try {
                                        pdfWriter.write(out);

                                    }catch (IOException e){
                                        e.printStackTrace();
                                    }
                                }
                            });


                            fileAdapter.notifyDataSetChanged();

                            FileIOUtils.clearDirectory( stagingDirPath );

                            SimpleDateFormat simpleDateFormatView = new SimpleDateFormat("dd-MM-yyyy hh:mm");
                            final String timestampView = simpleDateFormatView.format(new Date());

                            Document newDocument = new Document();
                            newDocument.setName( textValue );
                            newDocument.setCategory( category );
                            newDocument.setPath( filename );
                            newDocument.setScanned( timestampView );
                            newDocument.setPageCount( pdfWriter.getPageCount() );
                            viewModel.saveDocument(newDocument);

                            pdfWriter.close();

                            bitmap.recycle();
                            System.gc();

                        }catch(IOException ioe){
                            ioe.printStackTrace();

                        }

                    }
                });

            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( ( requestCode == ScanConstants.PICKFILE_REQUEST_CODE || requestCode == ScanConstants.START_CAMERA_REQUEST_CODE ) &&
                resultCode == Activity.RESULT_OK) {

            Uri uri = data.getExtras().getParcelable( ScanConstants.SCANNED_RESULT );
            boolean doScanMore = data.getExtras().getBoolean( ScanConstants.SCAN_MORE );

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                saveBitmap( bitmap, doScanMore );

                if( doScanMore ){
                    scannedBitmaps.add(uri);
                    Intent intent = new Intent(this, ScanActivity.class);
                    intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
                    startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE);
                }

                //getContentResolver().delete(uri, null, null);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
