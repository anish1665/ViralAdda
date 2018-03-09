package com.ronak.viral.adda;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.ronak.viral.adda.inherit.BackPressFragment;
import com.ronak.viral.adda.inherit.PermissionsFragment;
import com.ronak.viral.adda.providers.CustomIntent;
import com.ronak.viral.adda.providers.fav.ui.FavFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HolderActivity extends AppCompatActivity{

    private Toolbar mToolbar;
    private Class<? extends Fragment> queueItem;
    private String[] queueItemData;

    public static void startActivity(Activity activity, Class<? extends Fragment> fragment, String[] data){
        Bundle bundle = new Bundle();
        bundle.putStringArray(MainActivity.FRAGMENT_DATA, data);
        bundle.putSerializable(MainActivity.FRAGMENT_CLASS, fragment);

        Intent intent = new Intent(activity, HolderActivity.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holder);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Class<? extends Fragment> fragmentClass = (Class<? extends Fragment>) getIntent().getExtras().getSerializable(MainActivity.FRAGMENT_CLASS);
        String[] args = getIntent().getExtras().getStringArray(MainActivity.FRAGMENT_DATA);

        if (CustomIntent.class.isAssignableFrom(fragmentClass)) {
            CustomIntent.performIntent(HolderActivity.this, args);
        } else {
            openFragment(fragmentClass, args);
        }
    }

    public void openFragment(Class<? extends Fragment> fragment, String[] data){
        if(!checkPermissionsHandleIfNeeded(fragment, data))
            return;

        try {
            Fragment frag = fragment.newInstance();

            // adding the data
            Bundle bundle = new Bundle();
            bundle.putStringArray(MainActivity.FRAGMENT_DATA, data);
            frag.setArguments(bundle);

            //Changing the fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, frag)
                    .commit();

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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
            case android.R.id.home:
                finish();
                return true;
            case R.id.settings:
                openFragment(SettingsFragment.class, new String[0]);
                return true;
            case R.id.favorites:
                openFragment(FavFragment.class, new String[0]);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
    	Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
    	
        if (fragment instanceof BackPressFragment) {
        	boolean handled = ((BackPressFragment) fragment).handleBackPress();
        	if (!handled)
        		super.onBackPressed();
        } else {         
        	super.onBackPressed();
        }
    }

    /**
     * Checks if the item can be opened because it has sufficient permissions.
     * @param fragment The item to check
     * @return true if the item is safe to open
     */
    private boolean checkPermissionsHandleIfNeeded(Class<? extends Fragment> fragment, String[] data){
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) return true;

        List<String> allPermissions = new ArrayList<>();
            if (PermissionsFragment.class.isAssignableFrom(fragment)) {
                try {
                    allPermissions.addAll(Arrays.asList(((PermissionsFragment) fragment.newInstance()).requiredPermissions()));
                } catch (Exception e) {
                    //Don't really care
                }
            }

        if (allPermissions.size() > 1) {
            boolean allGranted = true;
            for (String permission : allPermissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                    allGranted = false;
            }

            if (!allGranted) {
                //TODO An explanation before asking
                requestPermissions(allPermissions.toArray(new String[0]), 1);
                queueItem = fragment;
                queueItemData = data;
                return false;
            }

            return true;
        }

        return true;
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
                    openFragment(queueItem, queueItemData);
                } else {
                    // Permission Denied
                    Toast.makeText(HolderActivity.this, getResources().getString(R.string.permissions_required), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /** Implement if methods depend on this (like iaps?) don't work
  	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    List<Fragment> fragments = getSupportFragmentManager().getFragments();
    if (fragments != null)
    for (Fragment frag : fragments)
    if (frag != null)
    frag.onActivityResult(requestCode, resultCode, data);
    }
     */
}