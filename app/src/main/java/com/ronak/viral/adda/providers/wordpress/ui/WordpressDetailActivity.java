package com.ronak.viral.adda.providers.wordpress.ui;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.MotionEvent;
import android.view.ViewStub;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.ronak.viral.adda.Config;
import com.ronak.viral.adda.R;
import com.ronak.viral.adda.providers.wordpress.api.providers.JetPackProvider;
import com.ronak.viral.adda.comments.CommentsActivity;
import com.ronak.viral.adda.providers.fav.FavDbAdapter;
import com.ronak.viral.adda.util.DetailActivity;
import com.ronak.viral.adda.util.layout.ExpandedListView;
import com.ronak.viral.adda.util.Helper;
import com.ronak.viral.adda.util.MediaActivity;
import com.ronak.viral.adda.util.layout.TrackingScrollView;
import com.ronak.viral.adda.util.WebHelper;
import com.ronak.viral.adda.providers.web.WebviewActivity;
import com.ronak.viral.adda.providers.wordpress.api.JsonApiPostLoader;
import com.ronak.viral.adda.providers.wordpress.PostItem;
import com.ronak.viral.adda.providers.wordpress.api.WordpressGetTask;
import com.ronak.viral.adda.providers.wordpress.api.WordpressGetTaskInfo;
import com.squareup.picasso.Picasso;

/**
 * This activity is used to display a wordpress post
 */

public class WordpressDetailActivity extends DetailActivity implements JsonApiPostLoader.BackgroundPostCompleterListener {

    //By default, we remove the first image, however, you can disable this
    private static final boolean REMOVE_FIRST_IMG = true;
    //Preload all posts for faster loading, increases API requests
    public static final boolean PRELOAD_POSTS = true;

    //Utilties
    private FavDbAdapter mDbHelper;
    private WebView htmlTextView;
    private TextView mTitle;

    //Extra's
    public static final String EXTRA_POSTITEM = "postitem";
    public static final String EXTRA_API_BASE = "apiurl";
    public static final String EXTRA_DISQUS = "disqus";

    //Post information
    private PostItem post;
    private String disqusParseable;
    private String apiBase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Use the general detaillayout and set the viewstub for wordpress
        setContentView(R.layout.activity_details);
        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.activity_wordpress_details);
        View inflated = stub.inflate();

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        thumb = (ImageView) findViewById(R.id.image);
        coolblue = (RelativeLayout) findViewById(R.id.coolblue);

        //String url = getIntent().getStringExtra(EXTRA_API_BASE)
        //		+ "get_post/?post_id=";
        Bundle bundle = this.getIntent().getExtras();
        post = (PostItem) getIntent().getSerializableExtra(EXTRA_POSTITEM);
        disqusParseable = getIntent().getStringExtra(EXTRA_DISQUS);
        apiBase = getIntent().getStringExtra(EXTRA_API_BASE);

        //If we have a post and a bundle
        if (null != post && null != bundle) {

            String dateauthortext = getResources().getString(R.string.wordpress_subtitle_start) +
                    DateUtils.getRelativeDateTimeString(this, post.getDate().getTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL)
                    + getResources().getString(R.string.wordpress_subtitle_end)
                    + post.getAuthor();

            // getting a valid url, displaying it and setting a parralax
            // listener. Also a fallback for no image.
            String imageurl = post.getAttachmentUrl();
            if (null == imageurl || imageurl.equals("") || imageurl.equals("null"))
                imageurl = post.getThumbnailUrl();

            if ((null != imageurl && !imageurl.equals("") && !imageurl.equals("null"))) {
                Picasso.with(this).load(imageurl).fit().centerCrop().into(thumb);
                final String fImageUrl = imageurl;
                thumb.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {

                        Intent commentIntent = new Intent(WordpressDetailActivity.this, MediaActivity.class);
                        commentIntent.putExtra(MediaActivity.TYPE, MediaActivity.TYPE_IMG);
                        commentIntent.putExtra(MediaActivity.URL, fImageUrl);
                        startActivity(commentIntent);
                    }
                });

                ((TrackingScrollView) findViewById(R.id.scroller)).setOnTouchListener(new View.OnTouchListener() {

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if ((findViewById(R.id.progressBar).getVisibility() == View.VISIBLE) && android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.JELLY_BEAN)
                            return true;
                        else
                            return false;
                    }
                });
            }

            setUpHeader(imageurl);


            Helper.admobLoader(this, getResources(), findViewById(R.id.adView));

            mTitle = (TextView) findViewById(R.id.title);
            mTitle.setText(post.getTitle());

            TextView mDateAuthorView = (TextView) findViewById(R.id.dateauthorview);
            mDateAuthorView.setText(dateauthortext);

            htmlTextView = (WebView) findViewById(R.id.htmlTextView);
            htmlTextView.getSettings().setJavaScriptEnabled(true);
            htmlTextView.setBackgroundColor(Color.TRANSPARENT);
            htmlTextView.getSettings().setDefaultFontSize(
                    WebHelper.getWebViewFontSize(this));
            //htmlTextView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            htmlTextView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            htmlTextView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url != null
                            && (url.endsWith(".png") || url
                            .endsWith(".jpg") || url
                            .endsWith(".jpeg"))) {
                        Intent commentIntent = new Intent(WordpressDetailActivity.this, MediaActivity.class);
                        commentIntent.putExtra(MediaActivity.TYPE, MediaActivity.TYPE_IMG);
                        commentIntent.putExtra(MediaActivity.URL, url);
                        startActivity(commentIntent);
                        return true;
                    } else if (url != null
                            && (url.startsWith("http://") || url
                            .startsWith("https://"))) {
                        Intent mIntent = new Intent(WordpressDetailActivity.this, WebviewActivity.class);
                        mIntent.putExtra(WebviewActivity.OPEN_EXTERNAL, Config.OPEN_INLINE_EXTERNAL);
                        mIntent.putExtra(WebviewActivity.URL, url);
                        startActivity(mIntent);
                        return true;
                    } else {
                        Uri uri = Uri.parse(url);
                        Intent ViewIntent = new Intent(Intent.ACTION_VIEW, uri);

                        // Verify it resolves
                        PackageManager packageManager = getPackageManager();
                        List<ResolveInfo> activities = packageManager
                                .queryIntentActivities(ViewIntent, 0);
                        boolean isIntentSafe = activities.size() > 0;

                        // Start an activity if it's safe
                        if (isIntentSafe) {
                            startActivity(ViewIntent);
                        }
                        return true;
                    }
                }
            });


            //If the post is completed, load the body. Else, retrieve the full body first
            if (post.isCompleted()) {
                loadCompletedPost(post);
            } else {
                new JsonApiPostLoader(post, getIntent().getStringExtra(EXTRA_API_BASE), this).start();
            }

            Button btnFav = (Button) findViewById(R.id.favorite);

            // Listening to button event
            btnFav.setOnClickListener(new View.OnClickListener() {

                public void onClick(View arg0) {
                    mDbHelper = new FavDbAdapter(WordpressDetailActivity.this);
                    mDbHelper.open();

                    if (mDbHelper.checkEvent(post.getTitle(), post, FavDbAdapter.KEY_WORDPRESS)) {
                        // Item is new
                        mDbHelper.addFavorite(post.getTitle(), post, FavDbAdapter.KEY_WORDPRESS);
                        Toast toast = Toast
                                .makeText(
                                        WordpressDetailActivity.this,
                                        getResources().getString(
                                                R.string.favorite_success),
                                        Toast.LENGTH_LONG);
                        toast.show();
                    } else {
                        Toast toast = Toast.makeText(
                                WordpressDetailActivity.this,
                                getResources().getString(
                                        R.string.favorite_duplicate),
                                Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            });

            //If we have tags and a baseurl, load the related posts
            if (post.getTag() != null && getIntent().getStringExtra(EXTRA_API_BASE) != null) {
                final ExpandedListView relatedList = (ExpandedListView) findViewById(R.id.related_list);
                WordpressGetTaskInfo mInfo = new WordpressGetTaskInfo(null, relatedList, this, null, this.findViewById(R.id.contentholder), getIntent().getStringExtra(EXTRA_API_BASE), true);
                mInfo.ignoreId = post.getId();
                WordpressGetTask.getTagPosts(mInfo, post.getTag());

                relatedList.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int position,
                                            long id) {
                        Object o = relatedList.getItemAtPosition(position);
                        PostItem newsData = (PostItem) o;

                        Intent intent = new Intent(WordpressDetailActivity.this, WordpressDetailActivity.class);
                        intent.putExtra(EXTRA_POSTITEM, newsData);
                        intent.putExtra(EXTRA_API_BASE, getIntent().getStringExtra(EXTRA_API_BASE));
                        if (disqusParseable != null)
                            intent.putExtra(WordpressDetailActivity.EXTRA_DISQUS, disqusParseable);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        htmlTextView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        htmlTextView.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wordpress_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_share:
                shareContent();
                return true;
            case R.id.menu_view:
                Intent mIntent = new Intent(WordpressDetailActivity.this, WebviewActivity.class);
                mIntent.putExtra(WebviewActivity.OPEN_EXTERNAL, Config.OPEN_EXPLICIT_EXTERNAL);
                mIntent.putExtra(WebviewActivity.URL, post.getUrl());
                startActivity(mIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareContent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, post.getTitle() + "\n" + post.getUrl());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share using"));
    }

    private void loadCompletedPost(final PostItem result) {
        if (null != result) {
            setHTML(result.getContent());

            //If we have a commentsArray or a disqus url, enable comments
            if ((result.getCommentCount() != 0 && result.getCommentsArray() != null) ||
                    disqusParseable != null ||
                    (post.getPostType() == PostItem.PostType.JETPACK && result.getCommentCount() != 0)) {

                Button btnComment = (Button) findViewById(R.id.comments);

                //Set the comments count if we have it available
                if (result.getCommentCount() != 0)
                    btnComment.setText(Helper.formatValue(result.getCommentCount()) + " " + getResources().getString(R.string.comments));
                else
                    btnComment.setText(getResources().getString(R.string.comments));

                btnComment.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {

                        Intent commentIntent = new Intent(WordpressDetailActivity.this, CommentsActivity.class);

                        if (disqusParseable != null) {
                            commentIntent.putExtra(CommentsActivity.DATA_PARSEABLE, disqusParseable);
                            commentIntent.putExtra(CommentsActivity.DATA_TYPE, CommentsActivity.DISQUS);
                            commentIntent.putExtra(CommentsActivity.DATA_ID, post.getId().toString());
                        } else {
                            if (post.getPostType() == PostItem.PostType.JETPACK){
                                commentIntent.putExtra(CommentsActivity.DATA_PARSEABLE, JetPackProvider.getPostCommentsUrl(apiBase, post.getId().toString()));
                                commentIntent.putExtra(CommentsActivity.DATA_TYPE, CommentsActivity.WORDPRESS_JETPACK);
                            } else {
                                commentIntent.putExtra(CommentsActivity.DATA_PARSEABLE, result.getCommentsArray().toString());
                                commentIntent.putExtra(CommentsActivity.DATA_TYPE, CommentsActivity.WORDPRESS_JSON);
                            }
                        }

                        startActivity(commentIntent);
                    }
                });
            }
        } else {
            findViewById(R.id.progressBar).setVisibility(View.GONE);

            Helper.noConnection(WordpressDetailActivity.this);
        }
    }

    public void setHTML(String source) {
        Document doc = Jsoup.parse(source);

        //Remove the first image to prevent a repetition of the header image (if enabled and present)
        if (REMOVE_FIRST_IMG) {
            if (doc.select("img") != null && doc.select("img").first() != null)
                doc.select("img").first().remove();
        }

        String html = WebHelper.docToBetterHTML(doc, this);

        htmlTextView.loadDataWithBaseURL(post.getUrl(), html, "text/html", "UTF-8", "");
        htmlTextView.setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }


    @Override
    public void completed(final PostItem item) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                loadCompletedPost(item);
            }
        });
    }


}
