package jag.kumamoto.apps.gotochi.stamprally;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * デバイス設定の確認や設定変更するための機能を持つ。
 * @author aharisu
 *
 */
public final class DeviceCapabilitiesChecker {
	
	/**
	 * ネットワークアクセスが可能かを確認する。
	 * @param context 
	 * @return ネットワークアクセスが可能であればtrue.不可能ならばfalse.
	 */
	public static final boolean isNetworkConnected(Context context) {
		ConnectivityManager manager = (ConnectivityManager)
			context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo info = manager.getActiveNetworkInfo();
		if(info != null) {
			return info.isConnected();
		}
		
		return false;
	}
	
	/**
	 * Wi-Fi設定を変更するアクティビティを起動するインテントを取得
	 */
	public static Intent getWifiSettingsActivityIntent() {
    	Intent intent = new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);
    	//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	
    	return intent;
	}
	
	/**
	 * 位置情報を取得できる状態かを確認する
	 * @param context
	 * @return 位置情報を取得可能であればtrue.不可能ならばfalse.
	 */
	public static final boolean isEnableLocationService(Context context) {
		//位置情報取得はGPSが何かネットワークアクセスが可能であればOK
		return isNetworkConnected(context) || isRunningGPSService(context);
	}
	
	/**
	 * GPSが有効になっているかを確認する。
	 * @param context
	 * @return GPSが有効であればtrue.無効であればfalse.
	 */
	public static final boolean isRunningGPSService(Context context) {
		String gs = android.provider.Settings.Secure.getString(context.getContentResolver(),
				android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		
		return gs.indexOf("gps", 0) >= 0;
	}
	
	/**
	 * GPS設定を変更するアクティビティを起動するインテントを取得
	 */
	public static Intent getGPSSettingsActivityIntent() {
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		return intent;
	}
	
	/**
	 * デバイス設定を変更するアクティビティを起動するインテントを取得
	 */
	public static Intent getSettingsActivityIntent() {
		Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
		//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		return intent;
	}
	
}

