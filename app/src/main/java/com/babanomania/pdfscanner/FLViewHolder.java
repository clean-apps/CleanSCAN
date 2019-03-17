package com.babanomania.pdfscanner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class FLViewHolder extends RecyclerView.ViewHolder {

    public TextView textViewLabel;
    public TextView textViewTime;
    public RelativeLayout itemLayout;
    public File fl;

    public FLViewHolder(View itemView) {
        super(itemView);
        this.textViewLabel = itemView.findViewById(R.id.fileName);
        this.textViewTime = itemView.findViewById(R.id.timeLabel);
        this.itemLayout = itemView.findViewById(R.id.relativeLayout);
    }

    public void setFile( final File fl ){

        this.fl = fl;
        String label = fl.getName().split("#")[0];
        String timeLabel = fl.getName().split("#")[1]
                                .replace( ".png", "" )
                                .replace( "_", " " );

        this.textViewLabel.setText( label );
        this.textViewTime.setText( timeLabel );

        this.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri sharedFileUri = FileProvider.getUriForFile(v.getContext(), "com.scanlibrary.provider", fl);
                intent.setDataAndType( sharedFileUri, "image/*");
                PackageManager pm = v.getContext().getPackageManager();
                if (intent.resolveActivity(pm) != null) {
                    v.getContext().startActivity(intent);
                }
            }
        });
    }
}
