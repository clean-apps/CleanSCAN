package com.babanomania.pdfscanner;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfDocument;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.babanomania.pdfscanner.FLHandlers.DialogUtil;
import com.babanomania.pdfscanner.FLHandlers.DialogUtilCallback;
import com.babanomania.pdfscanner.FLHandlers.FLAdapter;
import com.babanomania.pdfscanner.FLHandlers.FLViewHolder;
import com.babanomania.pdfscanner.FLHandlers.PermissionUtil;
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

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


        RecyclerView recyclerView = findViewById(R.id.rw);
        String baseDirectory =  getApplicationContext().getString(R.string.base_storage_path);

        fileAdapter = new FLAdapter(baseDirectory, this);
        recyclerView.setAdapter( fileAdapter );

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

                    String textValue = "SCANNED_STG_";
                    String filename = baseDirectory + textValue + "#" + timestamp + ".png";
                    File dest = new File(sd, filename);

                    FileOutputStream out = new FileOutputStream(dest);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.flush();
                    out.close();

                    fileAdapter.update();

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

                            String filename = baseDirectory + textValue + "#" + timestamp + ".pdf";
                            File dest = new File(sd, filename);

                            FileOutputStream out = new FileOutputStream(dest);
                            document.writeTo(out);

                            out.flush();
                            out.close();

                            document.close();

                            fileAdapter.update();

                            if( stagingDir.listFiles() != null ){
                                for( File stagedFile : stagingDir.listFiles() ){
                                    stagedFile.delete();
                                }
                            }

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
