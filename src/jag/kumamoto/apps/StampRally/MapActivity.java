package jag.kumamoto.apps.StampRally;

import jag.kumamoto.apps.gotochi.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;


/**
 * 
 * スタンプラリーのチェックポイントを含むマップを表示するアクティビティ
 * 
 * @author aharisu
 *
 */
public class MapActivity extends com.google.android.maps.MapActivity{
	
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.map);
		
		findViewById(R.id_map.button).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				startActivity(new Intent(MapActivity.this, LocationInfoActivity.class));
			}
		});
		
	}
	
	@Override protected boolean isRouteDisplayed() {
		return false;
	}

}