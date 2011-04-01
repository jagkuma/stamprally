package jag.kumamoto.apps.gotochi.stamprally.Data;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * クイズデータを表すクラス
 * 
 * @author aharisu
 *
 */
public final class QuizData implements Parcelable{
	private static final String JsonNameQuizes = "quizes";
	private static final String JsonNameKey = "key";
	private static final String JsonNameID = "id";
	private static final String JsonNamePinKey = "pinKey";
	private static final String JsonNameTitle = "title";
	private static final String JsonNameHTML = "html"; 
	private static final String JsonNameChoices = "options";
	private static final String JsonNamePoint = "point";
	private static final String JsonNameOrder = "order";
	
	
	public final long id;
	public final long pinId;
	public final String title;
	public final String descriptionHTML;
	public final int point;
	public final int order;
	public final QuizChoices choices;
	
	
	private QuizData(long id, long pinId, 
			String title, String html, int point, int order,
			QuizChoices choices) {
		this.id = id;
		this.pinId = pinId;
		this.title = title;
		this.descriptionHTML = html;
		this.point = point;
		this.order = order;
		this.choices = choices;
	}
	
	
	public static QuizData[] decodeJSONObject(JSONObject obj) throws JSONException {
		JSONArray jsonQuizes = obj.getJSONArray(JsonNameQuizes);
		
		int count = jsonQuizes.length();
		QuizData[] quizes = new QuizData[count];
		
		int actualCount = 0;
		for(int i = 0;i < count;++i) {
			JSONObject jsonQuiz = jsonQuizes.getJSONObject(i);
			
			final long id = jsonQuiz.getJSONObject(JsonNameKey).getLong(JsonNameID);
			final long pinId = jsonQuiz.getJSONObject(JsonNamePinKey).getLong(JsonNameID);
			String title = jsonQuiz.getString(JsonNameTitle);
			final String html = constractHTML(jsonQuiz.getString(JsonNameHTML));
			final int point = jsonQuiz.isNull(JsonNamePoint) ? 1 :  jsonQuiz.getInt(JsonNamePoint);
			final int order = jsonQuiz.isNull(JsonNameOrder) ? 0 : jsonQuiz.getInt(JsonNameOrder);
			
			try {
				final QuizChoices choices = QuizChoices.decodeJSONArray(jsonQuiz.getJSONArray(JsonNameChoices));
				quizes[actualCount] = new QuizData(id, pinId, title, html, point, order, choices);
				++actualCount;
			}catch(JSONException e) {
				//とりあえず握りつぶす
			}
		}
		
		if(count != actualCount)
		{
			QuizData[] tmp = new QuizData[actualCount];
			for(int i = 0;i < actualCount;++i)
				tmp[i] = quizes[i];
			
			quizes = tmp;
		}
		
		return quizes;
	}
	
	private static String constractHTML(String html)
	{
		if(html.trim().startsWith("<html"))
			return html;
		else
		{
			return String.format(
					"<html xmlns=\"_http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"> <meta http-equiv=\"Content-Style-Type\" content=\"text/css\"> </head><body>%s</body></html>",
					html);
		}
	}

	
	/*
	 * 以降 Parcelableクラスの実装
	 */
	
	@Override public int describeContents() {
		return 0;
	}
	
	@Override public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeLong(pinId);
		dest.writeString(title);
		dest.writeString(descriptionHTML);
		dest.writeInt(point);
		dest.writeInt(order);
		dest.writeParcelable(choices, 0);
	}
	
	public static final Parcelable.Creator<QuizData> CREATOR = new Parcelable.Creator<QuizData>() {
		@Override public QuizData[] newArray(int size) {
			return new QuizData[size];
		}
		
		@Override public QuizData createFromParcel(Parcel source) {
			return new QuizData(
					source.readLong(),
					source.readLong(),
					source.readString(),
					source.readString(),
					source.readInt(),
					source.readInt(),
					(QuizChoices)source.readParcelable(QuizChoices.class.getClassLoader()));
	
		}
		
	};
	
}
