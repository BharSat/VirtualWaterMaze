package com.spatial.learning.cardboard;

import androidx.appcompat.app.AppCompatActivity;

public class CardboardRenderer {
    static {
        System.loadLibrary("cardboard");
    }

    public static void initCardboardRenderer(CardboardVrState app) {
        initialize(app, app.getWidth(), app.getHeight());
    }

    public static native void initialize(AppCompatActivity app, int width, int height);

    public static native void render(byte[] dataLeft, long offsetLeft, long lengthLeft,
                                     byte[] dataRight, long offsetRight, long lengthRight);

    public static native void cleanup();

    public static native float[] getPosition();

    public static native float[] getOrientation();
}
