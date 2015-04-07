package com.denisigo.multitrainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.denisigo.navdrawer.NavDrawerAdapter;
import com.denisigo.navdrawer.NavDrawerIconItem;
import com.denisigo.navdrawer.NavDrawerItem;
import com.denisigo.navdrawer.NavDrawerUserItem;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.plus.Plus.PlusOptions;
import com.google.example.games.basegameutils.BaseGameActivity;

public class MainActivity extends BaseGameActivity implements
		TimeIsUpDialog.TimeIsUpDialogListener,
		PausedDialog.PausedDialogListener {

	public static final int RC_SHOWLEADERBOARD = 42;
	public static final int RC_SHOWACHIEVEMENTS = 43;

	// Bounds for equation generator.
	private static final int MIN_OPERAND1 = 2;
	private static final int MAX_OPERAND1 = 9;
	private static final int MIN_OPERAND2 = 2;
	private static final int MAX_OPERAND2 = 9;
	// Timeout for timer.
	private static final int TIMEOUT = 60;

	// Stores result number
	private int mResult;
	// Amount of solved equations
	private int mSolvedCount;
	// Counter for seconds
	private int mCountDown;
	// Fully-formatted string of equation - 2x3=
	private String mEquation;
	// String for accumulating the answer
	private String mAnswer;
	// Whether timer is paused or not
	private boolean mIsPaused;
	// Whether to save score after sign in
	private boolean mSaveScoreAfterSignIn;

	// To store sign-in status
	private static final String PREFS_NAME = "settings";
	private static final String FIELD_SIGNEDIN = "signedin";

	private TextView mTvEquation, mTvTimer;
	private ToggleButton mTbTimerButton;
	private RelativeLayout mRlPleaseWait;

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private ListView mDrawerList;
	private NavDrawerAdapter mDrawerAdapter;
	private OnProfileImageLoadedListener mOnProfileImageLoadedlistener;

	private final static int NAVDRAWER_SIGNIN = 0;
	private final static int NAVDRAWER_LEADERBOARD = 1;
	private final static int NAVDRAWER_SHAREAPP = 2;
	private final static int NAVDRAWER_SIGNOUT = 3;
	private final static int NAVDRAWER_ACHIEVEMENTS = 4;

	private Handler mHandler = new Handler();

	MyCountDownTimer mTimer;

	GoogleApiClient mApiClient;

	/**
	 * Our countdown timer implementation. Since java's CountDownTimer has not
	 * desired behavior.
	 */
	private abstract class MyCountDownTimer implements Runnable {

		private boolean mRunning = false;
		private int mTimeout;
		private Handler mHandler = new Handler();

		public MyCountDownTimer(int timeout) {
			mTimeout = timeout;
		}

		public final void setRunning(boolean state) {
			mRunning = state;

			if (state) {
				schedule(0);
			} else {
				mHandler.removeCallbacks(this);
			}
		}

		public final MyCountDownTimer start() {
			setRunning(true);
			return this;
		}

		public final void cancel() {
			setRunning(false);
		}

		private final void schedule(long delay) {
			mHandler.postDelayed(this, delay);
		}

		public abstract void onTick(int secondsUntilFinished);

		public abstract void onFinish();

		@Override
		public final void run() {
			if (mRunning) {
				mTimeout--;
				onTick(mTimeout);

				if (mTimeout == 0) {
					onFinish();
					cancel();
				}

				if (mRunning) {
					schedule(1000);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setRequestedClients(CLIENT_GAMES | CLIENT_PLUS);
		getGameHelper();
		mHelper.setConnectOnStart(false);
		mHelper.setPlusApiOptions(PlusOptions.builder().build());

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		mDrawerList = (ListView) mDrawerLayout.findViewById(R.id.drawer_list);
		mDrawerAdapter = new NavDrawerAdapter(this);
		mDrawerList.setAdapter(mDrawerAdapter);
		mDrawerList.setOnItemClickListener(new NavDrawerItemClickListener());
		updateNavigationDrawer();

		mTbTimerButton = (ToggleButton) findViewById(R.id.timerButton);
		mTvTimer = (TextView) findViewById(R.id.timer);
		mTvEquation = (TextView) findViewById(R.id.equation);
		mRlPleaseWait = (RelativeLayout) findViewById(R.id.pleasewait);

		if (wasSignedIn())
			beginUserInitiatedSignIn();

		// Restore state if needed
		if (savedInstanceState == null) {
			resetTimer();
			nextEquation();
		} else {
			mResult = savedInstanceState.getInt("mResult");
			mSolvedCount = savedInstanceState.getInt("mSolvedCount");
			mCountDown = savedInstanceState.getInt("mCountDown");
			mEquation = savedInstanceState.getString("mEquation");
			mAnswer = savedInstanceState.getString("mAnswer");

			if (savedInstanceState.getBoolean("mIsPaused"))
				showPausedDialog();

			updateUI();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// If timer is running, remember it as paused state
		if (timerIsRunning()) {
			stopTimer();
			mIsPaused = true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// If we were paused, show paused dialog to not surprise user with timer
		// started counting right after activity appeared
		if (mIsPaused) {
			showPausedDialog();
			mIsPaused = false;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	/**
	 * If we're going to be destroyed, save our state.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("mResult", mResult);
		outState.putInt("mSolvedCount", mSolvedCount);
		outState.putInt("mCountDown", mCountDown);
		outState.putBoolean("mIsPaused", mIsPaused);
		outState.putString("mEquation", mEquation);
		outState.putString("mAnswer", mAnswer);

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private class NavDrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long id) {
			if (id == NAVDRAWER_SIGNIN) {
				beginUserInitiatedSignIn();
			} else if (id == NAVDRAWER_SIGNOUT) {
				signOut();
			} else if (id == NAVDRAWER_LEADERBOARD) {
				showLeaderboard();
			} else if (id == NAVDRAWER_ACHIEVEMENTS) {
				showAchievements();
			} else if (id == NAVDRAWER_SHAREAPP) {
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT,
						getString(R.string.appsharetext));
				sendIntent.setType("text/plain");
				startActivity(sendIntent);
			}

			mDrawerLayout.closeDrawer(mDrawerList);
		}
	}

	/**
	 * Update navigation drawer based on the current state.
	 */
	private void updateNavigationDrawer() {
		ArrayList<NavDrawerItem> drawerItems = new ArrayList<NavDrawerItem>();

		// Show sign in item if we're not logged in
		if (!isSignedIn()) {
			drawerItems.add(new NavDrawerIconItem(NAVDRAWER_SIGNIN,
					getString(R.string.sign_in), R.drawable.ic_gplus));
		} else {
			// Else show user profile item
			Player p = Games.Players.getCurrentPlayer(getApiClient());
			Uri icon = p.getIconImageUri();
			String displayName = p.getDisplayName();

			if (icon != null) {
				ImageManager im = ImageManager.create(getApplicationContext());
				mOnProfileImageLoadedlistener = new OnProfileImageLoadedListener();
				im.loadImage(mOnProfileImageLoadedlistener, icon);
			}

			drawerItems.add(new NavDrawerUserItem(-1, displayName,
					R.drawable.ic_action_person, R.drawable.profile_cover));
		}

		drawerItems.add(new NavDrawerIconItem(NAVDRAWER_LEADERBOARD,
				getString(R.string.leaderboard), R.drawable.ic_leaderboard));
		drawerItems.add(new NavDrawerIconItem(NAVDRAWER_ACHIEVEMENTS,
				getString(R.string.achievements), R.drawable.ic_achievements));
		drawerItems.add(new NavDrawerIconItem(NAVDRAWER_SHAREAPP,
				getString(R.string.share_app), R.drawable.ic_action_share));

		if (isSignedIn()) {
			drawerItems
					.add(new NavDrawerIconItem(NAVDRAWER_SIGNOUT,
							getString(R.string.sign_out),
							R.drawable.ic_lock_power_off));
		}
		mDrawerAdapter.setItems(drawerItems);
	}

	/**
	 * Callback for ImageManager on profile image loaded.
	 */
	private class OnProfileImageLoadedListener implements
			ImageManager.OnImageLoadedListener {
		@Override
		public void onImageLoaded(Uri uri, Drawable drawable,
				boolean isRequestedDrawable) {
			// First item in list should be user profile item, but let's check
			// it
			Object item = mDrawerAdapter.getItem(0);
			if (item.getClass() == NavDrawerUserItem.class)
				((NavDrawerUserItem) mDrawerAdapter.getItem(0))
						.setIcon(drawable);
		}
	}

	/**
	 * Create and start timer using current mCountDown
	 */
	private void startTimer() {
		if (timerIsRunning())
			return;

		mTbTimerButton.setChecked(true);

		mTimer = new MyCountDownTimer(mCountDown) {

			public void onTick(int secondsUntilFinished) {
				mCountDown = secondsUntilFinished;
				updateTimerUI();
			}

			public void onFinish() {
				mCountDown = 0;
				updateTimerUI();

				stopTimer();

				showPleaseWait();
			}
		}.start();
	}

	/**
	 * Stop and destroy timer.
	 */
	private void stopTimer() {
		mTbTimerButton.setChecked(false);

		mTimer.cancel();
		mTimer = null;
	}

	/**
	 * Start timer with initial countdown.
	 */
	private void restartTimer() {
		resetTimer();
		mSolvedCount = 0;
		nextEquation();
		startTimer();
	}

	/**
	 * Reset timer countdown.
	 */
	private void resetTimer() {
		mCountDown = TIMEOUT;
		updateTimerUI();
	}

	/**
	 * Whether the time is running?
	 * 
	 * @return
	 */
	private boolean timerIsRunning() {
		return mTimer != null;
	}

	/**
	 * Generate next equation.
	 */
	private void nextEquation() {

		mAnswer = "";

		Random rnd = new Random();

		int operand1 = rnd.nextInt(MAX_OPERAND1 - MIN_OPERAND1 + 1)
				+ MIN_OPERAND1;
		int operand2 = rnd.nextInt(MAX_OPERAND2 - MIN_OPERAND2 + 1)
				+ MIN_OPERAND2;

		mResult = operand1 * operand2;

		mEquation = Integer.toString(operand1) + "\u00D7"
				+ Integer.toString(operand2) + "=";

		mTvEquation.setTextColor(getResources().getColor(
				android.R.color.primary_text_dark));

		updateEquationUI();
	}

	/**
	 * Gets amount of numbers remaining for user to enter
	 * 
	 * @return amount of numbers remaining
	 */
	private int numbersRemaining() {
		return Integer.toString(mResult).length() - mAnswer.length();
	}

	/**
	 * Adds an answer number and checks if we have solved equation.
	 * 
	 * @param number
	 */
	private void addAnswerNumber(String number) {

		if (numbersRemaining() == 0)
			return;

		mAnswer += number;

		updateEquationUI();

		// If user entered all the numbers
		if (numbersRemaining() == 0) {
			// Is the answer correct?
			if (mResult == Integer.parseInt(mAnswer)) {
				mTvEquation.setTextColor(getResources().getColor(
						android.R.color.holo_green_light));

				// If we correctly solved, increment counter
				if (timerIsRunning()) {
					mSolvedCount++;
				}
			} else {
				mTvEquation.setTextColor(getResources().getColor(
						android.R.color.holo_red_light));
			}

			// Set slightly different timeout when timer is running
			if (timerIsRunning()) {
				scheduleNextEquation(200);
			} else {
				scheduleNextEquation(700);
			}
		}
	}

	/**
	 * Schedules next equation with some timeout.
	 * 
	 * @param timeout
	 */
	private void scheduleNextEquation(long timeout) {
		mHandler.postDelayed(new Runnable() {
			public void run() {
				nextEquation();
			}
		}, timeout);
	}

	/**
	 * Timer view click callback.
	 * 
	 * @param view
	 */
	public void onTimerClick(View view) {
		if (timerIsRunning()) {
			stopTimer();
			resetTimer();
		} else
			restartTimer();
	}

	/**
	 * Number button click callback.
	 * 
	 * @param view
	 */
	public void onNumberClick(View view) {
		addAnswerNumber(((Button) view).getText().toString());
	}

	/**
	 * Updates entire interface.
	 */
	private void updateUI() {
		updateEquationUI();
		updateTimerUI();
	}

	/**
	 * Update equation on the screen based on equation itself and numbers
	 * already entered.
	 */
	private void updateEquationUI() {
		char[] questions = new char[numbersRemaining()];
		Arrays.fill(questions, '?');
		mTvEquation.setText(mEquation + mAnswer + new String(questions));
	}

	/**
	 * Update timer on the screen
	 */
	private void updateTimerUI() {
		int minutes = (int) Math.ceil(mCountDown / 60);
		int seconds = mCountDown - minutes * 60;
		mTvTimer.setText(String.format("%02d:%02d", minutes, seconds));
	}

	/**
	 * Let's save score!
	 */
	private void saveScore() {
		if (!isSignedIn()) {
			mSaveScoreAfterSignIn = true;
			beginUserInitiatedSignIn();
		} else {
			doSaveScore();
		}
	}

	private void doSaveScore() {
		Games.Leaderboards.submitScore(getApiClient(),
				getString(R.string.leaderboard_top_users), mSolvedCount);

		// Unlock the achievements
		if (mSolvedCount >= 10) {
			Games.Achievements.unlock(getApiClient(),
					getString(R.string.achievement_10_epm));
		} 
		if (mSolvedCount >= 20) {
			Games.Achievements.unlock(getApiClient(),
					getString(R.string.achievement_20_epm));
		}
		if (mSolvedCount >= 30) {
			Games.Achievements.unlock(getApiClient(),
					getString(R.string.achievement_30_epm));
		}
		if (mSolvedCount >= 40) {
			Games.Achievements.unlock(getApiClient(),
					getString(R.string.achievement_40_epm));
		} 
		if (mSolvedCount >= 50) {
			Games.Achievements.unlock(getApiClient(),
					getString(R.string.achievement_50_epm));
		}

		// Open leaderboard
		showLeaderboard();

	}

	/**
	 * Opens our leaderboard.
	 */
	private void showLeaderboard() {
		if (isSignedIn()) {
			startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
					getApiClient(), getString(R.string.leaderboard_top_users)),
					RC_SHOWLEADERBOARD);
		} else
			beginUserInitiatedSignIn();
	}

	/**
	 * Opens achievements.
	 */
	private void showAchievements() {
		if (isSignedIn()) {
			startActivityForResult(
					Games.Achievements.getAchievementsIntent(getApiClient()),
					RC_SHOWACHIEVEMENTS);
		} else
			beginUserInitiatedSignIn();
	}

	/**
	 * Show "Time is up" dialog to allow user share or save score.
	 */
	private void showTimeIsUpDialog() {
		TimeIsUpDialog newFragment = new TimeIsUpDialog();
		Bundle args = new Bundle();
		args.putInt(TimeIsUpDialog.SOLVED_COUNT, mSolvedCount);
		newFragment.setArguments(args);
		newFragment.show(getFragmentManager(), "timeisup");
	}

	@Override
	public void onTimeIsUpDialogShareClick(TimeIsUpDialog dialog) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT,
				String.format(getString(R.string.sharetext), mSolvedCount));
		sendIntent.setType("text/plain");
		startActivity(sendIntent);
		resetTimer();
	}

	@Override
	public void onTimeIsUpDialogSaveScoreClick(TimeIsUpDialog dialog) {
		resetTimer();
		saveScore();
	}

	@Override
	public void onTimeIsUpDialogCloseClick(TimeIsUpDialog dialog) {
		resetTimer();
	}

	/**
	 * Show overlay to prevent user unintentionally click "Time is up" dialog's
	 * buttons while trying to click number buttons in the final seconds.
	 */
	private void showPleaseWait() {
		mRlPleaseWait.setVisibility(View.VISIBLE);
		mHandler.postDelayed(new Runnable() {
			public void run() {
				mRlPleaseWait.setVisibility(View.INVISIBLE);
				showTimeIsUpDialog();
			}
		}, 1500);
	}

	/**
	 * Show "Paused" dialog.
	 */
	private void showPausedDialog() {
		PausedDialog newFragment = new PausedDialog();
		newFragment.show(getFragmentManager(), "paused");
	}

	@Override
	public void onPausedDialogResumeClick(PausedDialog dialog) {
		startTimer();

	}

	@Override
	public void onPausedDialogCloseClick(PausedDialog dialog) {
		resetTimer();
	}

	/**
	 * Our implementation for sign out with sign in status storing added.
	 */
	protected void signOut() {
		super.signOut();
		storeSignedIn(false);
		updateNavigationDrawer();
	}

	/**
	 * Google API client connect callbacks.
	 */
	@Override
	public void onSignInFailed() {
		storeSignedIn(false);

		updateNavigationDrawer();
	}

	@Override
	public void onSignInSucceeded() {
		// If user went to sign in while wanting to save score, save it after
		// user signs in
		if (mSaveScoreAfterSignIn) {
			doSaveScore();
			mSaveScoreAfterSignIn = false;
		}

		updateNavigationDrawer();

		storeSignedIn(true);
	}

	/**
	 * Load/save previous SignedIn status.
	 * 
	 */
	private boolean wasSignedIn() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		return settings.getBoolean(FIELD_SIGNEDIN, false);
	}

	private void storeSignedIn(boolean status) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(FIELD_SIGNEDIN, status);
		editor.commit();
	}
}
