package com.scanlibrary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by jhansi on 05/04/15.
 */
public class Utils {

    private Utils() {

    }


    public static Uri getUri(Context context, final Bitmap bitmap) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
        String timestamp = simpleDateFormat.format(new Date());

        final String baseDirectory =  context.getString(R.string.base_scantmp_path);
        String filename = "TMP_STG_" + timestamp + ".png";

        try {
            String absPath = writeFile(baseDirectory, filename, new FileWritingCallbackS() {
                @Override
                public void write(FileOutputStream out) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }
            });

            return Uri.parse(absPath);

        }catch (IOException ioe){
            ioe.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBitmap(Context context, Uri uri) throws IOException {
        final File sd = Environment.getExternalStorageDirectory();
        File src = new File(sd, uri.getPath());
        //Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(src.getAbsolutePath()) );
        Bitmap bitmap = BitmapFactory.decodeFile(src.getAbsolutePath());
        return bitmap;
    }

    public static void setLightNavigationBar(View view, Activity activity){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            view.setSystemUiVisibility(flags);
            activity.getWindow().setNavigationBarColor(Color.WHITE);
        }
    }

    public static String writeFile( String baseDirectory, String filename, FileWritingCallbackS callback ) throws IOException {

        final File sd = Environment.getExternalStorageDirectory();
        String absFilename = baseDirectory + filename;
        File dest = new File(sd, absFilename);

        FileOutputStream out = new FileOutputStream(dest);

        callback.write( out );

        out.flush();
        out.close();

        return  absFilename;
    }

    private interface FileWritingCallbackS {
        public void write(FileOutputStream out);
    }

}