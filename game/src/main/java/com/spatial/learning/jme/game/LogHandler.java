package com.spatial.learning.jme.game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogHandler extends BaseAppState {
    public GameState gameState;
    public String path = "D:/bhargav/vwm/";
    public String name = null;
    public String playerName;
    public boolean initialized = false;

    public LogHandler() throws IOException {
    }

    @Override
    protected void initialize(Application app) {
        this.gameState = app.getStateManager().getState(GameState.class);
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
        try {
            if (getState(GameState.class).enabled && name != null && initialized) {
                BetterCharacterControl player = gameState.getPlayer();
                Vector3f location = player.getRigidBody().getPhysicsLocation();
                log(Math.round(location.getX() + 70) + "\t" + Math.round(location.getZ() + 70) + "\n");
            }
        } catch (NullPointerException ignored) {
        }
    }

    public boolean log(String toLog) {
        try {
            FileWriter fw = new FileWriter(name, true);
            fw.write(toLog);
            fw.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean newRound(int trialNo, int positionNo) {
        name = path + playerName + "/pos" + positionNo + "trial" + trialNo + ".txt";
        try {
            FileWriter fw = new FileWriter(name, false);
            fw.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        File file = new File(path + playerName);
        file.mkdir();
        initialized = true;
    }
}
