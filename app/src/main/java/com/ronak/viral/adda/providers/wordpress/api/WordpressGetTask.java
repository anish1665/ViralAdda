package com.ronak.viral.adda.providers.wordpress.api;

import java.util.ArrayList;

import org.json.JSONObject;

import com.ronak.viral.adda.R;
import com.ronak.viral.adda.providers.wordpress.api.providers.JsonApiProvider;
import com.ronak.viral.adda.util.Helper;
import com.ronak.viral.adda.providers.wordpress.PostItem;
import com.ronak.viral.adda.providers.wordpress.WordpressListAdapter;

import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

/**
 * Simply loads data from an url (gotten from a provider) and loads it into a list.
 * Various attributes of this list and the way to load are defined in a WordpressGetTaskInfo.
 */
public class WordpressGetTask extends AsyncTask<String, Integer, ArrayList<PostItem>> {

    private String url;
    private boolean initialload;
    private WordpressGetTaskInfo info;

    public static final int PER_PAGE = 15;
    public static final int PER_PAGE_RELATED = 4;

    public static String getRecentPosts(WordpressGetTaskInfo info) {
        //Let the provider compose an API url
        String url = info.provider.getRecentPosts(info);

        new WordpressGetTask(url, true, info).execute();

        return url;
    }

    public static String getTagPosts(WordpressGetTaskInfo info, String tag) {
        //Let the provider compose an API url
        String url = info.provider.getTagPosts(info, tag);

        new WordpressGetTask(url, true, info).execute();

        return url;
    }

    public static String getCategoryPosts(WordpressGetTaskInfo info, String category) {
        //Let the provider compose an API url
        String url = info.provider.getCategoryPosts(info, category);

        new WordpressGetTask(url, true, info).execute();

        return url;
    }

    public static String getSearchPosts(WordpressGetTaskInfo info, String query) {
        //A search request might interfere with a current loading therefore
        //we disable loading to ensure we can start a new request
        if (info.isLoading) {
            info.isLoading = false;
        }

        //Let the provider compose an API url
        String url = info.provider.getSearchPosts(info, query);

        new WordpressGetTask(url, true, info).execute();

        return url;
    }


    public static void loadMorePosts(WordpressGetTaskInfo info, String withUrl) {
        new WordpressGetTask(withUrl, false, info).execute();
    }

    public WordpressGetTask(String url, boolean firstload, WordpressGetTaskInfo info) {
        this.url = url;
        this.initialload = firstload;
        this.info = info;
    }

    @Override
    protected void onPreExecute() {
        if (info.isLoading) {
            this.cancel(true);
        } else {
            info.isLoading = true;
        }

        if (initialload) {
            //Show the full screen loading layout
            if (null != info.dialogLayout && info.dialogLayout.getVisibility() == View.GONE) {
                info.dialogLayout.setVisibility(View.VISIBLE);
                info.feedListView.setVisibility(View.GONE);
            }

            //Reset the page parameter and listview
            info.curpage = 0;

            if (null != info.feedListView) {
                info.feedListView.setAdapter(null);
            }

            //Add the footerview
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && !info.simpleMode) {
                info.feedListView.addFooterView(info.footerView);
            }
        } else {
            info.feedListView.addFooterView(info.footerView);
        }
    }

    @Override
    protected ArrayList<PostItem> doInBackground(String... params) {
        // String url = params[0];
        info.curpage = info.curpage + 1;
        url = url + Integer.toString(info.curpage);

        // getting JSON string from URL
        JSONObject json = Helper.getJSONObjectFromUrl(url);

        // parsing json data
        if (json != null)
            return info.provider.parsePosts(info, json);
        else
            return null;
    }

    @Override
    protected void onPostExecute(ArrayList<PostItem> result) {

        //Check if the response was null
        if (null != result) {
            updateList(initialload, result);
        } else {
            showErrorMessage();
        }

        //Alert if we have simply 0 posts, but a valid response
        if (null != result && result.size() < 1 && !info.simpleMode) {
            Toast.makeText(
                    info.context,
                    info.context.getResources().getString(R.string.no_results),
                    Toast.LENGTH_LONG).show();
        }

        //Hide the dialoglayout and else the footerview
        if (null != info.dialogLayout && info.dialogLayout.getVisibility() == View.VISIBLE) {
            info.dialogLayout.setVisibility(View.GONE);
            Helper.revealView(info.feedListView, info.frame);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                info.feedListView.removeFooterView(info.footerView);
            }
        } else {
            info.feedListView.removeFooterView(info.footerView);
        }

        info.isLoading = false;
    }


    public void updateList(boolean initialload, ArrayList<PostItem> posts) {
        if (initialload) {
            info.feedListAdapter = new WordpressListAdapter(info.context, 0, posts, info.simpleMode);
            info.feedListView.setAdapter(info.feedListAdapter);
        } else {
            info.feedListAdapter.addAll(posts);
            info.feedListAdapter.notifyDataSetChanged();
        }
    }

    public void showErrorMessage(){
        String message;
        if ((!info.baseurl.startsWith("http") || info.baseurl.endsWith("/")) && info.provider instanceof JsonApiProvider) {
            message =  info.baseurl + "' is most likely not a valid API base url.";
        } else {
            message = "Please press ok and refresh page";
        }

        Helper.noConnection(info.context, message);
    }

}
