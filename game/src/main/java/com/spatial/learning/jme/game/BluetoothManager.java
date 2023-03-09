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
    BluetoothDataWriter dataWriter;
    BluetoothDataReceiver dataReceiver;
    DataProcessor dataProcessor;

    public final StringBuilder readData = new StringBuilder();
    public final StringBuilder writeData = new StringBuilder();
    public final float[] dataFloats = new float[7];
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
        writeData.append(data);
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

    private void connectionSuccess() {
        guiHandler.connectionSuccess();
        dataWriter = new BluetoothDataWriter();
        dataReceiver = new BluetoothDataReceiver();
        dataProcessor = new DataProcessor();
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
                BluetoothManager.this.connectionSuccess();
            } catch (IOException e) {
                throw new RuntimeException(e);
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
        public boolean end = false;
        BluetoothDataWriter() {
            try {
                if (serverConnector.getSc() == null) {
                    throw new NullPointerException("First Connect before instantiating bluetooth data writer.");
                }
            } catch (NullPointerException e) {
                throw new NullPointerException("First Connect before instantiating bluetooth data writer.");
            }
        }
        public void run() {
            StreamConnection sc =serverConnector.getSc();
            OutputStream outputStream;
            String writeDataStr = "";
            do {
                try {
                    outputStream = sc.openOutputStream();
                    writeDataStr = writeData.toString();
                    synchronized (writeData) {
                        if (!writeDataStr.equals(""))
                            outputStream.write(writeData.toString().getBytes(StandardCharsets.UTF_8));
                    }
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } while (!end);
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
        private final int datLen = 50;
        public boolean end = false;
        @Override
        public void run() {
            String data;
            String dataToRead;
            while (true) {
                synchronized (readData) {
                    data = readData.toString();
                }
                if (data.length()==datLen) {
                    dataToRead = data;
                } else if (data.length()>datLen) {
                    dataToRead = data.substring(0, datLen);
                    synchronized (readData) {
                        readData.delete(0, datLen);
                    }
                } else {
                    dataToRead = null;
                }
                if (dataToRead!=null) {
                    if (dataToRead.equals("-".repeat(datLen))) {
                        break;
                    } else if (dataToRead.charAt(0) != '!') {
                        char c;
                        for (int i=0;i<dataToRead.length();i++) {
                            c = dataToRead.charAt(i);
                            if (c=='!') {
                                readData.delete(0, i);
                                break;
                            }
                        }
                    } else {
                        synchronized (dataFloats){
                            dataFloats[0] = tf(dataToRead, 1);
                            dataFloats[1] = tf(dataToRead, 8);
                            dataFloats[2] = tf(dataToRead, 15);
                            dataFloats[3] = tf(dataToRead, 22);
                            dataFloats[4] = tf(dataToRead, 29);
                            dataFloats[5] = tf(dataToRead, 36);
                            dataFloats[6] = tf(dataToRead, 43);
                        }
                    }
                }
            }
        }

        private float tf(String str, int s) {
            try {
                return Float.parseFloat(str.substring(s, s + 7));
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
