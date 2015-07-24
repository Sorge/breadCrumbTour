package info.sorgedev.breadcrumbtour.log;

import info.sorgedev.breadcrumbtour.R;
import info.sorgedev.breadcrumbtour.obj.VuforiaException;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Vec2I;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.Vuforia.UpdateCallbackInterface;

public class ApplicationSession implements UpdateCallbackInterface {

	private static final String LOGTAG = "Application_Session";
	
	private Activity activity;
	private VuforiaApplicationControl sessionControl;
	
	private boolean started = false;
	private boolean cameraRunning = false;
	
	private int screenWidth = 0;
	private int screenHeight = 0;
	
	private InitVuforiaTask initVuforiaTask;
	private LoadTrackerTask loadTrackerTask;
	
	private Object shutdownLock = new Object();
	
	private int vuforiaFlags = 0;
	
	private int camera = CameraDevice.CAMERA.CAMERA_DEFAULT;
	
	private boolean isPortrait = false;
	
	public ApplicationSession(VuforiaApplicationControl control) {
		this.sessionControl = control;
	}
	
	public void initAR(Activity activity, int screenOrientation) {
		VuforiaException vuforiaException = null;
		this.activity = activity;
		
		if ((screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
	            && (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO))
	            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
		
		activity.setRequestedOrientation(screenOrientation);
		
		updateActivityOrientation();
		
		storeScreenDimensions();
		
		activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		vuforiaFlags = Vuforia.GL_20; //TODO co to oznacza?
		
		if(initVuforiaTask != null) {
			String logMsg = "Cannot initialize SDK twice";
			vuforiaException = new VuforiaException(VuforiaException.VUFORIA_ALREADY_INITIALIZATED, logMsg);
			Log.e(LOGTAG, logMsg);
		}
		
		if(vuforiaException == null) {
			try {
				initVuforiaTask = new InitVuforiaTask();
				initVuforiaTask.execute();
			} catch(Exception e) {
				String logMsg = "Initializing Vuforia SDK failed";
				vuforiaException = new VuforiaException(VuforiaException.INITIALIZATION_FAILURE, logMsg);
				Log.e(LOGTAG, logMsg);
			}
		}
		
		if(vuforiaException != null) {
			sessionControl.onInitARDone(vuforiaException);
		}
	}
	
	public void startAR(int camera) throws VuforiaException {
		String error;
		if(cameraRunning) {
			error = "Camera is already running, unable to open again";
			Log.e(LOGTAG, error);
			throw new VuforiaException(VuforiaException.CAMERA_INITIALIZATION_FAILURE, error);
		}
		
		this.camera = camera;
		if(!CameraDevice.getInstance().init(camera)) {
			error = "Unable to open camera device: " + camera;
			Log.e(LOGTAG, error);
			throw new VuforiaException(VuforiaException.CAMERA_INITIALIZATION_FAILURE, error);
		}
		
		configureVideoBackground();
		
		if (!CameraDevice.getInstance().selectVideoMode(CameraDevice.MODE.MODE_DEFAULT)) {
            error = "Unable to set video mode";
            Log.e(LOGTAG, error);
            throw new VuforiaException(
                VuforiaException.CAMERA_INITIALIZATION_FAILURE, error);
        }
		
		if(!CameraDevice.getInstance().start()) {
			error = "Unable to start camera device: " + camera;
            Log.e(LOGTAG, error);
            throw new VuforiaException(VuforiaException.CAMERA_INITIALIZATION_FAILURE, error);
		}
		
		//TODO set matrix; ustawienie srodowiska?
		
		sessionControl.doStartTrackers();
		
		cameraRunning = true;
		
//		if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO)) {
//			CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
//		}
		CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
		
	}
	
	public void stopAR() throws VuforiaException {
		if(initVuforiaTask != null && initVuforiaTask.getStatus() != InitVuforiaTask.Status.FINISHED) {
			initVuforiaTask.cancel(true);
			initVuforiaTask = null;
		}
		
		if(loadTrackerTask != null && loadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED) {
			loadTrackerTask.cancel(true);
			loadTrackerTask = null;
		}
		
		initVuforiaTask = null;
		loadTrackerTask = null;
		
		started = false;
		
		stopCamera();
		
		synchronized (shutdownLock) {
			boolean unloadTrackersResult;
            boolean deinitTrackersResult;
            
            unloadTrackersResult = sessionControl.doUnloadTrackersData();
            deinitTrackersResult = sessionControl.doDeinitTrackers();
            
            Vuforia.deinit();
            
            if(!unloadTrackersResult) {
            	throw new VuforiaException(VuforiaException.UNLOADING_TRACKERS_FAILURE, "Failed to unload trackers\' data");
            }
            
            if(!deinitTrackersResult) {
            	throw new VuforiaException(VuforiaException.TRACKERS_DEINITIALIZATION_FAILURE, "Failed to deinitialize trackers");
            }
		}
	}
	
	public void resumeAR() throws VuforiaException {
		Vuforia.onResume();
		
		if(started) {
			startAR(camera);
		}
	}
	
	public void pauseAR() throws VuforiaException {
		if(started) {
			stopCamera();
		}
		Vuforia.onPause();
	}
	
	@Override
	public void QCAR_onUpdate(State state) {
		sessionControl.onQCARUpdate(state);
	}
	
	public void onConfigurationChanged()
    {
        updateActivityOrientation();
        
        storeScreenDimensions();
        
        if (isARRunning())
        {
            configureVideoBackground();
            
            //TODO setProjectionMatrix();
        }
        
    }
	
	public void onResume()
    {
        Vuforia.onResume();
    }
    
    
    public void onPause()
    {
        Vuforia.onPause();
    }
    
    
    public void onSurfaceChanged(int width, int height)
    {
        Vuforia.onSurfaceChanged(width, height);
    }
    
    public void onSurfaceCreated()
    {
        Vuforia.onSurfaceCreated();
    }
    
    private void storeScreenDimensions() {
    	DisplayMetrics metrics = new DisplayMetrics();
    	activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
    	screenHeight = metrics.heightPixels;
    	screenWidth = metrics.widthPixels;
    }
    
    private void updateActivityOrientation() {
    	Configuration config = activity.getResources().getConfiguration();
    	
    	switch(config.orientation) {
    	case Configuration.ORIENTATION_PORTRAIT:
    		isPortrait = true;
    		break;
    	case Configuration.ORIENTATION_LANDSCAPE:
    		isPortrait = false;
    		break;
    	case Configuration.ORIENTATION_UNDEFINED:
		default:
			break;
    	}
    	
    	Log.i(LOGTAG, "Activity is in " + (isPortrait ? "PORTRAIT" : "LANDSCAPE"));
    }
    
    public void stopCamera() {
    	if(cameraRunning) {
    		sessionControl.doStopTrackers();
    		CameraDevice.getInstance().stop();
    		CameraDevice.getInstance().deinit();
    		cameraRunning = false;
    	}
    }
    
    //TODO setFocusMode zrobic
    
    private void configureVideoBackground() {
    	CameraDevice cameraDevice = CameraDevice.getInstance();
        VideoMode vm = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);
        
        VideoBackgroundConfig config = new VideoBackgroundConfig();
        config.setEnabled(true);
        config.setSynchronous(true);
        config.setPosition(new Vec2I(0, 0));
        
        int xSize = 0, ySize = 0;
        if (isPortrait) {
            xSize = (int) (vm.getHeight() * (screenHeight / (float) vm
                .getWidth()));
            ySize = screenHeight;
            
            if (xSize < screenWidth)
            {
                xSize = screenWidth;
                ySize = (int) (screenWidth * (vm.getWidth() / (float) vm
                    .getHeight()));
            }
        } else {
            xSize = screenWidth;
            ySize = (int) (vm.getHeight() * (screenWidth / (float) vm
                .getWidth()));
            
            if (ySize < screenHeight)
            {
                xSize = (int) (screenHeight * (vm.getWidth() / (float) vm
                    .getHeight()));
                ySize = screenHeight;
            }
        }
        
        config.setSize(new Vec2I(xSize, ySize));
        
        Log.i(LOGTAG, "Configure Video Background : Video (" + vm.getWidth()
                + " , " + vm.getHeight() + "), Screen (" + screenWidth + " , "
                + screenHeight + "), mSize (" + xSize + " , " + ySize + ")");
            
        Renderer.getInstance().setVideoBackgroundConfig(config);
    }
    
    private boolean isARRunning()
    {
        return started;
    }
	
	private class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean>
	{

		private int progressValue = -1;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			synchronized (shutdownLock) {
				Vuforia.setInitParameters(activity, vuforiaFlags, "AZKWA2n/////AAAAAWs6QxV+1kEzsmrLXv6gUNsqZB9XMnGh5XYtiOKS0cpdFnCWGP7xH1t3vtbVkLL9WgZybrpPmwD8rIOWvj2KHAfEHJq5IbdWk3l35oUHrh2q1MbyZAnJZ/Rv9yRZU0DjF1/Ac0Ixh87FRX/iMm9r5m63p0Jm2NDA2kw2thWt6Fsj4eJVD4Hb8U3qfoWeNGeKibqlU08kIrYglrpmMXEseOoGGA72Bbn3GoVXjvRTFjMpedr9lCELWxrIHFEHKym20eybxif6HNwED+DC9QVYhBwgESpyO5f2uzi3RIjHimfWwFjp4FenTD9ZL3jTSq9hxxsn3FZb0YLlWSdHTHra+0Dgd6zBPeGyNysfm1vdRF+n");
				do {
					progressValue = Vuforia.init();
					publishProgress(progressValue);
				} while(!isCancelled() && progressValue >=0 && progressValue < 100);
				
				return(progressValue > 0);
			}
		}
		
		protected void onProgressUpdate(Integer... values) {
			//TODO do some progress bar or anything
		}
		
		protected void onPostExecute(Boolean result) {
			VuforiaException vuforiaException = null;
			
			if(result) {
				Log.d(LOGTAG, "InitVuforiaTask.onPostExecute: Vuforia initialization successful");
				
				boolean initTrackersResult;
				initTrackersResult = sessionControl.doInitTrackers();
				
				if(initTrackersResult) {
					try {
						loadTrackerTask = new LoadTrackerTask();
						loadTrackerTask.execute();
					} catch (Exception e) {
						String error = "Loading tracking data set failed";
						vuforiaException = new VuforiaException(VuforiaException.LOADING_TRACKERS_FAILURE, error);
						Log.e(LOGTAG, error);
						sessionControl.onInitARDone(vuforiaException);
					}
				} else {
					vuforiaException = new VuforiaException(VuforiaException.TRACKERS_INITIALIZATION_FAILURE, "Failed to initialiaze trackers");
					sessionControl.onInitARDone(vuforiaException);
				}
			} else {
				String error;
				error = getInitializationErrorString(progressValue);
				Log.e(LOGTAG, "InitVuforiaTask.onPostExecute: " + error + " Exiting.");
				vuforiaException = new VuforiaException(VuforiaException.INITIALIZATION_FAILURE, error);
				sessionControl.onInitARDone(vuforiaException);
			}
		}
		
	}
	
	private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean>
	{

		@Override
		protected Boolean doInBackground(Void... params) {
			synchronized(shutdownLock) {
				return sessionControl.doLoadTrackersData();
			}
		}
		
		protected void onPostExecute(Boolean result) {
			VuforiaException vuforiaException = null;
			
			Log.d(LOGTAG, "LoadTrackerTask.onPostExecute: execution " + (result ? "successful" : "failed"));
			
			if(!result) {
				String error = "Failed to load tracker data.";
				Log.e(LOGTAG, error);
				vuforiaException = new VuforiaException(VuforiaException.LOADING_TRACKERS_FAILURE, error);
			} else {
				System.gc();
				Vuforia.registerCallback(ApplicationSession.this);
				
				started = true;
			}
			
			sessionControl.onInitARDone(vuforiaException);
		}
		
	}
	
	private String getInitializationErrorString(int code)
    {
        if (code == Vuforia.INIT_DEVICE_NOT_SUPPORTED)
            return activity.getString(R.string.INIT_ERROR_DEVICE_NOT_SUPPORTED);
        if (code == Vuforia.INIT_NO_CAMERA_ACCESS)
            return activity.getString(R.string.INIT_ERROR_NO_CAMERA_ACCESS);
        if (code == Vuforia.INIT_LICENSE_ERROR_MISSING_KEY)
            return activity.getString(R.string.INIT_LICENSE_ERROR_MISSING_KEY);
        if (code == Vuforia.INIT_LICENSE_ERROR_INVALID_KEY)
            return activity.getString(R.string.INIT_LICENSE_ERROR_INVALID_KEY);
        if (code == Vuforia.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT)
            return activity.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT);
        if (code == Vuforia.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT)
            return activity.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT);
        if (code == Vuforia.INIT_LICENSE_ERROR_CANCELED_KEY)
            return activity.getString(R.string.INIT_LICENSE_ERROR_CANCELED_KEY);
        if (code == Vuforia.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH)
            return activity.getString(R.string.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH);
        else
        {
            return activity.getString(R.string.INIT_LICENSE_ERROR_UNKNOWN_ERROR);
        }
    }

}
