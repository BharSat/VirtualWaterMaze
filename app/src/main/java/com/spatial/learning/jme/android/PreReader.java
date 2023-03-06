package com.spatial.learning.jme.android;

import android.content.Context;

import com.spatial.learning.jme.game.FileReader;

import java.io.FileNotFoundException;
import java.io.OutputStream;

public class PreReader implements FileReader {
    AndroidLauncher launcher;
    PreReader(AndroidLauncher launcher) {
        this.launcher=launcher;
    }

    @Override
    public OutputStream getOutputStream(String name) {
        try {
            return launcher.openFileOutput(name, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean useThisReader() {
        return true;
    }

    @Override
    public String readFile() {
        return "VWM/Virtual Water Maze Data File - 1.0.0\n" +
                "#Demo\n" +
                "#0\n" +
                "#Home \"D:\\kalarab\\Demo.txt\"\n" +
                "#Constants no_of_sessions 1 no_of_trials 1 cue_format models/<model_name>/<model_name>.glb arena default1 arena_scale 512.0 player_speed 52\n" +
                "#Sessions\n" +
                "#1\n" +
                "\t#1 probe no start 0.0 0.0 end 100.0 100.0 rect 52 52 cue 0 -200.0 0.0 -200.0 \"Mill\"\n" +
                ";\n" +
                "\n" +
                "#End;\n";
    }

    @Override
    public void openFile(String Path) {

    }
}
