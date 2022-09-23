package com.spatial.learning.jme.desktopmodule;

import com.jme3.system.AppSettings;
import com.spatial.learning.jme.game.SpatialLearningVWM;

/**
 * Used to launch a jme application in desktop environment
 */
public class DesktopLauncher {
    public static void main(String[] args) {
        final SpatialLearningVWM game = new SpatialLearningVWM();

        final AppSettings appSettings = new AppSettings(true);

        appSettings.setResolution(1192, 1080);
        appSettings.setWidth(1192);
        appSettings.setHeight(1080);
//        appSettings.setWindowXPosition(100);
        appSettings.setWindowYPosition(500);
        appSettings.setFullscreen(true);

        game.setSettings(appSettings);
        game.setShowSettings(false);
        game.setDisplayStatView(false);
        game.start();
    }
}
