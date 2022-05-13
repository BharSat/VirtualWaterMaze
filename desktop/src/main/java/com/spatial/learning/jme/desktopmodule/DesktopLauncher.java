package com.spatial.learning.jme.desktopmodule;

import com.spatial.learning.jme.game.SpatialLearningVWM;
import com.jme3.system.AppSettings;

/**
 * Used to launch a jme application in desktop environment
 *
 */
public class DesktopLauncher {
    public static void main(String[] args) {
        final SpatialLearningVWM game = new SpatialLearningVWM();

        final AppSettings appSettings = new AppSettings(true);

        game.setSettings(appSettings);
        game.setShowSettings(true);
        game.start();
    }
}
