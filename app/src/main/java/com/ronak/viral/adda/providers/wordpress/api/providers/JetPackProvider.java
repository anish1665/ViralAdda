package com.ronak.viral.adda.providers.wordpress.api.providers;

import android.text.Html;

import com.ronak.viral.adda.providers.wordpress.PostItem;
import com.ronak.viral.adda.providers.wordpress.api.WordpressGetTask;
import com.ronak.viral.adda.providers.wordpress.api.WordpressGetTaskInfo;
import com.ronak.viral.adda.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This is a provider for the Wordpress Fragment over JetPack API.
 */
public class JetPackProvider implements WordpressProvider {

    //Jetpack
    private static final String JETPACK_BASE = "https://public-api.wordpress.com/rest/v1.1/sites/";
    private static final String JETPACK_FIELDS = "&fields=ID,author,title,URL,content,discussion,featured_image,post_thumbnail,tags,discussion,date,attachments";
    private static final SimpleDateFormat JETPACK_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+00:00'", Locale.getDefault());

    @Override
    public String getRecentPosts(WordpressGetTaskInfo info) {
        StringBuilder builder = new StringBuilder();
        builder.append(JETPACK_BASE);
        builder.append(info.baseurl);
        builder.append("/posts/?number=");
        builder.append(WordpressGetTask.PER_PAGE);
        builder.append(JETPACK_FIELDS);
        builder.append("&page=");

        return builder.toString();
    }

    @Override
    public String getTagPosts(WordpressGetTaskInfo info, String tag) {
        StringBuilder builder = new StringBuilder();
        builder.append(JETPACK_BASE);
        builder.append(info.baseurl);
        builder.append("/posts/?number=");
        if (info.simpleMode)
            builder.append(WordpressGetTask.PER_PAGE_RELATED);
        else
            builder.append(WordpressGetTask.PER_PAGE);
        builder.append("&tag=");
        builder.append(tag);
        builder.append("&page=");

        return builder.toString();
    }

    @Override
    public String getCategoryPosts(WordpressGetTaskInfo info, String category) {
        StringBuilder builder = new StringBuilder();
        builder.append(JETPACK_BASE);
        builder.append(info.baseurl);
        builder.append("/posts/?number=");
        builder.append(WordpressGetTask.PER_PAGE);
        builder.append("&category=");
        builder.append(category);
        builder.append("&page=");

        return builder.toString();
    }

    @Override
    public String getSearchPosts(WordpressGetTaskInfo info, String query) {
        StringBuilder builder = new StringBuilder();
        builder.append(JETPACK_BASE);
        builder.append(info.baseurl);
        builder.append("/posts/?number=");
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

            info.pages = json.getInt("found") / WordpressGetTask.PER_PAGE + (json.getInt("found") % WordpressGetTask.PER_PAGE == 0 ? 0 : 1);

            // parsing json object
            if (json.has("posts")) {
                JSONArray posts = json.getJSONArray("posts");

                result = new ArrayList<PostItem>();

                for (int i = 0; i < posts.length(); i++) {
                    try {
                        JSONObject post = (JSONObject) posts.getJSONObject(i);
                        PostItem item = itemFromJsonObject(post);

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

    public static String getPostCommentsUrl(String baseurl, String postId) {
        StringBuilder builder = new StringBuilder();
        builder.append(JETPACK_BASE);
        builder.append(baseurl);
        builder.append("/posts/");
        builder.append(postId);
        builder.append("/replies/");

        return builder.toString();
    }

    public static PostItem itemFromJsonObject(JSONObject post) throws JSONException {
        PostItem item = new PostItem(PostItem.PostType.JETPACK);

        item.setId(post.getLong("ID"));
        item.setAuthor(post.getJSONObject("author").getString("name"));
        try {
            item.setDate(JETPACK_DATE_FORMAT.parse(post.getString("date")));
        } catch (ParseException e) {
            Log.printStackTrace(e);
        }
        item.setTitle(Html.fromHtml(post.getString("title"))
                .toString());
        item.setUrl(post.getString("URL"));
        item.setContent(post.getString("content"));
        item.setCommentCount(post.getJSONObject("discussion").getLong("comment_count"));
        item.setAttachmentUrl(post.getString("featured_image"));

        //If there is a post thumbnail, save it
        if (!post.isNull("post_thumbnail")) {
            long thumbId = post.getJSONObject("post_thumbnail").getLong("ID");

            //We can try to get the thumbnail directly, but that one is usually to large, so we check if it
            //is inside the post attachments first.
            boolean thumbInAttachments = false;
            if (post.has("attachments") && post.getJSONObject("attachments").names() != null) {
                JSONObject attachments = post.getJSONObject("attachments");
                for(int i = 0; i< attachments.names().length(); i++){
                    JSONObject attachment = attachments.getJSONObject(attachments.names().getString(i));
                    if (attachment.getLong("ID") == thumbId &&
                            attachment.has("thumbnails") &&
                            attachment.getJSONObject("thumbnails").has("thumbnail")) {
                        item.setThumbnailUrl(attachment.getJSONObject("thumbnails").getString("thumbnail"));
                        thumbInAttachments = true;
                    }
                }
            }

            if (!thumbInAttachments)
            item.setThumbnailUrl(post.getJSONObject("post_thumbnail").getString("URL"));
        }

        //If there are tags, save the first one
        JSONObject tags = post.getJSONObject("tags");
        if (tags != null && tags.names() != null && tags.names().length() > 0)
            item.setTag(tags.getJSONObject(tags.names().getString(0)).getString("slug"));

        item.setPostCompleted();

        return item;
    }

}
