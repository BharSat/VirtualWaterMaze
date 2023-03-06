package com.spatial.learning.jme.game;

import java.io.OutputStream;

public interface FileReader {
    public boolean useThisReader();
    String readFile();
    void openFile(String Path);
    OutputStream getOutputStream(String Name);
}
