package jag.kumamoto.apps.StampRally;

import android.content.Context;
import android.location.Location;

import com.google.android.maps.MapView;

public final class MyLocationOverlay extends com.google.android.maps. MyLocationOverlay{
	public static interface LocationChangeListener {
		public void onLocationChanged(Location location);
	}
	
	private final LocationChangeListener mListener;
	
	
	public MyLocationOverlay(Context context, MapView view, LocationChangeListener listener) {
		super(context, view);
		
		this.mListener = listener;
	}
	
	@Override public synchronized void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		
		if(mListener != null) {
			mListener.onLocationChanged(location);
		}
	}

}
