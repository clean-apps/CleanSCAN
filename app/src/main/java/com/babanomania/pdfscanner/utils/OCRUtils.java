package com.babanomania.pdfscanner.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

public class OCRUtils {

    public static String getTextFromBitmap(Context context, Bitmap bitmap ){

        TextRecognizer textVision = new TextRecognizer.Builder( context ).build();
        if( textVision.isOperational() ){

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = textVision.detect(frame);
            StringBuilder delectedText = new StringBuilder();

            for( int counter = 0 ; counter < items.size(); counter++ ){
                TextBlock block = items.valueAt(counter);
                delectedText.append(  block.getValue() );
                delectedText.append(  " " );
            }

            return delectedText.toString();

        } else {
            Toast.makeText( context, "Could not make text vision work", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
