package com.ronak.viral.adda.providers.radio;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ronak.viral.adda.R;
import com.ronak.viral.adda.providers.radio.parser.AlbumArtGetter;

import co.mobiwise.library.radio.RadioListener;
import co.mobiwise.library.radio.RadioManager;

/**
 * The purpose of this class is to have notification updated in the background.
 */
public class NotificationUpdater implements RadioListener{

    private Context context;
    private static NotificationUpdater updater;

    public static NotificationUpdater getStaticNotificationUpdater(Context context){
        if (updater == null){
            updater = new NotificationUpdater(context);
        }
        return updater;
    }

    private NotificationUpdater(Context context){
        this.context = context;
    }

    @Override
    public void onRadioLoading() {

    }

    @Override
    public void onRadioConnected() {

    }

    @Override
    public void onRadioStarted() {

    }

    @Override
    public void onRadioStopped() {

    }

    @Override
    public void onMetaDataReceived(String key, String value) {
        if (key != null && (key.equals("StreamTitle") || key.equals("title")) && !value.equals("")) {
            String title = value;
            String artist = "";
            if (value.contains(" - ")){
                title = value.split("-")[1];
                artist = value.split("-")[0];
            }

            if (value == null || value.equals("")) value = title;
            final String infoString = value;

            RadioManager.getService().updateNotification(title, artist,
                    R.drawable.ic_radio_playing,
                    BitmapFactory.decodeResource(
                            context.getResources(),
                            co.mobiwise.library.R.drawable.default_art));

            updateAlbumArt(infoString, title, artist);

        }
    }

    @Override
    public void onAudioSessionId(int i) {

    }

    @Override
    public void onError() {

    }

    private void updateAlbumArt(String infoString, final String title, final String artist){
        AlbumArtGetter.getImageForQuery(infoString, new AlbumArtGetter.AlbumCallback() {
            @Override
            public void finished(Bitmap art) {
                if (art != null) {
                    RadioManager.getService().updateNotification(title, artist,
                            R.drawable.ic_radio_playing,
                            art);
                }
            }
        }, context);
    }
}
