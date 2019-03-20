package com.babanomania.pdfscanner.FLHandlers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.babanomania.pdfscanner.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FLAdapter extends RecyclerView.Adapter<FLViewHolder> {

    private final String baseDirectory;
    private File[] fileList;

    public boolean multiSelect = false;
    public List<File> selectedItems = new ArrayList<>();
    protected ActionMode mActionMode;
    final Context c;

    private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            multiSelect = true;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            if( selectedItems.size() == 0 || selectedItems.size() == 1 ){
                MenuInflater inflater = mode.getMenuInflater();
                menu.clear();
                inflater.inflate(R.menu.single_select_menu, menu);
                mode.setTitle( "1 Selected" );
                return true;

            } else {
                MenuInflater inflater = mode.getMenuInflater();
                menu.clear();
                inflater.inflate(R.menu.multi_select_menu, menu);
                mode.setTitle( selectedItems.size() + " Selected" );
                return true;
            }

        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {

            switch (item.getItemId()) {
                case R.id.menu_delete:

                    for (File fileItem  : selectedItems) {
                        fileItem.delete();
                    }

                    mode.finish();
                    return true;

                case R.id.menu_edit:

                    final File itemToRename = selectedItems.get(0);
                    String originalName = itemToRename.getName();
                    final String fileNamePrefix = originalName.split("#")[0];
                    final String fileNameSuffix = originalName.split("#")[1];

                    DialogUtil.askUserFilaname( c, fileNamePrefix, new DialogUtilCallback() {

                        @Override
                        public void onSave(String textValue) {

                            final File sd = Environment.getExternalStorageDirectory();
                            String baseDirectory = c.getString(R.string.base_storage_path);
                            String newFileName = baseDirectory + textValue + "#" + fileNameSuffix;
                            itemToRename.renameTo( new File( sd, newFileName ) );

                            Toast toast = Toast.makeText( c, "renamed to " + newFileName, Toast.LENGTH_SHORT);
                            toast.show();

                            update();

                        }
                    });

                    mode.finish();
                    return true;

                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            multiSelect = false;
            selectedItems.clear();
            update();
        }
    };

    public FLAdapter( String pBaseDirectory, Context context ){
        this.baseDirectory = pBaseDirectory;
        this.c = context;
        updateFileList();

    }

    public void update(){
        updateFileList();
        notifyDataSetChanged();
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
        FLViewHolder viewHolder = new FLViewHolder(listItem, actionModeCallbacks, this );

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