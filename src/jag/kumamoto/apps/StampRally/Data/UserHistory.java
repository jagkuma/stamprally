package jag.kumamoto.apps.StampRally.Data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;

public final class UserHistory {
	public final long[] executedPinIds;
	public final long[] executedQuizIds;
	
	public UserHistory(long[] executedPinIds, long[] executedQuizIds) {
		this.executedPinIds = executedPinIds;
		this.executedQuizIds = executedQuizIds;
	}
	
	public static int decodeJSONGotochiData(JSONObject obj) throws JSONException {
		JSONObject objData = obj.getJSONObject("gotochiData");
		return objData.getInt("point");
	}
	
	public static UserHistory decodeJSONGetExecutedIds(JSONObject obj) throws JSONException {
		JSONArray ary = obj.getJSONArray("history");
		int count = ary.length();
		
		ArrayList<Long> pinIdAry = new ArrayList<Long>();
		ArrayList<Long> quizIdAry = new ArrayList<Long>();
		
		for(int i = 0;i < count;++i) {
			JSONObject o = ary.getJSONObject(i);
			if(o.getInt("executed") == 1) {
				if(o.isNull("quiz")) {
					pinIdAry.add(o.getJSONObject("pin").getJSONObject("key").getLong("id"));
				} else {
					quizIdAry.add(o.getJSONObject("quiz").getJSONObject("key").getLong("id"));
				}
			}
		}
		
		return new UserHistory(convertArrayListToAry(pinIdAry), convertArrayListToAry(quizIdAry));
	}
	
	private static long[] convertArrayListToAry(ArrayList<Long> list) {
		int size = list.size();
		Long[] objAry = new Long[size];
		list.toArray(objAry);
		
		long[] ary = new long[size];
		for(int i = 0;i < size;++i) {
			ary[i] = objAry[i];
		}
		
		return ary;
	}
	
}
