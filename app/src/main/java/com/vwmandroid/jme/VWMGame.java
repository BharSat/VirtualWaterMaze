package com.vwmandroid.jme;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.TouchListener;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import quick.game.app.QuickGameState;

public class VWMGame extends SimpleApplication implements TouchListener, ActionListener {

    protected Boolean up = false;
    protected Boolean created = false;

    @Override
    public void simpleInitApp() {
        stateManager.attach(new BulletAppState());
        stateManager.attach(new QuickGameState());
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (up) {
            Vector3f nl = cam.getDirection().clone().add(0f, 0f, 1f);
            stateManager.getState(QuickGameState.class).playerAppState.player.setPhysicsLocation(nl);
        }
        if (!created) {
            try {
                stateManager.getState(QuickGameState.class).createFloor(new Vector3f(-20f, -1f, -20f), new Vector3f(20f, -1f, 20f));
                stateManager.getState(QuickGameState.class).startSun(new Vector3f(2.8f, -2.8f, -2.8f));
                created=true;
            }
            catch (NullPointerException e) { created = false;}
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {

    }

    @Override
    public void onTouch(String name, TouchEvent evt, float tpf) {
        if (evt.getType().equals(TouchEvent.Type.DOWN)) {
            up = true;
        } else if (evt.getType().equals(TouchEvent.Type.UP)) {
            up = false;
        }
    }

    @Override
    public void onAction(String s, boolean b, float v) {

    }
}
