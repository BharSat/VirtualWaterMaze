package com.spatial.learning.jme.game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.LightControl;
import com.jme3.scene.shape.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The type Model handler.
 */
public class ModelHandler extends BaseAppState {
    private final List<int[]> combinations = new ArrayList<>();
    private final List<Node> models = new ArrayList<>();
    public Boolean initialized = false;
    protected String[] modelList = {"Tree", "Jet", "Castle", "Star", "Mill", "Rocket", "Tower", "Star", "Lighthouse", "PBox", "Flag", "Copter", "Castle", "Planet", "Mill", "PBox", "Lighthouse", "Rocket", "Tower", "Glider", "Flag", "Glider"};
    protected List<Vector3f> modelLocs;
    protected float platfomSize = 4.5f;
    protected float[][] platformLocations = {{-13.61f, -7.33f}, {-3.79f, 19.64f}, {7.57f, 11.39f}, {-0.15f, 8.44f}, {-12.78f, 6.83f}, {-3.72f, -8.72f}, {-3.52f, -5.2f}, {-9.19f, -0.2f}, {5.09f, 3.85f}, {-1.64f, -4.03f}};
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
    private Boolean probe;

    private Geometry flag;
    private int modelStartIndex;
    private int modelEndIndex;

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

        Box flagb = new Box(1, 1, 1);
        flag = new Geometry("Box", flagb);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        flag.setMaterial(mat);
        flag.setLocalTranslation(platformLocation);
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
    }

    protected void loadModels(int start, int end) {
        List<Node> oldModels = new ArrayList<Node>(models);
        boolean empty = start == 0;
        if (!empty) {
            models.clear();
            models.add(oldModels.get(0));
            models.add(oldModels.get(1));
        }
        for (int i = start; i < end; i++) {
            if (i > modelStartIndex && i < modelEndIndex) {
                models.add(oldModels.get(i - modelStartIndex));
            } else {
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
        if (!empty) {
            modelStartIndex = start + 2;
            modelEndIndex = end + 2;
        } else {
            modelStartIndex = start;
            modelEndIndex = end;
        }
    }

    @Override
    protected void onDisable() {

    }

    protected void generateLocations() {
        Vector3f groundStatic = new Vector3f(70f, 1f, 0f);
        Vector3f skyStatic = new Vector3f(-70f, 30f, 0f);
        Vector3f groundChange = new Vector3f(0f, 1f, -70f);
        Vector3f skyChange = new Vector3f(0f, 30f, 70f);
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
    }

    protected void loadPositionModels(int position) {
        rootNode.detachChild(modelNode);
        int[] combination = this.combinations.get(position);
        loadModels(position, position + 4);
        modelNode.detachAllChildren();
        for (int index : combination) {
            Node model = getModelAt(index);
            modelNode.attachChild(model);
        }
        rootNode.attachChild(modelNode);
    }

    protected Node getModelAt(int index) {
        if (index == 0) {
            return models.get(0);
        } else if (index == 1) {
            return models.get(1);
        } else {
            return models.get(index - modelStartIndex);
        }

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
        if (positionNumber == 2 || positionNumber == 10) {
            if (trialNumber == 2) {
                probe = true;
            }
        } else if (positionNumber == 4 || positionNumber == 8) {
            if (trialNumber == 4) {
                probe = true;
            }
        } else {
            probe = false;
        }
//        System.out.println(positionNumber + ":" + trialNumber);
        loadPositionModels(positionNumber);
        if (!this.getStateManager().getState(LogHandler.class).newRound(trialNumber, positionNumber)) {
            System.out.println("Failed at " + trialNumber + " " + positionNumber);
        }
        if (probe) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    nextTrial();
                }
            }, 60000);
        }
        platformLocation.set(toPlatFormLocation(platformLocations[positionNumber], new Vector3f()));
        this.getStateManager().getState(GameState.class).startLight();
        this.getStateManager().getState(GameState.class).getPlayer().warp(startLocations[startSets[positionNumber][trialNumber - 1]]);

    }

    public void foundPosition() {
        rootNode.detachChild(flag);
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
            if (!probe) {
                checkPlayerHasWon();
            }
        } catch (NullPointerException ignored) {
        }
    }

    public Vector3f toPlatFormLocation(float[] xy, Vector3f store) {
        store.setX(xy[0] - 12.5f);
        store.setZ(xy[1] - 12.5f);
        return store;
    }

    public void showFlag() {
        rootNode.attachChild(flag);

    }
}
