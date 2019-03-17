package com.babanomania.pdfscanner.FLHandlers;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.babanomania.pdfscanner.R;

import java.io.File;

public class FLViewHolder extends RecyclerView.ViewHolder {

    private TextView textViewLabel;
    private TextView textViewTime;
    private RelativeLayout itemLayout;
    private ActionMode.Callback actionModeCallbacks;
    private FLAdapter adapter;
    private File fl;


    public FLViewHolder(View itemView, ActionMode.Callback actionModeCallbacks, FLAdapter adapter ) {
        super(itemView);
        this.textViewLabel = itemView.findViewById(R.id.fileName);
        this.textViewTime = itemView.findViewById(R.id.timeLabel);
        this.itemLayout = itemView.findViewById(R.id.relativeLayout);
        this.adapter = adapter;
        this.actionModeCallbacks  = actionModeCallbacks;
    }

    void selectItem(File item) {
        if (this.adapter.multiSelect) {
            if (this.adapter.selectedItems.contains(item)) {
                this.adapter.selectedItems.remove(item);
                itemLayout.setBackgroundColor(Color.WHITE);

            } else {
                this.adapter.selectedItems.add(item);
                itemLayout.setBackgroundResource(R.color.colorPrimaryLight);
            }
        }
    }

    public void setFile( final File fl ){

        this.fl = fl;
        String label = fl.getName().split("#")[0];
        String timeLabel = fl.getName().split("#")[1]
                                .replace( ".png", "" )
                                .replace( "_", " " );

        this.textViewLabel.setText( label );
        this.textViewTime.setText( timeLabel );

        if (adapter.selectedItems.contains(fl)) {
            itemLayout.setBackgroundColor(Color.LTGRAY);

        } else {
            itemLayout.setBackgroundColor(Color.WHITE);

        }

        this.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( adapter.multiSelect ){

                    selectItem(fl);

                    if( adapter.selectedItems.size() == 0 ){
                        adapter.mActionMode.finish();

                    } else {
                        adapter.mActionMode.invalidate();
                    }

                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri sharedFileUri = FileProvider.getUriForFile(v.getContext(), "com.scanlibrary.provider", fl);
                    intent.setDataAndType( sharedFileUri, "image/*");
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
                selectItem(fl);
                return true;
            }
        });
    }


}
