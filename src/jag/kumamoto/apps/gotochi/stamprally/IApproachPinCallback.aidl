package jag.kumamoto.apps.gotochi.stamprally;

import jag.kumamoto.apps.gotochi.stamprally.Data.StampPin;

oneway interface IApproachPinCallback {
	void onApproachPin(in StampPin pin, in int distanceInMeter);
}