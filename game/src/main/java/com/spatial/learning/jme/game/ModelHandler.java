package com.spatial.learning.jme.game;

import static java.lang.Math.pow;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.scene.Node;
import com.jme3.scene.control.LightControl;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The type Model handler.
 */
public class ModelHandler extends BaseAppState {
    private final Map<String, List<Float>> cueMap = new HashMap<>();
    public Boolean initialized = false;
    public String modelPathFormat;
    public float scale, playerSpeed, retardFactor;
    public boolean fileInited = false;
    protected Vector3f playerLocation = new Vector3f(), platformTopLeft = new Vector3f(), platformBottomRight = new Vector3f();
    private int positionNumber = -1, trialNumber, maxTrials, maxSessions;
    private float startX, startZ, endX, endZ, endXLength, endZLength;
    private String endShape, rootDir;
    private SpatialLearningVWM app;
    private DirectionalLight sun;
    private PointLight sceneLight;
    private final Node lightNode = new Node();
    private Node rootNode;
    private AssetManager assetManager;
    private Node modelNode;
    private Boolean probe;
    private Boolean enabled = false;
    private Timer timer = new Timer();
    private FilterPostProcessor fpp;
    private FogFilter fog;
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            timeUp();
        }
    };
    private BluetoothManager bluetoothManager;
    private boolean isDirectBluetooth = false;
    private boolean isTransmitter = true;

    @Override
    protected void initialize(Application app) {
        if (this.app == null) {
            this.app = (SpatialLearningVWM) app;
        }
        this.isDirectBluetooth = this.app.isDirectBluetooth;
        this.isTransmitter = this.app.isTransmitter;
        this.assetManager = this.app.getAssetManager();
        this.rootNode = this.app.getRootNode();
        this.modelNode = new Node();
        rootNode.attachChild(modelNode);
        initialized = true;
    }

    protected void readSettingsFile(String path_to_game_file) {
        try {
            File rootName = new File(path_to_game_file);
            Path rootPath = rootName.toPath();
            Path parentDir = rootPath.getParent();
            rootDir = parentDir.toFile().getAbsolutePath();
        } catch (NullPointerException e) {
            rootDir = "";
            System.out.println();
        }
        this.getState(LogHandler.class).root(rootDir);
        this.assetManager.registerLocator(rootDir, FileLocator.class);

        this.app.fileReader.openFile(path_to_game_file);
        String content = this.app.fileReader.readFile();
        this.app.dataReader = new DataReader();
        this.app.data = this.app.dataReader.stringToData(content);
        // System.out.println("Data: ");
        // System.out.println(this.app.data);
        maxSessions = Integer.parseInt(app.data.get("data").get("sessions"));
        maxTrials = Integer.parseInt(app.data.get("data").get("trials"));
        trialNumber = maxTrials;
        modelPathFormat = app.data.get("data").get("cue_format");
        scale = Float.parseFloat(app.data.get("data").get("scale"));
        float version = Float.parseFloat(app.data.get("data").get("version"));
        if (version == 1.0f) {
            scale = scale / 2;
        }
        playerSpeed = Float.parseFloat(app.data.get("data").get("speed"));
        retardFactor = Float.parseFloat(app.data.get("data").get("retard_factor"));

        sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        lightNode.addLight(sun);

        sceneLight = new PointLight();
        sceneLight.setColor(ColorRGBA.White.mult(0.3f));
        lightNode.addLight(sceneLight);
        LightControl playerLight = new LightControl(sceneLight);


        Node scene = (Node) assetManager.loadModel("models/scene.glb");
        scene.setLocalTranslation(0f, this.app.getGround(), 0f);
        scene.scale(scale / 50, scale / 200, scale / 50);
        scene.addControl(playerLight);

        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(scene);
        RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0f);
        getState(BulletAppState.class).getPhysicsSpace().add(landscape);
        rootNode.attachChild(scene);
        // SkyFactory.createSky(assetManager, "Textures/BrightSky.dds", SkyFactory.EnvMapType.CubeMap);
        if (version >= 3.0f) {
            fpp = new FilterPostProcessor(assetManager);
            //fpp.setNumSamples(4);
            int numSamples = getApplication().getContext().getSettings().getSamples();
            if (numSamples > 0) {
                fpp.setNumSamples(numSamples);
            }
            fog = new FogFilter();
            fog.setFogColor(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
            fog.setFogDistance(Float.parseFloat(app.data.get("data").get("fog_distance")));
            fog.setFogDensity(Float.parseFloat(app.data.get("data").get("fog_density")));
            fpp.addFilter(fog);
            getApplication().getViewPort().addProcessor(fpp);
        }

        fileInited = true;
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {

    }

    private boolean boolYesNo(String yesNo) {
        return yesNo.equals("yes");
    }

    public void stopLight() {
        for (Light light : lightNode.getLocalLightList()) {
            rootNode.removeLight(light);
        }
        this.app.getViewPort().setBackgroundColor(ColorRGBA.Black);
    }

    public void startLight() {
        for (Light light : lightNode.getLocalLightList()) {
            rootNode.addLight(light);
        }

        this.app.getViewPort().setBackgroundColor(ColorRGBA.fromRGBA255(51, 204, 255, 255));
    }

    public Node getLightNode() {
        return lightNode;
    }

    protected void loadPosition(int position, int trial) {
        rootNode.detachChild(modelNode);
        modelNode.detachAllChildren();
        Map<String, String> data = this.app.data.get((position + 1) + " " + (trial + 1));
        probe = boolYesNo(data.get("probe"));
        String[] startData = data.get("start").split(" ");
        startX = Float.parseFloat(startData[0]);
        startZ = Float.parseFloat(startData[1]);


        String[] endData = data.get("end").split(" ");
        endX = Float.parseFloat(endData[0]);
        endZ = Float.parseFloat(endData[1]);
        endShape = endData[4];
        endXLength = Float.parseFloat(endData[2]);
        endZLength = Float.parseFloat(endData[3]);

        for (int i = 0, n = Integer.parseInt(data.get("cues")); i < n; i++) {
            String[] cueData = data.get("cue" + (i + 1)).split(" ");

            Float[] cueLocArr = {Float.parseFloat(cueData[1]), Float.parseFloat(cueData[2]), Float.parseFloat(cueData[3])};
            List<Float> cueLoc = Arrays.asList(cueLocArr);
            String name = cueData[4].replace("\"", "").trim();
            cueMap.put(name, cueLoc);
            File cueRootFile = new File(rootDir + "/" +
                    modelPathFormat.replace("<model_name>", name));
            Path cueRootPath = cueRootFile.toPath().getParent();
            String cueRootPathStr = cueRootPath.toString();
            Node model;
            try {
                model = (Node) assetManager.loadModel(cueRootFile.getPath());
            } catch (IllegalArgumentException ignored) {
                this.assetManager.registerLocator(cueRootPathStr, FileLocator.class);
                model = (Node) assetManager.loadModel(cueRootFile.getName());
            } catch (AssetNotFoundException e) {
                model = (Node) assetManager.loadModel(modelPathFormat.replace("<model_name>", name));
            }
            model.setLocalTranslation(cueLoc.get(0), cueLoc.get(1), cueLoc.get(2));
            modelNode.attachChild(model);
        }
        double y = Math.random() * 2 * Math.PI;
        Quaternion rotation = new Quaternion();
        rotation.fromAngleAxis((float) y, Vector3f.UNIT_Y);
        this.getApplication().getCamera().setRotation(rotation);

        rootNode.attachChild(modelNode);
    }

    public void nextTrial() {
        this.getStateManager().getState(GameState.class).enabled = true;
        enabled = true;
        task.cancel();
        task = new TimerTask() {
            @Override
            public void run() {
                timeUp();
            }
        };
        if (trialNumber == maxTrials) {
            positionNumber++;
            trialNumber = -1;
        }
        this.trialNumber++;
        loadPosition(positionNumber, trialNumber);
        if (!this.getStateManager().getState(LogHandler.class).newRound(trialNumber, positionNumber)) {
            System.out.println("Failed to open log file at " + trialNumber + " " + positionNumber);
        }
        timer = new Timer();
        if (probe) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    foundPosition();
                }
            }, 60000);
        } else {
            timer.schedule(task, 60000);
        }
        this.startLight();
        this.getStateManager().getState(GameState.class).getPlayer().warp(new Vector3f(startX, 1.0f, startZ));

    }

    public void foundPosition() {
        this.getStateManager().getState(GameState.class).getPlayer().warp(new Vector3f(0, 3, 0));
        this.getStateManager().getState(GameState.class).enabled = false;
        this.enabled = false;
        this.stopLight();
        this.getStateManager().getState(GuiHandler.class).initGuiBetweenRounds(this);
    }

    public void start() {
        nextTrial();
    }

    public void checkPlayerHasWon() {
        if (enabled) {
            playerLocation.set(this.app.getStateManager().getState(GameState.class).getPlayer().getRigidBody().getPhysicsLocation());
            platformTopLeft.set(endX - endXLength / 2, 0, endZ - endZLength / 2);
            platformBottomRight.set(endX + endXLength / 2, 0, endZ + endZLength / 2);
            if (endShape.equals("rect") && (playerLocation.x > platformTopLeft.x) && (playerLocation.x < platformBottomRight.x) && (playerLocation.z > platformTopLeft.z) && (playerLocation.z < platformBottomRight.z)) {
                foundPosition();
            } else if (endShape.equals("circle") && isInsideEllipse(endX, endZ, playerLocation.x, playerLocation.z, endXLength, endZLength)) {
                foundPosition();
            }
//            System.out.println("" + trialNumber + playerLocation.x + platformTopLeft.x + playerLocation.x + platformBottomRight.x + playerLocation.z + platformTopLeft.z + playerLocation.z + platformBottomRight.z);
        }
    }

    public boolean isInsideEllipse(float h, float k, float x, float y, float a, float b) {
        float res = (float) ((pow((x - h), 2) / pow(a, 2)) + (pow((y - k), 2) / pow(b, 2)));
        return res <= 1;
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

    private void timeUp() {
        Node Flag = (Node) assetManager.loadModel("models/Flag/Flag.glb");
        Flag.scale(10f, 10f, 10f);
        Flag.setLocalTranslation(new Vector3f(endX, this.app.getGround(), endZ));
        modelNode.attachChild(Flag);
    }

    public void setBluetoothManager(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }

}
