package jag.kumamoto.apps.gotochi.stamprally;

import java.io.IOException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import jag.kumamoto.apps.gotochi.stamprally.Data.StampPin;
import jag.kumamoto.apps.gotochi.stamprally.Data.StampRallyURL;
import jag.kumamoto.apps.gotochi.stamprally.Data.User;
import aharisu.util.DataGetter;
import aharisu.util.Pair;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

final class EveryKindSettingsHelper {
	public static interface Callback {
		public void onPollingIntervalChanged(int type);
		public void onShowUrgeChanged(boolean bool);
		public void onStampPinChanged();
	}
	
	private final ViewGroup mLayout;
	private User mUser;
	private final Callback mListener;
	
	public EveryKindSettingsHelper(ViewGroup layout, User user, Callback listener) {
		mLayout = layout;
		mUser = user;
		this.mListener = listener;
		
		initializeView();
	}
	
	private void initializeView() {
		RadioGroup pollingGroup = (RadioGroup)mLayout.findViewById(R.id_settings.arrive_polling_group);
		pollingGroup.check(convertPollingTypeToId(StampRallyPreferences.getArrivePollingIntervalType()));
		pollingGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(mListener != null) {
					mListener.onPollingIntervalChanged(convertIdToPollingType(checkedId));
				}
			}
		});
		
		settingStampPinPrevCheckTime(StampRallyPreferences.getLastCheckDateStampPin());
		
		mLayout.findViewById(R.id_settings.check_new_stamp).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				checkNewStampPin();
			}
		});
		
		
		CheckBox chkShowUrge = (CheckBox)mLayout.findViewById(R.id_settings.show_urge);
		chkShowUrge.setChecked(StampRallyPreferences.getShowUrgeDialog());
		if(mUser == null) {
			chkShowUrge.setEnabled(true);
		} else {
			chkShowUrge.setEnabled(false);
		}
		chkShowUrge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(mListener != null) {
					mListener.onShowUrgeChanged(isChecked);
				}
			}
		});
	}
	
	private int convertPollingTypeToId(int type) {
		return type == 0 ? R.id_settings.polling_short :
			type == 2 ? R.id_settings.polling_long :
			R.id_settings.polling_normal;
	}
	
	private int convertIdToPollingType(int id) {
		return id == R.id_settings.polling_short ? 0 :
			id == R.id_settings.polling_long ? 2 :
			1;
	}
	
	private void settingStampPinPrevCheckTime(long time) {
		if(time != 0) {
			((TextView)mLayout.findViewById(R.id_settings.prev_check_time)).setText(
					String.format(mLayout.getContext().getResources().getString(
							R.string.settings_prev_check_time_stamp_pin_format)
							,new Date(time).toLocaleString()));
		} else {
			((TextView)mLayout.findViewById(R.id_settings.prev_check_time)).setText(
					R.string.settings_no_check_stamp_pin);
		}
	}
	
	private void checkNewStampPin() {
		Context context = mLayout.getContext();
		
		final ProgressDialog dialog = new ProgressDialog(context);
		dialog.setMessage(context.getResources().getString(R.string.settings_progress_confirming));
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(false);
		dialog.show();
		
		new AsyncTask<Void, Void, Integer>() {
			@Override protected Integer doInBackground(Void... params) {
				
				try {
					JSONObject obj = DataGetter.getJSONObject(StampRallyURL.getGetAllPinQuery());
					if(StampRallyURL.isSuccess(obj)) {
						StampPin[] serverPins = StampPin.decodeJSONObject(obj);
						obj = null;
						
						//DBからピンを取得
						StampPin[] dbPins = StampRallyDB.getStampPins();
						
						//Server<->DB間の更新情報を更新
						Pair<StampPin[], StampPin[]> extract = StampPin.extractNewAndDeletePins(dbPins, serverPins);
						StampRallyDB.deleteStampPins(extract.v2);
						StampRallyDB.insertStampPins(extract.v1);
						
						if(extract.v1.length > 0 && mListener != null) {
							//新規追加のピンがあれば強制的に到着チェックを行う
							mListener.onStampPinChanged();
						}
						
						//新規追加のピンの数を返す
						return extract.v1.length;
					} else {
						//XXX サーバとの通信失敗(クエリの間違い?)
						Log.e("check pins", obj.toString());
					}
				} catch(IOException e) {
					//XXX ネットワーク通信の失敗
					e.printStackTrace();
				} catch(JSONException e) {
					//XXX JSONフォーマットが不正
					e.printStackTrace();
				}
				
				return null;
			}
			
			@Override protected void onPostExecute(Integer result) {
				dialog.dismiss();
				
				if(result != null) {
					
					long time = System.currentTimeMillis();
					StampRallyPreferences.setLastCheckDateStampPin(time);
					settingStampPinPrevCheckTime(time);
					
					
					if(result > 0) {
						Toast.makeText(mLayout.getContext(), 
								String.format(mLayout.getContext().getResources().getString(
										R.string.settings_get_new_stamp_format), (int)result)
										,Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mLayout.getContext(), 
								R.string.settings_no_new_stamp, Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(mLayout.getContext(), 
							R.string.common_communication_failure, Toast.LENGTH_SHORT).show();
				}
			}
		}.execute((Void)null);
	}
	
	public void setUser(User user) {
		mUser = user;
		
		CheckBox chkShowUrge = (CheckBox)mLayout.findViewById(R.id_settings.show_urge);
		if(mUser == null) {
			chkShowUrge.setEnabled(true);
		} else {
			chkShowUrge.setEnabled(false);
			
			//ログイン時にスタンプピンの更新をしている可能性があるので
			//スタンプピンの前回更新確認時間を再設定
			settingStampPinPrevCheckTime(StampRallyPreferences.getLastCheckDateStampPin());
		}
	}
	
}
