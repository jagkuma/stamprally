package aharisu.mascot;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * 
 * マスコットの動作を決定する抽象ステートクラス
 * 
 * @author aharisu
 *
 */
public abstract class MascotState {
	private static final int HitTestMargin = 7;
	private static final int DefaultInterval = 675;
	
	private final Rect mBounds = new Rect();
	
	protected final IMascot mMascot;
	
	protected MascotState(IMascot mascot) {
		mMascot = mascot;
	}
	
	public abstract Bitmap getImage();
	public abstract void getBounds(Rect outRect);
	
	public boolean isAllowExist() {
		return true;
	}
	
	public boolean isAllowInterrupt() {
		return true;
	}
	
	public int getUpdateInterval() {
		return DefaultInterval;
	}
	
	public Mascot.Level getEntryPriority() {
		return Mascot.Level.Middle;
	}
	
	public Mascot.Level getExistProbability() {
		return Mascot.Level.Middle;
	}
	
	public void entry(Rect bounds) {}
	public boolean update() {return true;}
	public void exist() {}
	
	public void draw(Canvas canvas) {
		Bitmap image = getImage();
		getBounds(mBounds);
		
		if(image != null && !mBounds.isEmpty()) {
			Rect src = new Rect(0, 0, image.getWidth(), image.getHeight());
			canvas.drawBitmap(image, src, mBounds, null);
		}
	}
	
	public boolean hitTest(int x, int y) {
		getBounds(mBounds);
		
		mBounds.inset(-HitTestMargin, -HitTestMargin);
		return mBounds.contains(x, y);
	}

	protected final void centering(Rect bounds, int width, int height) {
		bounds.left = bounds.centerX() - width / 2;
		bounds.right = bounds.left + width;
		
		bounds.top = bounds.centerY() - height / 2;
		bounds.bottom = bounds.top + height;
	}
	
	protected final void splitImage(Bitmap image, Bitmap[] outImages, int vCount, int hCount) {
		int width = image.getWidth() / vCount;
		int height = image.getHeight() / hCount;
		
		Rect srcRect = new Rect();
		Rect destRect = new Rect(0, 0, width, height);
		for(int y = 0;y < hCount;++y) {
			srcRect.top = y * height;
			srcRect.bottom = srcRect.top + height;
			
			for(int x = 0;x < vCount;++x) {
			
				Bitmap img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
				Canvas canvas = new Canvas(img);
				
				srcRect.left = x * width;
				srcRect.right = srcRect.left + width;
				
				canvas.drawBitmap(image, srcRect, destRect, null);
				
				outImages[y*vCount+x] = img;
			}
		}
	}
	
	protected final void splitImage(Bitmap image, Bitmap[] outImages, int numSplit) {
		splitImage(image, outImages, numSplit, 1);
	}
	
}
