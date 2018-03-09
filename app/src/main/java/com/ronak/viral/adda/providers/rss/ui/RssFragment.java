package com.ronak.viral.adda.providers.rss.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.graphics.Bitmap;

import com.ronak.viral.adda.MainActivity;
import com.ronak.viral.adda.R;
import com.ronak.viral.adda.providers.rss.RSSFeed;
import com.ronak.viral.adda.providers.rss.RSSHandler;
import com.ronak.viral.adda.providers.rss.RSSItem;
import com.ronak.viral.adda.util.Helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import com.ronak.viral.adda.util.Log;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 *  This activity is used to display a list of rss items
 */

public class RssFragment extends Fragment {
	
	private RSSFeed myRssFeed = null;
	private Activity mAct;
	private LinearLayout ll;
	private String url;
	private RelativeLayout pDialog;	
	
	public class RssAdapter extends ArrayAdapter<RSSItem> {

		 public RssAdapter(Context context, int textViewResourceId,
				 List<RSSItem> list) {
			 	super(context, textViewResourceId, list);
		 }

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) { 
			View row = convertView;
		  
			final ViewHolder holder;
		 
			if(row==null){
		      
				LayoutInflater inflater=mAct.getLayoutInflater();
				row=inflater.inflate(R.layout.fragment_rss_row, null);
		   
				holder = new ViewHolder();
		      
				holder.listTitle=(TextView)row.findViewById(R.id.listtitle);
				holder.listPubdate=(TextView)row.findViewById(R.id.listpubdate);
				holder.listDescription=(TextView)row.findViewById(R.id.shortdescription);
				holder.listThumb =(ImageView)row.findViewById(R.id.listthumb);
			  	
				row.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
		 
			holder.listTitle.setText(myRssFeed.getList().get(position).getTitle());	  
			holder.listPubdate.setText(myRssFeed.getList().get(position).getPubdate());
		  
			String html = myRssFeed.getList().get(position).getRowDescription();
		  
			holder.listDescription.setText(html);
			
			holder.listThumb.setImageDrawable(null);

			String thumburl = myRssFeed.getList().get(position).getThumburl();
			if (!thumburl.equals("") && thumburl != null){
				//setting the image
				final ImageView imageView = holder.listThumb; // The view Picasso is loading an image into
				final Target target = new Target() {
					@Override
					public void onBitmapLoaded ( final Bitmap bitmap, Picasso.LoadedFrom from){
           						 /* Save the bitmap or do something with it here */

						if (10 > bitmap.getWidth() || 10 > bitmap.getHeight()) {
							// handle scaling
							holder.listThumb.setVisibility(View.GONE);
						} else {
							holder.listThumb.setVisibility(View.VISIBLE);
							holder.listThumb.setImageBitmap(bitmap);
						}
					}

					@Override
					public void onBitmapFailed(Drawable errorDrawable) {
					}

					@Override
					public void onPrepareLoad(Drawable placeHolderDrawable) {
					}
				};

				imageView.setTag(target);

				Picasso.with(mAct)
                        .load(myRssFeed.getList().get(position).getThumburl())
						.into(target);
			} else {
				holder.listThumb.setVisibility(View.GONE);
			}
		 
			return row;
		 }
		 
	}
	
	static class ViewHolder {
		  TextView listTitle;
		  TextView listPubdate;
		  TextView listDescription;
		  ImageView listThumb;
		  int position;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ll = (LinearLayout) inflater.inflate(R.layout.fragment_list, container, false);
		setHasOptionsMenu(true);

	    return ll;
	}
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		mAct = getActivity();

		url = RssFragment.this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];

		new MyTask().execute();
	}

	private class MyTask extends AsyncTask<Void, Void, Void>{
		
		@Override
		protected void onPreExecute(){
			pDialog = (RelativeLayout) ll.findViewById(R.id.progressBarHolder);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				URL rssUrl = new URL(url);
				SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
				SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
				XMLReader myXMLReader = mySAXParser.getXMLReader();
				RSSHandler myRSSHandler = new RSSHandler();
				myXMLReader.setContentHandler(myRSSHandler);
				InputSource myInputSource = new InputSource(rssUrl.openStream());
				myXMLReader.parse(myInputSource);
				
				myRssFeed = myRSSHandler.getFeed();

			} catch (MalformedURLException e) {
				Log.printStackTrace(e);
			} catch (ParserConfigurationException e) {
				Log.printStackTrace(e);
			} catch (SAXException e) {
				Log.printStackTrace(e);
			} catch (IOException e) {
				Log.printStackTrace(e);
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			ListView listview = (ListView) ll.findViewById(R.id.list);
			
			if (myRssFeed != null) {
				RssAdapter adapter = new RssAdapter(mAct,
						R.layout.fragment_rss_row, myRssFeed.getList());
				listview.setAdapter(adapter);

				listview.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View v,
							int position, long id) {
						Intent intent = new Intent(mAct,
								RssDetailActivity.class);
						Bundle bundle = new Bundle();
						intent.putExtra(RssDetailActivity.EXTRA_RSSITEM, myRssFeed.getItem(position));

						intent.putExtras(bundle);
						startActivity(intent);

					}
				});

			} else {
				String message = null;
				if (!url.startsWith("http"))
					message = "Debug info: '" + url + "' is most likely not a valid RSS url. Make sure the url entered in your configuration starts with 'http' and verify if it's valid XML using https://validator.w3.org/feed/";
				Helper.noConnection(mAct, message);
			}

			if (pDialog.getVisibility() == View.VISIBLE) {
				pDialog.setVisibility(View.GONE);
				//feedListView.setVisibility(View.VISIBLE);
				Helper.revealView(listview,ll);
			}
			super.onPostExecute(result);
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.rss_menu, menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
        case R.id.refresh_rss:
	    	new MyTask().execute();
	    	return true;
        case R.id.info:    
        	//show information about the feed in general in a dialog 
            if (myRssFeed!=null)
			{
				String FeedTitle = (myRssFeed.getTitle());
				String FeedDescription = (myRssFeed.getDescription());
				//String FeedPubdate = (myRssFeed.getPubdate()); most times not present
				String FeedLink = (myRssFeed.getLink());
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
				
				String titlevalue = getResources().getString(R.string.feed_title_value);
				String descriptionvalue = getResources().getString(R.string.feed_description_value);
				String linkvalue = getResources().getString(R.string.feed_link_value);
				
				if (FeedLink.equals("")){
	                 builder.setMessage(titlevalue+": \n"+FeedTitle+
      		               "\n\n"+descriptionvalue+": \n"+FeedDescription);
				} else {
					 builder.setMessage(titlevalue+": \n"+FeedTitle+
          		           "\n\n"+descriptionvalue+": \n"+FeedDescription +
          		           "\n\n"+linkvalue+": \n"+FeedLink);
				};
				
	                 builder.setNegativeButton(getResources().getString(R.string.ok),null)
	                 .setCancelable(true);
	            builder.create();
	            builder.show();
				
			}else{
				
			}     
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}