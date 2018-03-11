package com.ronak.viral.adda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.ronak.viral.adda.drawer.MenuItemCallback;
import com.ronak.viral.adda.drawer.NavItem;
import com.ronak.viral.adda.drawer.SimpleMenu;
import com.ronak.viral.adda.drawer.TabAdapter;
import com.ronak.viral.adda.inherit.BackPressFragment;
import com.ronak.viral.adda.inherit.CollapseControllingFragment;
import com.ronak.viral.adda.inherit.PermissionsFragment;
import com.ronak.viral.adda.providers.CustomIntent;
import com.ronak.viral.adda.providers.fav.ui.FavFragment;
import com.ronak.viral.adda.util.Log;
import com.ronak.viral.adda.util.Helper;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements MenuItemCallback, ConfigParser.CallBack {

	private Toolbar mToolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private NavigationView navigationView;
    private TabAdapter adapter;
    private static SimpleMenu menu;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private static final String TAG = "MainActivity";

    //Keep track of the interstitials we show
    private int interstitialCount = -1;

    //Data to pass to a fragment
	public static String FRAGMENT_DATA = "transaction_data";
    public static String FRAGMENT_CLASS = "transation_target";

	public static boolean TABLET_LAYOUT = true;
   // String nameCheck = "KTV Sierra Leone";

    //Permissions Queu
	List<NavItem> queueItem;
    MenuItem queueMenuItem;


    @Override
    public void configLoaded(boolean facedException) {
        if (facedException){
            if (Helper.isOnlineShowDialog(MainActivity.this))
                Toast.makeText(this, R.string.invalid_configuration, Toast.LENGTH_LONG).show();
        } else {
            //Load the first item (we assume the first item doesn't require purchase)
            menuItemClicked(menu.getFirstMenuItem().getValue(), menu.getFirstMenuItem().getKey(), false);
        }
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Load the appropriate layout
		if (useTabletMenu()){
			setContentView(R.layout.activity_main_tablet);
			Helper.setStatusBarColor(MainActivity.this,
					ContextCompat.getColor(this, R.color.myPrimaryDarkColor));
		} else {
			setContentView(R.layout.activity_main);
		}

        String nameCompare = getString(R.string.app_name);

       /* if(nameCompare.equals(nameCheck)){
            //Do Nothing
        }else {
            finish();
        }*/

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);

		if (!useTabletMenu())
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		else {
			getSupportActionBar().setDisplayShowHomeEnabled(false);
		}

        //Drawer
        if (!useTabletMenu()) {
            drawer = (DrawerLayout) findViewById(R.id.drawer);
            toggle = new ActionBarDrawerToggle(
                    this, drawer, mToolbar, R.string.drawer_open, R.string.drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
        }

        //Layouts
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        //Check if we should open a fragment based on the arguments we have
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(FRAGMENT_CLASS)) {
            try {
                Class<? extends Fragment> fragmentClass = (Class<? extends Fragment>) getIntent().getExtras().getSerializable(FRAGMENT_CLASS);
                if (fragmentClass != null) {
                    String[] extra = getIntent().getExtras().getStringArray(FRAGMENT_DATA);

                    HolderActivity.startActivity(this, fragmentClass, extra);
                    finish();
                    //Optionally, we can also point intents to holderactivity directly instead of MainAc.
                }
            } catch (Exception e) {
                //If we come across any errors, just continue and open the default fragment
                Log.printStackTrace(e);
            }
        }

        //Menu items
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        menu = new SimpleMenu(navigationView.getMenu(), this);
        if (Config.USE_HARDCODED_CONFIG) {
            Config.configureMenu(menu, this);
        } else if (!Config.CONFIG_URL.isEmpty() && Config.CONFIG_URL.contains("http"))
            new ConfigParser(Config.CONFIG_URL, menu, this, this).execute();
        else
            new ConfigParser("config.json", menu, this, this).execute();
        tabLayout.setupWithViewPager(viewPager);

		if (!useTabletMenu()) {
			drawer.setStatusBarBackgroundColor(
				ContextCompat.getColor(this, R.color.myPrimaryDarkColor));
		}

		applyDrawerLocks();

		Helper.admobLoader(this, getResources(), findViewById(R.id.adView));
		Helper.updateAndroidSecurityProvider(this);



        // Checking if the user would prefer to show the menu on start
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean checkBox = prefs.getBoolean("menuOpenOnStart", false);
        if (checkBox && !useTabletMenu()) {
            drawer.openDrawer(GravityCompat.START);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                onTabBecomesActive(position);
            }
        });
	}

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
                    //Retry to open the menu item
                    //(we can assume the item is 'purchased' otherwise a permission check would have not occured)
					menuItemClicked(queueItem, queueMenuItem, false);
				} else {
					// Permission Denied
					Toast.makeText(MainActivity.this, getResources().getString(R.string.permissions_required), Toast.LENGTH_SHORT)
							.show();
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

    @Override
    public void menuItemClicked(List<NavItem> actions, MenuItem item, boolean requiresPurchase) {
        //Close the drawer
        if (drawer != null)
            drawer.closeDrawer(GravityCompat.START);

        //Check if the user is allowed to open item
        if (requiresPurchase && !isPurchased()) return; //isPurchased will handle this.
        if (!checkPermissionsHandleIfNeeded(actions, item)) return; //checkPermissions will handle.

        if (isCustomIntent(actions)) return;

        //Uncheck all other items, check the current item
        if (item != null) {
            for (MenuItem menuItem : menu.getMenuItems())
                menuItem.setChecked(false);
            item.setChecked(true);
        }

        //Load the new tabs
        adapter = new TabAdapter(getSupportFragmentManager(), actions, this);
        viewPager.setAdapter(adapter);

        //Show or hide the tab bar depending on if we need it
        if (actions.size() == 1)
            tabLayout.setVisibility(View.VISIBLE);
        else
            tabLayout.setVisibility(View.VISIBLE);

        //Show in interstitial
        showInterstitial(false);

        onTabBecomesActive(0);
    }

    private void onTabBecomesActive(int position){
        Fragment fragment = adapter.getItem(position);
        //If fragment does not support collapse, or if OS does not support collaps, disable collapsing toolbar
        if ((fragment instanceof CollapseControllingFragment
                && !((CollapseControllingFragment) fragment).supportsCollapse())
                ||
                (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT))
            lockAppBar();
        else
            unlockAppBar();

        if (position != 0)
            showInterstitial(true);
    }

    /**
     * Show an interstitial ad
     * @param fromPager if the showing is triggered from a swipe in the viewpager
     */
    private void showInterstitial(boolean fromPager) {
        //if (fromPager) return;
        if (getResources().getString(R.string.admob_interstitial_id).length() == 0) return;

        if (interstitialCount == (Config.INTERSTITIAL_INTERVAL - 1)) {
            final InterstitialAd mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_id));
            AdRequest adRequestInter = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mInterstitialAd.show();
                }
            });
            mInterstitialAd.loadAd(adRequestInter);

            interstitialCount = 0;
        } else {
            interstitialCount++;
        }

    }

    /**
     * Checks if the item is/contains a custom intent, and if that the case it will handle it.
     * @param items List of NavigationItems
     * @return True if the item is a custom intent, in that case
     */
    private boolean isCustomIntent(List<NavItem> items){
        NavItem customIntentItem = null;
        for (NavItem item : items){
            if (CustomIntent.class.isAssignableFrom(item.getFragment())){
                customIntentItem = item;
            }
        }

        if (customIntentItem == null) return false;
        if (items.size() > 1) Log.e("INFO", "Custom Intent Item must be only child of menu item! Ignorning all other tabs");

        CustomIntent.performIntent(MainActivity.this, customIntentItem.getData());
        return true;
    }

    /**
     * If the item can be opened because it either has been purchased or does not require a purchase to show.
     * @return true if the app is purchased. False if the app hasn't been purchased, or if iaps are disabled
     */
    private boolean isPurchased(){
        String license = getResources().getString(R.string.google_play_license);
        // if item does not require purchase, or app has purchased, or license is null/empty (app has no in app purchases)
        if (!SettingsFragment.getIsPurchased(this) && !license.equals("")) {
            String[] extra = new String[] {SettingsFragment.SHOW_DIALOG};
            HolderActivity.startActivity(this, SettingsFragment.class, extra);

            return false;
        }

        return true;
    }

    /**
     * Checks if the item can be opened because it has sufficient permissions.
     * @param tabs The tabs to check
     * @return true if the item is safe to open
     */
    private boolean checkPermissionsHandleIfNeeded(List<NavItem> tabs, MenuItem item){
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) return true;

        List<String> allPermissions = new ArrayList<>();
        for (NavItem tab : tabs){
            if (PermissionsFragment.class.isAssignableFrom(tab.getFragment())) {
                try {
                    allPermissions.addAll(Arrays.asList(((PermissionsFragment) tab.getFragment().newInstance()).requiredPermissions()));
                } catch (Exception e) {
                    //Don't really care
                }
            }
        }

        if (allPermissions.size() > 1) {
            boolean allGranted = true;
            for (String permission : allPermissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                    allGranted = false;
            }

            if (!allGranted) {
                //TODO An explaination before asking
                requestPermissions(allPermissions.toArray(new String[0]), 1);
                queueItem = tabs;
                queueMenuItem = item;
                return false;
            }

            return true;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.settings:
                HolderActivity.startActivity(this, SettingsFragment.class, null);
                return true;
            case R.id.favorites:
                HolderActivity.startActivity(this, FavFragment.class, null);
                return true;
            case R.id.contact:
                HolderActivity.startActivity(this, ContactFragment.class, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onBackPressed() {
		Fragment activeFragment = adapter.getCurrentFragment();

		if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else if (activeFragment instanceof BackPressFragment){
			boolean handled = ((BackPressFragment) activeFragment).handleBackPress();
            if (!handled){
                super.onBackPressed();
            }
		} else {
            super.onBackPressed();
        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		if (fragments != null)
			for (Fragment frag : fragments)
				if (frag != null)
					frag.onActivityResult(requestCode, resultCode, data);
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
    }

	//Check if we should adjust our layouts for tablets
	public boolean useTabletMenu(){
		return (getResources().getBoolean(R.bool.isWideTablet) && TABLET_LAYOUT);
	}

    //Apply the appropiate locks to the drawer
    public void applyDrawerLocks(){
        if (drawer == null) {
            if (Config.HIDE_DRAWER)
                navigationView.setVisibility(View.GONE);
            return;
        }

        if (Config.HIDE_DRAWER){
            toggle.setDrawerIndicatorEnabled(false);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    public void lockAppBar() {
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setScrollFlags(0);
    }

    public void unlockAppBar() {
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
    }



}