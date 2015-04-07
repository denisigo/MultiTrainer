package com.denisigo.navdrawer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.denisigo.multitrainer.R;

/**
 * Simple item for NavDrawer. Just have text.
 */
public class NavDrawerSimpleItem extends NavDrawerBaseItem {

	public NavDrawerSimpleItem(int id, String title) {
		super(id, title);
		setLayoutId(R.layout.navdrawer_simple_item);
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView, ViewGroup root) {
		convertView = super.getView(inflater, convertView, root);
		
		// Fill layout data
		((TextView)convertView.findViewById(R.id.title)).setText(getTitle());
		
		return convertView;
	}

}
