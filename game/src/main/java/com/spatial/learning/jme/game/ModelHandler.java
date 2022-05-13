package com.spatial.learning.jme.game;

public class ModelHandler {
    private SpatialLearningVWM app;
    ModelHandler (SpatialLearningVWM app) {
        this.app = app;
    }

    public void initialize() {
        Node rootNode = this.app.getRootNode();
        AssetManager assetManager = this.app.getAssetManager();
    }
}
