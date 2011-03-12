package jag.kumamoto.apps.gotochi.stamprally;

import jag.kumamoto.apps.gotochi.stamprally.Data.User;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

/**
 * 
 * スタンプラリーのホームになるアクティビティ
 * 
 * @author aharisu
 * 
 */
public class HomeActivity extends Activity {
	private static final int RequestFirstStartSettings = 1;

	private final ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 最初にDBのシングルトンインスタンスを作成する
		StampRallyDB.createInstance(getApplicationContext());

		// プリファレンスクラスにコンテキストを設定する
		StampRallyPreferences.setContext(getApplicationContext());

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.home);

		// ヘルプ画面へ遷移
		findViewById(R.id_home.help).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(HomeActivity.this,
								HelpActivity.class);
						User user = StampRallyPreferences.getUser();
						if (user != null) {
							intent.putExtra(ConstantValue.ExtrasUser, user);
						}
						startActivity(intent);
					}
				});

		// Special Thanks画面へ遷移
		findViewById(R.id_home.thanks).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(HomeActivity.this,
								ThanksActivity.class);

						startActivity(intent);
					}
				});

		// マップ画面へ遷移
		findViewById(R.id_home.map).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						gotoStampRallyMap();
					}
				});

		// 設定画面へ遷移
		findViewById(R.id_home.settings).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(HomeActivity.this,
								SettingsActivity.class);
						User user = StampRallyPreferences.getUser();
						if (user != null) {
							intent.putExtra(ConstantValue.ExtrasUser, user);
						}
						startActivity(intent);
					}
				});

		// コレクション画面へ繊維
		findViewById(R.id_home.collections).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(HomeActivity.this,
								CollectionsActivity.class);
						User user = StampRallyPreferences.getUser();
						if (user != null) {
							intent.putExtra(ConstantValue.ExtrasUser, user);
						}
						startActivity(intent);
					}
				});
		
		//看板の設定
		View signboard = findViewById(R.id_home.signboard);
		signboard.setOnTouchListener(createSignboardOnTouchListener());
		signboard.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				gotoStampRallyMap();
			}
		});

		// 到着確認サービスを開始
		startArriveWatcherservice();
	}

	@Override
	protected void onDestroy() {
		stopArriveWatcherService();

		super.onDestroy();
	}

	private void startArriveWatcherservice() {
		Intent intent = new Intent(this, ArriveWatcherService.class);

		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	private void stopArriveWatcherService() {
		unbindService(mConnection);
	}
	
	private void gotoStampRallyMap() {
		if (StampRallyPreferences.isFirstStampRallyStart()) {
			firstStartAction();
		} else {
			Intent intent = new Intent(HomeActivity.this,
					MapActivity.class);
			User user = StampRallyPreferences.getUser();
			if (user != null) {
				intent.putExtra(ConstantValue.ExtrasUser, user);
			}
			startActivity(intent);
		}
	}

	private void firstStartAction() {
		StampRallyPreferences.setFlagFirstStampRallyStart();

		Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
		intent.putExtra(ConstantValue.ExtrasFirstSettings, true);
		intent.putExtra(ConstantValue.ExtrasLoginRequest, true);
		startActivityForResult(intent, RequestFirstStartSettings);
	}
	
	private View.OnTouchListener createSignboardOnTouchListener() {
		return new View.OnTouchListener() {
				private Drawable mBackground;
			
			@Override public boolean onTouch(View v, MotionEvent event) {
					switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						mBackground = v.getBackground();
						v.setBackgroundColor(0xffadfec4);
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						v.setBackgroundDrawable(mBackground);
						break;
					}
				return false;
			}
		};
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RequestFirstStartSettings:
			User user = null;
			if (resultCode == Activity.RESULT_OK) {
				user = data.getExtras().getParcelable(ConstantValue.ExtrasUser);
			}

			// Mapアクティビティを起動する
			Intent intent = new Intent(HomeActivity.this, MapActivity.class);
			if (user != null) {
				intent.putExtra(ConstantValue.ExtrasUser, user);
			}
			startActivity(intent);
			break;
		}
	}

}
