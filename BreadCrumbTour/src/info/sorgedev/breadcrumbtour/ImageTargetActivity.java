package info.sorgedev.breadcrumbtour;

import java.util.ArrayList;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ObjectTracker;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vuforia;

import info.sorgedev.breadcrumbtour.log.ApplicationSession;
import info.sorgedev.breadcrumbtour.log.ImageRenderer;
import info.sorgedev.breadcrumbtour.log.LoadingDialogHandler;
import info.sorgedev.breadcrumbtour.log.VuforiaApplicationControl;
import info.sorgedev.breadcrumbtour.log.VuforiaSufraceView;
import info.sorgedev.breadcrumbtour.obj.VuforiaException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Switch;

public class ImageTargetActivity extends Activity implements VuforiaApplicationControl {

	private static final String LOGTAG = "ImageTargets";
	
	protected ApplicationSession appSession;
	
	private DataSet currentDataset;
	private int currectDatasetSelectionIndex = 0;
	private int startDatasetsIndex = 0;
	private int datasetsNumber = 0;
	private ArrayList<String> datasetStrings = new ArrayList<String>();
	
	private VuforiaSufraceView glView;
	
	private GestureDetector gestureDetector;
	
	private ImageRenderer renderer;
	
	private boolean switchDatasetAsap = false;
	private boolean flash = false;
	private boolean autofocus = false;
	private boolean extendedTracking = false;
	
	private View flashOptionView;
	
	private RelativeLayout uiLayout;
	
	protected LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);
	
	private AlertDialog errorDialog;
	
	protected boolean isDroidDevice = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Log.d(LOGTAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		appSession = new ApplicationSession(this);
		
		startLoadingAnimation();
		datasetStrings.add("bread_crumb.xml");
		
		appSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		gestureDetector = new GestureDetector(this, new GestureListener());
		
		isDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith("droid");
	}
	
	private class GestureListener extends GestureDetector.SimpleOnGestureListener
	{
		private final Handler autofocusHandler = new Handler();

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return super.onSingleTapUp(e);
			//TODO pobranie wskazowki
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			autofocusHandler.postDelayed(new Runnable() {
				public void run() {
					boolean result = CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
					
					if(!result) {
						Log.e("DoubleTap", "Unable to trigger focus");
					}
				}
			}, 1000L);
			return true;
		}		
	}

	@Override
	protected void onResume() {
		
		Log.d(LOGTAG, "onResume");
		super.onResume();
		
		if(isDroidDevice) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		
		try {
			appSession.resumeAR();
		} catch (VuforiaException e) {
			Log.e(LOGTAG, e.getMessage());
		}
		
		if(glView != null) {
			glView.setVisibility(View.VISIBLE);
			glView.onResume();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration config) {
		Log.d(LOGTAG, "onCofigurationChanged");
		super.onConfigurationChanged(config);
		
		appSession.onConfigurationChanged();
	}

	@Override
	protected void onPause() {
		
		Log.d(LOGTAG, "onPause");
		super.onPause();
		
		if(glView != null) {
			glView.setVisibility(View.INVISIBLE);
			glView.onPause();
		}
		
		if(flashOptionView != null && flash) {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				((Switch) flashOptionView).setChecked(false);
			} else {
				((CheckBox) flashOptionView).setChecked(false);
			}
		}
		
		try {
			appSession.pauseAR();
		} catch(VuforiaException e) {
			Log.e(LOGTAG, e.getMessage());
		}
	}

	@Override
	protected void onDestroy() {

		Log.d(LOGTAG, "onDestroy");
		super.onDestroy();
		
		try {
			appSession.stopAR();
		} catch (VuforiaException e) {
			Log.e(LOGTAG, e.getMessage());
		}
		
		//TODO unload textures
		
		System.gc();
	}
	
	private void initApplicationAR() {
		int depthSize = 16;
		int stencilSize = 0;		
		boolean translucent = Vuforia.requiresAlpha();
		
		glView = new VuforiaSufraceView(this);
		glView.init(translucent, depthSize, stencilSize);
		
		//TODO set renderer
		renderer = new ImageRenderer(this, appSession);
		glView.setRenderer(renderer);
	}
	
	private void startLoadingAnimation() {
		LayoutInflater inflater = LayoutInflater.from(this);
		uiLayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay, null, false);
		
		uiLayout.setVisibility(View.VISIBLE);
		uiLayout.setBackgroundColor(Color.BLACK);
		
		loadingDialogHandler.loadingDialogContainer = uiLayout.findViewById(R.id.loading_indicator);
		
		loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING);
		
		addContentView(uiLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	
	public boolean doLoadTrackersData() {
		TrackerManager trackerManager = TrackerManager.getInstance();
		ObjectTracker objectTracker = (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());
		
		if(objectTracker == null) {
			return false;
		}
		
		if(currentDataset == null) {
			currentDataset = objectTracker.createDataSet();
		}
		
		if(currentDataset == null) {
			return false;
		}
		
		if(!currentDataset.load(datasetStrings.get(currectDatasetSelectionIndex), STORAGE_TYPE.STORAGE_APPRESOURCE)) {
			return false;
		}
		
		if(!objectTracker.activateDataSet(currentDataset)) {
			return false;
		}
		
		int numTrackables = currentDataset.getNumTrackables();
		for(int i = 0; i < numTrackables; i++) {
			Trackable trackable = currentDataset.getTrackable(i);
			if(isExtendedTrackingActive()) {
				trackable.startExtendedTracking();
			}
			
			String name = "Current Dataset: " + trackable.getName();
			trackable.setUserData(name);
			Log.d(LOGTAG, "UserData:Set the following user data " + (String) trackable.getUserData()) ;
		}
		return true;
	}
	
	public boolean doUnloadTrackersData() {
		boolean result = true;
		
		TrackerManager trackerManager = TrackerManager.getInstance();
		ObjectTracker objectTracker = (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());
		if (objectTracker == null)
            return false;
		
		if (currentDataset != null && currentDataset.isActive())
        {
            if (objectTracker.getActiveDataSet().equals(currentDataset)
                && !objectTracker.deactivateDataSet(currentDataset))
            {
                result = false;
            } else if (!objectTracker.destroyDataSet(currentDataset))
            {
                result = false;
            }
            
            currentDataset = null;
        }
        
        return result;
	}
	
	public void onInitARDone(VuforiaException exception) {
		if (exception == null) {
			initApplicationAR();
			
			//TODO add gl surface view
			addContentView(glView, new LayoutParams(LayoutParams.MATCH_PARENT,
	                LayoutParams.MATCH_PARENT));
		
			uiLayout.bringToFront();
			uiLayout.setBackgroundColor(Color.TRANSPARENT);
			
			try
            {
                appSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
            } catch (VuforiaException e)
            {
                Log.e(LOGTAG, e.getMessage());
            }
			
			boolean result = CameraDevice.getInstance().setFocusMode(
	                CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
			
			if (result) {
                autofocus = true;
			} else {
                Log.e(LOGTAG, "Unable to enable continuous autofocus");
			}
		} else {
			Log.e(LOGTAG, exception.getMessage());
            showInitializationErrorMessage(exception.getMessage());
		}
	}
	
	public void showInitializationErrorMessage(String message) {
		final String errorMessage = message;
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (errorDialog != null)
                {
                    errorDialog.dismiss();
                }
                
                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                    ImageTargetActivity.this);
                builder
                    .setMessage(errorMessage)
                    .setTitle(getString(R.string.INIT_ERROR))
                    .setCancelable(false)
                    .setIcon(0)
                    .setPositiveButton(getString(R.string.button_OK),
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                finish();
                            }
                        });
                
                errorDialog = builder.create();
                errorDialog.show();
            }
        });
	}
	
	public void onQCARUpdate(State state)
    {
        if (switchDatasetAsap)
        {
            switchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker
                .getClassType());
            if (ot == null || currentDataset == null
                || ot.getActiveDataSet() == null)
            {
                Log.d(LOGTAG, "Failed to swap datasets");
                return;
            }
            
            doUnloadTrackersData();
            doLoadTrackersData();
        }
    }
	
	public boolean doInitTrackers()
    {
        // Indicate if the trackers were initialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;
        
        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                LOGTAG,
                "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }
	
	public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;
        
        Tracker objectTracker = TrackerManager.getInstance().getTracker(
            ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();
        
        return result;
    }
	
	public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;
        
        Tracker objectTracker = TrackerManager.getInstance().getTracker(
            ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();
        
        return result;
    }
	
	public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());
        
        return result;
    }
	
	public boolean onTouchEvent(MotionEvent event)
    {        
        return gestureDetector.onTouchEvent(event);
    }
	
	boolean isExtendedTrackingActive()
    {
        return extendedTracking;
    }
	
	final public static int CMD_BACK = -1;
    final public static int CMD_EXTENDED_TRACKING = 1;
    final public static int CMD_AUTOFOCUS = 2;
    final public static int CMD_FLASH = 3;
    final public static int CMD_CAMERA_FRONT = 4;
    final public static int CMD_CAMERA_REAR = 5;
    final public static int CMD_DATASET_START_INDEX = 6;
    
    //TODO male menu z kilkoma tylko opcjami
    //nie calosc jak w samplach
	
}
