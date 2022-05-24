package com.spatial.learning.jme.game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

public class CamState extends BaseAppState implements ActionListener {
    Boolean up = false, down = false, left = false, right = false;
    SpatialLearningVWM app;
    Camera cam;
    Vector3f camDir = new Vector3f(0, 0, 0), camLeft = new Vector3f(0, 0, 0);
    @Override
    protected void initialize(Application app) {
        this.app = (SpatialLearningVWM) app;
        this.cam = this.app.getCamera();
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        initActions();
    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void update(float tpf) {
        camDir.set(cam.getDirection().clone().mult(.06f));
        camLeft.set(cam.getLeft().clone().mult(.04f));
        if (up) {
            cam.setLocation(cam.getLocation().add(camDir));
        }
        if (down) {
            cam.setLocation(cam.getLocation().add(camDir.negate()));
        }
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        float pi = FastMath.PI;
        switch (name) {
            case "Front":
                this.up = isPressed;
            case "Back":
                this.down = isPressed;
            case "Left":
                this.left = isPressed;
            case "Right":
                this.right = isPressed;
            case "MUp":
                this.cam.setRotation(this.cam.getRotation().add(new Quaternion(0, +((0.01f*pi)/180), 0, 1)));
            case "MDown":
                this.cam.setRotation(this.cam.getRotation().add(new Quaternion(0, +((0.01f*pi)/180), 0, 1)));
            case "MLeft":
                this.cam.setRotation(this.cam.getRotation().add(new Quaternion(0, 0, -((0.01f*pi)/180), 1)));
            case "MRight":
                this.cam.setRotation(this.cam.getRotation().add(new Quaternion(0, 0, -((0.01f*pi)/180), 1)));
        }
        System.out.println(name);
    }

    void initActions() {
        InputManager inputManager = this.app.getInputManager();
        inputManager.addMapping("MLeft", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("MRight", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("MUp", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("MDown", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("Front", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Back", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addListener(this, "Front", "Left", "Right", "Back", "MLeft", "MRight", "MUp", "MDown");
    }

    void rotateUp() {
        float x, y, z, w;
        Quaternion rotation = cam.getRotation();
        x = rotation.getX();
        y = rotation.getY();
        z = rotation.getZ();
        w = rotation.getW();

    }
}
