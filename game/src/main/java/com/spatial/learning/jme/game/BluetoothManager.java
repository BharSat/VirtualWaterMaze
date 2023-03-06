package com.spatial.learning.jme.game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class BluetoothManager {
    private final GuiHandler guiHandler;
    BluetoothServerConnector serverConnector;
    public final StringBuilder readData = new StringBuilder();
    BluetoothManager(GuiHandler guiHandler) {
        this.guiHandler = guiHandler;
        serverConnector = new BluetoothServerConnector();
    }

    public void startServer() {
        try {
            serverConnector.start();
        } catch (IllegalThreadStateException ignored) {}
    }

    public void write(String data) {
        BluetoothDataWriter dataWriter = new BluetoothDataWriter(data);
        dataWriter.start();
    }

    public void close() {
        serverConnector.close();
    }

    public String getDeviceName() throws IOException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        return localDevice.getFriendlyName();
    }

    public String getDeviceBluetoothAddress() throws IOException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        return localDevice.getBluetoothAddress();
    }

    public String getRemoteDeviceName() throws IOException {
        RemoteDevice remoteDevice = RemoteDevice.getRemoteDevice(serverConnector.sc);
        return remoteDevice.getFriendlyName(false);
    }

    public String getRemoteDeviceBluetoothAddress() throws IOException {
        RemoteDevice remoteDevice = RemoteDevice.getRemoteDevice(serverConnector.sc);
        return remoteDevice.getBluetoothAddress();
    }

    private String remove(String toRem) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String i: toRem.split("-")) {
            stringBuilder.append(i);
        }
        return stringBuilder.toString();
    }

    private class BluetoothServerConnector extends Thread {
        private final String uuidStr = remove(java.util.UUID.randomUUID().toString());
        private final UUID uuid = new UUID(uuidStr, false);

        private StreamConnectionNotifier scn;
        private StreamConnection sc;

        BluetoothServerConnector() {}

        @Override
        public void run() {
            String serviceName = "VWMBTDirect";
            String connURL = "btspp://localhost: "+ uuid +";"+"name="+ serviceName;
            try {
                scn = (StreamConnectionNotifier) Connector.open(connURL);
                sc = scn.acceptAndOpen();
                guiHandler.connectionSuccess();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public StreamConnection getSc() {
            return sc;
        }
        public boolean close() {
            try {
                scn.close();
                sc.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    private class BluetoothDataWriter extends Thread {
        String data;
        BluetoothDataWriter(String data) {
            try {
                if (serverConnector.getSc() == null) {
                    throw new NullPointerException("First Connect before instantiating bluetooth data writer.");
                }
            } catch (NullPointerException e) {
                throw new NullPointerException("First Connect before instantiating bluetooth data writer.");
            }
            this.data = data;
        }
        public void run() {
            StreamConnection sc =serverConnector.getSc();
            OutputStream outputStream = null;
            try {
                outputStream = sc.openOutputStream();
                outputStream.write(data.getBytes(StandardCharsets.UTF_8));
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class BluetoothDataReceiver extends Thread {
        InputStream inputStream;
        int i;
        String temp;
        byte[] buffer = new byte[1024];

        @Override
        public void run() {
            try {
                inputStream = serverConnector.getSc().openInputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (true) {
                try {
                    i = inputStream.read(buffer);
                    temp = Arrays.toString(Arrays.copyOfRange(buffer, 0, i + 1));
                    synchronized (readData) {
                        readData.append(temp);
                    }
                } catch (IOException e) {
                    return;
                }
            }
        }
    }
    private class DataProcessor extends Thread{
        @Override
        public void run() {
            String data;
            String dataToRead = null;
            while (true) {
                synchronized (readData) {
                    data = readData.toString();
                }
                if (data.length()==30) {
                    dataToRead = data;
                } else if (data.length()>30) {
                    dataToRead = data.substring(0, 30);
                    synchronized (readData) {
                        readData.delete(0, 30);
                    }
                } else {
                    dataToRead = null;
                }
                if (dataToRead!=null) {

                }
            }
        }
    }
}
