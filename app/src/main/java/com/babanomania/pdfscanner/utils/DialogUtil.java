package com.babanomania.pdfscanner.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.babanomania.pdfscanner.R;

public class DialogUtil {

    public static void askUserFilaname( Context context, String promptFileName, String promptCategory, final DialogUtilCallback callback ){

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View mView = layoutInflaterAndroid.inflate(R.layout.file_input_dialog_box, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(mView);

        final EditText fileNameText = mView.findViewById(R.id.userInputDialog);
        if( promptFileName != null ){
            fileNameText.setText(promptFileName);
        }

        final Spinner categorySelection = mView.findViewById(R.id.userInputCategory);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySelection.setAdapter(adapter);

        if( promptCategory != null ){
            String[] categoryArray = mView.getResources().getStringArray( R.array.category_array );
            for( int i = 0; i < categoryArray.length; i++ ){
                if( promptCategory.equals( categoryArray[i] ) ) {
                    categorySelection.setSelection(i);
                }
            }
        }

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        callback.onSave(
                                fileNameText.getText().toString(),
                                categorySelection.getSelectedItem().toString()
                        );
                    }
                })

                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();

    }
}
