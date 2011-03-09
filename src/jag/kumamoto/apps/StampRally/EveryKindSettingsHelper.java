package jag.kumamoto.apps.StampRally;

import java.io.IOException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import jag.kumamoto.apps.StampRally.Data.StampPin;
import jag.kumamoto.apps.StampRally.Data.StampRallyURL;
import jag.kumamoto.apps.StampRally.Data.User;
import jag.kumamoto.apps.gotochi.R;
import aharisu.util.DataGetter;
import aharisu.util.Pair;
import android.app.ProgressDialog;
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
	public static interface OnValueChangeListener {
		public void onPollingIntervalChanged(int type);
		public void onShowUrgeChanged(boolean bool);
	}
	
	private final ViewGroup mLayout;
	private User mUser;
	private final OnValueChangeListener mListener;
	
	public EveryKindSettingsHelper(ViewGroup layout, User user, OnValueChangeListener listener) {
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
		if(mUser == null) {
			chkShowUrge.setEnabled(true);
			chkShowUrge.setChecked(StampRallyPreferences.getShowUrgeDialog());
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
					"前回確認した時間" + " : " + new Date(time).toLocaleString());
		} else {
			((TextView)mLayout.findViewById(R.id_settings.prev_check_time)).setText(
					"まだ一度も確認していません");
		}
	}
	
	private void checkNewStampPin() {
		final ProgressDialog dialog = new ProgressDialog(mLayout.getContext());
		dialog.setMessage("確認中です");
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
								result.toString() + "個の新しいスタンプを取得しました", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mLayout.getContext(), 
								"新しいスタンプはありませんでした", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(mLayout.getContext(), "確認に失敗しました", Toast.LENGTH_SHORT).show();
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
		}
	}
	
}
