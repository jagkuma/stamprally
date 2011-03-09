package jag.kumamoto.apps.StampRally;

import jag.kumamoto.apps.StampRally.Data.StampPin;
import jag.kumamoto.apps.StampRally.IApproachPinCallback;

interface IArriveWatcherService {
	void showArriveNotification(in StampPin pin);
	void removeArriveNotification(long pinId);
	long[] getArrivedStampPins();
	void changeArriveCheckInterval(int type);
	
	void registerApproachCallback(in IApproachPinCallback callback);
	void unregisterApproachCallback(in IApproachPinCallback callback);
}