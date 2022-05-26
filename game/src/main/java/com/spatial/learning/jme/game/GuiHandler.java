package com.spatial.learning.jme.game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.style.BaseStyles;

public class GuiHandler extends BaseAppState {

    SpatialLearningVWM app;
    Container mainContainer;

    @Override
    protected void initialize(Application app) {
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
        this.app = (SpatialLearningVWM) app;

        this.mainContainer = new Container();

        this.app.getGuiNode().attachChild(mainContainer);
        mainContainer.setLocalTranslation(250, 350, 0);
        mainContainer.setLayout(new BorderLayout());
        mainContainer.scale(2f);

        mainContainer.addChild(startGui());
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

    public Node startGui() {
        Container container = new Container();
        container.addChild(new Label("Welcome\n"));
        Button button = new Button("Start");
        button.setTextHAlignment(HAlignment.Center);
        button.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                start();
            }
        });
        container.addChild(button);
        return container;
    }

    public void initGuiBetweenRounds(ModelHandler modelHandler) {
        this.app.getInputManager().setCursorVisible(false);
        Container container = new Container();
        container.addChild(new Label("Great Job!\n"));
        Button button = new Button("Next Round");
        button.setTextHAlignment(HAlignment.Center);
        button.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                modelHandler.nextPosition();
            }
        });
        container.addChild(button);
        mainContainer.attachChild(container);
        this.app.getGuiNode().attachChild(mainContainer);
        System.out.println("kkkk");
        this.app.getFlyByCamera().setEnabled(false);
        this.app.getInputManager().setCursorVisible(true);
    }
}
