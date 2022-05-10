package com.vwmandroid.app.ui.main;

import android.app.Activity;
import android.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLayoutChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.FrameLayout.LayoutParams;
import com.jme3.audio.AudioRenderer;
import com.jme3.input.JoyInput;
import com.jme3.input.android.AndroidSensorJoyInput;
import com.jme3.input.controls.TouchListener;
import com.jme3.input.controls.TouchTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.input.event.TouchEvent;
import com.jme3.system.AppSettings;
import com.jme3.system.SystemListener;
import com.jme3.system.android.JmeAndroidSystem;
import com.jme3.system.android.OGLESContext;
import com.jme3.util.AndroidLogHandler;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.jme3.app.*;
import com.vwmandroid.jme.VWMGame;

public class MainFragment extends Fragment implements TouchListener, OnClickListener, OnLayoutChangeListener, SystemListener {
    private static final Logger logger = Logger.getLogger(AndroidHarnessFragment.class.getName());
    protected String appClass = "com.vwmandroid.jme.VWMGame";
    protected int eglBitsPerPixel = 24;
    protected int eglAlphaBits = 0;
    protected int eglDepthBits = 16;
    protected int eglSamples = 0;
    protected int eglStencilBits = 0;
    protected int frameRate = -1;
    protected int maxResolutionDimension = -1;
    protected String audioRendererType = "OpenAL_SOFT";
    protected boolean joystickEventsEnabled = false;
    protected boolean keyEventsEnabled = true;
    protected boolean mouseEventsEnabled = true;
    protected boolean mouseEventsInvertX = false;
    protected boolean mouseEventsInvertY = false;
    protected boolean finishOnAppStop = true;
    protected boolean handleExitHook = true;
    protected String exitDialogTitle = "Do you want to exit?";
    protected String exitDialogMessage = "Use your home key to bring this app into the background or exit to terminate it.";
    protected int splashPicID = 0;
    protected FrameLayout frameLayout = null;
    protected GLSurfaceView view = null;
    protected ImageView splashImageView = null;
    private final String ESCAPE_EVENT = "TouchEscape";
    private boolean firstDrawFrame = true;
    private VWMGame app = null;
    private int viewWidth = 0;
    private int viewHeight = 0;

    public MainFragment() {
    }

    public Application getJmeApplication() {
        return this.app;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onCreate(Bundle savedInstanceState) {
        this.initializeLogHandler();
        logger.fine("onCreate");
        super.onCreate(savedInstanceState);
        logger.log(Level.FINE, "Creating settings");
        AppSettings settings = new AppSettings(true);
        settings.setEmulateMouse(this.mouseEventsEnabled);
        settings.setEmulateMouseFlipAxis(this.mouseEventsInvertX, this.mouseEventsInvertY);
        settings.setUseJoysticks(this.joystickEventsEnabled);
        settings.setEmulateKeyboard(this.keyEventsEnabled);
        settings.setBitsPerPixel(this.eglBitsPerPixel);
        settings.setAlphaBits(this.eglAlphaBits);
        settings.setDepthBits(this.eglDepthBits);
        settings.setSamples(this.eglSamples);
        settings.setStencilBits(this.eglStencilBits);
        settings.setAudioRenderer(this.audioRendererType);
        settings.setFrameRate(this.frameRate);

        try {
            if (this.app == null) {
                Class clazz = Class.forName(this.appClass);
                this.app = (VWMGame)clazz.newInstance();
            }

            this.app.setSettings(settings);
            this.app.start();
        } catch (Exception var4) {
            this.handleError("Class " + this.appClass + " init failed", var4);
        }

        OGLESContext ctx = (OGLESContext)this.app.getContext();
        ctx.setSystemListener(this);
        this.setRetainInstance(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        logger.fine("onCreateView");
        this.view = ((OGLESContext)this.app.getContext()).createView(this.getActivity());
        JmeAndroidSystem.setView(this.view);
        this.createLayout();
        this.view.addOnLayoutChangeListener(this);
        return this.frameLayout;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        logger.fine("onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    public void onStart() {
        logger.fine("onStart");
        super.onStart();
    }

    public void onResume() {
        logger.fine("onResume");
        super.onResume();
        this.gainFocus();
    }

    public void onPause() {
        logger.fine("onPause");
        this.loseFocus();
        super.onPause();
    }

    public void onStop() {
        logger.fine("onStop");
        super.onStop();
    }

    public void onDestroyView() {
        logger.fine("onDestroyView");
        if (this.splashImageView != null && this.splashImageView.getParent() != null) {
            ((ViewGroup)this.splashImageView.getParent()).removeView(this.splashImageView);
        }

        if (this.view.getParent() != null) {
            ((ViewGroup)this.view.getParent()).removeView(this.view);
        }

        if (this.frameLayout != null && this.frameLayout.getParent() != null) {
            ((ViewGroup)this.frameLayout.getParent()).removeView(this.frameLayout);
        }

        this.view.removeOnLayoutChangeListener(this);
        this.splashImageView = null;
        this.frameLayout = null;
        this.view = null;
        JmeAndroidSystem.setView((View)null);
        super.onDestroyView();
    }

    public void onDestroy() {
        logger.fine("onDestroy");
        if (this.app != null) {
            this.app.stop(false);
        }

        this.app = null;
        super.onDestroy();
    }

    public void onDetach() {
        logger.fine("onDetach");
        super.onDetach();
    }

    public void handleError(String errorMsg, Throwable t) {
        String stackTrace = "";
        String title = "Error";
        if (t != null) {
            StringWriter sw = new StringWriter(100);
            t.printStackTrace(new PrintWriter(sw));
            stackTrace = sw.toString();
            title = t.toString();
        }
        final String finalTitle = title;

        final String finalMsg = (errorMsg != null ? errorMsg : "Uncaught Exception") + "\n" + stackTrace;
        logger.log(Level.SEVERE, finalMsg);
        this.getActivity().runOnUiThread(() -> {
            Builder builder = new Builder(MainFragment.this.getActivity());
            builder.setTitle(finalTitle);
            builder.setPositiveButton("Kill", MainFragment.this);
            builder.setMessage(finalMsg);
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    public void onClick(DialogInterface dialog, int whichButton) {
        if (whichButton != -2) {
            if (this.app != null) {
                this.app.stop(true);
            }

            this.app = null;
            this.getActivity().finish();
        }
        else {}

    }

    public void onTouch(String name, TouchEvent evt, float tpf) {
        if (name.equals("TouchEscape")) {
            switch(evt.getType()) {
                case KEY_UP:
                    this.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Builder builder = new Builder(MainFragment.this.getActivity());
                            builder.setTitle(MainFragment.this.exitDialogTitle);
                            builder.setPositiveButton("Yes", MainFragment.this);
                            builder.setNegativeButton("No", MainFragment.this);
                            builder.setMessage(MainFragment.this.exitDialogMessage);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });
            }
        }
        else {
            this.app.onTouch(name, evt, tpf);
        }

    }

    public void createLayout() {
        logger.log(Level.FINE, "Splash Screen Picture Resource ID: {0}", this.splashPicID);
        LayoutParams lp = new LayoutParams(-1, -1, 17);
        if (this.frameLayout != null && this.frameLayout.getParent() != null) {
            ((ViewGroup)this.frameLayout.getParent()).removeView(this.frameLayout);
        }

        this.frameLayout = new FrameLayout(this.getActivity());
        if (this.view.getParent() != null) {
            ((ViewGroup)this.view.getParent()).removeView(this.view);
        }

        this.frameLayout.addView(this.view);
        if (this.splashPicID != 0) {
            this.splashImageView = new ImageView(this.getActivity());
            Drawable drawable = this.getResources().getDrawable(this.splashPicID);
            if (drawable instanceof NinePatchDrawable) {
                this.splashImageView.setBackgroundDrawable(drawable);
            } else {
                this.splashImageView.setImageResource(this.splashPicID);
            }

            if (this.splashImageView.getParent() != null) {
                ((ViewGroup)this.splashImageView.getParent()).removeView(this.splashImageView);
            }

            this.frameLayout.addView(this.splashImageView, lp);
            logger.fine("Splash Screen Created");
        } else {
            logger.fine("Splash Screen Skipped.");
        }

    }

    public void removeSplashScreen() {
        logger.log(Level.FINE, "Splash Screen Picture Resource ID: {0}", this.splashPicID);
        if (this.splashPicID != 0) {
            if (this.frameLayout != null) {
                if (this.splashImageView != null) {
                    this.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            MainFragment.this.splashImageView.setVisibility(View.INVISIBLE);
                            MainFragment.this.frameLayout.removeView(MainFragment.this.splashImageView);
                        }
                    });
                } else {
                    logger.fine("splashImageView is null");
                }
            } else {
                logger.fine("frameLayout is null");
            }
        }

    }

    protected void initializeLogHandler() {
        Logger log = LogManager.getLogManager().getLogger("");
        Handler[] var2 = log.getHandlers();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Handler handler = var2[var4];
            if (log.getLevel() != null && log.getLevel().intValue() <= Level.FINE.intValue()) {
                Log.v("AndroidHarness", "Removing Handler class: " + handler.getClass().getName());
            }

            log.removeHandler(handler);
        }

        Handler handler = new AndroidLogHandler();
        log.addHandler(handler);
        handler.setLevel(Level.ALL);
    }

    public void initialize() {
        this.app.initialize();
        if (this.handleExitHook) {
            if (this.app.getInputManager().hasMapping("SIMPLEAPP_Exit")) {
                this.app.getInputManager().deleteMapping("SIMPLEAPP_Exit");
            }

            this.app.getInputManager().addMapping("TouchEscape", new Trigger[]{new TouchTrigger(4)});
            this.app.getInputManager().addListener(this, new String[]{"TouchEscape"});
        }

    }

    public void reshape(int width, int height) {
        this.app.reshape(width, height);
    }

    public void update() {
        this.app.update();
        if (this.firstDrawFrame) {
            this.removeSplashScreen();
            this.firstDrawFrame = false;
        }

    }

    public void requestClose(boolean esc) {
        this.app.requestClose(esc);
    }

    public void destroy() {
        if (this.app != null) {
            this.app.destroy();
        }

        if (this.finishOnAppStop) {
            this.getActivity().finish();
        }

    }

    public void gainFocus() {
        logger.fine("gainFocus");
        if (this.view != null) {
            this.view.onResume();
        }

        if (this.app != null) {
            AudioRenderer audioRenderer = this.app.getAudioRenderer();
            if (audioRenderer != null) {
                audioRenderer.resumeAll();
            }

            if (this.app.getContext() != null) {
                JoyInput joyInput = this.app.getContext().getJoyInput();
                if (joyInput != null && joyInput instanceof AndroidSensorJoyInput) {
                    AndroidSensorJoyInput androidJoyInput = (AndroidSensorJoyInput)joyInput;
                    androidJoyInput.resumeSensors();
                }
            }
        }

        if (this.app != null) {
            this.app.gainFocus();
        }

    }

    public void loseFocus() {
        logger.fine("loseFocus");
        if (this.app != null) {
            this.app.loseFocus();
        }

        if (this.view != null) {
            this.view.onPause();
        }

        if (this.app != null) {
            AudioRenderer audioRenderer = this.app.getAudioRenderer();
            if (audioRenderer != null) {
                audioRenderer.pauseAll();
            }

            if (this.app.getContext() != null) {
                JoyInput joyInput = this.app.getContext().getJoyInput();
                if (joyInput != null && joyInput instanceof AndroidSensorJoyInput) {
                    AndroidSensorJoyInput androidJoyInput = (AndroidSensorJoyInput)joyInput;
                    androidJoyInput.pauseSensors();
                }
            }
        }

    }

    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (v.equals(this.view) && v.equals(this.view) && this.maxResolutionDimension > 0) {
            int newWidth = right - left;
            int newHeight = bottom - top;
            if (this.viewWidth != newWidth || this.viewHeight != newHeight) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "SurfaceView layout changed: old width: {0}, old height: {1}, new width: {2}, new height: {3}", new Object[]{this.viewWidth, this.viewHeight, newWidth, newHeight});
                }

                this.viewWidth = newWidth;
                this.viewHeight = newHeight;
                int fixedSizeWidth = this.viewWidth;
                int fixedSizeHeight = this.viewHeight;
                if (this.viewWidth > this.viewHeight && this.viewWidth > this.maxResolutionDimension) {
                    fixedSizeWidth = this.maxResolutionDimension;
                    fixedSizeHeight = (int)((float)this.maxResolutionDimension * ((float)this.viewHeight / (float)this.viewWidth));
                } else if (this.viewHeight > this.viewWidth && this.viewHeight > this.maxResolutionDimension) {
                    fixedSizeWidth = (int)((float)this.maxResolutionDimension * ((float)this.viewWidth / (float)this.viewHeight));
                    fixedSizeHeight = this.maxResolutionDimension;
                } else if (this.viewWidth == this.viewHeight && this.viewWidth > this.maxResolutionDimension) {
                    fixedSizeWidth = this.maxResolutionDimension;
                    fixedSizeHeight = this.maxResolutionDimension;
                }

                if (fixedSizeWidth != this.viewWidth || fixedSizeHeight != this.viewHeight) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "setting surfaceview resolution to width: {0}, height: {1}", new Object[]{fixedSizeWidth, fixedSizeHeight});
                    }

                    this.view.getHolder().setFixedSize(fixedSizeWidth, fixedSizeHeight);
                }
            }
        }

    }
}
