package com.spatial.learning.jme.game;

public interface BluetoothReceiver {
    void connect(String deviceName, String HardwareAddress, boolean useDeviceName, boolean useHardwareAddress);
    String[] getAssetPaths();
    String[] getScenePaths();
    String getTrialData();
    void sendMovementData(float lax, float lay, float laz,  float rorx, float rory, float rorz);
    int[] getPlayerPositionData();
}
