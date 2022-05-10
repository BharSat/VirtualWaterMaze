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
    public final Object obj = new Object();

    @Override
    public void simpleInitApp() {
        synchronized (obj) {
            stateManager.attach(new QuickGameState());
            try {
                System.out.println("Initializing");
                AfterInit obj = new AfterInit(this);
                obj.start();
                this.obj.wait();
            } catch (Exception e) {}
            System.out.println("Initialized");
            try{
                stateManager.getState(QuickGameState.class).createFloor(new Vector3f(-10f, 0, -10f), new Vector3f(10f, 0f, 10f));
            }
            catch (NullPointerException e) { System.out.println(stateManager.getState(QuickGameState.class).sceneState == null);}
        }
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        
    }
    
    @Override
    public void simpleRender(RenderManager rm) {
        
    }
    private class AfterInit extends Thread {
        SpatialTask st;
        
        @Override
        public void run() {
            Boolean go = false;
            if (st==null) {
                return;
            }
            synchronized (st.obj) {
                while (st.stateManager.getState(QuickGameState.class).initialized==false || st.stateManager.getState(QuickGameState.class).sceneState == null) {
                    go=false;
                    System.out.println(st.stateManager.getState(QuickGameState.class).initialized==true);
                }
                st.obj.notify();
                go=true;
            }
        }
        
        private AfterInit(SpatialTask st) {
            this.st = st;
        }
    }
    
}
