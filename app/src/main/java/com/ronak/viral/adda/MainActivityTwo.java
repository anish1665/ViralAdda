package com.ronak.viral.adda;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;
import com.ronak.viral.adda.drawer.NavItem;
import com.ronak.viral.adda.drawer.TabAdapter;
import com.ronak.viral.adda.fragments.WebViewFragmentTwo;
import com.ronak.viral.adda.helper.ConfigParserTwo;
import com.ronak.viral.adda.helper.FunctionHelper;
import com.ronak.viral.adda.providers.fav.ui.FavFragment;
import com.ronak.viral.adda.providers.yt.ui.YoutubeFragment;
import com.ronak.viral.adda.util.Helper;
import com.ronak.viral.adda.viralModels.Tab;
import com.ronak.viral.adda.viralModels.ViralObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivityTwo extends AppCompatActivity {

    private android.support.v7.widget.Toolbar toolbar;
    private android.support.design.widget.TabLayout tabs;
    private android.support.v4.view.ViewPager viewpager;
    List<NavItem> actions;
    private Context context;
    public static String FRAGMENT_DATA = "transaction_data";
    private int scrollCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_two);
        this.viewpager = (ViewPager) findViewById(R.id.viewpager);
        this.tabs = (TabLayout) findViewById(R.id.tabs);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        context = this;

        FunctionHelper.initToolbar(this, toolbar, "Movies Adda", "");

        checkInternetConnection();
        initListener();


    }

    private void initListener() {
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                showInterstitial();
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void checkInternetConnection() {
        if (!FunctionHelper.isConnectedToInternet(context)) {
            FunctionHelper.showAlertDialogWithTwoOpt(context, "Please connect to internet!", new FunctionHelper.DialogOptionsSelectedListener() {
                @Override
                public void onSelect(boolean isYes) {
                    if (isYes) {
                        checkInternetConnection();
                    } else {
                        finish();
                    }
                }

                @Override
                public void onDialogBackClick() {
                    finish();
                }
            }, "Try Again!", "Exit");

        } else {
            init();
        }
    }

    private void JstForTestingPurpose(boolean facedException, List<ViralObject> viralObjectList) {
        Log.e("isConfigLoaded", "" + facedException);


        {
            List<Tab> tabList = new ArrayList<>();

            List<String> stringsUrl = new ArrayList<>();
            stringsUrl.add("https://www.facebook.com/TaylorSwift/");//http://www.mi.com/in/

            Tab tab = new Tab("website", null, stringsUrl);
            tabList.add(tab);


            ViralObject viralObject = new ViralObject("facebook", null, null, false, tabList);
            viralObjectList.add(viralObject);
        }
        {
            List<Tab> tabList = new ArrayList<>();

            List<String> stringsUrl = new ArrayList<>();
            stringsUrl.add("http://www.mi.com/in/");//http://www.mi.com/in/

            Tab tab = new Tab("website", null, stringsUrl);
            tabList.add(tab);


            ViralObject viralObject = new ViralObject("Blog", null, null, false, tabList);
            viralObjectList.add(viralObject);
        }
        String res = new Gson().toJson(viralObjectList);
        Log.e("ViralObjList", res);

    }

    private void initFragments() {
        TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager(), actions, context);
        viewpager.setAdapter(tabAdapter);
        tabs.setupWithViewPager(viewpager);
    }

    private void init() {
        final ProgressDialog dialog;
        dialog = new ProgressDialog(context);
        dialog.setMessage("Please wait..");

        dialog.show();
        new ConfigParserTwo(Config.CONFIG_URL, MainActivityTwo.this, new ConfigParserTwo.CallBack() {
            @Override
            public void configLoaded(boolean facedException, List<ViralObject> viralObjectList) {
                dialog.dismiss();
//                JstForTestingPurpose(facedException, viralObjectList);
                if (!facedException) {
                    actions = new ArrayList<>();
                    for (int i = 0; i < viralObjectList.size(); i++) {
                        List<Tab> tabsItem = viralObjectList.get(i).getTabs();
                        for (int j = 0; j < tabsItem.size(); j++) {
                            if (tabsItem.get(j).getTitle().equalsIgnoreCase("Youtube")) {
                                actions.add(new NavItem(viralObjectList.get(i).getTitle(), YoutubeFragment.class, tabsItem.get(j).getArguments().toArray(new String[0])));
                            } else if (tabsItem.get(j).getTitle().equalsIgnoreCase("website")) {
                                actions.add(new NavItem(viralObjectList.get(i).getTitle(), WebViewFragmentTwo.class, tabsItem.get(j).getArguments().toArray(new String[0])));
                            }
                        }
                    }
                    initFragments();
                } else {
                    FunctionHelper.showAlertDialogWithOneOpt(context, "Sorry! there seems to be some issue, we can't load data, try again later!", new FunctionHelper.DialogOptionsSelectedListener() {
                        @Override
                        public void onSelect(boolean isYes) {
                            finish();
                        }

                        @Override
                        public void onDialogBackClick() {
                            finish();
                        }
                    }, "Ok");
                }
            }
        }).execute();

        Helper.admobLoader(this, getResources(), findViewById(R.id.adView));
        Helper.updateAndroidSecurityProvider(this);
    }

    private void showInterstitial() {
        //if (fromPager) return;
        if (getResources().getString(R.string.admob_interstitial_id).length() == 0) return;

        if (scrollCount == (Config.INTERSTITIAL_INTERVAL - 1)) {
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

            scrollCount = 0;
        } else {
            scrollCount++;
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

}
