package com.babanomania.pdfscanner;

import android.content.Context; import android.content.DialogInterface; import android.content.Intent; import android.graphics.Bitmap; import android.graphics.pdf.PdfRenderer; import android.os.AsyncTask; import android.os.Bundle; import android.os.Environment; import android.os.ParcelFileDescriptor; import android.util.Log; import android.view.View; import android.widget.Button; import android.widget.EditText; import android.widget.ProgressBar; import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog; import androidx.appcompat.app.AppCompatActivity;

import com.babanomania.pdfscanner.utils.OCRUtils; import com.babanomania.pdfscanner.utils.PDFUtils; import com.babanomania.pdfscanner.utils.UIUtil;

import java.io.File; import java.util.ArrayList; import java.util.List;

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
    UIUtil.setLightNavigationBar(relativeLayout, this);

    this.ocrText = findViewById(R.id.ocrText);
    this.shareButton = findViewById(R.id.shareBtn);
    this.progressBar = findViewById(R.id.extractingProgress);

    this.ocrText.setText(getResources().getString(R.string.ocr_waiting_text));
    setTitle(getResources().getString(R.string.ocr_title));

    this.progressBar.setVisibility(View.VISIBLE);
    this.shareButton.setVisibility(View.GONE);
    this.shareButton.setOnClickListener(v -> {
        String textToShare = ocrText.getText().toString();
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        startActivity(Intent.createChooser(sharingIntent, "Share Using"));
    });

    Bundle bundle = getIntent().getExtras();
    final String filePath = bundle.getString(FILE_PATH);
    showRedactionChoiceDialog(filePath);
}

private void showRedactionChoiceDialog(final String filePath) {
    final CharSequence[] options = {"HIPAA Guidelines", "Custom Word List", "No Redaction"};

    new AlertDialog.Builder(this)
        .setTitle("Select Redaction Mode")
        .setItems(options, (dialog, which) -> {
            boolean useHIPAA = false;
            boolean applyRedaction = true;

            switch (which) {
                case 0: // HIPAA
                    useHIPAA = true;
                    break;
                case 1: // Custom list
                    useHIPAA = false;
                    break;
                case 2: // No redaction
                    applyRedaction = false;
                    break;
            }

            new OCRExtractTask(OCRActivity.this, getApplicationContext(), filePath, applyRedaction, useHIPAA).execute();
        })
        .setCancelable(false)
        .show();
}

public void setText(String content) {
    this.ocrText.setText(content);
}

private class OCRExtractTask extends AsyncTask<String, Void, String> {
    private OCRActivity ocrActivity;
    private Context context;
    private String filePath;
    private boolean applyRedaction;
    private boolean redactWithHIPAA;

    public OCRExtractTask(OCRActivity ocrActivity, Context context, String filePath, boolean applyRedaction, boolean redactWithHIPAA) {
        this.ocrActivity = ocrActivity;
        this.context = context;
        this.filePath = filePath;
        this.applyRedaction = applyRedaction;
        this.redactWithHIPAA = redactWithHIPAA;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            ArrayList<Bitmap> bitmaps = new ArrayList<>();
            final String baseDirectory = context.getString(R.string.base_storage_path);
            final File sd = Environment.getExternalStorageDirectory();
            File toOcr = new File(sd, baseDirectory + this.filePath);

            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(toOcr, ParcelFileDescriptor.MODE_READ_ONLY));
            final int pageCount = renderer.getPageCount();

            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                int width = context.getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                int height = context.getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                bitmaps.add(bitmap);
                page.close();
            }
            renderer.close();

            StringBuilder extractedText = new StringBuilder();
            for (Bitmap eachPage : bitmaps) {
                String pageText = OCRUtils.getTextFromBitmap(context, eachPage);
                if (applyRedaction) {
                    pageText = applyRedactions(pageText, redactWithHIPAA);
                }
                extractedText.append(pageText).append("\n\n");
            }

            String finalText = extractedText.toString();
            PDFUtils.writeSearchablePDF(toOcr.getName(), finalText, context);

            runOnUiThread(() -> {
                ocrActivity.setText(finalText);
                shareButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            });

        } catch (Exception e) {
            Log.e("Clean Scan", "Unable to extract text", e);
            runOnUiThread(() -> {
                ocrActivity.setText(getResources().getString(R.string.ocr_failed_text));
                shareButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            });
        }
        return null;
    }

    private String applyRedactions(String text, boolean useHIPAA) {
        List<String> redactionList = useHIPAA ? OCRUtils.getHIPAAWordList(context) : OCRUtils.getCustomWordList(context);
        for (String term : redactionList) {
            text = text.replaceAll("(?i)\\b" + term + "\\b", "[REDACTED]");
        }
        return text;
    }
}

}

        
