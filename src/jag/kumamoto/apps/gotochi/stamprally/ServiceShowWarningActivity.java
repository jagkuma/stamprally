package jag.kumamoto.apps.gotochi.stamprally;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * デバイス設定が整っていないときにユーザに対し警告を出すためのActivity.<br/>
 * <br/>
 * {@link GotochiService}からダイアログを表示するために利用される.<br/>
 * ダイアログを表示するためだけに利用されるので、Activityは半透明の背景色で子ビューを持たない.
 * @author aharisu
 *
 */
public class ServiceShowWarningActivity extends Activity{
	public static final String WARNING_NETWORK_DISABLE = "network-warning";
	public static final String WARNING_GPS_DISABLE = "gps-warning";
	//public static final String WARNING_LOCATION_DISABLE = "location-warning"; 
	

	private IArriveWatcherService mArriveWatcher;
	private final ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			synchronized (mConnection) {
				mArriveWatcher = IArriveWatcherService.Stub.asInterface(service);
				
				if(mResetupFlag) {
					mResetupFlag = false;
					//すでに設定画面から戻ってきた後
					//なのでサービスに位置情報の再構築を命令してアクティビティを終了する
					try {
						mArriveWatcher.resetupLocationListener();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					
					ServiceShowWarningActivity.this.finish();
				}
			}
		}
	};
	
	
	private static final int RequestSettings = 1;
	
	private boolean mNetworkDisable;
	private boolean mGPSDisable;
	//private boolean mLocationDisable;
	
	private boolean mResetupFlag = false;
	
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		if(extras == null) {
			finish();
			return;
		}
		
		mNetworkDisable = extras.getBoolean(WARNING_NETWORK_DISABLE, false);
		mGPSDisable = extras.getBoolean(WARNING_GPS_DISABLE, false);
		//mLocationDisable = extras.getBoolean(WARNING_LOCATION_DISABLE, false);
		
		if(!mNetworkDisable && !mGPSDisable) {
			finish();
			return;
		}
		
		startArriveWatcherservice();
		
		showWarningDialog();
	}
	
	private void showWarningDialog() {
		final Context context = this;
		
		//設定の変更を促すダイアログを表示する
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.device_cap_dialog_title);
		builder.setCancelable(false);
		
		//ダイアログ内に表示されるビューを構築する
		LayoutInflater inflater = (LayoutInflater)context.
			getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Resources res = context.getResources();
		
		View layout = inflater.inflate(R.layout.device_cap_dialog, null);
		
		TextView tvwMessage = (TextView)layout.findViewById(R.id_device_cap.tvwMessage);
		if(mNetworkDisable){
			tvwMessage.append(res.getText(R.string.device_cap_network_disable));
			tvwMessage.append("\n");
		}
		if(mGPSDisable) {
			tvwMessage.append(res.getText(R.string.device_cap_gps_disable));
			tvwMessage.append("\n");
		}
		/*
		if(mLocationDisable) {
			tvwMessage.append(res.getText(R.string.device_cap_location_disable));
			tvwMessage.append("\n");
		}
		*/
		
		builder.setView(layout);
		final Dialog dialog = builder.create();
		
		layout.findViewById(R.id_device_cap.btnSettings).setOnClickListener(new View.OnClickListener() {
			
			@Override public void onClick(View v) {
				dialog.dismiss();
				
				Intent intent;
				/*
				if(mLocationDisable || mNetworkDisable&& mGPSDisable) {
					DeviceCapabilitiesChecker.startSettingsActivity(context);
				} else */if(mNetworkDisable) {
					intent=  DeviceCapabilitiesChecker.getWifiSettingsActivityIntent();
				} else {
					intent = DeviceCapabilitiesChecker.getGPSSettingsActivityIntent();
				}
				
				startActivityForResult(intent, RequestSettings);
			}
		});
		
		dialog.show();
	}
	
	
	@Override protected void onDestroy() {
		stopArriveWatcherService();
		
		super.onDestroy();
	}
	
	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == RequestSettings) {
			
			synchronized (mConnection) {
				if(mArriveWatcher != null) {
					try {
						mArriveWatcher.resetupLocationListener();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					
					ServiceShowWarningActivity.this.finish();
				} else {
					mResetupFlag = true;
				}
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void startArriveWatcherservice() {
		Intent intent = new Intent(this, ArriveWatcherService.class);

		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	private void stopArriveWatcherService() {
		mArriveWatcher = null;
		unbindService(mConnection);
	}
	
}

