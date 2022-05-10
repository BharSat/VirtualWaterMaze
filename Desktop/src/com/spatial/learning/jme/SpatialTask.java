/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spatial.learning.jme;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import quick.game.app.QuickGameState;

/**
 *
 * @author Bhargav_NDL
 */
public class SpatialTask extends SimpleApplication{

    @Override
    public void simpleInitApp() {
        stateManager.attach(new QuickGameState());
        try {
            System.out.println("Initializing");
            AfterInit obj = new AfterInit(this);
            obj.start();
            this.wait();
        } catch (Exception e) {}
        System.out.println("Initialized");
        System.out.println(stateManager.getState(QuickGameState.class).playerAppState);
        stateManager.getState(QuickGameState.class).createFloor(new Vector3f(-10f, 0, -10f), new Vector3f(10f, 0f, 10f));
        
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        
    }
    
    @Override
    public void simpleRender(RenderManager rm) {
        
    }
    private class AfterInit extends Thread {
        SpatialTask st;
        
        public void run() {
            Boolean go = false;
            if (st==null) {
                return;
            } else {
                while (st.stateManager.getState(QuickGameState.class).initialized==false) {
                    go=false;
                }
                st.notify();
                go=true;
            }
        }
        
        private AfterInit(SpatialTask st) {
            this.st = st;
        }
    }
    
}
