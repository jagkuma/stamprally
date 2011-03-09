package aharisu.widget;

import jag.kumamoto.apps.gotochi.R;
import aharisu.util.AsyncDataGetter;
import aharisu.util.Size;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RadioButton;

/**
 * 
 * テキスト部分に画像を表示することのできるラジオボタン
 * 
 * @author aharisu
 *
 */
public class ImageRadioButton extends RadioButton{
	private static final int MaxImageSize = 200;
	
	private Bitmap mImage = null;
	
	public ImageRadioButton(Context context) {
		super(context);
	}
	
	public ImageRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ImageRadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setImage(Bitmap image) {
		this.mImage = image;
		
		requestLayout();
		forceLayout();
		invalidate();
	}
	
	public void setImageURL(String url) {
		AsyncDataGetter.getBitmapCache(url, new AsyncDataGetter.BitmapCallback() {
			
			@Override public void onGetData(Bitmap data) {
				mImage = data;
				
				requestLayout();
				forceLayout();
				invalidate();
			}
			
			@Override public Size getMaxImageSize() {
				return new Size(MaxImageSize, MaxImageSize);
			}
			
			@Override public void onFailure(Exception e) {
				//TODO ダミーの画像を表示する？
			}
		});
		
		this.mImage =  BitmapFactory.decodeResource(
				getContext().getResources(), R.drawable.now_loading);
		requestLayout();
		forceLayout();
		invalidate();
	}
	
	
	@Override protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if(mImage != null) {
			canvas.drawBitmap(mImage, getPaddingLeft(), 0, null);
		}
	}
	
	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		
		if(mImage != null) {
			int left = getPaddingLeft();
			
			int width = getMeasuredWidth();
			int containImageWidth = left + mImage.getWidth();
			if(width < containImageWidth) {
				width = containImageWidth;
			}
			
			int height = getMeasuredHeight();
			int containImageHeight = left + mImage.getHeight();
			if(height < containImageHeight) {
				height = containImageHeight;
			}
			
			if(getMeasuredWidth() != width ||
					getMeasuredHeight() != height) {
				setMeasuredDimension(containImageWidth, containImageHeight);
			}
		}
	}
	

}
