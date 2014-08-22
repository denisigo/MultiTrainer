package com.denisigo.multitrainer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;

public class TimeIsUpDialog extends DialogFragment {

	public interface TimeIsUpDialogListener {
		public void onTimeIsUpDialogShareClick(TimeIsUpDialog dialog);

		public void onTimeIsUpDialogSaveScoreClick(TimeIsUpDialog dialog);

		public void onTimeIsUpDialogCloseClick(TimeIsUpDialog dialog);
	}

	// Use this instance of the interface to deliver action events
	TimeIsUpDialogListener mListener;
	// How much equations solved
	public static final String SOLVED_COUNT = "SOLVED_COUNT";

	// Override the Fragment.onAttach() method to instantiate the
	// NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (TimeIsUpDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.time_is_up)
				.setMessage(
						String.format(
								getString(R.string.timeisup_message),
								args.getInt(SOLVED_COUNT)))
				.setNegativeButton(R.string.close,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								mListener
										.onTimeIsUpDialogCloseClick(TimeIsUpDialog.this);
								dismiss();
							}
						})
				.setPositiveButton(R.string.share,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								mListener
										.onTimeIsUpDialogShareClick(TimeIsUpDialog.this);
								dismiss();
							}
						})
				.setNeutralButton(R.string.save_score,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								mListener
										.onTimeIsUpDialogSaveScoreClick(TimeIsUpDialog.this);
								dismiss();
							}
						});
		// Create the AlertDialog object and return it
		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}
}
