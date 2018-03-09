package com.ronak.viral.adda.providers.pinterest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ronak.viral.adda.MainActivity;
import com.ronak.viral.adda.R;
import com.ronak.viral.adda.util.Helper;
import com.ronak.viral.adda.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This activity is used to display a list of instagram photos
 */

public class PinterestFragment extends Fragment {

	private ArrayList<Pin> pinList = null;
	private ListView pinListView = null;
	private View footerView;
	private Activity mAct;
	private DownloadFilesTask mTask;
	private PinterestAdapter pinListAdapter = null;

	private LinearLayout ll;
	RelativeLayout dialogLayout;

	String nextpageurl;

	String id;

	Boolean isLoading = false;

	private static String API_URL = "https://api.pinterest.com/v1/boards/";
	private static String API_URL_END = "/pins/?fields=id,original_link,note,image,media,attribution,created_at,creator(image,first_name),counts&limit=100&access_token=";

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ll = (LinearLayout) inflater.inflate(R.layout.fragment_list,
				container, false);
		setHasOptionsMenu(true);

		id = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];

		footerView = inflater.inflate(R.layout.listview_footer, null);
		pinListView = (ListView) ll.findViewById(R.id.list);
		pinListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				if (pinListAdapter == null)
					return;

				if (pinListAdapter.getCount() == 0)
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
			pinListAdapter = new PinterestAdapter(mAct, pinList);
			pinListView.setAdapter(pinListAdapter);
		} else {
			pinListAdapter.addAll(pinList);
			pinListAdapter.notifyDataSetChanged();
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
					pinListView.setVisibility(View.GONE);
				}

				nextpageurl = (API_URL + id + API_URL_END  + getResources().getString(R.string.pinterest_access_token));

				if (null != pinList) {
					pinList.clear();
				}
				if (null != pinListView) {
					pinListView.setAdapter(null);
				}
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
					pinListView.addFooterView(footerView);
				}
			} else {
				pinListView.addFooterView(footerView);
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (null != pinList && pinList.size() > 0) {
				updateList(initialload);
			} else {
				Helper.noConnection(mAct);
			}
			
			if (dialogLayout.getVisibility() == View.VISIBLE) {
				dialogLayout.setVisibility(View.GONE);
				// pinListView.setVisibility(View.VISIBLE);
				Helper.revealView(pinListView, ll);

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
					pinListView.removeFooterView(footerView);
				}
			} else {
				pinListView.removeFooterView(footerView);
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
		if (pinList == null){
			pinList = new ArrayList<Pin>();
		}
		try {
			if (json.getJSONObject("page").has("next") &&
                    json.getJSONObject("page").getString("next").contains("http"))
				nextpageurl = json.getJSONObject("page").getString("next");
			else
				nextpageurl = null;
			// parsing json object
			 JSONArray dataJsonArray = json.getJSONArray("data");
             for (int i = 0; i < dataJsonArray.length(); i++) {
                 JSONObject photoJson = dataJsonArray.getJSONObject(i);
                 Pin pin = new Pin();
                 pin.id = photoJson.getString("id");
                 pin.type = photoJson.getJSONObject("media").getString("type");
                 pin.creatorName = photoJson.getJSONObject("creator").getString("first_name");
                 pin.creatorImageUrl = photoJson.getJSONObject("creator").getJSONObject("image").getJSONObject("60x60").getString("url");
                 pin.caption = photoJson.getString("note");

                 pin.imageUrl = photoJson.getJSONObject("image").getJSONObject("original").getString("url");

                 SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                 pin.createdTime = format.parse(photoJson.getString("created_at"));
                 pin.repinCount = photoJson.getJSONObject("counts").getInt("repins");
                 pin.commentsCount = photoJson.getJSONObject("counts").getInt("comments");
                 
                 pin.link = photoJson.getString("original_link");
                 
                 if (pin.type.equals("video") && photoJson.getJSONObject("attribution").getString("url") != null) {
                     pin.videoUrl = photoJson.getJSONObject("attribution").getString("url");
                 }
                 
                 // Add to array list
                 pinList.add(pin);
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
