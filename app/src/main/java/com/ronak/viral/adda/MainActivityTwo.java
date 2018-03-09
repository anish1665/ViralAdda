package com.ronak.viral.adda;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.gson.Gson;
import com.ronak.viral.adda.drawer.NavItem;
import com.ronak.viral.adda.drawer.TabAdapter;
import com.ronak.viral.adda.fragments.WebViewFragmentTwo;
import com.ronak.viral.adda.helper.ConfigParserTwo;
import com.ronak.viral.adda.providers.yt.ui.YoutubeFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_two);
        this.viewpager = (ViewPager) findViewById(R.id.viewpager);
        this.tabs = (TabLayout) findViewById(R.id.tabs);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        context = this;
        new ConfigParserTwo(Config.CONFIG_URL, MainActivityTwo.this, new ConfigParserTwo.CallBack() {
            @Override
            public void configLoaded(boolean facedException, List<ViralObject> viralObjectList) {
                JstForTestingPurpose(facedException, viralObjectList);
                actions = new ArrayList<>();
                for (int i = 0; i < viralObjectList.size(); i++) {
                    List<Tab> tabsItem = viralObjectList.get(i).getTabs();
                    for (int j = 0; j < tabsItem.size(); j++) {
                        if (tabsItem.get(j).getTitle().equalsIgnoreCase("Youtube")) {
                            actions.add(new NavItem(viralObjectList.get(i).getTitle(), YoutubeFragment.class, tabsItem.get(j).getArguments().toArray(new String[0])));
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString("url", "http://www.mi.com/in/");
                            WebViewFragmentTwo fragobj = new WebViewFragmentTwo();
                            fragobj.setArguments(bundle);
                            actions.add(new NavItem(viralObjectList.get(i).getTitle(), fragobj.getClass(), null));
                        }
                    }
                }
                initFragments();
            }
        }).execute();
        init();


    }

    private void JstForTestingPurpose(boolean facedException, List<ViralObject> viralObjectList) {
        Log.e("isConfigLoaded", "" + facedException);
        String res = new Gson().toJson(viralObjectList);
        Log.e("ViralObjList", res);

        List<Tab> tabList = new ArrayList<>();
        Tab tab = new Tab("facebook", "http://www.mi.com/in/", null);
        tabList.add(tab);

        ViralObject viralObject = new ViralObject("facebook", null, null, false, tabList);
        viralObject.setTitle("facebook");
        viralObjectList.add(viralObject);
    }

    private void initFragments() {
        TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager(), actions, context);
        viewpager.setAdapter(tabAdapter);
        tabs.setupWithViewPager(viewpager);
    }

    private void init() {


    }


}
