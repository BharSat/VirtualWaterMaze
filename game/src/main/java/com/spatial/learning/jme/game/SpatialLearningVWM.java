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
    public boolean started = false;
    public StartTask startTask = new StartTask() {
        @Override
        void run() {}
    };

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
        stateManager.update(0);
        started = true;
        startTask.run();
    }

    public float getGround() {
        return -5f;
    }

    public void startGame() {
        stateManager.getState(GameState.class).start();
    }
    public void startGame(String name, String filePath) {
        if (this.started) {
//            try {
                stateManager.getState(GuiHandler.class).start(name, filePath);
//            } catch (NullPointerException exception) {
//                throw new RuntimeException(exception);
//                System.out.println(stateManager.getState(GuiHandler.class)+" Gui Handler Class");
//                stateManager.attach(new GuiHandler());
//                this.startGame(name, filePath);
//            }
        } else {
            System.out.println("Not Started");
            this.start();
            this.startTask = new StartTask() {
                @Override
                void run() {
                    SpatialLearningVWM.this.startGame(name, filePath);
                }
            };
        }
    }
    private static abstract class StartTask {
        abstract void run();
    }

}
