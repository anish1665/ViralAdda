package com.ronak.viral.adda.providers.wordpress;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ronak.viral.adda.R;
import com.squareup.picasso.Picasso;

public class WordpressListAdapter extends ArrayAdapter<PostItem> {

	private ArrayList<PostItem> listData;

	private LayoutInflater layoutInflater;

	private Context mContext;
	
	private Boolean simpleMode;
	
	private String TAG_TOP = "TOP";
	
	public WordpressListAdapter(Context context, Integer something, ArrayList<PostItem> listData, Boolean simpleMode) {
		super(context, something, listData);
		this.listData = listData;
		layoutInflater = LayoutInflater.from(context);
		mContext = context;
		this.simpleMode = simpleMode;
	}

	@Override
	public int getCount() {
		return listData.size();
	}

	@Override
	public PostItem getItem(int position) {
		return listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	public View getView(int position, View convertView, ViewGroup parent) {
		PostItem newsItem = (PostItem) listData.get(position);
		
		//if it is the first item, give a special treatment.
		if (position == 0 && (null != newsItem.getAttachmentUrl() && !newsItem.getAttachmentUrl().equals("")) && !simpleMode){
			
			convertView = layoutInflater.inflate(R.layout.listview_highlight, null);
            Picasso.with(mContext).load(newsItem.getAttachmentUrl()).placeholder(R.drawable.placeholder).fit().centerCrop().into((ImageView) convertView.findViewById(R.id.imageViewHighlight));

			((TextView) convertView.findViewById(R.id.textViewHighlight)).setText(newsItem.getTitle());
			convertView.setTag(TAG_TOP);
			return convertView;
		}
		
		ViewHolder holder;
		if (convertView == null || convertView.getTag().equals(TAG_TOP)) {
			convertView = layoutInflater.inflate(R.layout.fragment_wordpress_list_row, null);
			holder = new ViewHolder();
			holder.headlineView = (TextView) convertView.findViewById(R.id.title);
			holder.reportedDateView = (TextView) convertView.findViewById(R.id.date);
			holder.imageView = (ImageView) convertView.findViewById(R.id.thumbImage);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.imageView.setImageBitmap(null);
		}

		holder.headlineView.setText(newsItem.getTitle());
		holder.reportedDateView.setText(DateUtils.getRelativeDateTimeString(mContext, newsItem.getDate().getTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));

        holder.imageView.setVisibility(View.GONE);

		if (null == newsItem.getThumbnailUrl() || newsItem.getThumbnailUrl().equals("") || newsItem.getThumbnailUrl().equals("null")){
			if (null != newsItem.getAttachmentUrl() && !newsItem.getAttachmentUrl().equals("") && !newsItem.getAttachmentUrl().equals("null")){
				//there is a attachment url we can use instead
				holder.imageView.setVisibility(View.VISIBLE);
                Picasso.with(mContext).load(newsItem.getAttachmentUrl()).fit().centerInside().into(holder.imageView);
			}
		} else {
			//there is a thumbnail url available to show
			holder.imageView.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(newsItem.getThumbnailUrl()).into(holder.imageView);
        }
		
		return convertView;
	}

	static class ViewHolder {
		TextView headlineView;
		TextView reportedDateView;
		ImageView imageView;
	}
}
