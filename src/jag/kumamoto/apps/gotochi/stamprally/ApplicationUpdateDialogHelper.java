package jag.kumamoto.apps.gotochi.stamprally;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;

public final class ApplicationUpdateDialogHelper {
	private static final String IntentData = "market://search?q=pname:jag.kumamoto.apps.gotochi.stamprally";
	
	private ApplicationUpdateDialogHelper() {
	}
	
	public static void showApplicationUpdateDialog(final Context context) {
		new Handler(context.getMainLooper()).post(new Runnable() {
			
			@Override public void run() {
				new AlertDialog.Builder(context)
					.setTitle(R.string.update_dialog_title)
					.setMessage(context.getResources().getString(R.string.update_dialog_message))
					.setCancelable(false)
					.setPositiveButton(R.string.update_dialog_button, new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(IntentData)));
						}
					})
					.show();
				
			}
		});
	}

}
