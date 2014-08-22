package com.denisigo.navdrawer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Interface for NavDrawer item.
 */
public interface NavDrawerItem {
	/**
	 * Get title of item.
	 * 
	 * @return
	 */
	public String getTitle();

	/**
	 * Set title for item
	 * 
	 * @param title Title of the item.
	 */
	public void setTitle(String title);

	/**
	 * Get id of the view.
	 * 
	 * @return int id of the view
	 */
	public int getId();

	/**
	 * Set id for the view to be able to track clicks on it.
	 * 
	 * @param id
	 */
	public void setId(int id);

	/**
	 * Each item is responsible for inflating its own view.
	 * 
	 * @param inflater
	 *            Inflater service provided by adapter.
	 * @param convertView
	 *            Previously created view which might be reused. Implementors
	 *            MUST check for view.getId() to match id of layout they use.
	 * @param root
	 *            Root view to inflate.
	 * @return View instance.
	 */
	public View getView(LayoutInflater inflater, View convertView,
			ViewGroup root);
}
