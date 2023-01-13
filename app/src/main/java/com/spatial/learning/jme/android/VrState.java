package com.spatial.learning.jme.android;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;

public class VrState extends BaseAppState {
    Application app;
    boolean enabled;
    float width, height;
    Camera cam2;
    ViewPort view2;
    @Override
    protected void initialize(Application app) {
        this.app = app;
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        width = app.getCamera().getWidth();
        height = app.getCamera().getHeight();
        cam2 = app.getCamera().clone();
        view2 = app.getRenderManager().createMainView("view2", cam2);
        view2.attachScene(((SimpleApplication)app).getRootNode());
        view2.attachScene(((SimpleApplication)app).getGuiNode());
        enabled = true;
    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void update(float tpf) {
        System.out.println("Update"+enabled);
        if (enabled) {
            app.getCamera().setViewPort(0, width/2, height/2, 0);
            cam2.setLocation(app.getCamera().getLocation().add(app.getCamera().getLeft().negate().mult(0.5f)));
            cam2.setViewPort(width/2, width, height, height/2);
        }
    }
}
