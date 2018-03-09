package com.ronak.viral.adda.providers.facebook;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import com.ronak.viral.adda.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ronak.viral.adda.MainActivity;
import com.ronak.viral.adda.R;
import com.ronak.viral.adda.util.Helper;

/**
 * This fragment is used to display a list of facebook posts
 */

public class FacebookFragment extends Fragment {

	//private ArrayList<FacebookItem> postList = null;
	private ListView listView = null;
	private View footerView;
	private Activity mAct;
	private DownloadFilesTask mTask;
	private FacebookAdapter postListAdapter = null;

	private LinearLayout ll;
	RelativeLayout dialogLayout;

	String nextpageurl;

	String username;

	Boolean isLoading = false;

	private static String API_URL_BEGIN = "https://graph.facebook.com/";
	private static String API_URL_MIDDLE = "/posts/?access_token=";
	private static String API_URL_END = "&date_format=U&fields=comments.limit(50).summary(1),likes.limit(0).summary(1),from,picture,message,story,name,link,id,created_time,full_picture,source,type&limit=10";

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ll = (LinearLayout) inflater.inflate(R.layout.fragment_list,
				container, false);
		setHasOptionsMenu(true);

		username = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];

		footerView = inflater.inflate(R.layout.listview_footer, null);
		listView = (ListView) ll.findViewById(R.id.list);
		listView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				if (postListAdapter == null)
					return;

				if (postListAdapter.getCount() == 0)
					return;

				int l = visibleItemCount + firstVisibleItem;
				if (l >= totalItemCount && !isLoading && nextpageurl != null) {
					mTask = new DownloadFilesTask(false);
					mTask.execute();
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}
		});
		return ll;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();

		mTask = new DownloadFilesTask(true);
		mTask.execute();
	}


	public void updateList(ArrayList<FacebookItem> posts, boolean initialload) {
		if (initialload) {
			postListAdapter = new FacebookAdapter(mAct, posts);
			listView.setAdapter(postListAdapter);
		} else {
			postListAdapter.addAll(posts);
			postListAdapter.notifyDataSetChanged();
		}
	}

	private class DownloadFilesTask extends AsyncTask<String, Integer, ArrayList<FacebookItem>> {

		boolean initialload;

		DownloadFilesTask(boolean firstload) {
			this.initialload = firstload;
		}

		@Override
		protected void onPreExecute() {
			if (isLoading) {
				this.cancel(true);
			} else {
				isLoading = true;
			}
			if (initialload) {
				dialogLayout = (RelativeLayout) ll
						.findViewById(R.id.progressBarHolder);

				if (dialogLayout.getVisibility() == View.GONE) {
					dialogLayout.setVisibility(View.VISIBLE);
					listView.setVisibility(View.GONE);
				}

				nextpageurl = (API_URL_BEGIN + username + API_URL_MIDDLE  + getResources().getString(R.string.facebook_access_token) + API_URL_END);

				if (null != listView) {
					listView.setAdapter(null);
				}
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
					listView.addFooterView(footerView);
				}
			} else {
				listView.addFooterView(footerView);
			}
		}

		@Override
		protected void onPostExecute(ArrayList<FacebookItem> posts) {
			
			if (null != posts && posts.size() > 0) {
				updateList(posts, initialload);
			} else {
				String token = getResources().getString(R.string.facebook_access_token);
				String message = null;
				if (token.equals("YOURFACEBOOKTOKENHERE")){
					message = "Debug info: '" + token + "' is most likely not a valid ACCESS token.";
				}
				Helper.noConnection(mAct, message);
			}
			
			if (dialogLayout.getVisibility() == View.VISIBLE) {
				dialogLayout.setVisibility(View.GONE);
				// listView.setVisibility(View.VISIBLE);
				Helper.revealView(listView, ll);

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
					listView.removeFooterView(footerView);
				}
			} else {
				listView.removeFooterView(footerView);
			}
			isLoading = false;
		}

		@Override
		protected ArrayList<FacebookItem> doInBackground(String... params) {
			JSONObject json = Helper.getJSONObjectFromUrl(nextpageurl);
			return parseJson(json);
		}
	}


	public ArrayList<FacebookItem> parseJson(JSONObject json) {
		ArrayList<FacebookItem> postList = new ArrayList<FacebookItem>();
		
		try {
			if (json.has("paging") && json.getJSONObject("paging").has("next"))
				nextpageurl = json.getJSONObject("paging").getString("next");
			else
				nextpageurl = null;

			// parsing json object
			 JSONArray dataJsonArray = json.getJSONArray("data");
             for (int i = 0; i < dataJsonArray.length(); i++) {
            	 try {
                 JSONObject photoJson = dataJsonArray.getJSONObject(i);
                 FacebookItem post = new FacebookItem();
                 post.id = photoJson.getString("id");
                 post.type = photoJson.getString("type");
                 post.username = photoJson.getJSONObject("from").getString("name");
                 post.profilePhotoUrl = "https://graph.facebook.com/" + photoJson.getJSONObject("from").getString("id") + "/picture?type=large";
                 post.createdTime = new Date(photoJson.getLong("created_time") * 1000);
                 post.likesCount = photoJson.getJSONObject("likes").getJSONObject("summary").getInt("total_count");
                 if (photoJson.has("link"))
                	 post.link =  photoJson.getString("link");
                 else 
                	 post.link = "https://www.facebook.com/" + post.id;
                 
                 if (post.type.equals("video")) {
                     post.videoUrl = photoJson.getString("source");
                 }
                 
                 if (photoJson.has("message")){
                	 post.caption = photoJson.getString("message");
                 } else if (photoJson.has("story")){
                	 post.caption = photoJson.getString("story");
                 } else if (photoJson.has("name")){
                	 post.caption = photoJson.getString("name");
                 } else {
                	 post.caption = "";
                 }
                 
                 if (photoJson.has("full_picture")){
                	 post.imageUrl = photoJson.getString("full_picture");
                 }
                 
                 //post.captionUsername = photoJson.getJSONObject("caption").getJSONObject("from").getString("username");)
                	 
                 post.commentsCount = photoJson.getJSONObject("comments").getJSONObject("summary").getInt("total_count");
                 post.commentsArray = photoJson.getJSONObject("comments").getJSONArray("data");

                 // Add to array list
                 postList.add(post);
            	 } catch (Exception e) {
         			Log.e("INFO", "Item " + i +" skipped because of exception");
         			Log.printStackTrace(e);
         		}
			}

			return postList;
		} catch (Exception e) {
			Log.printStackTrace(e);

			return null;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.refresh_menu, menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.refresh:
			if (!isLoading) {
				new DownloadFilesTask(true).execute();
			} else {
				Toast.makeText(mAct, getString(R.string.already_loading),
						Toast.LENGTH_LONG).show();
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
