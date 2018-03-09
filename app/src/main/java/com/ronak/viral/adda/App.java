package com.ronak.viral.adda;

import android.app.Application;
import android.content.Intent;

import com.crittercism.app.Crittercism;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;
import com.ronak.viral.adda.providers.web.WebviewActivity;

import org.json.JSONObject;

/**
  *
 * @author Karthik
 * Copyright 2017
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Auto-error reporting
        Crittercism.initialize(this, "83ed97abdd274005ace744f3c8813d4a00555300");

        //OneSignal Push
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new NotificationHandler())
                .init();
    }

    // This fires when a notification is opened by tapping on it or one is received while the app is running.
    class NotificationHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            try {
                JSONObject data = result.notification.payload.additionalData;
                if (data != null) {
                    String url = data.optString("url", null);
                    if (url != null) {
                        //If the app is not on foreground, clicking the notification will start the app, and push_url will be used.
                        Intent browserIntent;
                        //if (!result.notification.isAppInFocus) {
                            browserIntent = new Intent(App.this, WebviewActivity.class);
                            browserIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                            browserIntent.putExtra(WebviewActivity.OPEN_EXTERNAL, Config.OPEN_INLINE_EXTERNAL);
                            browserIntent.putExtra(WebviewActivity.URL, result.notification.payload.additionalData.getString("url"));
                            android.util.Log.v("INFO", "Received notification while app was on background");
                       // } else { //If the app is in foreground, don't interup the current activities, but open webview in a new window.
                       //     browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(result.notification.payload.additionalData.getString("url")));
                       //     browserIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                       //     android.util.Log.v("INFO", "Received notification while app was on foreground");
                       // }
                        startActivity(browserIntent);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    }
}