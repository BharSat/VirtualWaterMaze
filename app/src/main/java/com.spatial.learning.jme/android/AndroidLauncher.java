package com.spatial.learning.jme.android;

import com.jme3.app.AndroidHarness;
import com.spatial.learning.jme.game.SpatialLearningVWM;


public class AndroidLauncher extends AndroidHarness {

    public AndroidLauncher() {
        appClass = SpatialLearningVWM.class.getCanonicalName();
    }
}
