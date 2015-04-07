package com.denisigo.navdrawer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Base implementation for NavDrawer item.
 * 
 */
public abstract class NavDrawerBaseItem implements NavDrawerItem {
	private int mId;
	private String mTitle;
	private int mLayoutId;

	public NavDrawerBaseItem(int id, String title) {
		setId(id);
		setTitle(title);
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public int getLayoutId() {
		return mLayoutId;
	}

	protected void setLayoutId(int id) {
		mLayoutId = id;
	}

	public View getView(LayoutInflater inflater, View convertView,
			ViewGroup root) {
		// We want to reuse only suitable views
		if (convertView != null && convertView.getId() != getLayoutId())
			convertView = null;

		// Create a new view if there is no suitable convertView
		if (convertView == null)
			convertView = inflater.inflate(getLayoutId(), root, false);

		return convertView;
	}

}
