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
    protected float platfomSize = 5f;
    Vector3f playerLocation = new Vector3f(), platformLocation = new Vector3f(), platformTopLeft = new Vector3f(), platformBottomRight = new Vector3f();
    private SpatialLearningVWM app;
    private Node rootNode;
    private AssetManager assetManager;
    private Node modelNode;
    private int postionNumber = -1;

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
            Node scene = (Node) assetManager.loadModel(this.getAssetPath(toLoad));
            scene.scale(0.7f);
            scene.setLocalTranslation(modelLocs.get(i));
            models.add(scene);

            PointLight playerLight = new PointLight();
            playerLight.setColor(ColorRGBA.fromRGBA255(5, 5, 5, 5));
            rootNode.addLight(playerLight);
            scene.addControl(new LightControl(playerLight));
        }
    }

    @Override
    protected void onDisable() {

    }

    protected void generateLocations() {
        Vector3f groundStatic = new Vector3f(50f, 1f, 50f);
        Vector3f skyStatic = new Vector3f(-50f, 30f, 50f);
        Vector3f groundChange = new Vector3f(50f, 1f, -50f);
        Vector3f skyChange = new Vector3f(-50f, 30f, -50f);
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
        int[] combination = this.combinations.get(position);
        modelNode.detachAllChildren();
        for (int index : combination) {
            Node model = models.get(index);
            modelNode.attachChild(model);
        }
    }

    public String getAssetPath(String name) {
        return "Models/Cues/" + name + "/" + name + ".glb";
    }

    public void nextPosition() {
        this.postionNumber++;
        loadPositionModels(postionNumber);
    }

    public void foundPosition() {
        this.getStateManager().getState(GuiHandler.class).initGuiBetweenRounds(this);
    }

    public void start() {
        nextPosition();
    }

    public void checkPlayerHasWon(Vector3f platformLocation) {
        playerLocation.set(this.app.getStateManager().getState(GameState.class).getPlayer().getRigidBody().getPhysicsLocation());
        platformTopLeft.set(platformLocation.x - platfomSize, 0, platformLocation.z - platfomSize);
        platformBottomRight.set(platformLocation.x + platfomSize, 0, platformLocation.z + platfomSize);
        if ((playerLocation.x > platformTopLeft.x) && (playerLocation.x < platformBottomRight.x) && (playerLocation.z > platformTopLeft.z) && (playerLocation.z < platformBottomRight.z)) {
            foundPosition();
        }
        System.out.println((playerLocation.x > platformTopLeft.x) && (playerLocation.x < platformBottomRight.x) && (playerLocation.z > platformTopLeft.z) && (playerLocation.z < platformBottomRight.z));
    }

    @Override
    public void update(float tpf) {
        try {
            checkPlayerHasWon(new Vector3f(0f, 0f, 0f));
        } catch (NullPointerException ignored) {
        }
    }
}
