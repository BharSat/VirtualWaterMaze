package com.spatial.learning.jme.game;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.bullet.BulletAppState;

import java.io.IOException;
import java.util.Map;

public class SpatialLearningVWM extends SimpleApplication {
    public ProjectManager projectManager;
    public Reader reader;
    public Map<String, Map<String, String>> data;

    public SpatialLearningVWM() {
    }

    public SpatialLearningVWM(AppState... initialStates) {
        super(initialStates);
    }

    @Override
    public void simpleInitApp() {
        stateManager.attach(new BulletAppState());
        stateManager.attach(new ModelHandler());
        stateManager.attach(new GameState());
        stateManager.attach(new GuiHandler());
        try {
            stateManager.attach(new LogHandler());
        } catch (IOException ignored) {
        }
    }

    public float getGround() {
        return -5f;
    }

    public void startGame() {
        stateManager.getState(GameState.class).start();
    }

}
