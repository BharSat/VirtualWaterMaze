package com.spatial.learning.jme.game;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.bullet.BulletAppState;

public class SpatialLearningVWM extends SimpleApplication {
    private final float locations = 50.00f;
    private final float ground = -1f;

    public SpatialLearningVWM() {
    }

    public SpatialLearningVWM(AppState... initialStates) {
        super(initialStates);
    }

    @Override
    public void simpleInitApp() {
        stateManager.attach(new BulletAppState());
        stateManager.attach(new GameState());
        System.out.println("Init done.");
    }

    public float getTotalLocations() {
        return this.locations;
    }

    public float getGround() {
        return this.ground;
    }

}
