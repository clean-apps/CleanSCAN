package com.babanomania.pdfscanner.fileView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import androidx.appcompat.view.ActionMode;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.babanomania.pdfscanner.R;
import com.babanomania.pdfscanner.persistance.Document;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FLViewHolder extends RecyclerView.ViewHolder {

    private ImageView categoryIcon;
    private TextView textViewLabel;
    private TextView textViewTime;
    private TextView textViewCategory;
    private TextView textPageCount;
    private LinearLayout itemLayout;
    private ActionMode.Callback actionModeCallbacks;
    private FLAdapter adapter;
    private Document documnt;
    private boolean isDark;

    private Map<String, Integer> categoryImageMap = new HashMap<>();

    public FLViewHolder(View itemView, ActionMode.Callback actionModeCallbacks, FLAdapter adapter, boolean isDark ) {
        super(itemView);
        this.categoryIcon =  itemView.findViewById(R.id.imageView);
        this.textViewLabel = itemView.findViewById(R.id.fileName);
        this.textViewTime = itemView.findViewById(R.id.timeLabel);
        this.textViewCategory = itemView.findViewById(R.id.categoryLabel);
        this.textPageCount = itemView.findViewById(R.id.pageCount);
        this.itemLayout = itemView.findViewById(R.id.relativeLayout);
        this.adapter = adapter;
        this.actionModeCallbacks  = actionModeCallbacks;
        this.isDark = isDark;

        categoryImageMap.put( "Others", R.drawable.ic_category_others );
        categoryImageMap.put( "Shopping", R.drawable.ic_category_shopping );
        categoryImageMap.put( "Vehicle", R.drawable.ic_category_vehicle );
        categoryImageMap.put( "Medical", R.drawable.ic_category_medical );
        categoryImageMap.put( "Legal", R.drawable.ic_category_legal );
        categoryImageMap.put( "Housing", R.drawable.ic_category_housing );
        categoryImageMap.put( "Books", R.drawable.ic_category_books );
        categoryImageMap.put( "Food", R.drawable.ic_category_food );
        categoryImageMap.put( "Banking", R.drawable.ic_category_banking );
        categoryImageMap.put( "Receipts", R.drawable.ic_category_receipt );
        categoryImageMap.put( "Manuals", R.drawable.ic_category_manuals );
        categoryImageMap.put( "Travel", R.drawable.ic_category_travel );
        categoryImageMap.put( "Notes", R.drawable.ic_category_notes );
        categoryImageMap.put( "ID", R.drawable.ic_category_id );
    }

    void selectItem(Document item) {
        if (this.adapter.multiSelect) {
            if (this.adapter.selectedItems.contains(item)) {
                this.adapter.selectedItems.remove(item);
//                itemLayout.setBackgroundColor(Color.WHITE);
//                if dark                 itemLayout.setBackgroundColor(Color.DARK);
//                else
//                white

                if(isDark){
                    itemLayout.setBackgroundResource(R.color.colorDark);
                }
                else{
                    itemLayout.setBackgroundColor(Color.WHITE);
                }


            } else {
                this.adapter.selectedItems.add(item);
                itemLayout.setBackgroundResource(R.color.colorPrimaryLight);
            }
        }
    }

    public void setDocument(final Document document ){

        this.documnt = document;

        this.textViewLabel.setText( document.getName() );
        this.textViewTime.setText( document.getScanned() );
        this.textViewCategory.setText( document.getCategory() );

        if( document.getPageCount() > 1 ) {
            this.textPageCount.setVisibility( View.VISIBLE );
            this.textPageCount.setText( String.valueOf(document.getPageCount()) + " Pages" );

        } else {
            this.textPageCount.setVisibility( View.GONE );

        }

        if (adapter.selectedItems.contains(document)) {
             itemLayout.setBackgroundColor(Color.LTGRAY);
        } else {
            if(isDark){
                itemLayout.setBackgroundResource(R.color.colorDark);
            }
            else{
                itemLayout.setBackgroundColor(Color.WHITE);
            }
        }

        Integer resourceId = categoryImageMap.get( document.getCategory() );
        if( resourceId == null ){
            this.categoryIcon.setImageResource(R.drawable.ic_category_others);

        } else {

            this.categoryIcon.setImageResource(resourceId);
        }

        this.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( adapter.multiSelect ){

                    selectItem(document);

                    if( adapter.selectedItems.size() == 0 ){
                        adapter.mActionMode.finish();

                    } else {
                        adapter.mActionMode.invalidate();
                    }

                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    final File sd = Environment.getExternalStorageDirectory();
                    String baseDirectory = v.getContext().getString(R.string.base_storage_path);
                    String newFileName = baseDirectory + document.getPath();
                    File toOpen = new File( sd, newFileName );

                    Uri sharedFileUri = FileProvider.getUriForFile(v.getContext(), "com.babanomania.pdfscanner.provider", toOpen);
                    intent.setDataAndType( sharedFileUri, "application/pdf");
                    PackageManager pm = v.getContext().getPackageManager();
                    if (intent.resolveActivity(pm) != null) {
                        v.getContext().startActivity(intent);
                    }
                }

            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                adapter.mActionMode = ((AppCompatActivity)view.getContext()).startSupportActionMode(actionModeCallbacks);
                selectItem(document);
                return true;
            }
        });
    }


}
