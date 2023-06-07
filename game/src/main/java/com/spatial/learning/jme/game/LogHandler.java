package com.spatial.learning.jme.game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class LogHandler extends BaseAppState {
    public SpatialLearningVWM app;
    public GameState gameState;
    public String path;
    public String name = null;
    public String dir = null;
    public String playerName;
    public boolean initialized = false;
    OutputStream outStream;

    public LogHandler() throws IOException {
    }

    public void root(String rootDir) {
        path = rootDir;
        File file = new File(path + "/logs");
        file.mkdir();
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SpatialLearningVWM) app;
        this.gameState = app.getStateManager().getState(GameState.class);
    }

    @Override
    protected void cleanup(Application app) {
        try {
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException ignored) {
        }
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
                Vector3f direction = getApplication().getCamera().getDirection();
                log(Math.round(location.getX() * 100) + "\t" + Math.round(location.getZ() * 100) + "\t" + Math.round(location.getZ() * 100) + "\t"
                        + direction.getX() + "\t" + direction.getY() + "\t" + direction.getZ() + "\n");
            }
        } catch (NullPointerException ignored) {
        }
    }

    public boolean log(String toLog) {
        try {
            if (app.fileReader.useThisReader()) {
                outStream.write(toLog.getBytes(StandardCharsets.UTF_8));
                return true;
            } else {
                FileWriter fw = new FileWriter(name, true);
                fw.write(toLog);
                fw.close();
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    public boolean newRound(int trialNo, int positionNo) {
        dir = path + "/logs/" + playerName;
        name = path + "/logs/" + playerName + "/pos" + positionNo + "trial" + trialNo + ".txt";
        try {
            if (this.app.fileReader.useThisReader()) {
                outStream = app.fileReader.getOutputStream(playerName + "pos" + positionNo + "trial" + trialNo + ".txt");
                return true;
            }
            File dirF = new File(dir);
            dirF.mkdir();
            File file = new File(name);
            file.createNewFile();
            FileWriter fw = new FileWriter(file, false);
            fw.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        File file = new File(path + "/logs/" + playerName);
        file.mkdir();
        initialized = true;
    }
}
