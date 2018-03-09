package com.ronak.viral.adda.providers.radio.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ronak.viral.adda.inherit.CollapseControllingFragment;
import com.ronak.viral.adda.MainActivity;
import com.ronak.viral.adda.inherit.PermissionsFragment;
import com.ronak.viral.adda.R;
import com.ronak.viral.adda.providers.radio.NotificationUpdater;
import com.ronak.viral.adda.providers.radio.parser.AlbumArtGetter;
import com.ronak.viral.adda.providers.radio.parser.UrlParser;
import com.ronak.viral.adda.util.Helper;

import com.ronak.viral.adda.providers.radio.visualizer.DrawingPanel;
import com.ronak.viral.adda.util.Log;

import co.mobiwise.library.radio.RadioListener;
import co.mobiwise.library.radio.RadioManager;

/**
 *  This fragment is used to listen to a radio station
 */
public class MediaFragment extends Fragment implements OnClickListener, RadioListener, PermissionsFragment, CollapseControllingFragment {

    private RadioManager mRadioManager;
    private boolean runningOnOldConnection;
    private String[] arguments;
    private String urlToPlay;
    private Activity mAct;

    //Layouts
    private DrawingPanel dPanel;
    private ImageView imageView;
    private LinearLayout ll;
    private ProgressBar loadingIndicator;
    private Button buttonPlay;
    private Button buttonStopPlay;

    //Auto error solving
    private static int RETRY_INTERVAL = 7000;
    private int errorcount = 0;
    private static int RETRY_MAX = 2;

    //If we should use a visualizer or album art
    private boolean VISUALIZER_ENABLED = true;

    private static int audioSessionID = 0;


    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ll = (LinearLayout) inflater.inflate(R.layout.fragment_radio, container, false);

        initializeUIElements();

        //Get the arguments and 'parse' them
        arguments = MediaFragment.this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA);
        if (arguments.length > 1)
            VISUALIZER_ENABLED = arguments[1].equals("visualizer");

        //Initialize visualizer or imageview for album art
        if (VISUALIZER_ENABLED){
            int vType = 15;
            int cMode = 0;
            int cMode2 = 0;
            boolean frequency = true;
            dPanel = new DrawingPanel(getActivity(), vType, cMode, cMode2, frequency, audioSessionID);

            ((RelativeLayout) ll.findViewById(R.id.visualizerView)).addView(dPanel, 0);
        } else {
            imageView = new ImageView(ll.getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setBackground(getResources().getDrawable(R.drawable.radio));

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);

            imageView.setLayoutParams(params);

            ((RelativeLayout) ll.findViewById(R.id.visualizerView)).addView(imageView, 0);
        }

	    return ll;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		mAct = getActivity();
		
		Helper.isOnlineShowDialog(mAct);

        //Get the radioManager
        mRadioManager = RadioManager.with(mAct);

        mRadioManager.registerListener(NotificationUpdater.getStaticNotificationUpdater(mAct.getBaseContext()));

        //If we are already playing, wait until stop is clicked before re-connecting from this thread
        if (!mRadioManager.isConnected()) {
            mRadioManager.connect();
            runningOnOldConnection = false;
        } else {
            runningOnOldConnection = true;
        }

        //Parse the url on the background
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                urlToPlay = (UrlParser.getUrl(arguments[0]));

                if (isPlaying()) {
                    if (!mRadioManager.getService().getRadioUrl().equals(urlToPlay)) {
                        mAct.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(mAct, getResources().getString(R.string.radio_playing_other), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }

        });

    }

    private void initializeUIElements() {
        loadingIndicator = (ProgressBar) ll.findViewById(R.id.loadingIndicator);
        loadingIndicator.setMax(100);
        loadingIndicator.setVisibility(View.INVISIBLE);

        buttonPlay = (Button) ll.findViewById(R.id.btn_play);
        buttonPlay.setOnClickListener(this);

        buttonStopPlay = (Button) ll.findViewById(R.id.btn_pause);
        buttonStopPlay.setOnClickListener(this);

        updateButtons();
    }

    public void updateButtons(){
        if (isPlaying() || loadingIndicator.getVisibility() == View.VISIBLE){
        	buttonPlay.setEnabled(false);
        	buttonStopPlay.setEnabled(true);
        } else {
            buttonPlay.setEnabled(true);
            buttonStopPlay.setEnabled(false);

            updateMediaInfoFromBackground(null);
        }
    }

    public void onClick(View v) {
        if (v == buttonPlay) {
            if (urlToPlay != null) {
                startPlaying();

                //Check the sound level
                AudioManager am = (AudioManager) mAct.getSystemService(Context.AUDIO_SERVICE);
                int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (volume_level < 2) {
                    Toast.makeText(mAct, getResources().getString(R.string.volume_low), Toast.LENGTH_SHORT).show();
                }
            } else {
                //The loading of urlToPlay should happen almost instantly, so this code should never be reached
            }
        } else if (v == buttonStopPlay) {
            stopPlaying();
        }
    }

    private void startPlaying() {
        //Show loading view
        loadingIndicator.setVisibility(View.VISIBLE);

        //Start the radio playing
        mRadioManager.startRadio(urlToPlay);

        //Notification open intent
        Bundle bundle = new Bundle();
        bundle.putStringArray(MainActivity.FRAGMENT_DATA, getArguments().getStringArray(MainActivity.FRAGMENT_DATA));
        bundle.putSerializable(MainActivity.FRAGMENT_CLASS, this.getClass());
        RadioManager.getService().setNotificationOpenIntent(getActivity(), bundle);

        //Set the notification meta
        mRadioManager.updateNotification(mAct.getResources().getString(R.string.notification_playing), "",
                R.drawable.ic_radio_playing,
                BitmapFactory.decodeResource(
                        mAct.getResources(),
                        co.mobiwise.library.R.drawable.default_art));

        //Update the UI
        updateButtons();
    }

    private void stopPlaying() {

        //Stop the radio playing
        mRadioManager.stopRadio();

        //Hide loading layout if shown
        loadingIndicator.setVisibility(View.INVISIBLE);

        //Update the UI
        updateButtons();

        //Do a 'reset' if we're using a player from a different url
        if (runningOnOldConnection) {
            resetRadioManager();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    //@param info - the text to be updated. Giving a null string will hide the info.
    public void updateMediaInfoFromBackground(String info) {
        TextView nowPlayingTitle = (TextView) ll.findViewById(R.id.now_playing_title);
        TextView nowPlaying = (TextView) ll.findViewById(R.id.now_playing);

        if (info != null)
            nowPlaying.setText(info);

        if (info != null && nowPlayingTitle.getVisibility() == View.GONE){
            nowPlayingTitle.setVisibility(View.VISIBLE);
            nowPlaying.setVisibility(View.VISIBLE);
        } else if (info == null){
            nowPlayingTitle.setVisibility(View.GONE);
            nowPlaying.setVisibility(View.GONE);
        }
    }

   @Override
    public String[] requiredPermissions() {
        //return new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE};
       return null;
    }

    @Override
    public void onRadioLoading() {

    }

    @Override
    public void onRadioConnected() {
    }

    @Override
    public void onRadioStarted() {
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //Hide the loading indicator
                loadingIndicator.setVisibility(View.INVISIBLE);

                //Update buttons
                updateButtons();
            }
        });
    }

    @Override
    public void onRadioStopped() {
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //Hide the loading indicator
                loadingIndicator.setVisibility(View.INVISIBLE);

                //Update buttons
                updateButtons();

                //Only if the fragment is already on the foreground hide the notification
                if (MediaFragment.this.isVisible())
                    RadioManager.getService().cancelNotification();
            }
        });
    }

    @Override
    public void onMetaDataReceived(String key, final String value) {
        if (key != null && (key.equals("StreamTitle") || key.equals("title")) && !value.equals("")) {

            //Update the mediainfo shown above the controls
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateMediaInfoFromBackground(value);
                }
            });

            //Show album art based on the metadata
            updateAlbumArt(value);

        }
    }

    @Override
    public void onAudioSessionId(int i) {
        audioSessionID = i;
        if (VISUALIZER_ENABLED) {
            dPanel.setAudioSessionID(audioSessionID);
        }
    }

    @Override
    public void onError() {
        Log.v("INFO", "onerror");
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errorcount < RETRY_MAX) {
                    loadingIndicator.setVisibility(View.VISIBLE);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            errorcount += 1;
                            startPlaying();
                        }
                    }, RETRY_INTERVAL);
                } else {
                    //Inform the user
                    Toast.makeText(mAct, mAct.getResources().getString(R.string.error_retry), Toast.LENGTH_SHORT).show();
                    Log.v("INFO", "Received various errors, tried to create a new RadioManager");

                    //Do the 'reset'
                    resetRadioManager();

                    //Update the UI
                    loadingIndicator.setVisibility(View.INVISIBLE);
                    updateButtons();
                }
            }
        });
    }

    @Override
    public void onResume() {
        updateButtons();
        super.onResume();

        //Register for updates
        mRadioManager.registerListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        //Unregister from updates
        mRadioManager.unregisterListener(this);
    }

    private boolean isPlaying(){
        return (null != mRadioManager && null != RadioManager.getService() && RadioManager.getService().isPlaying());
    }

    private void resetRadioManager(){
        try {
            mRadioManager.disconnect();
        } catch (Exception e){
            //Do nothing, apparently we where not connected in the first place.
        }
        RadioManager.flush();
        mRadioManager = RadioManager.with(mAct);
        mRadioManager.connect();
        mRadioManager.registerListener(this);
        mRadioManager.registerListener(NotificationUpdater.getStaticNotificationUpdater(mAct.getBaseContext()));
        runningOnOldConnection = false;
    }

    private void updateAlbumArt(String infoString) {
        if (imageView != null){
            AlbumArtGetter.getImageForQuery(infoString, new AlbumArtGetter.AlbumCallback() {
                @Override
                public void finished(Bitmap art) {
                    if (art != null) {
                        imageView.setImageBitmap(art);
                    }
                }
            }, mAct);
        }
    }


    @Override
    public boolean supportsCollapse() {
        return false;
    }
}