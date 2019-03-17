package com.babanomania.pdfscanner;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class FLAdapter extends RecyclerView.Adapter<FLViewHolder> {

    private final String baseDirectory;
    private File[] fileList;

    public FLAdapter( String pBaseDirectory ){
        this.baseDirectory = pBaseDirectory;
        updateFileList();

    }

    public void update(){
        updateFileList();
        notifyDataSetChanged();
    }

    public void deleteFile( int index ){
        fileList[index].delete();
        update();
    }

    private void updateFileList(){

        File sd = Environment.getExternalStorageDirectory();
        File dir = new File(sd, this.baseDirectory);

        if( dir.listFiles() != null ) {
            this.fileList = dir.listFiles();

            Arrays.sort(    this.fileList,
                            new Comparator<File>(){
                                public int compare(File f1, File f2){
                                    return Long.valueOf( f2.lastModified() ).compareTo( f1.lastModified() );
                                }
                            }
                );

        } else {
            this.fileList = new File[0];
        }
    }

    @NonNull
    @Override
    public FLViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View listItem = layoutInflater.inflate( R.layout.file_item_view, viewGroup, false );
        FLViewHolder viewHolder = new FLViewHolder(listItem);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FLViewHolder viewHolder, int i) {
            viewHolder.setFile( this.fileList[i] );
    }

    @Override
    public int getItemCount() {
            return this.fileList.length;
    }

}