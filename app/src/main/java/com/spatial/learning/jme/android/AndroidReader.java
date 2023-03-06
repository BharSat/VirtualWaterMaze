package com.spatial.learning.jme.android;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.spatial.learning.jme.game.FileReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class AndroidReader implements FileReader {
    private AndroidLauncher activity;
    private String text;
    private boolean hasPreRead = false;
    private Uri uri;
    private boolean useBluetooth = false;

    @Override
    public OutputStream getOutputStream(String name) {
        try {
            return activity.openFileOutput(name, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String preRead(Uri uri) {
        this.setUri(uri);
        text = this.readFile();
        hasPreRead = true;
        return text;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setUseBluetooth(boolean useBluetooth) {
        this.useBluetooth = useBluetooth;
    }

    @Override
    public boolean useThisReader() {
        return true;
    }

    @Override
    public String readFile() {
//        File file;
//        if (path.startsWith("/sdcard")) {
//            String newPath = new File(path).getName();
//            file = new File(activity.getExternalFilesDir(path), newPath);
//        } else {
//            file = new File(path);
//        }
//        java.io.FileReader reader;
//        try {
//            reader = new java.io.FileReader(file);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
        if (this.uri == null) {
            this.uri = this.activity.readerUri;
        }
        if (!hasPreRead) {
            if (!useBluetooth) {
                InputStream reader;
                while (activity.readerUri == null) {
                    System.out.println("Waiting for uri");
                }
                try {
                    reader = activity.getContentResolver().openInputStream(this.uri);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                BufferedReader bReader = new BufferedReader(new InputStreamReader(reader));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                String ls = System.getProperty("line.separator");
                while (true) {
                    try {
                        if ((line = bReader.readLine()) == null) break;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    stringBuilder.append(line);
                    stringBuilder.append(ls);
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                try {
                    bReader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return stringBuilder.toString();
            } else {
                return "";
            }
        } else {
            return text;
        }
    }

    public void openFile(String Path) {

//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("text/plain");
        activity.getmGetContent().launch("text/plain");
    }

    public void setActivity(AppCompatActivity activity) {
        this.activity = (AndroidLauncher) activity;
    }

    private class BluetoothReceiver {

        BluetoothReceiver() {
        }

        public void initConnection(String name, String hardwareAddress, boolean useName, boolean useHardwareAddress) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("Bluetooth Permissions have not been granted.");
            }
            Set<BluetoothDevice> pairedDevices = activity.bluetoothAdapter.getBondedDevices();

            BluetoothDevice toConnect = null;

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    if ((name.equals(deviceName) || !useName)
                            && (hardwareAddress.equals(deviceHardwareAddress) || !useHardwareAddress)) {
                        toConnect = device;
                    }
                }
            }
            if (toConnect == null) {
                throw new RuntimeException("Could not find device " + name + " with hardware address " + hardwareAddress);
            }
            ConnectThread thread = new ConnectThread(toConnect);
            thread.start();
        }
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
