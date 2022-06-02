package com.spatial.learning.jme.game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;

import java.io.FileWriter;
import java.io.IOException;

public class LogHandler extends BaseAppState {
    public GameState gameState;
    public FileWriter fw = new FileWriter("D:/bhargav/a/Log.txt", false);

    public LogHandler() throws IOException {
    }

    @Override
    protected void initialize(Application app) {
        this.gameState = app.getStateManager().getState(GameState.class);
        try {
            fw.write("Initializing\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {
        try {
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(float tpf) {
        try {
            if (getState(GameState.class).enabled) {
                BetterCharacterControl player = gameState.getPlayer();
                Vector3f location = player.getRigidBody().getPhysicsLocation();
                System.out.println(log(Math.round(location.getX()) + "\t" + Math.round(location.getZ()) + "\n"));
            }
        } catch (NullPointerException ignored) {}
    }

    public boolean log(String toLog) {
        try {
            fw.write(toLog);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
