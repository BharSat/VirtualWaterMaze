package com.spatial.learning.jme.game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.*;

public class GuiHandler extends BaseAppState {

    SpatialLearningVWM app;
    Container mainContainer;
    TextField playerNameField;
    TextField filePathField;
    boolean init = false;

    @Override
    protected void initialize(Application app) {
        if (app==null) {
            throw new RuntimeException("App passed to Gui Handler is null");
        } else {
            System.out.println("App at Gui Handler is not null; Continuing");
        }
        this.app = (SpatialLearningVWM) app;
        GuiGlobals.initialize(this.app);
//        BaseStyles.loadGlassStyle();
//        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
        mainContainer = new Container();
        Vector3f preferredHeight = mainContainer.getPreferredSize().mult(0.5f);
        mainContainer.setLocalTranslation(this.app.getCamera().getWidth() * .5f - preferredHeight.x, this.app.getCamera().getHeight() * .5f - preferredHeight.z, 0);

        this.app.getGuiNode().attachChild(mainContainer);
        init = true;
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
        start(playerNameField.getText(), filePathField.getText());
    }
    public void start(String playerName, String path) {
        if (app==null) {
            throw new RuntimeException("this.app is null at GuiHandler.start");
        } else {
            System.out.println("this.app is not null at GuiHandler.start; Continuing");
        }
        this.app.getGuiNode().detachAllChildren();
        this.mainContainer.detachAllChildren();
        this.app.getFlyByCamera().setEnabled(true);
        this.app.getInputManager().setCursorVisible(false);
        this.getStateManager().getState(LogHandler.class).setPlayerName(playerName);
        this.getStateManager().getState(ModelHandler.class).readSettingsFile(path);
        this.app.startGame();
    }

    public void startGui() {
        mainContainer.addChild(new Label("Enter File Path:"));
        filePathField = mainContainer.addChild(new TextField("__"));
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
