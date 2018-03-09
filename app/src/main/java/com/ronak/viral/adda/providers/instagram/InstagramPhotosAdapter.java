package com.ronak.viral.adda.providers.instagram;

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
import com.ronak.viral.adda.comments.CommentsActivity;
import com.ronak.viral.adda.util.Helper;
import com.ronak.viral.adda.util.MediaActivity;
import com.ronak.viral.adda.util.WebHelper;
import com.ronak.viral.adda.providers.web.WebviewActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class InstagramPhotosAdapter  extends ArrayAdapter<InstagramPhoto>{
	
	private Context context;

    public InstagramPhotosAdapter(Context context, List<InstagramPhoto> objects) {
        super(context, 0, objects);
    	this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final InstagramPhoto photo = getItem(position);
        InstagramPhotoViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_instagram_row, parent, false);

            viewHolder = new InstagramPhotoViewHolder();
            viewHolder.profileImg = (ImageView) convertView.findViewById(R.id.profile_image);
            viewHolder.userNameView = (TextView)convertView.findViewById(R.id.name);
            viewHolder.dateView = (TextView)convertView.findViewById(R.id.date);
            viewHolder.inlineImg = (ImageView)convertView.findViewById(R.id.photo);
            viewHolder.likesCountView = (TextView)convertView.findViewById(R.id.like_count);
            viewHolder.descriptionView = (TextView)convertView.findViewById(R.id.message);
            viewHolder.descriptionView = (TextView)convertView.findViewById(R.id.message);
            viewHolder.shareBtn = (Button) convertView.findViewById(R.id.share);
            viewHolder.openBtn = (Button) convertView.findViewById(R.id.open);
            viewHolder.commentsBtn = (Button) convertView.findViewById(R.id.comments);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (InstagramPhotoViewHolder)convertView.getTag();
        }

        viewHolder.profileImg.setImageDrawable(null);
        Picasso.with(context).load(photo.profilePhotoUrl).into(viewHolder.profileImg);

        String username  = photo.username.substring(0,1).toUpperCase(Locale.getDefault()) + photo.username.substring(1).toLowerCase(Locale.getDefault());
        viewHolder.userNameView.setText(username);

        viewHolder.dateView.setText(DateUtils.getRelativeDateTimeString(context,photo.createdTime.getTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));

        viewHolder.inlineImg.setImageDrawable(null);
        Picasso.with(context).load(photo.imageUrl).placeholder(R.drawable.placeholder).into(viewHolder.inlineImg);

        if (photo.type.equals("image")){
            viewHolder.inlineImg.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

	                Intent commentIntent = new Intent(context, MediaActivity.class);
	                commentIntent.putExtra(MediaActivity.TYPE, MediaActivity.TYPE_IMG);
	                commentIntent.putExtra(MediaActivity.URL, photo.imageUrl);
	                context.startActivity(commentIntent);
                }
            });
        }
        else {
            viewHolder.inlineImg.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

	                Intent commentIntent = new Intent(context, MediaActivity.class);
	                commentIntent.putExtra(MediaActivity.TYPE, MediaActivity.TYPE_VID);
	                commentIntent.putExtra(MediaActivity.URL, photo.videoUrl);
	                context.startActivity(commentIntent);
                }
            });
        }

        viewHolder.likesCountView.setText(Helper.formatValue(photo.likesCount));

        if (photo.caption != null){
        	viewHolder.descriptionView.setText(Html.fromHtml(photo.caption));
        	viewHolder.descriptionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, WebHelper.getTextViewFontSize(context));
        }
        
        viewHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

    			Intent sendIntent = new Intent();
    			sendIntent.setAction(Intent.ACTION_SEND);
    			
    			// this is the text that will be shared
    			sendIntent.putExtra(Intent.EXTRA_TEXT,photo.link);
    			
    			sendIntent.setType("text/plain");
    			context.startActivity(Intent.createChooser(sendIntent, context.getResources()
    					.getString(R.string.share_header)));
            }
        });
		
        viewHolder.openBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent mIntent = new Intent(context, WebviewActivity.class);
                mIntent.putExtra(WebviewActivity.OPEN_EXTERNAL, Config.OPEN_EXPLICIT_EXTERNAL);
                mIntent.putExtra(WebviewActivity.URL, photo.link);
                context.startActivity(mIntent);
            }
        });

        // Set comments
        viewHolder.commentsBtn.setText(Helper.formatValue(photo.commentsCount) + " " + context.getResources().getString(R.string.comments));

        viewHolder.commentsBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    // Start NewActivity.class
                    Intent commentIntent = new Intent(getContext(), CommentsActivity.class);
                    commentIntent.putExtra(CommentsActivity.DATA_TYPE, CommentsActivity.INSTAGRAM);
                    commentIntent.putExtra(CommentsActivity.DATA_ID, photo.id);
                    getContext().startActivity(commentIntent);
                }
        });

        return convertView;
    }
    
    class InstagramPhotoViewHolder {

        ImageView profileImg;
        ImageView inlineImg;
        
        TextView userNameView;
        TextView dateView;
        TextView likesCountView;
        TextView descriptionView;
        
        Button shareBtn;
        Button openBtn;
        Button commentsBtn;
    }
}
