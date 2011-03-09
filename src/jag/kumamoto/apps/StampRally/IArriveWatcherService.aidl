package jag.kumamoto.apps.StampRally;

import jag.kumamoto.apps.StampRally.Data.StampPin;
import jag.kumamoto.apps.StampRally.IApproachPinCallback;
import android.location.Location;

interface IArriveWatcherService {
	void showArriveNotification(in StampPin pin);
	void removeArriveNotification(long pinId);
	long[] getArrivedStampPins();
	void changeArriveCheckInterval(int type);
	void checkArrive();
	void onLocationChanged(in Location location);
	
	void registerApproachCallback(in IApproachPinCallback callback);
	void unregisterApproachCallback(in IApproachPinCallback callback);
}