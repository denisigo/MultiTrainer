package com.denisigo.navdrawer;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.denisigo.multitrainer.R;

/**
 * Simple item for NavDrawer. Just have text.
 */
public class NavDrawerUserItem extends NavDrawerBaseItem {
	
	private View mView;
	private int mIconId;
	private Drawable mIconDrawable;
	private int mBackgroundId;
	private Drawable mBackgroundDrawable;

	public NavDrawerUserItem(int id, String title, int icon, int background) {
		super(id, title);
		mIconId = icon;
		mBackgroundId = background;
		setLayoutId(R.layout.navdrawer_user_item);
	}

	public NavDrawerUserItem(int id, String title, Drawable icon, Drawable background) {
		super(id, title);
		mIconDrawable = icon;
		mBackgroundDrawable = background;
		setLayoutId(R.layout.navdrawer_icon_item);
	}

	public void setIcon(Drawable icon) {
		mIconDrawable = icon;
		updateView();
	}
	
	public void setBackground(Drawable icon) {
		mBackgroundDrawable = icon;
		updateView();
	}

	private void updateView() {
		if (mView != null) {
			((TextView) mView.findViewById(R.id.title)).setText(getTitle());
			
			if (mIconDrawable == null)
				((ImageView) mView.findViewById(R.id.icon))
					.setImageResource(mIconId);
			else
				((ImageView) mView.findViewById(R.id.icon))
				.setImageDrawable(mIconDrawable);
			
			if (mBackgroundDrawable == null)
				((ImageView) mView.findViewById(R.id.background))
					.setBackgroundResource(mBackgroundId);
			else
				((ImageView) mView.findViewById(R.id.background))
				.setBackground(mBackgroundDrawable);
		}
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView,
			ViewGroup root) {
		mView = super.getView(inflater, convertView, root);

		updateView();

		return mView;
	}
}
