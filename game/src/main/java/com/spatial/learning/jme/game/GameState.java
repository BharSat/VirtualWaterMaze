package com.spatial.learning.jme.game;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.control.LightControl;

public class GameState extends BaseAppState implements ActionListener {
    public Boolean initialized = false;
    boolean up = false, down = false, right = false, left = false;
    private Boolean sceneInitialized = false;
    private Camera cam;
    private SpatialLearningVWM app;
    private Node rootNode;
    private AssetManager assetManager;
    private FlyByCamera flyCam;
    private AppStateManager stateManager;
    private BulletAppState physics;
    private BetterCharacterControl player;
    private InputManager inputManager;
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();
    private Vector3f walkDirection = new Vector3f();

    public GameState() {
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SpatialLearningVWM) app;
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.cam = this.app.getCamera();
        this.flyCam = this.app.getFlyByCamera();
        this.stateManager = getStateManager();
        this.physics = this.stateManager.getState(BulletAppState.class);
        this.inputManager = this.app.getInputManager();
        System.out.println("Init Done2.");
        initialized = true;
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {
        if (initialized) {
        }
    }

    @Override
    public void update(float tpf) {
        if (sceneInitialized) {
            camDir.set(cam.getDirection()).multLocal(5f);
            camLeft.set(cam.getLeft()).multLocal(1f);
            walkDirection.set(0, 0, 0);
            if (left) {
                walkDirection.addLocal(camLeft);
            }
            if (right) {
                walkDirection.addLocal(camLeft.negate());
            }
            if (up) {
                walkDirection.addLocal(camDir.mult(2f));
            }
            if (down) {
                walkDirection.addLocal(camDir.negate());
            }
            walkDirection.setY(0f);
            player.setWalkDirection(walkDirection);
            cam.setLocation(player.getRigidBody().getPhysicsLocation());
        }
    }

    public SpatialLearningVWM getVWMApplication() {
        return this.app;
    }

    private void initScene() {
        physics.setDebugEnabled(true);

        app.getViewPort().setBackgroundColor(ColorRGBA.fromRGBA255(51, 204, 255, 255));

        LightControl playerLight = initLight();
        flyCam.setMoveSpeed(40f);

        Node scene = (Node) assetManager.loadModel("Models/scene.glb");
        scene.setLocalTranslation(0f, this.getVWMApplication().getGround(), 0f);
        scene.scale(1f, 2f, 1f);
        scene.addControl(playerLight);

        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(scene);
        RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0f);
        physics.getPhysicsSpace().add(landscape);
        rootNode.attachChild(scene);

        player = new BetterCharacterControl(1.5f, 6f, 0.05f);
        physics.getPhysicsSpace().add(player);
        player.setGravity(new Vector3f(0f, -30f, 0f));

        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
    }

    private LightControl initLight() {

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        PointLight playerLight = new PointLight();
        playerLight.setColor(ColorRGBA.White.mult(0.3f));
        rootNode.addLight(playerLight);
        return new LightControl(playerLight);
    }

    @Override
    public void onAction(String binding, boolean isPressed, float tpf) {
        if (binding.equals("Left")) {
            left = isPressed;
        } else if (binding.equals("Right")) {
            right = isPressed;
        } else if (binding.equals("Up")) {
            up = isPressed;
        } else if (binding.equals("Down")) {
            down = isPressed;
        } else if (binding.equals("Jump") && player.getRigidBody().getPhysicsLocation().y < 1f) {
            player.jump();
            player.setJumpForce(new Vector3f(0f, 0.5f, 0f));
        }
    }

    public void start() {
        if (initialized) {
            initScene();
            this.stateManager.getState(ModelHandler.class).start();
            sceneInitialized = true;
        } else {
            throw new RuntimeException("Game Has not Initialized yet");
        }
    }

    public BetterCharacterControl getPlayer() {
        return player;
    }
}
