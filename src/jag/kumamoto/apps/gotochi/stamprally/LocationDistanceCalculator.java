package jag.kumamoto.apps.gotochi.stamprally;

import android.util.FloatMath;


/**
 * 
 * 経緯度をもとにロケーション間の距離を計測する
 * 
 * @author aharisu
 *
 */
public final class LocationDistanceCalculator {
		
	private static final class DataStore {
		public float latSeconds;
		public float latitude;
		public float longitude;
	}
	
	private float mLatSeconds;
	private float mLatOrigin;
	private float mLongiOrigin;
	
	private final DataStore mDataStore = new DataStore();
	
	public LocationDistanceCalculator(int latitudeE6, int longitudeE6) {
		this(latitudeE6 * 1e-6f, longitudeE6 * 1e-6f);
	}
	
	public LocationDistanceCalculator(float latitude, float longitude) {
		calclation(latitude, longitude, mDataStore);
		
		mLatSeconds = mDataStore.latSeconds;
		mLatOrigin = mDataStore.latitude;
		mLongiOrigin = mDataStore.longitude;
	}
	
	private void calclation(float latitude, float longitude, DataStore ret) {
		int latDegree = (int)latitude;
		latitude -= latDegree;
		latitude *= 60;
		
		int latMinute = (int)latitude;
		latitude -= latMinute;
		latitude *= 60;
		
		float latSecond = latitude;
		
		int longiDegree = (int)longitude;
		longitude -= longiDegree;
		longitude *= 60;
		
		int longiMinute = (int)longitude;
		longitude -= longiMinute;
		longitude *= 60;
		
		float longiSecond = longitude;
		
		
		ret.latSeconds = latDegree * 60 * 60 + latMinute * 60 + latSecond;
		ret.latitude = ret.latSeconds / 3600 * (float)Math.PI / 180;
		ret.longitude = (longiDegree * 60 * 60 + longiMinute * 60 + longiSecond) / 3600 * (float)Math.PI / 180;
	}
	
	
	/**
	 * 
	 * @param latitudeE6
	 * @param longitudeE6
	 * @return 基準点との距離.単位はm(メーター).
	 */
	public float calcDistance(int latitudeE6, int longitudeE6) {
		return calcDistance(latitudeE6 * 1e-6f, longitudeE6 * 1e-6f);
	}
	
	/**
	 * 
	 * @param latitude
	 * @param longitude
	 * @return 基準点との距離.単位はm(メーター).
	 */
	public float calcDistance(float latitude, float longitude) {
		calclation(latitude, longitude, mDataStore);
		
		float latO = (mLatSeconds + mDataStore.latSeconds) / 2 / 3600 * (float)Math.PI / 180;
		
		float tmp1 = (mLongiOrigin - mDataStore.longitude) * FloatMath.cos(latO);
		tmp1 = tmp1 * tmp1; //tmp1 ^ 2;
		float tmp2 = mLatOrigin - mDataStore.latitude;
		tmp2 = tmp2 * tmp2; //tmp2 ^ 2;
		
		return FloatMath.sqrt(tmp1 + tmp2) * 6370 * 1000;
	}
	
	public static float calcDistanec(float latitude1, float longitude1,
			float latitude2, float longitude2) {
		LocationDistanceCalculator calc = new LocationDistanceCalculator(latitude1, longitude1);
		
		return calc.calcDistance(latitude2, longitude2);
	}
			

}
