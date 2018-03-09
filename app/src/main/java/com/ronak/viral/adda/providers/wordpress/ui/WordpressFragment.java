package com.ronak.viral.adda.providers.wordpress.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.widget.ListView;
import android.widget.Toast;

import com.ronak.viral.adda.MainActivity;
import com.ronak.viral.adda.R;
import com.ronak.viral.adda.util.Log;
import com.ronak.viral.adda.providers.wordpress.PostItem;
import com.ronak.viral.adda.providers.wordpress.api.WordpressGetTask;
import com.ronak.viral.adda.providers.wordpress.api.WordpressGetTaskInfo;

/**
 * This fragment is used to display a list of wordpress articles
 */

public class WordpressFragment extends Fragment {

    //Layout attributes
	private ListView postList = null;
	private Activity mAct;
	private LinearLayout ll;

    //Keeping track of the WP
    private WordpressGetTaskInfo mInfo;
    private String urlSession;

    //The position of the listview at the moment a 'loadMore' request was made
	private int previousl;

    //The arguments we started this fragment with
	private String[] arguments;
	
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ll = (LinearLayout) inflater.inflate(R.layout.fragment_list_nopadding,
				container, false);
		setHasOptionsMenu(true);

		arguments = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA);

		postList = (ListView) ll.findViewById(R.id.list);
		postList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				Object o = postList.getItemAtPosition(position);
				PostItem newsData = (PostItem) o;

				Intent intent = new Intent(mAct, WordpressDetailActivity.class);
				intent.putExtra(WordpressDetailActivity.EXTRA_POSTITEM, newsData);
				intent.putExtra(WordpressDetailActivity.EXTRA_API_BASE, arguments[0]);
				//If a disqus parse-able is provided, pass it to the detailActivity
				if (arguments.length > 2)
					intent.putExtra(WordpressDetailActivity.EXTRA_DISQUS, arguments[2]);

				startActivity(intent);
			}
		});

		return ll;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();

		View footerView = getLayoutInflater(savedInstanceState).inflate(R.layout.listview_footer, null);
		RelativeLayout dialogLayout = (RelativeLayout) ll
				.findViewById(R.id.progressBarHolder);
		mInfo = new WordpressGetTaskInfo(footerView, postList, mAct, dialogLayout,ll, arguments[0], false);

		postList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
								 int visibleItemCount, int totalItemCount) {

				if (mInfo.feedListAdapter == null)
					return;

				if (mInfo.feedListAdapter.getCount() == 0)
					return;

				int l = visibleItemCount + firstVisibleItem;

				//Check if we are at the end of the list, not at the same position as we previously asked to load more & there are more pages available
				if (l >= totalItemCount && l != previousl && !mInfo.isLoading && mInfo.curpage < mInfo.pages) {
					//Load more and remember the position
					WordpressGetTask.loadMorePosts(mInfo, urlSession);
					previousl = l;
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}
		});

		getPosts();
	}

	public void getPosts() {
		if (arguments.length > 1 && !arguments[1].equals("")) {
			urlSession = WordpressGetTask.getCategoryPosts(mInfo, arguments[1]);
		} else {
			urlSession = WordpressGetTask.getRecentPosts(mInfo);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.refresh_menu, menu);

		// set & get the search button in the actionbar
		final SearchView searchView = new SearchView(mAct);

		searchView.setQueryHint(getResources().getString(
				R.string.video_search_hint));
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			//
			@Override
			public boolean onQueryTextSubmit(String query) {
				try {
					query = URLEncoder.encode(query, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					Log.printStackTrace(e);
				}
				searchView.clearFocus();

				urlSession = WordpressGetTask.getSearchPosts(mInfo, query);

				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}

		});

		searchView
				.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {

					@Override
					public void onViewDetachedFromWindow(View arg0) {
						if (!mInfo.isLoading) {
							getPosts();
						}
					}

					@Override
					public void onViewAttachedToWindow(View arg0) {
						// search was opened
					}
				});

		// TODO make menu an xml item
		menu.add("search")
				.setIcon(R.drawable.ic_action_search)
				.setActionView(searchView)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.refresh:
			if (!mInfo.isLoading) {
				getPosts();
			} else {
				Toast.makeText(mAct, getString(R.string.already_loading),
						Toast.LENGTH_LONG).show();
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
