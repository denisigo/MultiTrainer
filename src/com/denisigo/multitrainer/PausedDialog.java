package com.denisigo.multitrainer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;

public class PausedDialog extends DialogFragment {

	public interface PausedDialogListener {
		public void onPausedDialogResumeClick(PausedDialog dialog);

		public void onPausedDialogCloseClick(PausedDialog dialog);
	}

	// Use this instance of the interface to deliver action events
	PausedDialogListener mListener;

	// Override the Fragment.onAttach() method to instantiate the
	// NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (PausedDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.paused)
				.setMessage(String.format(getString(R.string.paused_message)))
				.setNegativeButton(R.string.close,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								mListener
										.onPausedDialogCloseClick(PausedDialog.this);
								dismiss();
							}
						})
				.setPositiveButton(R.string.resume,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								mListener
										.onPausedDialogResumeClick(PausedDialog.this);
								dismiss();
							}

						});
		// Create the AlertDialog object and return it
		Dialog dialog = builder.create();
		//dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}
}
