package com.spatial.learning.jme.android;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.spatial.learning.jme.game.BluetoothReceiver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

public class AndroidBTReceiver implements BluetoothReceiver {
    AndroidLauncher activity;
    AndroidBTReceiver(AndroidLauncher activity) {
        this.activity = activity;
    }
    @Override
    public void connect(String deviceName, String HardwareAddress, boolean useDeviceName, boolean useHardwareAddress) {

    }

    @Override
    public String[] getAssetPaths() {
        return new String[0];
    }

    @Override
    public String[] getScenePaths() {
        return new String[0];
    }

    @Override
    public String getTrialData() {
        return null;
    }

    @Override
    public void sendMovementData(float lax, float lay, float laz, float rorx, float rory, float rorz) {

    }

    @Override
    public int[] getPlayerPositionData() {
        return new int[0];
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;
        String data = null;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                tmp = device.createRfcommSocketToServiceRecord(UUID.randomUUID());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("NO Bluetooth scanning permissions");
            }
            activity.bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    throw new RuntimeException(closeException);
                }
            }
            InputStream inputStream;
            try {
                inputStream = mmSocket.getInputStream();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                    throw new RuntimeException(e);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            byte[] buffer = new byte[1024];
            int noOfBytes;
            BufferThread bufferThread = new BufferThread();
            boolean shouldBreak = false;
            while (true) {
                try {
                    noOfBytes = inputStream.read(buffer);
                    shouldBreak = bufferThread.addData(buffer, noOfBytes);
                    if (shouldBreak) {
                        break;
                    }
                } catch (IOException e) {
                    System.out.println("Socket was disconnected");
                    break;
                }
            }
            this.data = bufferThread.getData();
            try {
                inputStream.close();
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static class BufferThread extends Thread {
        StringBuilder data;
        int len;
        public void run() {
            data = new StringBuilder();
            len = 0;
        }
        public boolean addData(byte[] bytes, int len) {
            data.append(Arrays.toString(Arrays.copyOfRange(bytes, 0, len-1)));
            this.len += len;
            return false;
        }
        public String getData() {
            return data.toString();
        }
        public int getLen() {
            return len;
        }
    }
}
