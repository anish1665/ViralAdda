package com.ronak.viral.adda.providers.overview.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.ronak.viral.adda.HolderActivity;
import com.ronak.viral.adda.MainActivity;
import com.ronak.viral.adda.R;
import com.ronak.viral.adda.drawer.NavItem;
import com.ronak.viral.adda.providers.overview.CategoryAdapter;
import com.ronak.viral.adda.providers.overview.OverviewParser;

import java.util.ArrayList;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Karthik
 * Copyright 2017
 */
public class OverviewFragment extends Fragment implements CategoryAdapter.OnOverViewClick {

    //Views
    private RelativeLayout rl;
    private RecyclerView mRecyclerView;
    private View mLoadingView;

    private String overviewString;

    //List
    private CategoryAdapter multipleItemAdapter;

    private ViewTreeObserver.OnGlobalLayoutListener recyclerListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rl = (RelativeLayout) inflater.inflate(R.layout.fragment_overview,null);
        setHasOptionsMenu(true);
        mRecyclerView = (RecyclerView) rl.findViewById(R.id.rv_list);

        final StaggeredGridLayoutManager mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        rl.findViewById(R.id.swiperefresh).setEnabled(false);

        overviewString = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];

        recyclerListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Get the view width, and check if it could be valid
                int viewWidth = mRecyclerView.getMeasuredWidth();
                if (viewWidth <= 0 ) return;

                //Remove the VTO
                mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                //Calculate and update the span
                float cardViewWidth = getResources().getDimension(R.dimen.card_width_overview);
                int newSpanCount = Math.max(1, (int) Math.floor(viewWidth / cardViewWidth));
                mLayoutManager.setSpanCount(newSpanCount);
                mLayoutManager.requestLayout();
            }
        };
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(recyclerListener);

        mLoadingView = rl.findViewById(R.id.loading_view);

        //Load items
        loadItems();

        return rl;
    }

    public void loadItems() {
        new OverviewParser(overviewString, getActivity(), new OverviewParser.CallBack() {
            @Override
            public void categoriesLoaded(ArrayList<NavItem> result, boolean failed) {
                if (failed) return;

                //Add all the new posts to the list and notify the adapter
                multipleItemAdapter = new CategoryAdapter(result, getContext(), OverviewFragment.this);
                mRecyclerView.setAdapter(multipleItemAdapter);
            }
        }).execute();


        loadCompleted();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(recyclerListener);
    }

    private void loadCompleted(){
        //Hide the loading view if it isn't already hidden
        if (mLoadingView.getVisibility() == View.VISIBLE)
            mLoadingView.setVisibility(View.GONE);
    }

    @Override
    public void onOverViewSelected(NavItem item) {
        HolderActivity.startActivity(getActivity(), item.getFragment(), item.getData());
    }
}
