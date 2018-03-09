package com.ronak.viral.adda.providers.wordpress.api.providers;

import android.text.Html;

import com.ronak.viral.adda.Config;
import com.ronak.viral.adda.providers.wordpress.PostItem;
import com.ronak.viral.adda.providers.wordpress.api.JsonApiPostLoader;
import com.ronak.viral.adda.providers.wordpress.api.WordpressGetTask;
import com.ronak.viral.adda.providers.wordpress.api.WordpressGetTaskInfo;
import com.ronak.viral.adda.providers.wordpress.ui.WordpressDetailActivity;
import com.ronak.viral.adda.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * This is a provider for the Wordpress Fragment over JSON API.
 */
public class JsonApiProvider implements WordpressProvider {

    //WP-REST
    private static final String API_LOC = "/?json=";
    private static final String API_LOC_FRIENDLY = "/api/";
    private static final String PARAMS = "date_format=U&exclude=comments,categories,custom_fields";

    @Override
    public String getRecentPosts(WordpressGetTaskInfo info) {
        StringBuilder builder = new StringBuilder();
        builder.append(info.baseurl);
        builder.append(getApiLoc());
        builder.append("get_recent_posts");
        builder.append(getParams(PARAMS));
        builder.append("&count=");
        builder.append(WordpressGetTask.PER_PAGE);
        builder.append("&page=");

        return builder.toString();
    }

    @Override
    public String getTagPosts(WordpressGetTaskInfo info, String tag) {
        StringBuilder builder = new StringBuilder();
        builder.append(info.baseurl);
        builder.append(getApiLoc());
        builder.append("get_tag_posts");
        builder.append(getParams(PARAMS));
        builder.append("&count=");
        if (info.simpleMode)
            builder.append(WordpressGetTask.PER_PAGE_RELATED);
        else
            builder.append(WordpressGetTask.PER_PAGE);
        builder.append("&tag_slug=");
        builder.append(tag);
        builder.append("&page=");

        return builder.toString();
    }

    @Override
    public String getCategoryPosts(WordpressGetTaskInfo info, String category) {
        StringBuilder builder = new StringBuilder();
        builder.append(info.baseurl);
        builder.append(getApiLoc());
        builder.append("get_category_posts");
        builder.append(getParams(PARAMS));
        builder.append("&count=");
        builder.append(WordpressGetTask.PER_PAGE);
        builder.append("&category_slug=");
        builder.append(category);
        builder.append("&page=");

        return builder.toString();
    }

    @Override
    public String getSearchPosts(WordpressGetTaskInfo info, String query) {
        StringBuilder builder = new StringBuilder();
        builder.append(info.baseurl);
        builder.append(getApiLoc());
        builder.append("get_search_results");
        builder.append(getParams(PARAMS));
        builder.append("&count=");
        builder.append(WordpressGetTask.PER_PAGE);
        builder.append("&search=");
        builder.append(query);
        builder.append("&page=");

        return builder.toString();
    }

    @Override
    public ArrayList<PostItem> parsePosts(WordpressGetTaskInfo info, JSONObject json) {

        ArrayList<PostItem> result = null;

        try {
            info.pages = json.getInt("pages");

            // parsing json object
            if (json.has("posts")) {
                JSONArray posts = json.getJSONArray("posts");

                result = new ArrayList<PostItem>();

                for (int i = 0; i < posts.length(); i++) {
                    try {
                        JSONObject post = (JSONObject) posts.getJSONObject(i);
                        PostItem item = itemFromJsonObject(post);

                        //Complete the post in the background (if enabled)
                        if (WordpressDetailActivity.PRELOAD_POSTS)
                            new JsonApiPostLoader(item, info.baseurl, null).start();

                        if (!item.getId().equals(info.ignoreId)) {
                            result.add(item);
                        }
                    } catch (Exception e) {
                        Log.v("INFO", "Item " + i + " of " + posts.length()
                                + " has been skipped due to exception!");
                        Log.printStackTrace(e);
                    }
                }
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }

        return result;
    }


    public static PostItem itemFromJsonObject(JSONObject post) throws JSONException {
        PostItem item = new PostItem(PostItem.PostType.JSON);

        item.setTitle(Html.fromHtml(post.getString("title"))
                .toString());
        item.setDate(new Date(post.getLong("date") * 1000));
        item.setId(post.getLong("id"));
        item.setUrl(post.getString("url"));
        item.setContent(post.getString("content"));
        if (post.has("author")) {
            Object author = post.get("author");
            if (author instanceof JSONArray
                    && ((JSONArray) author).length() > 0) {
                author = ((JSONArray) author).getJSONObject(0);
            }

            if (author instanceof JSONObject
                    && ((JSONObject) author).has("name")) {
                item.setAuthor(((JSONObject) author)
                        .getString("name"));
            }
        }

        if (post.has("tags") && post.getJSONArray("tags").length() > 0) {
            item.setTag(((JSONObject) post.getJSONArray("tags").get(0)).getString("slug"));
        }

        // TODO do we dear to remove catch clause?
        try {
            boolean thumbnailfound = false;

            if (post.has("thumbnail")) {
                String thumbnail = post.getString("thumbnail");
                if (!thumbnail.equals("")) {
                    item.setThumbnailUrl(thumbnail);
                    thumbnailfound = true;
                }
            }

            if (post.has("attachments")) {

                JSONArray attachments = post
                        .getJSONArray("attachments");

                // checking how many attachments post has and
                // grabbing the first one
                if (attachments.length() > 0) {
                    JSONObject attachment = attachments
                            .getJSONObject(0);

                    item.setAttachmentUrl(attachment
                            .getString("url"));

                    // if we do not have a thumbnail yet, get
                    // one now. But only if 'images' exists and is of type JSONObject
                    if (attachment.has("images")
                            && !thumbnailfound && attachment.optJSONObject("images") != null) {

                        JSONObject thumbnail;
                        if (attachment.getJSONObject("images")
                                .has("post-thumbnail")) {
                            thumbnail = attachment
                                    .getJSONObject("images")
                                    .getJSONObject(
                                            "post-thumbnail");

                            item.setThumbnailUrl(thumbnail
                                    .getString("url"));
                        } else if (attachment.getJSONObject(
                                "images").has("thumbnail")) {
                            thumbnail = attachment
                                    .getJSONObject("images")
                                    .getJSONObject("thumbnail");

                            item.setThumbnailUrl(thumbnail
                                    .getString("url"));
                        }

                    }
                }
            }

        } catch (Exception e) {
            Log.printStackTrace(e);
        }

        return item;
    }

    public static String getPostUrl(long id, String baseurl) {
        StringBuilder builder = new StringBuilder();
        builder.append(baseurl);
        builder.append(getApiLoc());
        builder.append("get_post");
        builder.append(getParams("post_id="));
        builder.append(id);

        return builder.toString();
    }

    public static String getParams(String params) {
        String query = (Config.USE_WP_FRIENDLY) ? "?" : "&";
        return query + params;
    }

    public static String getApiLoc() {
        return (Config.USE_WP_FRIENDLY) ? API_LOC_FRIENDLY : API_LOC;
    }
}
