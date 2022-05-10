/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spatial.learning.jme;

import com.jme3.system.AppSettings;

/**
 *
 * @author Bhargav_NDL
 */
public class Main {
    public static void main(String args[]) {
        SpatialTask app = new SpatialTask();
        AppSettings cfg = new AppSettings(true);
        cfg.setFrameRate(60); // set to less than or equal screen refresh rate
        cfg.setVSync(true);   // prevents page tearing
        cfg.setFrequency(60); // set to screen refresh rate
        cfg.setResolution(1024, 768);   
        cfg.setFullscreen(true); 
        cfg.setSamples(2);    // anti-aliasing
        cfg.setTitle("My jMonkeyEngine 3 Game"); 
        app.setShowSettings(false); // or don't display splashscreen
        app.setSettings(cfg);
        app.start();
    }
}
