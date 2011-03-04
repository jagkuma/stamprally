package jag.kumamoto.apps.StampRally;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import jag.kumamoto.apps.StampRally.Data.StampPin;
import jag.kumamoto.apps.StampRally.Data.StampRallyURL;
import jag.kumamoto.apps.StampRally.Data.User;
import jag.kumamoto.apps.gotochi.R;
import aharisu.mascot.BitmapLoader;
import aharisu.mascot.IMascot;
import aharisu.mascot.Mascot;
import aharisu.mascot.MascotView;
import aharisu.mascot.StateRandomWalk;
import aharisu.mascot.StateRepetition;
import aharisu.mascot.StateTimeZoneRepetition;
import aharisu.mascot.TimeZoneState;
import aharisu.mascot.UserInteractionState;
import aharisu.util.DataGetter;
import aharisu.util.ImageUtill;
import aharisu.util.Pair;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SlidingDrawer;
import android.widget.ZoomControls;


/**
 * 
 * スタンプラリーのチェックポイントを含むマップを表示するアクティビティ
 * 
 * @author aharisu
 *
 */
public class MapActivity extends com.google.android.maps.MapActivity{
	
	private StampPinOverlay mPinOverlay;
	private StampPin[] mStampPins;
	private User mUser;
	
	private boolean mIsOpenSlidingDrawer = false;
	
	private IArriveWatcherService mArriveWatcher;
	private final ServiceConnection mConnection = new ServiceConnection() {
		
		@Override public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override public void onServiceConnected(ComponentName name, IBinder service) {
			mArriveWatcher = IArriveWatcherService.Stub.asInterface(service);
		}
	};
	
	
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			mUser = extras.getParcelable(ConstantValue.ExtrasUser);
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.map);
		
		
		final MapView map = (MapView)findViewById(R.id_map.mapview);
		
		//ズームボタンの表示と大きさ調整
		DisplayMetrics metrics = new DisplayMetrics();
		((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
		map.setBuiltInZoomControls(true);
		ZoomControls zoomCtrl = (ZoomControls)map.getZoomButtonsController().getZoomControls();
		ViewGroup.LayoutParams params = zoomCtrl.getLayoutParams();
		params.width = ViewGroup.LayoutParams.FILL_PARENT;
		for(int i = 0;i < zoomCtrl.getChildCount();++i) {
			zoomCtrl.getChildAt(i).getLayoutParams().width = (int)(62 * metrics.scaledDensity);
		}
		zoomCtrl.setGravity(Gravity.BOTTOM|Gravity.RIGHT);
		zoomCtrl.setPadding((int)(2 * metrics.scaledDensity), 0, 0, 0);
		 
		
		List<Overlay> overlayList = map.getOverlays();
		
		//スタンプラリーの場所を示すピンのレイヤを追加
		mPinOverlay = new StampPinOverlay(this,
				getResources().getDrawable(R.drawable.marker_none),
				map);
		overlayList.add(mPinOverlay);
		
		
		//ピンの情報を示すレイヤを追加
		PinInfoOverlay infoOverlay = new PinInfoOverlay(createPinInfoOnClickListener(),
				mPinOverlay, map, getResources().getDrawable(R.drawable.marker_none));
		overlayList.add(infoOverlay);
		mPinOverlay.setInfoOverlay(infoOverlay);
		
		
		//DBからスタンプがある場所のピンデータを取得する
		GetAsyncStampPinsFromDB();
		
		
		//スライディングドローワの設定
		SlidingDrawer drawer = (SlidingDrawer)findViewById(R.id_map.slidingdrawer);
		drawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
			@Override public void onDrawerOpened() {
				mIsOpenSlidingDrawer = true;
				
				//設定ビューが開いている間はマップを動かせないようにする
				map.setClickable(false);
			}
		});
		drawer.setOnDrawerScrollListener(new SlidingDrawer.OnDrawerScrollListener() {
			
			@Override public void onScrollStarted() {
				//開き始めるとズームボタンを消す
				map.getZoomButtonsController().setVisible(false);
			}
			
			@Override public void onScrollEnded() {
			}
		});
		drawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
			@Override public void onDrawerClosed() {
				mIsOpenSlidingDrawer = false;
				
				//設定ビューが閉じるとマップを動かせるようにする
				map.setClickable(true);
			}
		});
		
		constractOptionView();
		
		//マスコットの状態を初期化する
		initializeMascotState();
		
		//スタンプラリーのピンの到着を監視するサービスとバインドする
		bindArriveWatcherservice();
	}
	
	private void GetAsyncStampPinsFromDB() {
		new AsyncTask<Void, Void, StampPin[]>() {
			
			@Override protected StampPin[] doInBackground(Void... params) {
				return StampRallyDB.getStampPins();
			}
			
			
			@Override protected void onPostExecute(StampPin[] result) {
				
				mStampPins = result == null ? new StampPin[0] : result;
				
				mPinOverlay.addStampPins(result);
				((MapView)findViewById(R.id_map.mapview)).invalidate();
				
				if(isCheckUpdate()) {
					GetAsyncStampPinsFromServer();
					StampRallyPreferences.setLastCheckDateStampPin(System.currentTimeMillis());
				}
			}
		}.execute((Void)null);
	}
	
	private void GetAsyncStampPinsFromServer() {
		new AsyncTask<Void, Void, Pair<StampPin[], Pair<StampPin[], StampPin[]>>>() {
			
			@Override protected Pair<StampPin[], Pair<StampPin[], StampPin[]>>doInBackground(Void... params) {
				StampPin[] pins = null;
				try {
					JSONObject obj = DataGetter.getJSONObject(StampRallyURL.getGetAllPinQuery());
					if(StampRallyURL.isSuccess(obj)) {
						pins = StampPin.decodeJSONObject(obj);
					} else {
						//XXX サーバとの通信失敗(クエリの間違い?)
						Log.e("get pins", obj.toString());
					}
				} catch (IOException e1) {
					//XXX ネットワーク通信の失敗
					e1.printStackTrace();
				} catch (JSONException e1) {
					//XXX JSONフォーマットが不正
					e1.printStackTrace();
				}
				
				if(pins == null) {
					pins = new StampPin[0];
				}
				
				return new Pair<StampPin[], Pair<StampPin[], StampPin[]>>(pins,
						StampPin.extractNewAndDeletePins(mStampPins, pins));
			}
			
			
			@Override protected void onPostExecute(Pair<StampPin[], Pair<StampPin[], StampPin[]>> result) {
				if(result == null)
					return;
				
				mStampPins = result.v1;
				
				StampRallyDB.deleteStampPins(result.v2.v2);
				StampRallyDB.insertStampPins(result.v2.v1);
				
				mPinOverlay.removeStampPins(result.v2.v2);
				mPinOverlay.addStampPins(result.v2.v1);
				
				if((result.v2.v1 != null &&  result.v2.v1.length != 0) ||
						(result.v2.v2 != null &&  result.v2.v2.length != 0)) {
					((MapView)findViewById(R.id_map.mapview)).invalidate();
				}
			}
			
		}.execute((Void)null);
	}
	
	private void constractOptionView() {
		final class Data {
			public final int radioGroupId;
			public final int checkBoxId;
			public final int[] radioButtonIds;
			public final StampPinOverlay.Filter[] filters;
			
			public Data(int radioGroupId, int checkBoxId, int[] radioButtonIds, StampPinOverlay.Filter[] filters) {
				this.radioGroupId = radioGroupId;
				this.checkBoxId = checkBoxId;
				this.radioButtonIds = radioButtonIds;
				this.filters = filters;
			}
		}
		
		Data[] datas = new Data[] {
				//訪れたか否か
				new Data(R.id_map.show_marker_alt_visit, R.id_map.alt_visite_check, 
						new int[] {R.id_map.radio_alt_visit_no_visite, R.id_map.radio_alt_visit_visited},
						new StampPinOverlay.Filter[] {
							//訪れたことがないピンを表示
							new StampPinOverlay.Filter() {
								
								@Override public boolean filter(StampPin pin) {
									return !pin.isArrive;
								}
							},
							//訪れたことがあるピンを表示
							new StampPinOverlay.Filter() {
								
								@Override public boolean filter(StampPin pin) {
									return pin.isArrive;
								}
							},
				}),
				
				//スタンプのタイプ
				new Data(R.id_map.show_marker_alt_type, R.id_map.alt_type_check, 
						new int[] {R.id_map.radio_alt_type_stamp, R.id_map.radio_alt_type_quiz},
						new StampPinOverlay.Filter[] {
							//スタンプだけのピンを表示
							new StampPinOverlay.Filter() {
								
								@Override public boolean filter(StampPin pin) {
									return pin.type == StampPin.STAMP_TYPE_NONE;
								}
							},
							//クイズのあるピンを表示
							new StampPinOverlay.Filter() {
								
								@Override public boolean filter(StampPin pin) {
									return pin.type == StampPin.STAMP_TYPE_QUIZ;
								}
							},
				}),
		};
		
		for(final Data data : datas) {
			RadioGroup altGroup = (RadioGroup)findViewById(data.radioGroupId);
			altGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				@Override public void onCheckedChanged(RadioGroup group, int checkedId) {
					boolean show = false;
					int count = data.radioButtonIds.length - 1;
					int i;
					for(i = 0;i < count;++i) {
						if(checkedId == data.radioButtonIds[i] && 
								((RadioButton)group.findViewById(checkedId)).isChecked()) {
							show = true;
							mPinOverlay.addShowPinFilter(data.filters[i], false);
						} else {
							mPinOverlay.removeShowPinFilter(data.filters[i], false);
						}
					}
					
					if(checkedId == data.radioButtonIds[i] &&
								((RadioButton)group.findViewById(checkedId)).isChecked()) {
						show = true;
						mPinOverlay.addShowPinFilter(data.filters[i], true);
					} else {
						mPinOverlay.removeShowPinFilter(data.filters[i], true);
					}
					
					if(show) {
						((CheckBox)group.findViewById(data.checkBoxId)).setChecked(true);
					}
				}
			});
			
			CheckBox chkEnable = (CheckBox)altGroup.findViewById(data.checkBoxId);
			chkEnable.setTag(altGroup);
			chkEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				
				@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(!isChecked) {
						((RadioGroup)buttonView.getTag()).clearCheck();
					}
				}
			});
			
		}
	}
		
	
	
	/**
	 * 前回アップデート確認をしたときから日付が変わっているかを確認する
	 * @return アップデートが必要であればtrue.不必要ならfalse.
	 */
	private boolean isCheckUpdate() {
		Date last = new Date(StampRallyPreferences.getLastCheckDateStampPin());
		Date now = new Date(System.currentTimeMillis());

		return last.getYear() != now.getYear() ||
				last.getMonth() != now.getMonth() ||
				last.getDate() != now.getDate();
	}
	
	
	private PinInfoOverlay.OnClickListener createPinInfoOnClickListener() {
		return new PinInfoOverlay.OnClickListener() {
			@Override public void onClick(StampPin pin) {
				
				Intent intent = new Intent(MapActivity.this, LocationInfoActivity.class);
				intent.putExtra(ConstantValue.ExtrasStampPin, pin);
				
				boolean isArrived = isShowGoQuiz(pin);
				intent.putExtra(ConstantValue.ExtrasIsArrive, isArrived);
				
				if(pin.type == StampPin.STAMP_TYPE_QUIZ && isArrived) {
					intent.putExtra(ConstantValue.ExtrasShowGoQuiz, true);
				}
				
				if(mUser != null) {
					intent.putExtra(ConstantValue.ExtrasUser, mUser);
				}
				
				startActivity(intent);
			}
		};
	}
	
	
	private boolean isShowGoQuiz(StampPin pin) {
		if(mArriveWatcher != null) {
			try {
				long[] ids = mArriveWatcher.getArrivedStampPins();
				
				return Arrays.binarySearch(ids, pin.id) >= 0;
			} catch(RemoteException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	private void initializeMascotState() {
		MascotView view = (MascotView)findViewById(R.id_map.mascot);
		IMascot mascot = view.getMascot();
		
		//ランダム歩行の基本状態追加
		view.addBasicState(new StateRandomWalk(mascot, 
					ImageUtill.loadImage(getResources().openRawResource(R.raw.kumamon), 1024, 1024)));
		
		//ダブルタップのときのこけるアニメーション
		StateRepetition falling = new StateRepetition(mascot, UserInteractionState.Type.DoubleTap, 
				new BitmapLoader.RawResourceBitmapLoader(this, R.raw.koke, 3, 1));
		//一つ導入画像がある
		falling.setNumHeaderFrame(1);
		//導入画像を以外を3回リピートする
		falling.setNumRepetition(3);
		view.addUserInteractionState(falling);
		
		//テキスト表示状態用の画像を設定
		view.setSpeakStateBitmapLoader(new BitmapLoader.RawResourceBitmapLoader(this, R.raw.speak, 5, 1));
		
		//スクロール中状態用の画像を設定
		view.setScrollStateBitmapLoader(new BitmapLoader.RawResourceBitmapLoader(this, R.raw.scroll, 2, 1));
		
		//入浴中状態を設定	
		StateTimeZoneRepetition bathing = new StateTimeZoneRepetition(mascot, TimeZoneState.Type.Evening,
				new BitmapLoader.RawResourceBitmapLoader(this, R.raw.ofuro, 2, 1),
				Mascot.Level.Middle, Mascot.Level.Low);
		bathing.setNumRepetition(-1);
		view.addTimeZoneState(bathing);
		//昼・夜は入浴状態に移る確率は低い
		view.addTimeZoneState(bathing.copy(TimeZoneState.Type.Daytime,
				Mascot.Level.Low, Mascot.Level.Middle));
		view.addTimeZoneState(bathing.copy(TimeZoneState.Type.Night,
				Mascot.Level.Low, Mascot.Level.Low));
		
		//睡眠状態を設定
		StateTimeZoneRepetition sleeping = new StateTimeZoneRepetition(mascot, TimeZoneState.Type.Night,
				new BitmapLoader.RawResourceBitmapLoader(this, R.raw.sleeping, 3, 1),
				Mascot.Level.Middle, Mascot.Level.Low);
		sleeping.setNumRepetition(-1);
		view.addTimeZoneState(sleeping);
		//昼は睡眠状態に移る確率は低い
		view.addTimeZoneState(sleeping.copy(TimeZoneState.Type.Daytime,
				Mascot.Level.Low, Mascot.Level.Middle));
	}
	
	
	@Override protected boolean isRouteDisplayed() {
		return false;
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
	
	@Override protected void onResume() {
		super.onResume();
		
		((MascotView)findViewById(R.id_map.mascot)).start();
	}
	
	@Override protected void onPause() {
		super.onPause();
		
		((MascotView)findViewById(R.id_map.mascot)).stop();
	}
	
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(mIsOpenSlidingDrawer && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
    		((SlidingDrawer)findViewById(R.id_map.slidingdrawer)).animateClose();
    		
    		return true;
    	}
    	
    	return super.onKeyDown(keyCode, event);
    }
	
}
