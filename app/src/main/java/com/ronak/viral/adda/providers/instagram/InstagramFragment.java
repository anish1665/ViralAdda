package com.ronak.viral.adda.providers.instagram;

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
 * This activity is used to display a list of instagram photos
 */

public class InstagramFragment extends Fragment {

	private ArrayList<InstagramPhoto> photosList = null;
	private ListView photosListView = null;
	private View footerView;
	private Activity mAct;
	private DownloadFilesTask mTask;
	private InstagramPhotosAdapter photosListAdapter = null;

	private LinearLayout ll;
	RelativeLayout dialogLayout;

	String nextpageurl;

	String username;

	Boolean isLoading = false;

	private static String API_URL = "https://api.instagram.com/v1/users/";
	private static String API_URL_END = "/media/recent?access_token=";

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
		photosListView = (ListView) ll.findViewById(R.id.list);
		photosListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				if (photosListAdapter == null)
					return;

				if (photosListAdapter.getCount() == 0)
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


	public void updateList(boolean initialload) {
		if (initialload) {
			photosListAdapter = new InstagramPhotosAdapter(mAct, photosList);
			photosListView.setAdapter(photosListAdapter);
		} else {
			photosListAdapter.addAll(photosList);
			photosListAdapter.notifyDataSetChanged();
		}
	}

	private class DownloadFilesTask extends AsyncTask<String, Integer, Boolean> {

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
					photosListView.setVisibility(View.GONE);
				}

				nextpageurl = (API_URL + username + API_URL_END  + getResources().getString(R.string.instagram_access_token));

				if (null != photosList) {
					photosList.clear();
				}
				if (null != photosListView) {
					photosListView.setAdapter(null);
				}
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
					photosListView.addFooterView(footerView);
				}
			} else {
				photosListView.addFooterView(footerView);
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (null != photosList && photosList.size() > 0) {
				updateList(initialload);
			} else {
				Helper.noConnection(mAct);
			}
			
			if (dialogLayout.getVisibility() == View.VISIBLE) {
				dialogLayout.setVisibility(View.GONE);
				// photosListView.setVisibility(View.VISIBLE);
				Helper.revealView(photosListView, ll);

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
					photosListView.removeFooterView(footerView);
				}
			} else {
				photosListView.removeFooterView(footerView);
			}
			isLoading = false;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			//Getting data from url and parsing JSON
			JSONObject json = Helper.getJSONObjectFromUrl(nextpageurl);
			parseJson(json);
			return true;
		}
	}

	public void parseJson(JSONObject json) {
		if (photosList == null){
			photosList = new ArrayList<InstagramPhoto>();
		}
		try {
			if (json.getJSONObject("pagination").has("next_url"))
				nextpageurl = json.getJSONObject("pagination").getString("next_url");
			else
				nextpageurl = null;
			// parsing json object
			 JSONArray dataJsonArray = json.getJSONArray("data");
             for (int i = 0; i < dataJsonArray.length(); i++) {
                 JSONObject photoJson = dataJsonArray.getJSONObject(i);
                 InstagramPhoto photo = new InstagramPhoto();
                 photo.id = photoJson.getString("id");
                 photo.type = photoJson.getString("type");
                 photo.username = photoJson.getJSONObject("user").getString("username");
                 photo.profilePhotoUrl = photoJson.getJSONObject("user").getString("profile_picture");
                 if (photoJson.has("caption") && !photoJson.isNull("caption")){
                	 photo.caption = photoJson.getJSONObject("caption").getString("text");
                 	 photo.captionUsername = photoJson.getJSONObject("caption").getJSONObject("from").getString("username");
                 }
                 photo.imageUrl = photoJson.getJSONObject("images").getJSONObject("standard_resolution").getString("url");
                 photo.createdTime = new Date(photoJson.getLong("created_time") * 1000);
                 photo.likesCount = photoJson.getJSONObject("likes").getInt("count");
                 photo.link = photoJson.getString("link");
                 
                 if (photo.type.equals("video")) {
                     photo.videoUrl = photoJson.getJSONObject("videos").getJSONObject("standard_resolution").getString("url");
                 }

                 photo.commentsCount = photoJson.getJSONObject("comments").getInt("count");

                 // Add to array list
                 photosList.add(photo);
			}
		} catch (Exception e) {
			Log.printStackTrace(e);
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
