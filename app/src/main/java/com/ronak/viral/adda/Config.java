package com.ronak.viral.adda;

import com.ronak.viral.adda.drawer.SimpleMenu;

public class Config {

    private Config() {
    }



    //The Config.json file that defines your app's content.
    //Point url to JSON or leave empty to use config.json from assets.
    public static String CONFIG_URL = "https://sites.google.com/site/ronakviral1929/json/viral.json";

    //To open links in the WebView or outside the WebView.
    public static final boolean OPEN_EXPLICIT_EXTERNAL = true;
    public static final boolean OPEN_INLINE_EXTERNAL = false;

    //To open videos in our Local player or outside the local player
    public static final boolean PLAY_EXTERNAL = false;

    //Wordpress perma-friendly API requests (JSON API)
    public static final boolean USE_WP_FRIENDLY = true;

    //If ads are enabled, also show them on the youtube layout
    public static final boolean ADMOB_YOUTUBE = true;

    //If the drawer should be disabled
    public static final boolean HIDE_DRAWER = false;

    //The frequency in which interstitial ads are shown
    //('0' to never show, '1' to always show, '2' to show 1 out of 2, etc)
    public static final int INTERSTITIAL_INTERVAL = 5;

    //Will load configuration from hardcoded Config class instead of JSON.
    public static boolean USE_HARDCODED_CONFIG = false;

	//If you use a hardcoded config, initialise it below
    public static void configureMenu(SimpleMenu menu, ConfigParser.CallBack callback){
		
		/**
        List<NavItem> firstTabs = new ArrayList<NavItem>();
        firstTabs.add(new NavItem("RSS", RssFragment.class,
                new String[]{"http://feeds.feedburner.com/AndroidPolice"}));
        firstTabs.add(new NavItem("Rss Podcast", RssFragment.class,
                new String[]{"http://feeds.nature.com/nature/podcast/current"}));
        firstTabs.add(new NavItem("SoundCloud", SoundCloudFragment.class,
                new String[]{"13568105"}));
        firstTabs.add(new NavItem("WebView", WebviewFragment.class,
                new String[]{"http://sherdle.com"}));
        menu.add("Other", R.drawable.ic_details, firstTabs);

        List<NavItem> blogTabs = new ArrayList<NavItem>();
        blogTabs.add(new NavItem("Jetpack", WordpressFragment.class,
                new String[]{"en.blog.wordpress.com", ""}));
        blogTabs.add(new NavItem("Jetpack Cat", WordpressFragment.class,
                new String[]{"en.blog.wordpress.com", "events"}));
        blogTabs.add(new NavItem("Wordpress Recent", WordpressFragment.class,
                new String[]{"http://androidpolice.com", "", "http://androidpolice.disqus.com/;androidpolice;%d http://www.androidpolice.com/?p=%d"}));
        menu.add("Blogs", R.drawable.ic_details, blogTabs);

        List<NavItem> streamingTabs = new ArrayList<>();
        streamingTabs.add(new NavItem("Video", TvFragment.class,
                new String[]{"http://abclive.abcnews.com/i/abc_live4@136330/index_1200_av-b.m3u8"})); //TODO Test
        streamingTabs.add(new NavItem("AAC Shoutcast", MediaFragment.class,
                new String[]{"http://yp.shoutcast.com/sbin/tunein-station.pls?id=830692", "visualizer"}));
        streamingTabs.add(new NavItem("3FM", MediaFragment.class,
                new String[]{"http://yp.shoutcast.com/sbin/tunein-station.m3u?id=709809", "visualizer"}));
        menu.add("Streaming", R.drawable.ic_details, streamingTabs, true);

        SimpleSubMenu sub = new SimpleSubMenu(menu, "Test");
        List<NavItem> thirdTabs = new ArrayList<NavItem>();
        thirdTabs.add(new NavItem("Twitter", TweetsFragment.class,
                new String[]{"Android"}));
        thirdTabs.add(new NavItem("SoundCloud", SoundCloudFragment.class,
                new String[]{"13568105"}));
        thirdTabs.add(new NavItem("Tumblr", TumblrFragment.class,
                new String[]{"androidbackgrounds"}));
        thirdTabs.add(new NavItem("Instagram", InstagramFragment.class,
                new String[]{"2948597263"}));
        thirdTabs.add(new NavItem("Facebook", FacebookFragment.class,
                new String[]{"104958162837"}));
        thirdTabs.add(new NavItem("Youtube Channel", YoutubeFragment.class,
                new String[]{"PLOcMSsuppV4pWBxVVJGE9dOeHUtOxHJDd","UC7V6hW6xqPAiUfataAZZtWA"}));
        thirdTabs.add(new NavItem("Youtube PlayList", YoutubeFragment.class,
                new String[]{"PLOcMSsuppV4pWBxVVJGE9dOeHUtOxHJDd"}));
        sub.add("Social", R.drawable.ic_details, thirdTabs, true);

        List<NavItem> permissionTabs = new ArrayList<>();
        permissionTabs.add(new NavItem("AAC Shoutcast", MediaFragment.class,
                new String[]{"http://yp.shoutcast.com/sbin/tunein-station.pls?id=830692", "visualizer"}));
        permissionTabs.add(new NavItem("Maps Query", MapsFragment.class,
                new String[]{"pharmacy"}));
        permissionTabs.add(new NavItem("Maps Point", MapsFragment.class,
                new String[]{"<b>Adress:</b><br>SomeStreet 5<br>Sydney, Australia<br><br><i>Email: Mail@Company.com</i>",
                        "Company",
                        "This is where our office is.",
                        "-33.864",
                        "151.206",
                        "13"}));
        sub.add("Permissions", R.drawable.ic_details, permissionTabs);

        List<NavItem> customTab = new ArrayList<>();
        customTab.add(new NavItem("Open App", CustomIntent.class,
                new String[]{ "com.spotify.music", CustomIntent.OPEN_APP}));
        sub.add("Custom", R.drawable.ic_details, customTab);**/

        //Return the configuration
        callback.configLoaded(false);
    }

}