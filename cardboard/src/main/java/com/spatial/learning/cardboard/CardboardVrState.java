package com.spatial.learning.cardboard;

import androidx.appcompat.app.AppCompatActivity;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;

import java.nio.ByteBuffer;

public class CardboardVrState extends AppCompatActivity implements AppState {
    ByteBuffer outBuf;
    Eye leftEye, rightEye;
    Camera lCam, rCam;
    SimpleApplication app;
    AppStateManager appStateManager;
    static int LEFT_EYE = 0;
    static int RIGHT_EYE = 1;
    boolean left = false, right = false;
    byte[] dataLeft, dataRight;
    long offsetLeft, offsetRight;
    long lengthLeft, lengthRight;

    public static void render(byte[] dataLeft, long offsetLeft, long lengthLeft,
                              byte[] dataRight, long offsetRight, long lengthRight) {
        CardboardRenderer.render(dataLeft, offsetLeft, lengthLeft,
                dataRight, offsetRight, lengthRight);
    }

    public void render() {
        render(this.dataLeft, this.offsetLeft, this.lengthLeft, this.dataRight, this.offsetRight, this.lengthRight);
    }

    public void render(int type, byte[] data, long offset, long length) {
        if (type == LEFT_EYE) {
            this.dataLeft = data;
            this.offsetLeft = offset;
            this.lengthLeft = length;
            if (right) this.render();
        } else if (type == RIGHT_EYE) {
            this.dataRight = data;
            this.offsetRight = offset;
            this.lengthRight = length;
            if (left) {
                this.render();
            }
        }
    }

    public int getWidth() {
        return this.app.getViewPort().getCamera().getWidth();
    }

    public int getHeight() {
        return this.app.getViewPort().getCamera().getHeight();
    }

    public Vector3f getPosition() {
        return new Vector3f(CardboardRenderer.getPosition()[0], CardboardRenderer.getPosition()[1], CardboardRenderer.getPosition()[2]);
    }

    public Quaternion getOrientation() {
        return new Quaternion(CardboardRenderer.getOrientation()[0], CardboardRenderer.getOrientation()[1], CardboardRenderer.getOrientation()[2], CardboardRenderer.getOrientation()[3]);
    }

    @Override
    public void cleanup() {
        CardboardRenderer.cleanup();
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        this.app = (SimpleApplication) app;
        this.appStateManager = stateManager;

        leftEye = new Eye(LEFT_EYE, this);
        rightEye = new Eye(RIGHT_EYE, this);

        lCam = app.getCamera();
        rCam = lCam.clone();
    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void setEnabled(boolean active) {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {

    }

    @Override
    public void update(float tpf) {

    }

    @Override
    public void render(RenderManager rm) {

    }

    @Override
    public void postRender() {

    }

    public void alterCameras() {
        rCam.setLocation(lCam.getLocation().add(lCam.getLeft().negate()));
        lCam.setLocation(lCam.getLocation().add(lCam.getLeft()));
        rCam.setRotation(this.getOrientation());
        lCam.setRotation(this.getOrientation());
    }

    public class Eye implements SceneProcessor {
        ViewPort vp;
        RenderManager rm;
        int eyeNo;
        CardboardVrState vrState;

        Eye(int leftOrRight, CardboardVrState vrState) {
            eyeNo = leftOrRight;
            this.vrState = vrState;
        }

        @Override
        public void initialize(RenderManager rm, ViewPort vp) {
            this.vp = vp;
            this.rm = rm;
        }

        @Override
        public void reshape(ViewPort vp, int w, int h) {
        }

        @Override
        public boolean isInitialized() {
            return false;
        }

        @Override
        public void preFrame(float tpf) {
        }

        @Override
        public void postQueue(RenderQueue rq) {
        }

        @Override
        public void postFrame(FrameBuffer out) {
            vrState.alterCameras();
            //Texture texture = out.getColorTarget().getTexture();;
            //Image image = texture.getImage();
            //int width = image.getWidth();
            //int height = image.getHeight();
            //byte[] data = image.getData(0).array();
            //render(data, 0, (long) width *height*3);
            rm.getRenderer().readFrameBuffer(out, outBuf);
            this.vrState.render(this.eyeNo, outBuf.array(), 0, outBuf.position());
        }

        @Override
        public void cleanup() {

        }

        @Override
        public void setProfiler(AppProfiler profiler) {

        }

    }
}