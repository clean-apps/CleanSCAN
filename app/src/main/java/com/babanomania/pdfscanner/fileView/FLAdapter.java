package com.babanomania.pdfscanner.fileView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Toast;

import com.babanomania.pdfscanner.OCRActivity;
import com.babanomania.pdfscanner.R;
import com.babanomania.pdfscanner.persistance.Document;
import com.babanomania.pdfscanner.persistance.DocumentViewModel;
import com.babanomania.pdfscanner.utils.DialogUtil;
import com.babanomania.pdfscanner.utils.DialogUtilCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FLAdapter extends RecyclerView.Adapter<FLViewHolder> {

    final Context context;
    protected ActionMode mActionMode;

    public boolean multiSelect = false;
    private List<Document> documentList = new ArrayList<>();
    public List<Document> selectedItems = new ArrayList<>();

    private DocumentViewModel viewModel;

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

                    for (Document documentItem  : selectedItems) {

                        final String baseDirectory =  context.getString(R.string.base_storage_path);
                        final File sd = Environment.getExternalStorageDirectory();

                        File toDelete = new File( sd, baseDirectory + documentItem.getPath() );
                        toDelete.delete();
                        viewModel.deleteDocument(documentItem);
                    }

                    mode.finish();
                    return true;

                case R.id.menu_edit:

                    final Document docToRename = selectedItems.get(0);
                    DialogUtil.askUserFilaname(context, docToRename.getName(), docToRename.getCategory(), new DialogUtilCallback() {

                        @Override
                        public void onSave(String textValue, String category) {

                            docToRename.setName( textValue );
                            docToRename.setCategory( category );
                            viewModel.updateDocument(docToRename);

                            Toast toast = Toast.makeText(context, "Renamed to " + textValue, Toast.LENGTH_SHORT);
                            toast.show();

                            notifyDataSetChanged();

                        }
                    });

                    mode.finish();
                    return true;

                case R.id.menu_ocr:

                    final Document docToOcr = selectedItems.get(0);

                    final String baseDirectory =  context.getString(R.string.base_storage_path);
                    final File sd = Environment.getExternalStorageDirectory();

                    File toOcr = new File( sd, baseDirectory + docToOcr.getPath() );

                    Intent intent = new Intent( context, OCRActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString( OCRActivity.FILE_PATH, docToOcr.getPath()); //Your id
                    intent.putExtras(bundle); //Put your id to your next Intent
                    context.startActivity(intent);

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
            notifyDataSetChanged();
        }
    };

    public FLAdapter( DocumentViewModel viewModel, Context context ){
        this.viewModel = viewModel;
        this.context = context;
    }

    public void setData(List<Document> documents){
        this.documentList.clear();
        this.documentList.addAll( documents );
        notifyDataSetChanged();
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
            viewHolder.setDocument( this.documentList.get(i) );
    }

    @Override
    public int getItemCount() {
        return this.documentList.size();
    }

}