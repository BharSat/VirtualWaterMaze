package com.spatial.learning.jme.game;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

public class GameState extends BaseAppState implements ActionListener {
    private final Vector3f camDir = new Vector3f();
    private final Vector3f camLeft = new Vector3f();
    private final Vector3f walkDirection = new Vector3f();
    private ModelHandler mh;
    public boolean initialized = false;
    public boolean enabled = false;
    public boolean up = false, down = false, right = false, left = false;
    private Boolean sceneInitialized = false;
    private Camera cam;
    private SpatialLearningVWM app;
    private AppStateManager stateManager;
    private BulletAppState physics;
    private BetterCharacterControl player;
    private InputManager inputManager;

    public GameState() {
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SpatialLearningVWM) app;
        this.cam = this.app.getCamera();
        this.stateManager = getStateManager();
        this.physics = this.stateManager.getState(BulletAppState.class);
        this.inputManager = this.app.getInputManager();
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
    }

    @Override
    public void update(float tpf) {
        mh = getState(ModelHandler.class);
        if (sceneInitialized && mh.fileInited) {

            camDir.set(cam.getDirection()).multLocal(mh.playerSpeed);
            camLeft.set(cam.getLeft()).multLocal(mh.playerSpeed);
            walkDirection.set(0, 0, 0);
            if (!(up | down)) {
                walkDirection.addLocal(camDir.mult(mh.retardFactor));
            }
            if (left) {
                walkDirection.addLocal(camLeft);
            }
            if (right) {
                walkDirection.addLocal(camLeft.negate());
            }
            if (up) {
                walkDirection.addLocal(camDir);
            }
            if (down) {
                walkDirection.addLocal(camDir.negate());
            }
            walkDirection.setY(0f);
            player.setWalkDirection(walkDirection);
            cam.setLocation(player.getRigidBody().getPhysicsLocation());
        }
    }

    private void initScene() {

        app.getViewPort().setBackgroundColor(ColorRGBA.fromRGBA255(51, 204, 255, 255));

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

    @Override
    public void onAction(String binding, boolean isPressed, float tpf) {
        if (enabled) {
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
        } else {
            this.up = false;
            this.left = false;
            this.right = false;
            this.down = false;
        }
    }

    public void start() {
        if (initialized) {
            enabled = true;
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
