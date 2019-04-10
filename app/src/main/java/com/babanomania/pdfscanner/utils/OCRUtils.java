package com.babanomania.pdfscanner.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

public class OCRUtils {

    public static String getTextFromBitmap( final Context context, Bitmap bitmap ) throws InterruptedException{

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText( context, "Could not make text vision work", Toast.LENGTH_SHORT).show();
                                    }
                                });

        while(! result.isComplete() ){
            Thread.sleep(300);
        }

        return result.isSuccessful() ? result.getResult().getText() : null;
    }

/*    public static String getTextFromBitmap(Context context, Bitmap bitmap ){

        TextRecognizer textVision = new TextRecognizer.Builder( context ).build();
        if( textVision.isOperational() ){

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = textVision.detect(frame);
            StringBuilder detectedText = new StringBuilder();

            for( int counter = 0 ; counter < items.size(); counter++ ){
                TextBlock block = items.valueAt(counter);
                detectedText.append(  block.getValue() );
                detectedText.append(  " " );
            }

            return detectedText.toString();

        } else {
            Toast.makeText( context, "Could not make text vision work", Toast.LENGTH_SHORT).show();
            return null;
        }
    }*/
}
