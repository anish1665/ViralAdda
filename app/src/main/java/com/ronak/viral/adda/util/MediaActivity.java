package com.ronak.viral.adda.util;

import com.ronak.viral.adda.Config;
import com.ronak.viral.adda.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import com.ronak.viral.adda.util.Log;
import com.squareup.picasso.Picasso;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;


public class MediaActivity extends AppCompatActivity {

    public static boolean HIDE_TOOLBAR_OLD_DEVICES = true;

    public static String DOWNLOAD_ON_OPEN = "download_on_open";
	public static String TYPE = "type";
	public static String URL = "url";
	public static int TYPE_VID = 1;
	public static int TYPE_IMG = 2;
	public static int TYPE_AUDIO = 3;
	
	private boolean systemUIVisible;

	int type;
	String url;
    String downloadqueue;
	Toolbar mToolbar;
    ProgressDialog pDialog;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mDecorView = getWindow().getDecorView();
        mDecorView.setOnSystemUiVisibilityChangeListener(mOnSystemUiVisibilityChangeListener);
        
        setContentView(R.layout.activity_media);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
        hideSystemUI();
		
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        final VideoView videoView = (VideoView) findViewById(R.id.videoView);
        
        Bundle extras = getIntent().getExtras();
        url = extras.getString(URL);
        type = extras.getInt(TYPE);

        if (extras.containsKey(DOWNLOAD_ON_OPEN) && extras.getBoolean(DOWNLOAD_ON_OPEN)){
            file_download(url, MediaActivity.this);
            Toast.makeText(this, getResources().getString(R.string.downloading), Toast.LENGTH_LONG);
        }

        if (Config.PLAY_EXTERNAL){
            //Try opening it in another player and quitting the media activity
            openExternal(url);
            this.finish();
        }
        
        if (type == TYPE_VID || type == TYPE_AUDIO){
        	videoView.setVisibility(View.VISIBLE);
        	
            pDialog = new ProgressDialog(this);
            if (type == TYPE_VID)
            	pDialog.setTitle(getResources().getString(R.string.opening_video));
            else
            	pDialog.setTitle(getResources().getString(R.string.opening_audio));
            pDialog.setMessage(getResources().getString(R.string.loading));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

            try {
                MediaController mediacontroller = new MediaController(this);
                mediacontroller.setAnchorView(videoView);
                Uri video = Uri.parse(url);
                videoView.setMediaController(mediacontroller);
                videoView.setVideoURI(video);
            } catch (Exception e) {
                Log.printStackTrace(e);
            }

            videoView.requestFocus();
            videoView.setOnPreparedListener(new OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    pDialog.dismiss();
                    videoView.start();
                }
            });
            
            videoView.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					//'Handle' the error
                    Log.e("INFO", "Can't play media. Code: " + what + " Extra: " + extra + ". Proceeding to open in another player.");
					pDialog.dismiss();

                    //Try opening it in another player
                    openExternal(url);

					return true;
                }
            	
            });
            
        } else if (type == TYPE_IMG){
        	imageView.setVisibility(View.VISIBLE);
        	
            imageView.setImageDrawable(null);
            Picasso.with(this).load(url).into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                	if (systemUIVisible)
                		hideSystemUI();
                	else
                		showSystemUI();
                }
            });
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_media, menu);
        MenuItem item = menu.findItem(R.id.miShare);
        ShareActionProvider shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (url != null){
        	Intent shareIntent = new Intent();
        	shareIntent.setAction(Intent.ACTION_SEND);
        	shareIntent.putExtra(Intent.EXTRA_TEXT, url);

            shareIntent.setType("text/plain");
        
        	shareAction.setShareIntent(shareIntent);
        }
        
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.download:
			file_download(url, MediaActivity.this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    //Open the file with an intent instead of viewing it.
    public void openExternal(String url){
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        Intent mediaIntent = new Intent(Intent.ACTION_VIEW);
        mediaIntent.setDataAndType(Uri.parse(url), mimeType);
        startActivity(mediaIntent);
        finish();
    }

    //Download the shown media file
    public void file_download(String url,Context context) {

        String packageName = "com.android.providers.downloads";
        int state = this.getPackageManager().getApplicationEnabledSetting(packageName);

        //check if the download manager is disabled
        if(state==PackageManager.COMPONENT_ENABLED_STATE_DISABLED||
                state==PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                ||state==PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED){

            //If it is disabled, allow the user to enable it.
            Toast.makeText(this, R.string.downloadmanager_disabled, Toast.LENGTH_LONG).show();

            try {
                //Open the specific App Info page:
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);

            } catch ( ActivityNotFoundException e ) {
                //e.printStackTrace();

                //Open the generic Apps page:
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivity(intent);

            }


        } else {
            //If the download manager is enabled, check for permissions

            //check if there are sufficient permissions to download a file
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && (
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                downloadqueue = url;

            } else {
                url = url.replace(" ", "%20");
                DownloadManager downloadManager = (DownloadManager) ((Activity) context).getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                String name = URLUtil.guessFileName(url, null, MimeTypeMap.getFileExtensionFromUrl(url));
                String title = "File";
                if (type == TYPE_IMG) {
                    title = getResources().getString(R.string.file_image);
                }
                if (type == TYPE_VID) {
                    title = getResources().getString(R.string.file_video);
                }
                if (type == TYPE_AUDIO) {
                    title = getResources().getString(R.string.file_audio);
                }
                
				File fileDir = new File(Environment.DIRECTORY_DOWNLOADS);
				if (!fileDir.isDirectory()) {
        			fileDir.mkdirs();
   				}

                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(false)
                        .setDescription(getResources().getString(R.string.downloading))
                        .setTitle(title)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
                downloadManager.enqueue(request);
            }
        }

    }
    
    private View.OnSystemUiVisibilityChangeListener mOnSystemUiVisibilityChangeListener = new View.OnSystemUiVisibilityChangeListener() {
		@Override
        public void onSystemUiVisibilityChange(int visibility) {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == View.VISIBLE) {
                showSystemUI();
            } else {
            	hideSystemUI();
            }
        }
    };

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                boolean foundfalse = false;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        foundfalse = true;
                    }
                }
                if (!foundfalse){
                    file_download(downloadqueue, this);
                } else {
                    // Permission Denied
                    Toast.makeText(MediaActivity.this, getResources().getString(R.string.permissions_required), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    
    @SuppressLint("InlinedApi")
    private void showSystemUI(){
    	getSupportActionBar().show();
        if (android.os.Build.VERSION.SDK_INT >= 19) 
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        systemUIVisible = true;
    }
    
    @SuppressLint("InlinedApi")
    private void hideSystemUI(){

    	if (getSupportActionBar() != null){
            if (!(HIDE_TOOLBAR_OLD_DEVICES && android.os.Build.VERSION.SDK_INT <= 19)) {
                getSupportActionBar().hide();
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        systemUIVisible = false;
    }
 
}
