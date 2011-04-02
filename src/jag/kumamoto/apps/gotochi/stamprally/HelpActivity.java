package jag.kumamoto.apps.gotochi.stamprally;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

/**
 * 
 * スタンプラリーの操作方法などヘルプ画面のアクティビティ
 * 
 * @author aharisu
 *
 */
public class HelpActivity extends Activity {
	private static final String URL = "http://kumamotogotochi.appspot.com/apps/gotochi/stamprally/help";
	
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.help);
		
		WebView webView = (WebView)findViewById(R.id_help.webview);
		webView.loadUrl(URL);
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int newProgress) {
				ProgressBar progress = (ProgressBar)findViewById(R.id_help.progress);
				if(newProgress < 100) {
					if(progress.getVisibility() == View.GONE) {
						progress.setVisibility(View.VISIBLE);
					}
					
					progress.setProgress(newProgress);
				} else {
					progress.setVisibility(View.GONE);
				}
			}
		});
			
		
	}
	

}
