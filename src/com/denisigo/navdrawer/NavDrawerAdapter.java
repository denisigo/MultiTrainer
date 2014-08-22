package com.denisigo.navdrawer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class NavDrawerAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<NavDrawerItem> mItems;
	LayoutInflater mInflater;
	
	public NavDrawerAdapter(Context context) {
		mContext = context;
		init();
	}

	public NavDrawerAdapter(Context context, ArrayList<NavDrawerItem> items) {
		mContext = context;
		mItems = items;
		init();
	}
	
	public void setItems(ArrayList<NavDrawerItem> items){
		mItems = items;
		notifyDataSetChanged();
	}
	
	private void init(){
		mInflater = (LayoutInflater) mContext
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		if (mItems != null)
			return mItems.size();
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mItems != null)
			return mItems.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		NavDrawerItem item = (NavDrawerItem) getItem(position);
		if (item != null)
			return item.getId();
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup root) {
		return ((NavDrawerBaseItem) getItem(position)).getView(mInflater, convertView, root);
	}

}
