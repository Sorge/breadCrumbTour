package info.sorgedev.breadcrumbtour.log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.Log;
import rajawali.lights.DirectionalLight;
import rajawali.renderer.RajawaliRenderer;

public class ImageRenderer  extends RajawaliRenderer{
	
	private final String LOGTAG = "Renderer";
	
	private ApplicationSession session;
	
	private DirectionalLight light;

	public ImageRenderer(Context context, ApplicationSession session) {
		super(context);
		this.session = session;
		setFrameRate(60);
	}
	
	protected void initScene() {
		light = new DirectionalLight(1f, 0.2f, -1.0f); // set the direction
		light.setColor(1.0f, 1.0f, 1.0f);
		light.setPower(2);
		
		mCamera.setZ(4.2f);
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.d(LOGTAG, "GLRenderer.onSufraceCreated");
		
		super.onSurfaceCreated(gl, config);
		
		session.onSurfaceCreated();
	}
	
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
		session.onSurfaceChanged(width, height);
	}

}
