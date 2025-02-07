package com.spatial.learning.jme.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.spatial.learning.jme.game.GameState;

public class VrState extends BaseAppState {
    SimpleApplication app;
    boolean enabled;
    float width, height;
    Camera cam, cam2;
    ViewPort view2;
    AndroidLauncher launcher;
    private SensorManager sensorManager;
    private Sensor LASensor;
    private Sensor GYSensor;
    private SensorListener SensorListener;
    Float xa, ya, za, xg=null, yg=null, zg=null, wg=null;
    long prevLA, prevGY;
    Vector3f prevVelocity=new Vector3f(), finalVelocity=new Vector3f(), sAccel=new Vector3f(), position=new Vector3f(), calibratedVector=new Vector3f();
    Quaternion calibratedQuaternion;
    boolean calibrating = false;

    VrState(AndroidLauncher launcher) {
        this.launcher = launcher;
        sensorManager = (SensorManager) launcher.getSystemService(Context.SENSOR_SERVICE);
        LASensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        GYSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        SensorListener = new SensorListener();
        sensorManager.registerListener(SensorListener, LASensor,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(SensorListener, GYSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void calibrateStart() {
        calibrating = true;
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        width = app.getCamera().getWidth();
        height = app.getCamera().getHeight();

        cam = app.getCamera();
        cam2 = app.getCamera().clone();
        cam.setViewPort(0.0f , 1.0f   ,   0.0f , 1.0f);
        cam2.setViewPort(0.5f , 1.0f   ,   0.0f , 0.5f);
        view2 = app.getRenderManager().createMainView("view2", cam2);
        view2.attachScene(app.getRootNode());
        view2.attachScene(app.getGuiNode());
        enabled = true;
    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void update(float tpf) {
        if (enabled) {
            cam2.setLocation(app.getCamera().getLocation().add(app.getCamera().getLeft().negate().mult(0.5f)));
            cam2.setRotation(cam.getRotation());
        }
    }

    public void LASensorChanged(float x, float y, float z, long t) {
        if (!enabled) {
            prevVelocity=new Vector3f(-x, 0, -z);
        } else {
            float dt = (t-prevLA)/(10^9);
            ComputeFinalVelocity(prevVelocity, new Vector3f(x, 0, z), dt);
            try {
                app.getStateManager().getState(GameState.class).getPlayer().setWalkDirection(finalVelocity);
                app.getStateManager().getState(GameState.class).up = true;
                System.out.println(""  +app.getStateManager().getState(GameState.class).getPlayer().getRigidBody().getPhysicsLocation().toString());
                System.out.println("Final" + finalVelocity +"  " +dt);
            } catch (NullPointerException ignored) {
            } catch (Throwable e) {
                e.printStackTrace();
                ((SimpleApplication)app).destroy();
            }
            prevVelocity=new Vector3f(x, 0, z);
        }
        positionTrackerUpdate();
        prevLA = t;
    }
    public void GYSensorChanged(float x, float y, float z, float w, long t) {
        xg=x;
        yg=y;
        zg=z;
        wg=w;
        //app.getCamera().setRotation(app.getCamera().getRotation().add(new Quaternion(x-defXg, y-defYg, z-defZg, w-defWg)));
        prevGY = t;
    }
    public void ComputeFinalVelocity(Vector3f u, Vector3f a, float t) {
        try {
            finalVelocity = u.add(a.add(sAccel).multLocal(t));
            finalVelocity.add(prevVelocity.negate());
        } catch (NullPointerException e) {
            finalVelocity = new Vector3f(0, 0, 0);
        }
    }

    private class SensorListener implements SensorEventListener {
        SensorListener() {}

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION) {
                LASensorChanged(
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2],
                        sensorEvent.timestamp
                );
            } else if (sensorEvent.sensor.getType()==Sensor.TYPE_GYROSCOPE) {
                GYSensorChanged(
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2],
                        sensorEvent.values[3],
                        sensorEvent.timestamp
                );
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
    public void positionTrackerUpdate() {

    }
}
