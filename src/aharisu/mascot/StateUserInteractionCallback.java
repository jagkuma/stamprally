package aharisu.mascot;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class StateUserInteractionCallback extends UserInteractionState{
	public static interface Callback {
		public void onAction();
	}
	
	private final Callback mCallback;
	private final Rect mBounds = new Rect();
	
	public StateUserInteractionCallback(IMascot mascot, Type actionType, Callback callback) {
		super(mascot, actionType);
		
		if(callback == null) {
			throw new NullPointerException("callback is nulll");
		}
		
		this.mCallback = callback;
	}
	
	@Override public void getBounds(Rect outRect) {
		outRect.set(mBounds);
	}
	
	@Override public Bitmap getImage() {
		return null;
	}
	
	@Override public boolean isAllowExist() {
		return true;
	}
	
	@Override public boolean isAllowInterrupt() {
		return true;
	}
	
	@Override public int getUpdateInterval() {
		return 1;
	}
	
	@Override public boolean update() {
		mCallback.onAction();
		mMascot.stateChange();
		
		return false;
	}
	
	@Override public void entry(Rect bounds) {
		mBounds.set(bounds);
	}

}
