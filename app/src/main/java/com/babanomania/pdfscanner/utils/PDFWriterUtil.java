package com.babanomania.pdfscanner.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PDFWriterUtil {

    private PdfDocument document = new PdfDocument();

    public void addFile( File bitmapFile ){

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmapSt = BitmapFactory.decodeFile( bitmapFile.getAbsolutePath(), options);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder( bitmapSt.getWidth(), bitmapSt.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawPaint(paint);

        canvas.drawBitmap( bitmapSt, 0, 0 , null);
        document.finishPage(page);

    }

    public void addBitmap( Bitmap bitmap ){

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder( bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawPaint(paint);

        canvas.drawBitmap( bitmap, 0, 0 , null);
        document.finishPage(page);

    }

    public void write( FileOutputStream out ) throws IOException {
        document.writeTo(out);
    }

    public int getPageCount(){
        return document.getPages().size();
    }

    public void close() throws IOException {
        document.close();
    }
}
