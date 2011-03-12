package jag.kumamoto.apps.gotochi.stamprally;

import jag.kumamoto.apps.gotochi.stamprally.Data.StampPin;
import jag.kumamoto.apps.gotochi.stamprally.IApproachPinCallback;
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