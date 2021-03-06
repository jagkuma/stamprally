package jag.kumamoto.apps.gotochi.stamprally;

import jag.kumamoto.apps.gotochi.stamprally.WebAPI.TwitterTimelineGetter;
import aharisu.mascot.BitmapLoader;
import aharisu.mascot.IMascot;
import aharisu.mascot.Mascot;
import aharisu.mascot.MascotEvent;
import aharisu.mascot.MascotView;
import aharisu.mascot.StateRandomWalk;
import aharisu.mascot.StateRepetition;
import aharisu.mascot.StateTimeZoneRepetition;
import aharisu.mascot.TimeZoneState;
import aharisu.mascot.StateUserInteractionCallback;
import aharisu.mascot.UserInteractionState;
import aharisu.util.ImageUtill;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

public final class MapActivityMascotViewHelper {
	private class TimeLineUpdater extends AsyncTask<Void, Void, Void> {
		private long mNextUpdateTime = 0;

		@Override
		protected Void doInBackground(Void... params) {
			while (!isCancelled()) {
				if (mNextUpdateTime < System.currentTimeMillis()) {
					synchronized (mTimeLineGetter) {
						mTimeLineGetter.requestTweet(false, false);
					}
					mNextUpdateTime = getNextUpdateTime();
				}
				
				android.os.SystemClock.sleep(5 * 1000); 
			}


			return null;
		}

		private long getNextUpdateTime() {
			return System.currentTimeMillis() + 1 * 60 * 1000;// 現在時刻+1分
		}
	};

	private final MascotView mView;

	private final TwitterTimelineGetter mTimeLineGetter;

	private TimeLineUpdater mUpdater;

	public MapActivityMascotViewHelper(MascotView view) {
		this.mView = view;

		this.mTimeLineGetter = new TwitterTimelineGetter("55kumamon",
				StampRallyPreferences.getKumamonTweetMaxId());

		initializeMascotState();
	}

	private void initializeMascotState() {
		Context context = mView.getContext();
		Resources res = context.getResources();
		IMascot mascot = mView.getMascot();

		// ランダム歩行の基本状態追加
		mView.addBasicState(new StateRandomWalk(mascot, ImageUtill.loadImage(
				res, R.drawable.kumamon, 1024, 1024)));

		// ダブルタップのときのこけるアニメーション
		StateRepetition falling = new StateRepetition(mascot,
				UserInteractionState.Type.DoubleTap,
				new BitmapLoader.DrawableBitmapLoader(res, R.drawable.koke, 3, 1));
		// 一つ導入画像がある
		falling.setNumHeaderFrame(1);
		// 導入画像を以外を3回リピートする
		falling.setNumRepetition(3);
		mView.addUserInteractionState(falling);
		
		//シングルタップでTweetを表示する
		StateUserInteractionCallback singleTap = new StateUserInteractionCallback(mascot,
				UserInteractionState.Type.SingleTap, new StateUserInteractionCallback.Callback() {
					private String mPrevTweet = null;
					@Override public void onAction() {
						
						TwitterTimelineGetter.TweetData data;
						synchronized (mTimeLineGetter) {
							data = mTimeLineGetter.getTweet();
						}
						
						String tweet;
						if(data != null) {
							tweet = data.text;
						} else {
							tweet = mPrevTweet;
						}
						
						if(tweet != null) {
							mView.addMascotEvent(new MascotEvent(MascotEvent.Type.Tweet, tweet));
							mPrevTweet = tweet;
						}
					}
				});
		mView.addUserInteractionState(singleTap);
		
		// テキスト表示状態用の画像を設定
		mView
				.setSpeakStateBitmapLoader(new BitmapLoader.DrawableBitmapLoader(
						res, R.drawable.speak, 7, 1));

		// スクロール中状態用の画像を設定
		mView
				.setScrollStateBitmapLoader(new BitmapLoader.DrawableBitmapLoader(
						res, R.drawable.scroll, 2, 1));

		// 入浴中状態を設定
		StateTimeZoneRepetition bathing = new StateTimeZoneRepetition(mascot,
				TimeZoneState.Type.Evening,
				new BitmapLoader.DrawableBitmapLoader(res, R.drawable.ofuro,2, 1),
					Mascot.Level.Middle, Mascot.Level.Low);
		bathing.setNumRepetition(-1);
		mView.addTimeZoneState(bathing);
		// 昼・夜は入浴状態に移る確率は低い
		mView.addTimeZoneState(bathing.copy(TimeZoneState.Type.Daytime,
				Mascot.Level.Low, Mascot.Level.Middle));
		mView.addTimeZoneState(bathing.copy(TimeZoneState.Type.Night,
				Mascot.Level.Low, Mascot.Level.Low));

		// 睡眠状態を設定
		StateTimeZoneRepetition sleeping = new StateTimeZoneRepetition(mascot,
				TimeZoneState.Type.Night,
				new BitmapLoader.DrawableBitmapLoader(res, R.drawable.sleeping, 3, 1),
					Mascot.Level.Middle, Mascot.Level.Low);
		sleeping.setNumRepetition(-1);
		mView.addTimeZoneState(sleeping);
		// 昼は睡眠状態に移る確率は低い
		mView.addTimeZoneState(sleeping.copy(TimeZoneState.Type.Daytime,
				Mascot.Level.Low, Mascot.Level.Middle));
	}

	public void onResume() {
		mView.start();

		if (mUpdater == null) {
			mUpdater = new TimeLineUpdater();
			mUpdater.execute((Void) null);
		}
	}

	public void onPause() {
		mView.stop();

		if (mUpdater != null) {
			mUpdater.cancel(true);
			mUpdater = null;
		}
	}

	public void onDestroy() {
		//次回起動時に最低一つはtweetを取得できるようにわざと１少ないidを保存しておく
		StampRallyPreferences.setKumamonTweetMaxId(mTimeLineGetter.getMaxId() - 1);
	}

}
