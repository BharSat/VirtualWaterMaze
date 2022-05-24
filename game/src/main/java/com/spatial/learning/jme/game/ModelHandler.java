package com.spatial.learning.jme.game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class ModelHandler extends BaseAppState {
    private SpatialLearningVWM app;
    private final String[] modelList = {"Models/Cues/Star.obj"};
    private Node rootNode;
    private AssetManager assetManager;

    private Boolean initialized = false;

    @Override
    protected void initialize(Application app) {
        if (this.app==null) {this.app = (SpatialLearningVWM) app;}
        this.assetManager = this.app.getAssetManager();
        this.rootNode = this.app.getRootNode();
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        for (int i = 0; i < this.modelList.length; i++) {
            String toLoad = this.modelList[i];
            Spatial scene = assetManager.loadModel(toLoad);
            scene.scale(1.5f, 1.5f, 1.5f);
            rootNode.attachChild(scene);
        }
    }

    @Override
    protected void onDisable() {

    }
}
