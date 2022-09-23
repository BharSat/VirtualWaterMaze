package com.spatial.learning.jme.game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.*;
import com.simsilica.lemur.style.BaseStyles;

public class GuiHandler extends BaseAppState {

    SpatialLearningVWM app;
    Container mainContainer;
    TextField playerNameField;

    @Override
    protected void initialize(Application app) {
        this.app = (SpatialLearningVWM) app;
        GuiGlobals.initialize(this.app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
        mainContainer = new Container();
        this.app.getGuiNode().attachChild(mainContainer);
        mainContainer.setLocalTranslation(600, 600, 0);
        startGui();
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

    }

    public void start() {
        this.app.getGuiNode().detachAllChildren();
        this.mainContainer.detachAllChildren();
        this.app.getFlyByCamera().setEnabled(true);
        this.app.getInputManager().setCursorVisible(false);
        this.getStateManager().getState(LogHandler.class).setPlayerName(playerNameField.getText());
        this.app.startGame();
    }

    public void startGui() {
        mainContainer.addChild(new Label("Enter Your Name:"));
        playerNameField = mainContainer.addChild(new TextField("__"));
        Button startButton = mainContainer.addChild(new Button("Lets Start."));
        startButton.addClickCommands(source -> start());
        startButton.setTextHAlignment(HAlignment.Center);
    }

    public void initGuiBetweenRounds(ModelHandler modelHandler) {
        this.app.getInputManager().setCursorVisible(false);
        mainContainer.addChild(new Label("Great Job!\n"));
        Button button = mainContainer.addChild(new Button("Next Round"));
        button.setTextHAlignment(HAlignment.Center);
        button.addClickCommands(source -> {
            app.getGuiNode().detachAllChildren();
            mainContainer.detachAllChildren();
            app.getFlyByCamera().setEnabled(true);
            app.getInputManager().setCursorVisible(false);
            modelHandler.nextTrial();
        });
        this.app.getFlyByCamera().setEnabled(false);
        this.app.getInputManager().setCursorVisible(true);
        this.app.getGuiNode().attachChild(mainContainer);
    }
}
