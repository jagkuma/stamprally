package jag.kumamoto.apps.StampRally;

import jag.kumamoto.apps.StampRally.Data.StampPin;

oneway interface IApproachPinCallback {
	void onApproachPin(in StampPin pin, in int distanceInMeter);
}