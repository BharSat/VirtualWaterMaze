package com.spatial.learning.jme.game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static java.lang.Math.pow;

/**
 * The type Model handler.
 */
public class ModelHandler extends BaseAppState {
    private final Map<String, List<Float>> cueMap = new HashMap<>();
    public Boolean initialized = false;
    public String modelPathFormat;
    public float scale, playerSpeed;
    public boolean fileInited = false;
    protected Vector3f playerLocation = new Vector3f(), platformTopLeft = new Vector3f(), platformBottomRight = new Vector3f();
    private int positionNumber = -1, trialNumber, maxTrials, maxSessions;
    private float startX, startZ, endX, endZ, endXLength, endZLength;
    private String endShape, rootDir;
    private SpatialLearningVWM app;
    private Node rootNode;
    private AssetManager assetManager;
    private Node modelNode;
    private Boolean probe;

    @Override
    protected void initialize(Application app) {
        if (this.app == null) {
            this.app = (SpatialLearningVWM) app;
        }
        this.assetManager = this.app.getAssetManager();
        this.rootNode = this.app.getRootNode();
        this.modelNode = new Node();
        rootNode.attachChild(modelNode);
        initialized = true;
    }

    protected void readSettingsFile(String path_to_game_file) {
        File rootName = new File(path_to_game_file);
        Path rootPath = rootName.toPath();
        Path parentDir = rootPath.getParent();
        rootDir = parentDir.toFile().getAbsolutePath();//path_to_game_file.substring(0, path_to_game_file.lastIndexOf('/'));
        this.getState(LogHandler.class).root(rootDir);
        this.assetManager.registerLocator(rootDir, FileLocator.class);
        System.out.println(rootDir);
        this.app.projectManager = ProjectManager.newProject("", path_to_game_file);
        FileReader reader;
        try {
            reader = ProjectManager.openFileReader(path_to_game_file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader bReader = new BufferedReader(reader);
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        String ls = System.getProperty("line.separator");
        while (true) {
            try {
                if ((line = bReader.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        try {
            bReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String content = stringBuilder.toString();
        this.app.reader = new Reader(this.app.projectManager);
        this.app.data = this.app.reader.stringToData(content);
        // System.out.println("Data: ");
        // System.out.println(this.app.data);
        maxSessions = Integer.parseInt(app.data.get("data").get("sessions"));
        maxTrials = Integer.parseInt(app.data.get("data").get("trials"));
        trialNumber = maxTrials - 1;
        modelPathFormat = app.data.get("data").get("cue_format");
        scale = Float.parseFloat(app.data.get("data").get("scale"));
        playerSpeed = Float.parseFloat(app.data.get("data").get("speed"));

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

    protected void loadPosition(int position, int trial) {
        rootNode.detachChild(modelNode);
        modelNode.detachAllChildren();
        Map<String, String> data = this.app.data.get((position + 1) + " " + (trial + 1));
        probe = Boolean.parseBoolean(data.get("probe"));
        String[] startData = data.get("start").split(" ");
        startX = Float.parseFloat(startData[0]);
        startZ = Float.parseFloat(startData[1]);


        String[] endData = data.get("end").split(" ");
        endX = Float.parseFloat(endData[0]);
        endZ = Float.parseFloat(endData[1]);
        endShape = endData[4];
        endXLength = Float.parseFloat(endData[2]);
        endXLength = Float.parseFloat(endData[3]);

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
            this.assetManager.registerLocator(cueRootPathStr, FileLocator.class);
            Node model = (Node) assetManager.loadModel(cueRootFile.getName());
            model.setLocalTranslation(cueLoc.get(0), cueLoc.get(1), cueLoc.get(2));
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
        if (trialNumber == maxTrials) {
            positionNumber++;
            trialNumber = 1;
        }
//        System.out.println(positionNumber + ":" + trialNumber);
        loadPosition(positionNumber, trialNumber);
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
        this.getStateManager().getState(GameState.class).startLight();
        this.getStateManager().getState(GameState.class).getPlayer().warp(new Vector3f(startX, 1.0f, startZ));

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
        platformTopLeft.set(endX - endXLength / 2, 0, endZ - endZLength / 2);
        platformBottomRight.set(endX + endXLength / 2, 0, endZ + endZLength / 2);
        if (endShape.equals("rect") && (playerLocation.x > platformTopLeft.x) && (playerLocation.x < platformBottomRight.x) && (playerLocation.z > platformTopLeft.z) && (playerLocation.z < platformBottomRight.z)) {
            foundPosition();
        } else if (endShape.equals("circle") && isInsideEllipse(endX, endZ, playerLocation.x, playerLocation.z, endXLength, endZLength)) {
            foundPosition();
        }
//        System.out.println((playerLocation.x > platformTopLeft.x) && (playerLocation.x < platformBottomRight.x) && (playerLocation.z > platformTopLeft.z) && (playerLocation.z < platformBottomRight.z));
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
}
