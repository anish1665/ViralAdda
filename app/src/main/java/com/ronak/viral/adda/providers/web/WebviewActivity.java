package com.ronak.viral.adda.providers.web;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.ronak.viral.adda.inherit.BackPressFragment;
import com.ronak.viral.adda.MainActivity;
import com.ronak.viral.adda.R;

public class WebviewActivity extends AppCompatActivity{

    private Toolbar mToolbar;

    //Options to load a webpage
    public static String URL = "webview_url";
    public static String OPEN_EXTERNAL = "open_external";
    public static String LOAD_DATA = WebviewFragment.LOAD_DATA;
    public static String HIDE_NAVIGATION = WebviewFragment.HIDE_NAVIGATION;
    
    String mWebUrl = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holder);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Determine if we want to open this as intent or in the webview
        boolean openInWebView = true;
        if (getIntent().hasExtra(OPEN_EXTERNAL) && getIntent().getExtras().getBoolean(OPEN_EXTERNAL)){
            //If we have to load data directly, we can only do this locally
            if (!getIntent().hasExtra(LOAD_DATA)) {
                openInWebView = false;
            }
        }

        //Determine if we would like to fragment to display navigation, based on the passed bundle arguments
        boolean hideNavigation = false;
        if (getIntent().hasExtra(HIDE_NAVIGATION) && getIntent().getExtras().getBoolean(HIDE_NAVIGATION)){
            hideNavigation = true;
        }

        String data = null;
        if (getIntent().hasExtra(LOAD_DATA)){
            data = getIntent().getExtras().getString(LOAD_DATA);
        }

        //opening the webview fragment with passed url
        if (getIntent().hasExtra(URL)){
        	mWebUrl = getIntent().getExtras().getString(URL);
            if (openInWebView) {
                openWebFragmentForUrl(mWebUrl, hideNavigation, data);
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mWebUrl));
                startActivity(browserIntent);

                //Shutdown this activity
                finish();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
    
    
    public void openWebFragmentForUrl(String url, boolean hideNavigation, String data){
		Fragment fragment;
		fragment = new WebviewFragment();

        // adding the data
		Bundle bundle = new Bundle();
        bundle.putStringArray(MainActivity.FRAGMENT_DATA, new String[]{url});
        bundle.putBoolean(WebviewFragment.HIDE_NAVIGATION, hideNavigation);
        if (data != null)
            bundle.putString(WebviewFragment.LOAD_DATA, data);
		fragment.setArguments(bundle);

        //Changing the fragment
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.container, fragment)
				.commit();

        //Setting the title
        if (data == null)
		    setTitle(getResources().getString(R.string.webview_title));
        else
            setTitle("");
    }
    
    @Override
    public void onBackPressed() {
    	Fragment webview = getSupportFragmentManager().findFragmentById(R.id.container);
    	
        if (webview instanceof BackPressFragment) {
        	boolean handled = ((WebviewFragment)webview).handleBackPress();
        	if (!handled)
        		super.onBackPressed();
        } else {         
        	super.onBackPressed();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}