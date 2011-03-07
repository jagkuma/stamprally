package aharisu.mascot;


import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import aharisu.mascot.MascotEvent.Type;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;


/**
 * 
 * ビュー内を自由に動き回るマスコットの動作させるクラス
 * 
 * @author aharisu
 *
 */
public final class Mascot extends IMascot{
	
	public enum Level {
		High(2),
		Middle(1),
		Low(0);
		
		public final int  number;
		
		private Level(int number) {
			this.number = number;
		}
	};
	
	
	private final Handler mHandler = new Handler();
	private final MascotView.ShowMascotView mView;
	
	private final Queue<MascotEvent> mEventQueue = new LinkedList<MascotEvent>();
	
	private final ArrayList<MascotState> mBasicStateList = new ArrayList<MascotState>();
	private final ArrayList<UserInteractionState> mInteractionStateList = new ArrayList<UserInteractionState>();
	private final ArrayList<TimeZoneState> mTimeZoneStateList = new ArrayList<TimeZoneState>();
	private final StateSpeak mSpeakState = new StateSpeak(this);
	private final StateScroll mScrollState = new StateScroll(this);
	
	private MascotState mCurState;
	
	private Random mRand = new Random();
	
	private Object mSyncUpdateObj = new Object();
	private boolean mStateChange = false;
	
	private final Runnable mUpdate = new Runnable() {
		
		@Override public void run() {
			
			synchronized (mSyncUpdateObj) {
				if(!mIsStarted) {
					return;
				}
				
				if(mStateChange) {
					mStateChange = false;
					transition(getNextState());
				} else if(mCurState.isAllowInterrupt()) {
					MascotEvent event = mEventQueue.poll();
					if(event != null) {
						if((event.type == Type.Text  || event.type == Type.Tweet)
								&& mSpeakState.isEnable()) {
							
							mSpeakState.setEventType(event.type);
							mSpeakState.setText(event.text);
							forceTransition(mSpeakState);
						}
					} else if(mStateChange || isExist(mCurState)) {
						//今の状態と次の状態が同じ場合もある
						//その場合は何も起きない
						transition(getNextState());
					}
				}
				
				
				if(mCurState.update()) {
					mHandler.postDelayed(mUpdate, mCurState.getUpdateInterval());
				}
			}
		}
		
		private MascotState getNextState() {
			int max = 0;
			
			int[] priority = new int[]{1, 3, 7};
			
			for(MascotState state : mBasicStateList) {
				max += priority[state.getEntryPriority().number];
			}
			
			TimeZoneState.Type timezone = getCurrentTimeZone();
			for(TimeZoneState state : mTimeZoneStateList) {
				if(timezone == state.getTimeZoneType()) {
					max += priority[state.getEntryPriority().number];
				}
			}
			
			int number = mRand.nextInt(max - 1) + 1;
			
			for(MascotState state : mBasicStateList) {
				number -= priority[state.getEntryPriority().number];
				if(number <= 0)
					return state;
			}
			
			for(TimeZoneState state : mTimeZoneStateList) {
				if(timezone == state.getTimeZoneType()) {
					number -= priority[state.getEntryPriority().number];
					if(number <= 0) 
						return state;
				}
			}
			
			//ここは本来は実行されない
			Log.e("failure", "transition failure");
			return mBasicStateList.get(0);
		}
		
		private boolean isExist(MascotState state) {
			if(!state.isAllowExist())
				return false;
			
			int prob = mCurState.getExistProbability().number;
			return mRand.nextInt(99) < (new int[]{3, 10, 30})[prob];
		}
		
	};
	
	private boolean mIsStarted = false;
	
	public Mascot(MascotView.ShowMascotView view) {
		this.mView = view;
	}
	
	public void addEvent(MascotEvent event) {
		mEventQueue.offer(event);
	}
	
	public void addBasicState(MascotState state) {
		mBasicStateList.add(state);
	}
	
	public void addUserInteractionState(UserInteractionState state) {
		mInteractionStateList.add(state);
	}
	
	public void addTimeZoneState(TimeZoneState state) {
		mTimeZoneStateList.add(state);
	}
	
	/**
	 * @exception IllegalArgumentException
	 * @param loader
	 */
	public void setSpeakStateBitmapLoader(BitmapLoader loader) {
		mSpeakState.setLoader(loader);
	}
	
	/**
	 * @exception IllegalArgumentException
	 * @param loader
	 */
	public void setScrollStateBitmapLoader(BitmapLoader loader) {
		mScrollState.setLoader(loader);
	}
	
	public void draw(Canvas canvas) {
		if(mIsStarted) {
			mCurState.draw(canvas);
		}
	}
	
	public boolean hitTest(int x, int y) {
		if(mIsStarted) {
			return mCurState.hitTest(x, y);
		}
		
		return false;
	}
	
	/**
	 * @throws IllegalStateException BasicStateを一つも持っていないとき
	 */
	public void start() {
		if(mBasicStateList.size() == 0) {
			throw new IllegalStateException("基本状態が一つもありません");
		}
		
		mCurState = mBasicStateList.get(0);
		mHandler.postDelayed(mUpdate, mCurState.getUpdateInterval());
		
		mIsStarted = true;
	}
	
	public void stop() {
		mIsStarted = false;
	}
	
	public void onLongPress() {
		onUserInteraction(UserInteractionState.Type.LongPress);
	}
	
	public void onSingleTap() {
		onUserInteraction(UserInteractionState.Type.SingleTap);
	}
	
	public void onDoubleTap() {
		onUserInteraction(UserInteractionState.Type.DoubleTap);
	}
	
	public void onScroll(float distanceX, float distanceY) {
		synchronized (mSyncUpdateObj) {
			if(!mCurState.isAllowInterrupt()) {
				return;
			}
			
			if(mScrollState.isEnable() && mCurState != mScrollState) {
				forceTransition(mScrollState);
			}
			
			//これ以降はmCurState == mScrollStateが保証されている
			mScrollState.move(distanceX, distanceY);
		}
	}
	
	public void onFling(float velocityX, float velocityY) {
		onUserInteraction(UserInteractionState.Type.Fling);
	}
	
	public void onScrollEnd() {
		stateChange();
	}
	
	private void onUserInteraction(UserInteractionState.Type action) {
		synchronized (mSyncUpdateObj) {
			if(!mCurState.isAllowInterrupt()) {
				return;
			}
			
			UserInteractionState state = getUserInteractionState(action);
			if(state != null) {
				mHandler.removeCallbacks(mUpdate);
				mStateChange = false;
				
				transition(state);
				
				state.update();
				
				mHandler.postDelayed(mUpdate, state.getUpdateInterval());
			}
		}
	}
	
	private UserInteractionState getUserInteractionState(UserInteractionState.Type action) {
		int max = 0;
		
		int[] priority = new int[] {1, 3, 7};
		
		for(UserInteractionState state : mInteractionStateList) {
			if(state.getActionType() == action) {
				max += priority[state.getEntryPriority().number];
			}
		}
		
		if(max == 0)
			return null;
		
		int number = mRand.nextInt(max - 1) + 1;
		for(UserInteractionState state : mInteractionStateList) {
			if(state.getActionType() == action) {
				number -= priority[state.getEntryPriority().number];
				if(number <= 0)
					return state;
			}
		}
		
		throw new RuntimeException("get user insteraction failure");
	}
	
	private TimeZoneState.Type getCurrentTimeZone() {
		 int hours = new Date(System.currentTimeMillis()).getHours();
		 if(5 <= hours && hours < 10) {
			 return TimeZoneState.Type.Morning;
		 } else if(10 <= hours && hours < 17) {
			 return TimeZoneState.Type.Daytime;
		 } else if(17 <= hours && hours < 22) {
			 return TimeZoneState.Type.Evening;
		 } else {
			 return TimeZoneState.Type.Night;
		 }
	}
	
	private void transition(MascotState nextState) {
		if(mCurState != nextState) {
			forceTransition(nextState);
		}
	}
	
	private void forceTransition(MascotState nextState) {
		Rect bounds = new Rect();
		mCurState.getBounds(bounds);
		
		mCurState.exist();
		
		mCurState = nextState;
		
		Rect entryBounds =  new Rect(bounds);
		mCurState.entry(entryBounds);
		
		//前の状態の領域を消去
		mView.redraw(bounds.left, bounds.top, bounds.right, bounds.bottom);
	}
	
	@Override int getViewHeight() {
		return mView.getHeight();
	}
	
	@Override int getViewWidth() {
		return mView.getWidth();
	}
	
	@Override void redraw(int left, int top, int right, int bottom) {
		mView.redraw(left, top, right, bottom);
	}
	
	@Override void stateChange() {
		synchronized (mSyncUpdateObj) {
			mHandler.removeCallbacks(mUpdate);
			
			mStateChange = true;
			
			mUpdate.run();
		}
	}
	
	@Override void showText(String text, Rect mascotBounds) {
		mView.showText(text, mascotBounds);
	}
	
	@Override void hideText() {
		mView.hideText();
	}

}
