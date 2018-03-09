package com.ronak.viral.adda.providers.overview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ronak.viral.adda.R;
import com.ronak.viral.adda.drawer.NavItem;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Karthik
 * Copyright 2017
 */

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TEXT_TYPE = 0;
    private static final int IMAGE_TYPE = 1;

    private List<NavItem> data;
    private Context context;
    private OnOverViewClick callback;

    public CategoryAdapter(List<NavItem> data, Context context, OnOverViewClick click) {
        super();
        this.data = data;
        this.context = context;
        this.callback = click;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= 0 && position < data.size()){
            if (data.get(position).categoryImageUrl != null && !data.get(position).categoryImageUrl.isEmpty())
                return IMAGE_TYPE;
            else
                return TEXT_TYPE;
        }
        return super.getItemViewType(position);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == TEXT_TYPE)
            return new TextViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_overview_card_text, parent, false));
        else if (viewType == IMAGE_TYPE)
            return new ImageViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_overview_card_image, parent, false));

        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                callback.onOverViewSelected(data.get(holder.getAdapterPosition()));
            }
        });

        if (holder instanceof TextViewHolder) {

            ((TextViewHolder) holder).title.setText(data.get(position).getText(context));

        }  else if (holder instanceof ImageViewHolder) {

            Picasso.with(context)
                    .load(data.get(position).categoryImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(((ImageViewHolder) holder).image);
            ((ImageViewHolder) holder).title.setText(data.get(position).getText(context));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView image;

        public View itemView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

            title = (TextView) itemView.findViewById(R.id.title);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }

    private class TextViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public View itemView;

        public TextViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

            title = (TextView) itemView.findViewById(R.id.title);
        }
    }

    public interface OnOverViewClick{
        void onOverViewSelected(NavItem item);
    }


}