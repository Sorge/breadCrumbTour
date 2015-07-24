package info.sorgedev.breadcrumbtour.log;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;

public final class LoadingDialogHandler extends Handler {

	private final WeakReference<Activity> activity;
	public static final int HIDE_LOADING = 0;
	public static final int SHOW_LOADING = 1;
	
	public View loadingDialogContainer;
	
	public LoadingDialogHandler(Activity activity) {
		this.activity = new WeakReference<Activity>(activity);
	}
	
	public void handleMessage(Message msg) {
		Activity imageTargets = activity.get();
		if(imageTargets == null) {
			return;
		}
		
		if(msg.what == SHOW_LOADING) {
			loadingDialogContainer.setVisibility(View.VISIBLE);
		} else if(msg.what == HIDE_LOADING) {
			loadingDialogContainer.setVisibility(View.GONE);
		}
	}
	
}
