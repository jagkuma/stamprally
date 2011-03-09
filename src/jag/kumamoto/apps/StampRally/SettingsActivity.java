package jag.kumamoto.apps.StampRally;

import jag.kumamoto.apps.StampRally.Data.User;
import jag.kumamoto.apps.gotochi.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;



/**
 *
 * 設定画面のアクティビティ
 *
 * @author aharisu
 *
 */
public class SettingsActivity extends TabActivity{
	private static final int RequestAccountSetting = 1;
	
	private User mUser;
	
	private UserSettingsHelper mUserSettings;
	private EveryKindSettingsHelper mEveryKindSettings;
	
	private int mChangedType = -1;
	
	private boolean mLoginRequest;
	private boolean mShowRegistration;

	private IArriveWatcherService mArriveWatcher;
	private final ServiceConnection mConnection = new ServiceConnection() {
		
		@Override public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override public void onServiceConnected(ComponentName name, IBinder service) {
			mArriveWatcher = IArriveWatcherService.Stub.asInterface(service);
			if(mChangedType >= 0) {
				try {
					mArriveWatcher.changeArriveCheckInterval(mChangedType);
				} catch(RemoteException e) {
					e.printStackTrace();
				}
				mChangedType = 0;
			}
		}
	};
	
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		boolean isFirstStart = false;
		boolean isShowEverkindSettings = false;
		
		if(extras != null) {
			mUser = extras.getParcelable(ConstantValue.ExtrasUser);
			isFirstStart = extras.getBoolean(ConstantValue.ExtrasFirstSettings, false);
			mLoginRequest = extras.getBoolean(ConstantValue.ExtrasLoginRequest, false);
			isShowEverkindSettings = extras.getBoolean(ConstantValue.ExtrasShowEveryKindSettings, false);
		}
				
		//初回表示タイミングでキーボードを表示させないようにする
		this.getWindow().setSoftInputMode(
				android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.settings);
		
		//初回起動時の場合、ログインではなく新規登録画面を表示するようにする
		mShowRegistration = isFirstStart;
		initTabPages();
		if(isShowEverkindSettings) {
			getTabHost().setCurrentTab(1);
		}

		if(isFirstStart) {
			showFirstSettingsDialog();
		}

		
		//スタンプラリーのピンの到着を監視するサービスとバインドする
		bindArriveWatcherservice();
	}
	
	private void initTabPages() {
		final TabHost tabHost = getTabHost();
		
		TabHost.TabSpec spec;
		
		spec = tabHost.newTabSpec("user_settings");
		spec.setIndicator("ユーザ設定");
		spec.setContent(new TabContentFactory() {
			@Override public View createTabContent(String tag) {
				ViewGroup content = (ViewGroup)((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.settings_user_content, null);
				
				mUserSettings = new  UserSettingsHelper(content, mUser, mShowRegistration,
						new UserSettingsHelper.Callback() {
					
							@Override public void onLogin(User user) {
								mUser = user;
								if(mLoginRequest) {
									Intent intent = new Intent();
									intent.putExtra(ConstantValue.ExtrasUser, user);
									setResult(Activity.RESULT_OK, intent);
									finish();
								} else if(mEveryKindSettings != null) {
									mEveryKindSettings.setUser(user);
								}
							}
							
							@Override public void onLogout() {
								if(mEveryKindSettings != null) {
									mUser = null;
									mEveryKindSettings.setUser(null);
								}
							}
							
							@Override public void gotoAccountSettngs() {
								startActivityForResult(new Intent(android.provider.Settings.ACTION_SYNC_SETTINGS),
										RequestAccountSetting);
							}
							
							@Override public void onStampPinChanged() {
								if(mArriveWatcher != null) {
									try {
										mArriveWatcher.checkArrive();
									} catch (RemoteException e) {
										e.printStackTrace();
									}
								}
							}
						});
				
				return content;
			}
		});
		tabHost.addTab(spec);
		
		spec = tabHost.newTabSpec("other");
		spec.setIndicator("その他");
		spec.setContent(new TabContentFactory() {
			@Override public View createTabContent(String tag) {
				ViewGroup content = (ViewGroup)((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.settings_everykind_content, null);
						
				mEveryKindSettings = new EveryKindSettingsHelper(
						content,
						mUser,
						new EveryKindSettingsHelper.Callback() {
							
							@Override public void onShowUrgeChanged(boolean bool) {
								StampRallyPreferences.setShowUrgeDialog(bool);
							}
							
							@Override public void onPollingIntervalChanged(int type) {
								StampRallyPreferences.setArrivePollingIntervalType(type);
								if(mArriveWatcher != null) {
									try {
										mArriveWatcher.changeArriveCheckInterval(type);
									} catch(RemoteException e) {
										e.printStackTrace();
									}
								} else {
									mChangedType = type;
								}
							}
							
							@Override public void onStampPinChanged() {
								if(mArriveWatcher != null) {
									try {
										mArriveWatcher.checkArrive();
									} catch (RemoteException e) {
										e.printStackTrace();
									}
								}
							}
						});
				
				return content;
			}
		});
		tabHost.addTab(spec);
		
		tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			
			@Override public void onTabChanged(String tabId) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(tabHost.getWindowToken(), 0);
			}
		});
	}

	private void showFirstSettingsDialog() {
		new AlertDialog.Builder(this)
			.setMessage(
					"　このアプリケーションを始めるにはアカウントの登録が必要です。\n"
					+ "　端末に登録したGoogleアカウントを利用してアカウントを作成します。\n"
					+	"　なお、利用したGoogleアカウントを登録以外の目的で利用することはございません")
			.setPositiveButton("OK", null)
			.show();
	}
	
	@Override protected void onDestroy() {
		//スタンプラリーのピンの到着を監視するサービスをアンバインドする
		unbindArriveWatcherService();
		
		super.onDestroy();
	}

	private void bindArriveWatcherservice() {
		Intent intent = new Intent(this, ArriveWatcherService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	private void unbindArriveWatcherService() {
		unbindService(mConnection);
	}
	
	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == RequestAccountSetting) {
			mUserSettings.updateView();
			
			return;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
}
