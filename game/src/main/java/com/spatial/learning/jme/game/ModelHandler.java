package com.spatial.learning.jme.game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.control.LightControl;

import java.util.ArrayList;
import java.util.List;

public class ModelHandler extends BaseAppState {
    private final List<int[]> combinations = new ArrayList<>();
    private final List<Node> models = new ArrayList<>();
    public Boolean initialized = false;
    protected String[] modelList = {"Tree", "Planet", "Castle", "Star", "Mill", "Rocket", "Tower", "Star", "Lighthouse", "PBox"};
    protected List<Vector3f> modelLocs;
    protected float platfomSize = 1.3f;
    protected float[][] platformLocations = {{}, {}};
    protected Vector3f[] startLocations = {new Vector3f(30, 0, 0), new Vector3f(0, 0, 30), new Vector3f(-30, 0, 0), new Vector3f(-30, 0, -30)};
    protected int[][] startSets = {{1, 0, 2, 0}, {1, 0, 0, 2}, {2, 0, 2, 1}, {0, 2, 1, 2}, {1, 0, 2, 0}, {0, 2, 1, 1}, {1, 0, 2, 0}, {0, 0, 2, 1}, {1, 1, 2, 0}, {1, 0, 1, 2}};
    protected Vector3f platformLocation = new Vector3f(-49, 0, 0);
    protected Vector3f playerLocation = new Vector3f(), platformTopLeft = new Vector3f(), platformBottomRight = new Vector3f();
    private int positionNumber = -1;
    private int trialNumber = 4;
    private SpatialLearningVWM app;
    private Node rootNode;
    private AssetManager assetManager;
    private Node modelNode;

    @Override
    protected void initialize(Application app) {
        if (this.app == null) {
            this.app = (SpatialLearningVWM) app;
        }
        this.assetManager = this.app.getAssetManager();
        this.rootNode = this.app.getRootNode();
        this.generateLocations();
        this.setUpCombinations();
        this.modelNode = new Node();
        rootNode.attachChild(modelNode);
        initialized = true;
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        for (int i = 0; i < this.modelList.length; i++) {
            String toLoad = this.modelList[i];
            Node model = (Node) assetManager.loadModel(this.getAssetPath(toLoad));
            model.scale(0.7f);
            model.setLocalTranslation(modelLocs.get(i));
            models.add(model);

            PointLight modelLight = new PointLight();
            modelLight.setColor(ColorRGBA.fromRGBA255(5, 5, 5, 5));
            getStateManager().getState(GameState.class).getLightNode().addLight(modelLight);
            model.addControl(new LightControl(modelLight));
        }
    }

    @Override
    protected void onDisable() {

    }

    protected void generateLocations() {
        Vector3f groundStatic = new Vector3f(60f, 1f, 60f);
        Vector3f skyStatic = new Vector3f(-60f, 30f, 60f);
        Vector3f groundChange = new Vector3f(60f, 1f, -60f);
        Vector3f skyChange = new Vector3f(-60f, 30f, -60f);
        List<Vector3f> locList = new ArrayList<>();
        locList.add(groundStatic);
        locList.add(skyStatic);
        for (int i = 0; i < 10; i++) {
            locList.add(groundChange);
            locList.add(skyChange);
        }
        this.modelLocs = locList;
    }

    protected void setUpCombinations() {
        if (!this.combinations.isEmpty()) {
            throw new IllegalCallerException("Cannot set up combinations");
        }
        for (int i = 2; i < 10; i += 2) {
            int[] combination;
            combination = new int[]{0, 1, i, i + 1};
            this.combinations.add(combination);
        }
        System.out.println(this.combinations);
    }

    protected void loadPositionModels(int position) {
        rootNode.detachChild(modelNode);
        int[] combination = this.combinations.get(position);
        modelNode.detachAllChildren();
        for (int index : combination) {
            Node model = models.get(index);
            modelNode.attachChild(model);
        }
        rootNode.attachChild(modelNode);
    }

    public String getAssetPath(String name) {
        return "Models/Cues/" + name + "/" + name + ".glb";
    }

    public void nextTrial() {
        this.getStateManager().getState(GameState.class).enabled = true;
        this.trialNumber++;
        if (trialNumber == 5) {
            positionNumber++;
            trialNumber = 1;
        }
//        System.out.println(positionNumber + ":" + trialNumber);
        loadPositionModels(positionNumber);
        this.getStateManager().getState(GameState.class).startLight();
        this.getStateManager().getState(GameState.class).getPlayer().warp(startLocations[startSets[positionNumber][trialNumber - 1]]);
    }

    public void foundPosition() {
        this.getStateManager().getState(GameState.class).getPlayer().warp(new Vector3f(0, 3, 0));
        this.getStateManager().getState(GameState.class).enabled = false;
        this.getStateManager().getState(GameState.class).stopLight();
        this.getStateManager().getState(GuiHandler.class).initGuiBetweenRounds(this);
    }

    public void start() {
        nextTrial();
    }

    public void checkPlayerHasWon() {
        playerLocation.set(this.app.getStateManager().getState(GameState.class).getPlayer().getRigidBody().getPhysicsLocation());
        platformTopLeft.set(platformLocation.x - platfomSize, 0, platformLocation.z - platfomSize);
        platformBottomRight.set(platformLocation.x + platfomSize, 0, platformLocation.z + platfomSize);
        if ((playerLocation.x > platformTopLeft.x) && (playerLocation.x < platformBottomRight.x) && (playerLocation.z > platformTopLeft.z) && (playerLocation.z < platformBottomRight.z)) {
            foundPosition();
        }
//        System.out.println((playerLocation.x > platformTopLeft.x) && (playerLocation.x < platformBottomRight.x) && (playerLocation.z > platformTopLeft.z) && (playerLocation.z < platformBottomRight.z));
    }

    @Override
    public void update(float tpf) {
        try {
            checkPlayerHasWon();
        } catch (NullPointerException ignored) {
        }
    }
}
