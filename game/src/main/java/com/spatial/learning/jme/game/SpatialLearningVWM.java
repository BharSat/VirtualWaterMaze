package com.spatial.learning.jme.game;


import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.*;
import com.jme3.scene.shape.*;

import java.util.List;

public class SpatialLearningVWM extends SimpleApplication {
    private BulletAppState physics;
    private final float locations = 50.00f;
    private final float ground = -1f;

    public SpatialLearningVWM() {
    }

    public SpatialLearningVWM(AppState... initialStates) {
        super(initialStates);
    }

    @Override
    public void simpleInitApp() {
        this.physics = new BulletAppState();
        stateManager.attach(physics);
    }

    public float getTotalLocations() {
        return this.locations;
    }

    public float getGround () {
        return this.ground;
    }


    public BulletAppState getPhysics() {
        return physics;
    }
}
