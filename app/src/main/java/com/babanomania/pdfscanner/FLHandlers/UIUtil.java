package com.babanomania.pdfscanner.FLHandlers;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;

public class UIUtil {

    public static void setLightNavigationBar(View view, Activity activity){


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            view.setSystemUiVisibility(flags);
            activity.getWindow().setNavigationBarColor(Color.WHITE);
        }
    }
}
