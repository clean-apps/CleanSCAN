package com.babanomania.pdfscanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.babanomania.pdfscanner.utils.OCRUtils;
import com.babanomania.pdfscanner.utils.UIUtil;

import java.io.File;
import java.util.ArrayList;

public class OCRActivity extends AppCompatActivity {

    public EditText ocrText;
    public Button shareButton;
    private ProgressBar progressBar;
    public static String FILE_PATH = "file_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        RelativeLayout relativeLayout = findViewById(R.id.rl);
        UIUtil.setLightNavigationBar( relativeLayout, this );

        this.ocrText = findViewById(R.id.ocrText);
        this.shareButton = findViewById(R.id.shareBtn);
        this.progressBar = findViewById(R.id.extractingProgress);

        this.ocrText.setText( getResources().getString(R.string.ocr_waiting_text) );
        setTitle( getResources().getString(R.string.ocr_title) );

        this.progressBar.setVisibility(View.VISIBLE);
        this.shareButton.setVisibility(View.GONE);
        this.shareButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String textToShare = ocrText.getText().toString();
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, textToShare);
                        startActivity(Intent.createChooser(sharingIntent, "Share Using"));

                    }
                }
        );

        Bundle bundle = getIntent().getExtras();
        final String filePath = bundle.getString(FILE_PATH);
        new OCRExtractTask( this, getApplicationContext(), filePath )
                .execute();
    }

    public void setText( String content ){
        this.ocrText.setText( content );
    }

    private class OCRExtractTask extends AsyncTask<String, Void, String> {

        private OCRActivity ocrActivity;
        private Context context;
        private String filePath;

        public OCRExtractTask( OCRActivity ocrActivity, Context context, String filePath ){
            this.ocrActivity = ocrActivity;
            this.context = context;
            this.filePath = filePath;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                ArrayList<Bitmap> bitmaps = new ArrayList<>();
                final String baseDirectory = context.getString(R.string.base_storage_path);
                final File sd = Environment.getExternalStorageDirectory();

                File toOcr = new File(sd, baseDirectory + this.filePath);

                PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(toOcr, ParcelFileDescriptor.MODE_READ_ONLY));

                Bitmap bitmap;
                final int pageCount = renderer.getPageCount();
                for (int i = 0; i < pageCount; i++) {
                    PdfRenderer.Page page = renderer.openPage(i);

                    int width = context.getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                    int height = context.getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    bitmaps.add(bitmap);

                    // close the page
                    page.close();

                }

                // close the renderer
                renderer.close();

                StringBuffer extractedText = new StringBuffer();
                for (Bitmap eachPage : bitmaps ) {
                    extractedText.append(
                            OCRUtils.getTextFromBitmap(context, eachPage)
                    );

                }

                Log.d(  "Clean Scan", "detected text : " + extractedText );
                this.ocrActivity.setText(extractedText.toString() );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        shareButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                });

            }catch (Exception e){
                Log.e( "Clean Scan", "Unable to extract text", e );
                this.ocrActivity.setText( getResources().getString(R.string.ocr_failed_text) );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        shareButton.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }
                });

            } finally {
                return  null;
            }
        }
    }
}
