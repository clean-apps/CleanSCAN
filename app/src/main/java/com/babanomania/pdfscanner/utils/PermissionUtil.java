package com.babanomania.pdfscanner.utils;

import android.Manifest;
import android.app.Activity;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

public class PermissionUtil {

    public static void ask(final Activity activity ){

        TedPermission.with( activity.getApplicationContext() )
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        //Toast.makeText(activity, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(activity, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                    }


                })
                .setDeniedMessage(
                        "If you reject permission,you can not use this service\n\n" +
                        "Please turn on permissions at [Setting] > [Permission]")
                .setPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET

                ).check();
    }

}
