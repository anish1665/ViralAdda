package com.ronak.viral.adda.providers.wordpress.api;

import android.app.Activity;
import android.view.View;
import android.widget.ListView;

import com.ronak.viral.adda.providers.wordpress.WordpressListAdapter;
import com.ronak.viral.adda.providers.wordpress.api.providers.JetPackProvider;
import com.ronak.viral.adda.providers.wordpress.api.providers.JsonApiProvider;
import com.ronak.viral.adda.providers.wordpress.api.providers.WordpressProvider;

public class WordpressGetTaskInfo{


    //Paging and status
	public Integer pages;
	public Integer curpage = 0;
    public boolean isLoading;
    public WordpressListAdapter feedListAdapter = null;

    //Static information about this instance
	public String baseurl;
	public Boolean simpleMode;
	public WordpressProvider provider = null;
	public Long ignoreId = 0L; //ID of post not to add

    //Views to track
    public ListView feedListView = null;
	public View footerView;
	public View dialogLayout;
	public View frame;

    public Activity context;
	
	public WordpressGetTaskInfo(View footerView, ListView listView, Activity context, View dialogLayout, View frame, String baseurl, Boolean simpleMode) {
		this.footerView = footerView;
		this.feedListView = listView;
		this.context = context;
		this.dialogLayout = dialogLayout;
		this.frame = frame;
		this.baseurl = baseurl;
		this.simpleMode = simpleMode;

		//We'll assume that sitenames don't contain http. Only sitesnames are accepted by the JetPack API.
		if (!baseurl.startsWith("http"))
			this.provider = new JetPackProvider();
		else
			this.provider = new JsonApiProvider();
	}

}
