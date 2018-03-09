package com.ronak.viral.adda.providers.pinterest;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ronak.viral.adda.Config;
import com.ronak.viral.adda.R;
import com.ronak.viral.adda.providers.web.WebviewActivity;
import com.ronak.viral.adda.util.Helper;
import com.ronak.viral.adda.util.MediaActivity;
import com.ronak.viral.adda.util.WebHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PinterestAdapter extends ArrayAdapter<Pin>{
	
	private Context context;

    public PinterestAdapter(Context context, List<Pin> objects) {
        super(context, 0, objects);
    	this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Pin pin = getItem(position);
        PinterestViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_pinterest_row, parent, false);

            viewHolder = new PinterestViewHolder();
            viewHolder.profileImg = (ImageView) convertView.findViewById(R.id.profile_image);
            viewHolder.userNameView = (TextView)convertView.findViewById(R.id.name);
            viewHolder.dateView = (TextView)convertView.findViewById(R.id.date);
            viewHolder.inlineImg = (ImageView)convertView.findViewById(R.id.photo);
            viewHolder.repinCountView = (TextView)convertView.findViewById(R.id.like_count);
            viewHolder.descriptionView = (TextView)convertView.findViewById(R.id.message);
            viewHolder.commentsView = (TextView) convertView.findViewById(R.id.comments);
            viewHolder.shareBtn = (Button) convertView.findViewById(R.id.share);
            viewHolder.openBtn = (Button) convertView.findViewById(R.id.open);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (PinterestViewHolder)convertView.getTag();
        }

        viewHolder.profileImg.setImageDrawable(null);
        Picasso.with(context).load(pin.creatorImageUrl).into(viewHolder.profileImg);

        viewHolder.userNameView.setText(pin.creatorName);

        viewHolder.dateView.setText(DateUtils.getRelativeDateTimeString(context,pin.createdTime.getTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));

        viewHolder.inlineImg.setImageDrawable(null);
        Picasso.with(context).load(pin.imageUrl).placeholder(R.drawable.placeholder).into(viewHolder.inlineImg);

        if (pin.type.equals("image")){
            viewHolder.inlineImg.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

	                Intent intent = new Intent(context, MediaActivity.class);
	                intent.putExtra(MediaActivity.TYPE, MediaActivity.TYPE_IMG);
	                intent.putExtra(MediaActivity.URL, pin.imageUrl);
	                context.startActivity(intent);
                }
            });
        }
        else {
            viewHolder.inlineImg.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    Intent mIntent = new Intent(context, WebviewActivity.class);
                    mIntent.putExtra(WebviewActivity.OPEN_EXTERNAL, Config.OPEN_INLINE_EXTERNAL);
                    mIntent.putExtra(WebviewActivity.URL, pin.videoUrl);
                    context.startActivity(mIntent);
                }
            });
        }

        viewHolder.repinCountView.setText(Helper.formatValue(pin.repinCount));

        if (pin.caption != null){
        	viewHolder.descriptionView.setText(Html.fromHtml(pin.caption));
        	viewHolder.descriptionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, WebHelper.getTextViewFontSize(context));
        }
        
        viewHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

    			Intent sendIntent = new Intent();
    			sendIntent.setAction(Intent.ACTION_SEND);
    			
    			// this is the text that will be shared
    			sendIntent.putExtra(Intent.EXTRA_TEXT,pin.link);
    			
    			sendIntent.setType("text/plain");
    			context.startActivity(Intent.createChooser(sendIntent, context.getResources()
    					.getString(R.string.share_header)));
            }
        });
		
        viewHolder.openBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent mIntent = new Intent(context, WebviewActivity.class);
                mIntent.putExtra(WebviewActivity.OPEN_EXTERNAL, Config.OPEN_EXPLICIT_EXTERNAL);
                mIntent.putExtra(WebviewActivity.URL, pin.link);
                context.startActivity(mIntent);
            }
        });

        // Set comments
        if (pin.commentsCount == 0) viewHolder.commentsView.setVisibility(View.GONE);
        else {
            viewHolder.commentsView.setVisibility(View.VISIBLE);
            viewHolder.commentsView.setText(Helper.formatValue(pin.commentsCount) + " " + context.getResources().getString(R.string.comments));
        }

        return convertView;
    }
    
    private class PinterestViewHolder {

        ImageView profileImg;
        ImageView inlineImg;
        
        TextView userNameView;
        TextView dateView;
        TextView repinCountView;
        TextView descriptionView;
        TextView commentsView;
        
        Button shareBtn;
        Button openBtn;
    }
}
