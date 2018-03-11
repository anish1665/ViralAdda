package com.ronak.viral.adda.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;

/**
 * Created by anish on 30-08-2017.
 */

public class FunctionHelper {
    public static DialogOptionsSelectedListener dialogOptionsSelectedListener;

    public static void initToolbar(final AppCompatActivity activity, Toolbar toolbar, String title, String subtitle) {
        toolbar.setTitle(title);
        toolbar.setSubtitle(subtitle);
        toolbar.setTitleTextColor(Color.WHITE);
        activity.setSupportActionBar(toolbar);
    }

    public static void showAlertDialogWithTwoOpt(Context mContext, String message, final DialogOptionsSelectedListener dialogOptionsSelectedListener, String yesOption, String noOption) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(yesOption, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogOptionsSelectedListener != null)
                            dialogOptionsSelectedListener.onSelect(true);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(noOption, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogOptionsSelectedListener != null)
                            dialogOptionsSelectedListener.onSelect(false);
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog alert = builder.create();

        alert.show();
    }

    public static void showAlertDialogWithOneOpt(final Context mContext, String message, final DialogOptionsSelectedListener dialogOptionsSelectedListener, String yesOption) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(yesOption, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogOptionsSelectedListener != null)
                            dialogOptionsSelectedListener.onSelect(true);
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog alert = builder.create();

        alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialogOptionsSelectedListener.onDialogBackClick();
                }
                return true;
            }
        });
        alert.show();
    }

    public interface DialogOptionsSelectedListener {
        void onSelect(boolean isYes);

        void onDialogBackClick();
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

