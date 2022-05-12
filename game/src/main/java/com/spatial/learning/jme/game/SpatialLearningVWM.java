package com.spatial.learning.jme.game;


import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.*;
import com.jme3.scene.shape.*;

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

    public void initFloor() {
        Box floorMesh = new Box(new Vector3f(0f-(locations/2), ground, 0f-(locations/2)), new Vector3f(0f+(locations/2), ground, 0f+(locations/2)));
        Geometry floorGeom = new Geometry("floorGeom", floorMesh);
        Material floorMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floorMat.setTexture("floorTex", assetManager.loadTexture("Textures/Floor.png"));
    }

    public void initScene() {}

}
