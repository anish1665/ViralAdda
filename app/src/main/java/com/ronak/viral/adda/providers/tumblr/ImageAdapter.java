package com.ronak.viral.adda.providers.tumblr;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.ronak.viral.adda.R;
import com.squareup.picasso.Picasso;

public class ImageAdapter extends ArrayAdapter<TumblrItem> {

	private ArrayList<TumblrItem> listData;
	private LayoutInflater layoutInflater;
	private Context mContext;
	
	public ImageAdapter(Context context, Integer something, ArrayList<TumblrItem> listData) {
		super(context, something, listData);
		this.listData = listData;
		layoutInflater = LayoutInflater.from(context);
		mContext = context;
	}

	@Override
	public int getCount() {
		return listData.size();
	}

	@Override
	public TumblrItem getItem(int position) {
		return listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		View view = convertView;
		if (view == null) {
			view = layoutInflater.inflate(R.layout.fragment_tumblr_row, parent, false);
			holder = new ViewHolder();
			assert view != null;
			holder.imageView = (ImageView) view.findViewById(R.id.image);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		Picasso.with(mContext).load(listData.get(position).getUrl()).placeholder(R.drawable.placeholder).fit().centerCrop().into(holder.imageView);

		return view;
	}

	static class ViewHolder {
		ImageView imageView;
	}
}
