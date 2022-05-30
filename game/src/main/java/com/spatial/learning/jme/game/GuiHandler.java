package com.spatial.learning.jme.game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.style.BaseStyles;

public class GuiHandler extends BaseAppState {

    SpatialLearningVWM app;
    Container mainContainer;
    Container nextRoundScreen;
    Container startScreen;

    @Override
    protected void initialize(Application app) {
        this.app = (SpatialLearningVWM) app;
        GuiGlobals.initialize(this.app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
        mainContainer = new Container();
        this.app.getGuiNode().attachChild(mainContainer);
        mainContainer.setLocalTranslation(200, 400, -10f);
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
        this.app.startGame();
    }

    public void startGui() {
        mainContainer.addChild(new Label("Welcome to the lawn!"));
        Button clickMe = mainContainer.addChild(new Button("Lets Start."));
        clickMe.addClickCommands(source -> start());
        clickMe.setTextHAlignment(HAlignment.Center);
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
