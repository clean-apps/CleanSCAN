package com.babanomania.pdfscanner.utils;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileIOUtils {

    public static List<File> getAllFiles( String dirPath ){

        List<File> fileList = new ArrayList<>();

        final File sd = Environment.getExternalStorageDirectory();
        File targetDirectory = new File( sd, dirPath );

        if( targetDirectory.listFiles() != null ){
            for( File eachFile : targetDirectory.listFiles() ){
                fileList.add(eachFile);

            }
        }

        return fileList;

    }

    public static void mkdir( String dirPath ){

        final File sd = Environment.getExternalStorageDirectory();
        File storageDirectory = new File(sd, dirPath);
        if (!storageDirectory.exists()) {
            storageDirectory.mkdir();
        }
    }

    public static void clearDirectory( String dirPath ){

        final File sd = Environment.getExternalStorageDirectory();
        File targetDirectory = new File( sd, dirPath );

        if( targetDirectory.listFiles() != null ){
            for( File tempFile : targetDirectory.listFiles() ){
                tempFile.delete();
            }
        }

    }

    public static void writeFile( String baseDirectory, String filename, FileWritingCallback callback ) throws IOException {

        final File sd = Environment.getExternalStorageDirectory();
        String absFilename = baseDirectory + filename;
        File dest = new File(sd, absFilename);

        FileOutputStream out = new FileOutputStream(dest);

        callback.write( out );

        out.flush();
        out.close();
    }

}
