package com.ronak.viral.adda.providers;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.ronak.viral.adda.R;
import com.ronak.viral.adda.util.Log;

/**
 * To launch a custom intent from the menu. It is not actually a fragment
 * But it provides some helper methods for launching intents
 */
public class CustomIntent extends Fragment {

    public static String OPEN_URL = "url";
    public static String OPEN_APP = "app";
    public static String OPEN_ACTIVITY = "activity";

    public static void performIntent(Activity context, String[] params) {
        boolean success ;

        if (params[1].equals(OPEN_URL)) {
            success = openUrl(context, params[0]);
        } else if (params[1].equals(OPEN_APP)) {
            success = openApp(context, params[0]);
        } else if (params[1].equals(OPEN_ACTIVITY)) {
            success = openActivity(context, params[0]);
        } else {
            success = false;
        }

        if (!success)
            Toast.makeText(context, context.getResources().getString(R.string.intent_failed), Toast.LENGTH_LONG).show();
    }

    public static boolean openApp(Activity context, String packageName) {
        PackageManager manager = context.getPackageManager();

        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i == null) {
            return false;
            //throw new PackageManager.NameNotFoundException();
        }
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(i);

        return true;
    }

    public static boolean openUrl(Activity context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);

        return true;
    }

    public static boolean openActivity(Activity context, String classname) {
        try {
            Intent i = new Intent(context, Class.forName(classname));
            context.startActivity(i);
        } catch (ClassNotFoundException e) {
            Log.printStackTrace(e);
            return false;
        }
        return true;
    }
}
