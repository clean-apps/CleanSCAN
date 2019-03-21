package com.babanomania.pdfscanner.FLHandlers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

public class PermissionUtil {

    private static final int RUNTIME_PERMISSION_CODE = 7;

    public static void ask(final Activity activity ){

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){

            check_permission( activity, Manifest.permission.CAMERA, "Camera Permission is Required." );
            check_permission( activity, Manifest.permission.READ_EXTERNAL_STORAGE, "External Storage Read Permission is Required." );
            check_permission( activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, "External Storage Write Permission is Required." );

        }
    }

    private static void check_permission( final Activity activity, final String permission, final String message ){

        if(activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){

            if(activity.shouldShowRequestPermissionRationale(permission)){

                AlertDialog.Builder alert_builder = new AlertDialog.Builder(activity);
                alert_builder.setMessage(message);
                alert_builder.setTitle("Please Grant Permission.");
                alert_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        ActivityCompat.requestPermissions(
                                activity,
                                new String[]{ permission },
                                RUNTIME_PERMISSION_CODE

                        );
                    }
                });

                alert_builder.setNeutralButton("Cancel",null);

                AlertDialog dialog = alert_builder.create();

                dialog.show();

            } else {

                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{ permission },
                        RUNTIME_PERMISSION_CODE
                );
            }

        }
    }

}
