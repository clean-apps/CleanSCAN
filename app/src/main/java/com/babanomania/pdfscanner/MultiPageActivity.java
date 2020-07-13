package com.babanomania.pdfscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.babanomania.pdfscanner.utils.FileIOUtils;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.util.List;

public class MultiPageActivity extends AppCompatActivity {

    GridView pagesGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_page);

        setTitle( getResources().getString(R.string.multi_page_title) );

        pagesGridView = (GridView) findViewById(R.id.multi_page_grid);
        pagesGridView.setAdapter(new ImageAdapterGridView(this));

        pagesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(position == 0){
                    scanMore(view);

                }else {
                    Toast.makeText(getBaseContext(), "Page No " + (position + 1) + " Clicked", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Intent out = new Intent();
        out.putExtra(ScanConstants.SCANNED_RESULT, data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT));
        out.putExtra(ScanConstants.SCAN_MORE, data.getExtras().getBoolean(ScanConstants.SCAN_MORE));
        setResult(RESULT_OK, out);
        finish();

        System.gc();
    }

    public void saveNow(View view) {
        Intent out = new Intent();
        out.putExtra(ScanConstants.SAVE_PDF, Boolean.TRUE);
        setResult(RESULT_OK, out);
        finish();
    }

    public void scanMore(View view) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
        startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE);
    }

    public class ImageAdapterGridView extends BaseAdapter {
        private Context mContext;
        private List<File> stagingFiles;

        public ImageAdapterGridView(Context c) {
            mContext = c;

            final String stagingDirPath =  getApplicationContext().getString( R.string.base_staging_path);
            stagingFiles = FileIOUtils.getAllFiles( stagingDirPath );
        }

        public int getCount() {
            return stagingFiles.size() + 1;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView mImageView;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int imgSize = displayMetrics.widthPixels / 3;

            if ( position == 0 ){

                View addMoreView = getLayoutInflater().inflate(R.layout.add_more_img, null);
                addMoreView.setLayoutParams(new GridView.LayoutParams(imgSize, imgSize));
                addMoreView.setPadding(25, 25, 25, 25);
                return addMoreView;

            }else {

//                if (convertView == null || convertView instanceof LinearLayout ) {
//
//                    View eachFileView = getLayoutInflater().inflate(R.layout.each_file_img, null);
//                    ImageView imageView = eachFileView.findViewById(R.id.each_file_screenshot);
//                    eachFileView.setLayoutParams(new GridView.LayoutParams(imgSize, imgSize));
//                    eachFileView.setPadding(25, 25, 25, 25);
//
//                    mImageView = new ImageView(mContext);
//                    mImageView.setLayoutParams(new GridView.LayoutParams(imgSize, imgSize));
//                    mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                    mImageView.setPadding(25, 25, 25, 25);
//                    mImageView.setBackgroundResource(R.drawable.border_image);
//
//                } else {
//                    mImageView = (ImageView) convertView;
//                }

                View eachFileView = getLayoutInflater().inflate(R.layout.each_file_img, null);
                ImageView imageView = eachFileView.findViewById(R.id.each_file_screenshot);
                TextView textView = eachFileView.findViewById(R.id.each_pageno);

                int width = imgSize;
                int height = ( imgSize / 3 ) * 4;

                eachFileView.setLayoutParams(new GridView.LayoutParams( width, height )) ;
                eachFileView.setPadding(25, 25, 25, 25);
                eachFileView.setBackgroundResource(R.drawable.border_image);

                File imgFile = stagingFiles.get( stagingFiles.size() - position );
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);

                textView.setText("Page " + (stagingFiles.size() - position  + 1 ) );

                return eachFileView;
            }
        }
    }
}