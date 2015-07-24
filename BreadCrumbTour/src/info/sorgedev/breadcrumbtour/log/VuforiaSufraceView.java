package info.sorgedev.breadcrumbtour.log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;

public class VuforiaSufraceView extends GLSurfaceView {
	
	private static final String LOGTAG = "GLView";

	public VuforiaSufraceView(Context context) {
		super(context);
	}
	
	public void init(boolean translucent, int depth, int stencil) {
		Log.d(LOGTAG, "Init GLSurfaceView");
		
		if(translucent) {
			this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		}
		
		setEGLContextFactory(new ContextFactory());
		
		setEGLConfigChooser(translucent ? new ConfigChooser(8,8,8,8, depth, stencil) 
				: new ConfigChooser(5,6,5,0, depth, stencil));
	}
	
	private static void checkingEGLError(String prompt, EGL10 egl) {
		int error;
		while((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
			Log.e(LOGTAG, String.format("%s: EGL error: 0x%x", prompt, error));
		}
	}
	
	private static class ContextFactory implements GLSurfaceView.EGLContextFactory
	{

		private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
		
		@Override
		public EGLContext createContext(EGL10 egl, EGLDisplay display,
				EGLConfig eglConfig) 
		{
			EGLContext context;
			
			checkingEGLError("Before eflCreateContext", egl);
			int[] attr_list_gl20 = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
			
			context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attr_list_gl20);
			
			checkingEGLError("After eglCreateContext", egl);
			return context;
		}

		@Override
		public void destroyContext(EGL10 egl, EGLDisplay display,
				EGLContext context) 
		{
			egl.eglDestroyContext(display, context);
		}
		
	}
	
	private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser
	{		
		protected int redSize;
        protected int greenSize;
        protected int blueSize;
        protected int alphaSize;
        protected int depthSize;
        protected int stencilSize;
        private int[] value = new int[1];
		
		public ConfigChooser(int r, int g, int b, int a, int depth, int stencil)
        {
            redSize = r;
            greenSize = g;
            blueSize = b;
            alphaSize = a;
            depthSize = depth;
            stencilSize = stencil;
        }
		
		private EGLConfig getMatchingConfig(EGL10 egl, EGLDisplay display, int[] configAttribs) {
			int[] num_config = new int[1];
            egl.eglChooseConfig(display, configAttribs, null, 0, num_config);
            int numConfigs = num_config[0];
            if (numConfigs <= 0)
                throw new IllegalArgumentException("No matching EGL configs");
            
            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, configAttribs, configs, numConfigs,
                num_config);
            return chooseConfig(egl, display, configs);
		}
		
		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
			final int EGL_OPENGL_ES2_BIT = 0x0004;
            final int[] s_configAttribs_gl20 = { EGL10.EGL_RED_SIZE, 4,
                    EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
                    EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                    EGL10.EGL_NONE };
                
            return getMatchingConfig(egl, display, s_configAttribs_gl20);
		}
		
		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
			for (EGLConfig config : configs) {
				int d = findConfigAttrib(egl, display, config,
	                    EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                    EGL10.EGL_STENCIL_SIZE, 0);
                
                if (d < depthSize || s < stencilSize)
                    continue;
                
                int r = findConfigAttrib(egl, display, config,
                    EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config,
                    EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config,
                    EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttrib(egl, display, config,
                    EGL10.EGL_ALPHA_SIZE, 0);
                
                if (r == redSize && g == greenSize && b == blueSize
                    && a == alphaSize)
                    return config;
			}
			
			return null;
		}
		
		private int findConfigAttrib(EGL10 egl, EGLDisplay display,
	            EGLConfig config, int attribute, int defaultValue)
	        {
	            
	            if (egl.eglGetConfigAttrib(display, config, attribute, value))
	                return value[0];
	            
	            return defaultValue;
	        }
	}

}
