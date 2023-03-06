package com.spatial.learning.jme.desktopmodule;

import com.spatial.learning.jme.game.FileReader;
import com.spatial.learning.jme.game.ProjectManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class DesktopReader implements FileReader {
    String path;
    @Override
    public OutputStream getOutputStream(String name) {
        return new OutputStream() {
            @Override
            public void write(int i) throws IOException {

            }
        };
    }

    @Override
    public boolean useThisReader() {
        return false;
    }

    @Override
    public String readFile() {
        java.io.FileReader reader;
        try {
            reader = ProjectManager.openFileReader(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader bReader = new BufferedReader(reader);
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
    }

    @Override
    public void openFile(String Path) {
        path=Path;
    }
}
