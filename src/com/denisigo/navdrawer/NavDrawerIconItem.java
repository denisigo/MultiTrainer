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
public class NavDrawerIconItem extends NavDrawerBaseItem {

	private View mView;
	private int mIconId;
	private Drawable mIconDrawable;

	public NavDrawerIconItem(int id, String title, int icon) {
		super(id, title);
		mIconId = icon;
		setLayoutId(R.layout.navdrawer_icon_item);
	}

	public NavDrawerIconItem(int id, String title, Drawable icon) {
		super(id, title);
		mIconDrawable = icon;
		setLayoutId(R.layout.navdrawer_icon_item);
	}

	public void setIcon(Drawable icon) {
		mIconDrawable = icon;
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
