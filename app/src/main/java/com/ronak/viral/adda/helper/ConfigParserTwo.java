package com.ronak.viral.adda.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.gson.Gson;
import com.ronak.viral.adda.drawer.NavItem;
import com.ronak.viral.adda.drawer.SimpleSubMenu;
import com.ronak.viral.adda.providers.CustomIntent;
import com.ronak.viral.adda.providers.facebook.FacebookFragment;
import com.ronak.viral.adda.providers.instagram.InstagramFragment;
import com.ronak.viral.adda.providers.maps.MapsFragment;
import com.ronak.viral.adda.providers.overview.ui.OverviewFragment;
import com.ronak.viral.adda.providers.pinterest.PinterestFragment;
import com.ronak.viral.adda.providers.radio.ui.MediaFragment;
import com.ronak.viral.adda.providers.rss.ui.RssFragment;
import com.ronak.viral.adda.providers.soundcloud.ui.SoundCloudFragment;
import com.ronak.viral.adda.providers.tumblr.ui.TumblrFragment;
import com.ronak.viral.adda.providers.tv.TvFragment;
import com.ronak.viral.adda.providers.twi.ui.TweetsFragment;
import com.ronak.viral.adda.providers.web.WebviewFragment;
import com.ronak.viral.adda.providers.wordpress.ui.WordpressFragment;
import com.ronak.viral.adda.providers.yt.ui.YoutubeFragment;
import com.ronak.viral.adda.util.Helper;
import com.ronak.viral.adda.viralModels.Tab;
import com.ronak.viral.adda.viralModels.ViralObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Karthik
 * Copyright 2017
 */

/**
 * Async task class to get json by making HTTP call
 */
public class ConfigParserTwo extends AsyncTask<Void, Void, Void> {

    //Instance variables
    private String sourceLocation;
    @SuppressLint("StaticFieldLeak")
    private Activity context;
    private CallBack callback;
    private boolean facedException;
    private List<String> tabTitleList;
    private static JSONArray jsonMenu = null;
    private List<List<String>> mainArgsList;
    private List<ViralObject> viralObjectList;


    //Cache settings
    private static String CACHE_FILE = "menuCache.srl";
    final long MAX_FILE_AGE = 60 * 60 * 24 * 1;

    public ConfigParserTwo(String sourceLocation, Activity context, CallBack callback) {
        this.sourceLocation = sourceLocation;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(final Void... args) {

        if (jsonMenu == null)
            try {
                //Get the JSON
                if (sourceLocation.contains("http")) {
                    jsonMenu = getJSONFromCache();
                    if (getJSONFromCache() == null) {
                        Log.v("INFO", "Loading Menu Config from url.");
                        String jsonStr = Helper.getDataFromUrl(sourceLocation);
                        jsonMenu = new JSONArray(jsonStr);
                        saveJSONToCache(jsonStr);
                    } else {
                        Log.v("INFO", "Loading Menu Config from cache.");
                    }
                } else {
                    String jsonStr = Helper.loadJSONFromAsset(context, sourceLocation);
                    jsonMenu = new JSONArray(jsonStr);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        if (jsonMenu != null) {

            final JSONArray jsonMenuFinal = jsonMenu;


            //Adding menu items must happen on UIthread
            context.runOnUiThread(new Runnable() {
                public void run() {
                    viralObjectList = new ArrayList<>();
                    try {

                        for (int i = 0; i < jsonMenuFinal.length(); i++) {
                            JSONObject jsonMenuItem = jsonMenuFinal.getJSONObject(i);

                            String title = jsonMenuItem.getString("title");
                            String drawable = jsonMenuItem.getString("drawable");
                            String submenu = jsonMenuItem.getString("submenu");
                            boolean iap = jsonMenuItem.getBoolean("iap");
                            List<Tab> tabList = new ArrayList<>();

                            JSONArray jsonTabs = jsonMenuItem.getJSONArray("tabs");
                            for (int j = 0; j < jsonTabs.length(); j++) {
                                JSONObject jsonTabItem = jsonTabs.getJSONObject(j);
                                String innerTitle = jsonTabItem.getString("title");
                                String provider = jsonTabItem.getString("provider");
                                JSONArray arrayArgs = jsonTabItem.getJSONArray("arguments");
                                List<String> arguments = new ArrayList<>();
                                for (int k = 0; k < arrayArgs.length(); k++) {
                                    String arg = arrayArgs.optString(k);
                                    arguments.add(arg);
                                }
                                tabList.add(new Tab(innerTitle, provider, arguments));
                            }
                            viralObjectList.add(new ViralObject(title, drawable, submenu, iap, tabList));



                           /* String menuTitle = jsonMenuItem.getString("title");
                            tabTitleList.add(menuTitle);
                            JSONObject tabs = jsonMenuItem.getJSONObject("tabs");

                            Log.e("menuTitle [" + i + "]", menuTitle);*/
                        }


                    } catch (final JSONException e) {
                        e.printStackTrace();
                        Log.e("INFO", "JSON was invalid");
                        facedException = true;
                    }

                }
            }); //end of runOnUIThread
        } else {
            Log.e("INFO", "JSON Could not be retrieved");
            facedException = true;
        }

        return null;
    }

    public static NavItem navItemFromJSON(JSONObject jsonTab) throws JSONException {
        String tabTitle = jsonTab.getString("title");
        String tabProvider = jsonTab.getString("provider");

        //Parse the type
        Class<? extends Fragment> tabClass = null;
        if (tabProvider.equals("wordpress"))
            tabClass = WordpressFragment.class;
        else if (tabProvider.equals("facebook"))
            tabClass = FacebookFragment.class;
        else if (tabProvider.equals("rss"))
            tabClass = RssFragment.class;
        else if (tabProvider.equals("youtube"))
            tabClass = YoutubeFragment.class;
        else if (tabProvider.equals("instagram"))
            tabClass = InstagramFragment.class;
        else if (tabProvider.equals("webview"))
            tabClass = WebviewFragment.class;
        else if (tabProvider.equals("tumblr"))
            tabClass = TumblrFragment.class;
        else if (tabProvider.equals("stream"))
            tabClass = TvFragment.class;
        else if (tabProvider.equals("soundcloud"))
            tabClass = SoundCloudFragment.class;
        else if (tabProvider.equals("maps"))
            tabClass = MapsFragment.class;
        else if (tabProvider.equals("twitter"))
            tabClass = TweetsFragment.class;
        else if (tabProvider.equals("radio"))
            tabClass = MediaFragment.class;
        else if (tabProvider.equals("pinterest"))
            tabClass = PinterestFragment.class;
        else if (tabProvider.equals("custom"))
            tabClass = CustomIntent.class;
        else if (tabProvider.equals("overview"))
            tabClass = OverviewFragment.class;
        else
            throw new RuntimeException("Invalid type specified for tab");

        JSONArray args = jsonTab.getJSONArray("arguments");
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < args.length(); i++) {
            list.add(args.getString(i));
        }

        NavItem item = new NavItem(tabTitle, tabClass, list.toArray(new String[0])); //todo: 1

        //Add the image if present
        if (jsonTab.has("image")
                && jsonTab.getString("image") != null
                && !jsonTab.getString("image").isEmpty()) {
            item.setCategoryImageUrl(jsonTab.getString("image"));
        }

        return item;
    }

    @Override
    protected void onPostExecute(Void args) {
        if (callback != null)
            callback.configLoaded(facedException, viralObjectList);
    }

    public int getDrawableByName(String name) {
        Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier(name, "drawable",
                context.getPackageName());
        return resourceId;
    }

    public interface CallBack {
        void configLoaded(boolean success, List<ViralObject> viralObjectList);
    }


    public void saveJSONToCache(String json) {
        // Instantiate a JSON object from the request response
        try {
            // Save the JSONObject
            ObjectOutput out = null;

            out = new ObjectOutputStream(new FileOutputStream(new File(context.getCacheDir(), "") + CACHE_FILE));

            out.writeObject(json);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONArray getJSONFromCache() {
        // Load in an object
        try {
            ObjectInputStream in = null;
            File cacheFile = new File(new File(context.getCacheDir(), "") + CACHE_FILE);
            in = new ObjectInputStream(new FileInputStream(cacheFile));
            String jsonArrayRaw = (String) in.readObject();
            in.close();

            //If the cache is not outdated
            if (cacheFile.lastModified() + MAX_FILE_AGE > System.currentTimeMillis())
                return new JSONArray(jsonArrayRaw);
            else
                return null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

}
