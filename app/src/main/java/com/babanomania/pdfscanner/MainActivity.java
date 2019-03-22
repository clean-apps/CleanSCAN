package com.babanomania.pdfscanner;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
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
import android.view.View;

import com.babanomania.pdfscanner.persistance.DocumentViewModel;
import com.babanomania.pdfscanner.persistance.Document;
import com.babanomania.pdfscanner.utils.DialogUtil;
import com.babanomania.pdfscanner.utils.DialogUtilCallback;
import com.babanomania.pdfscanner.fileView.FLAdapter;
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

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.rw);
        UIUtil.setLightNavigationBar( recyclerView, this );
        PermissionUtil.ask(this);

        final File sd = Environment.getExternalStorageDirectory();

        final String baseStorageDirectory =  getApplicationContext().getString( R.string.base_storage_path);
        File scanStorageDirectory = new File(sd, baseStorageDirectory);
        if (!scanStorageDirectory.exists()) {
            scanStorageDirectory.mkdir();
        }

        final String baseStagingDirectory =  getApplicationContext().getString( R.string.base_staging_path);
        File scanStagingDirectory = new File(sd, baseStagingDirectory);
        if (!scanStagingDirectory.exists()) {
            scanStagingDirectory.mkdir();
        }

        viewModel = ViewModelProviders.of(this).get(DocumentViewModel.class);

        fileAdapter = new FLAdapter( viewModel, this);
        recyclerView.setAdapter( fileAdapter );

        viewModel.getAllDocuments().observe(this, new Observer<List<Document>>() {
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
            final String timestamp = simpleDateFormat.format(new Date());

            if( addMore ){

                try {

                    String filename = baseDirectory + "SCANNED_STG_" + timestamp + ".png";
                    File dest = new File(sd, filename);

                    FileOutputStream out = new FileOutputStream(dest);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.flush();
                    out.close();

                    fileAdapter.notifyDataSetChanged();

                }catch(IOException ioe){
                    ioe.printStackTrace();
                }

            } else {

                DialogUtil.askUserFilaname( c, null, new DialogUtilCallback() {

                    @Override
                    public void onSave(String textValue) {

                        try {

                            PdfDocument document = new PdfDocument();

                            String stagingDirPath = getApplicationContext().getString( R.string.base_staging_path );
                            File stagingDir = new File( sd, stagingDirPath );
                            if( stagingDir.listFiles() != null ){
                                for( File stagedFile : stagingDir.listFiles() ){

                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                    Bitmap bitmapSt = BitmapFactory.decodeFile( stagedFile.getAbsolutePath(), options);

                                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
                                    PdfDocument.Page page = document.startPage(pageInfo);

                                    Canvas canvas = page.getCanvas();
                                    Paint paint = new Paint();
                                    paint.setColor(Color.parseColor("#ffffff"));
                                    canvas.drawPaint(paint);

                                    canvas.drawBitmap(bitmapSt, 0, 0 , null);
                                    document.finishPage(page);

                                }
                            }

                            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
                            PdfDocument.Page page = document.startPage(pageInfo);

                            Canvas canvas = page.getCanvas();
                            Paint paint = new Paint();
                            paint.setColor(Color.parseColor("#ffffff"));
                            canvas.drawPaint(paint);

                            canvas.drawBitmap(bitmap, 0, 0 , null);
                            document.finishPage(page);

                            String filename = baseDirectory + "SCANNED_" + timestamp + ".pdf";
                            File dest = new File(sd, filename);

                            FileOutputStream out = new FileOutputStream(dest);
                            document.writeTo(out);

                            out.flush();
                            out.close();

                            document.close();

                            fileAdapter.notifyDataSetChanged();

                            if( stagingDir.listFiles() != null ){
                                for( File stagedFile : stagingDir.listFiles() ){
                                    stagedFile.delete();
                                }
                            }

                            SimpleDateFormat simpleDateFormatView = new SimpleDateFormat("dd-MM-yyyy hh:mm");
                            final String timestampView = simpleDateFormatView.format(new Date());

                            Document newDocument = new Document();
                            newDocument.setName( textValue );
                            newDocument.setCategory( "Receipts" );
                            newDocument.setPath( dest.getName() );
                            newDocument.setScanned( timestampView );
                            viewModel.saveDocument(newDocument);

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
