package com.spatial.learning.jme.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataReader {
    public final char a = (char) 10;

    public DataReader() {
    }

    public Map<String, Map<String, String>> stringToData(String dataString) {
        Map<String, Map<String, String>> toRet = new HashMap<>();
        List<String> sessionTxt = new ArrayList<>();
        String[] linesAsArray = dataString.split("\n");
        String header = "", nameofPrj = "", fileNo = "", rootPath = "", cueFormat = "", arenaName = "";
        int sessionNo = 0, trialNo = 0;
        float version = 3.0f, arenaScale = 1.0f, playerSpeed = 1.0f, retardation_factor = 1.0f, fogDistance = 50.0f, fogDensity = 2.0f;
        int lineNo = 0;
        for (String line : linesAsArray) {
            lineNo++;
            switch (lineNo) {
                case 1:
                    header = line;
                    version = Float.parseFloat(header.substring(35, 38));
                    break;
                case 2:
                    nameofPrj = line;
                    break;
                case 3:
                    fileNo = line;
                    break;
                case 4:
                    if (version == 1.0) {
                        rootPath = line.split("\"")[1];
                        break;
                    } else {
                        lineNo++;
                    }
                case 5:
                    String[] dataValues = line.split(" ");
                    sessionNo = Integer.parseInt(dataValues[2]);
                    trialNo = Integer.parseInt(dataValues[4]);
                    cueFormat = dataValues[6];
                    arenaName = dataValues[8];
                    arenaScale = Float.parseFloat(dataValues[10]);
                    playerSpeed = Float.parseFloat(dataValues[12]);
                    if (version >= 3.0) {
                        retardation_factor = Float.parseFloat(dataValues[14]);
                        fogDistance = Float.parseFloat(dataValues[16]);
                        fogDensity = Float.parseFloat(dataValues[18]);
                    }
                    break;
                default:
                    sessionTxt.add(line);
                    // System.out.println(line);
                    break;
            }
        }
        toRet.put("data", new HashMap<>());
        toRet.get("data").put("version", "" + version);
        toRet.get("data").put("name", nameofPrj + fileNo);
        toRet.get("data").put("root", rootPath);
        toRet.get("data").put("sessions", "" + sessionNo);
        toRet.get("data").put("trials", "" + trialNo);
        toRet.get("data").put("arena", arenaName);
        toRet.get("data").put("cue_format", cueFormat);
        toRet.get("data").put("scale", "" + arenaScale);
        toRet.get("data").put("speed", "" + playerSpeed);
        toRet.get("data").put("retard_factor", "" + retardation_factor);
        toRet.get("data").put("fog_distance", "" + fogDistance);
        toRet.get("data").put("fog_density", "" + fogDensity);

        String curLine, sessionID, trialID, probe;
        float startX, startZ, endX, endZ, endXlen, endZlen;
        String endShape;
        String[] trialData, cueData;
        for (int sessionCount = 1, lineCount = 1; sessionCount <= sessionNo; sessionCount++, lineCount += trialNo + 1) {
            sessionID = Integer.parseInt(sessionTxt.get(lineCount).substring(1).trim()) + " ";
            for (int curTrial = 1; curTrial <= trialNo; curTrial++) {
                System.out.println(curTrial);
                curLine = sessionTxt.get((lineCount + curTrial));
                trialData = curLine.split(" ");
                trialID = String.valueOf(Integer.parseInt(trialData[0].substring(2).trim()));
                toRet.put(sessionID + trialID, new HashMap<>());
                probe = trialData[2];
                startX = Float.parseFloat(trialData[4]);
                startZ = Float.parseFloat(trialData[5]);
                endX = Float.parseFloat(trialData[7]);
                endZ = Float.parseFloat(trialData[8]);
                endShape = trialData[9];
                endXlen = Float.parseFloat(trialData[10]);
                endZlen = Float.parseFloat(trialData[11]);

                toRet.get(sessionID + trialID).put("start", "" + startX + " " + startZ);
                toRet.get(sessionID + trialID).put("end",
                        "" + endX + " " + endZ + " " + endXlen + " " + endZlen + " " + endShape);
                toRet.get(sessionID + trialID).put("probe", probe);

                // Cue Data
                cueData = curLine.split("cue");
                int cueNo = cueData.length - 1;
                for (int cueCount = 1; cueCount <= cueNo; cueCount++) {
                    toRet.get(sessionID + trialID).put("cue" + cueCount, cueData[cueCount].substring(1));
                }
                toRet.get(sessionID + trialID).put("cues", String.valueOf(cueNo));
            }


        }
        return toRet;


    }

    /*public Map<String, Map<String, String>> rawToGameData(com.admin.vwm.game.classes.Loop mainLoop) {
        if (!mainLoop.name.equals("Body")) {
            System.out.println("ni, " + mainLoop.name);
            return null;
        }
        System.out.println(mainLoop.children);
        Map<String, Map<String, String>> toRet = new HashMap<>();

        toRet.put("data", new HashMap<>());
        com.admin.vwm.game.classes.Loop tempLoop;
        Map<String, String> tempMap = new HashMap<>();

        String mode = "start1";
        for (com.admin.vwm.game.classes.Loop loop : mainLoop.children) {
            System.out.println(mode + loop.name);
            switch (mode) {
                case "start1":
                    toRet.get("data").put("name", loop.name);
                    mode = "start2";
                    break;
                case "start2":
                    toRet.get("data").put("name", toRet.get("data").get("name") + loop.name);
                    mode = "start3";
                    break;
                case "start3":
                    toRet.get("data").put("path", loop.getArg(0));
                    mode = "start4";
                    break;
                case "start4":
                    System.out.println(loop.getArgs());
                    for (int i = 0, n = loop.getArgs().size() / 2; i < n; i++) {
                        String name = loop.getArg(i * 2);
                        String val = loop.getArg(i * 2 + 1);
                        System.out.println("Hello, " + name + val);
                        switch (name) {
                            case "no_of_sessions":
                                toRet.get("data").put("sessions", String.valueOf(Integer.parseInt(val)));
                                System.out.println("Sessions");
                            case "no_of_trials":
                                toRet.get("data").put("trials", String.valueOf(Integer.parseInt(val)));
                            case "cue_format":
                                toRet.get("data").put("modelFormat", val);
                            case "arena":
                                toRet.get("data").put("arena", val);
                            case "arena_scale":
                                toRet.get("data").put("scale", String.valueOf(Integer.parseInt(val)));
                            case "player_speed":
                                toRet.get("data").put("speed", String.valueOf(Float.parseFloat(val)));
                            default:
                                System.out.println("Incorrect Option:" + name);
                        }
                    }
                    mode = "start5";
                    break;
                case "start5":
                    int curSession,
                            curTrial = 0;
                    for (int i = 0, n = loop.getChildren().size(); i < n; i++) {
                        curSession = Integer.parseInt(loop.name);
                        com.admin.vwm.game.classes.Loop sessionLoop = loop.getChild(i);
                        com.admin.vwm.game.classes.Loop trialLoop = sessionLoop.getChild(0);
                        for (int j = 0, m = trialLoop.getChildren().size(); j < m; j++) {
                            com.admin.vwm.game.classes.Loop trial = trialLoop.getChild(j);
                            curTrial = Integer.parseInt(trial.name);
                            int k = 0, o = trial.getArgs().size();
                            int cues = 0;
                            while (k < o) {
                                switch (trial.getArg(k)) {
                                    case "probe":
                                        tempMap.put("probe", trial.getArg(k + 1));
                                        k++;
                                    case "start":
                                        tempMap.put("start", trial.getArg(k + 1) + " " + trial.getArg(k + 2) + " " + trial.getArg(k + 3) + " ");
                                        k += 3;
                                    case "end":
                                        tempMap.put("end", trial.getArg(k + 1) + " " + trial.getArg(k + 2) + " " + trial.getArg(k + 3) + " " + trial.getArg(k + 4) + " " + trial.getArg(k + 5));
                                        k += 5;
                                    case "cue":
                                        tempMap.put("cue" + trial.getArg(k + 1), trial.getArg(k + 2) + " " + trial.getArg(k + 3) + " " + trial.getArg(k + 4) + " " + trial.getArg(k + 5));
                                        k += 5;
                                        cues++;
                                }
                                k++;
                            }
                            tempMap.put("cues", "" + cues);
                            toRet.put("" + curSession + " " + curTrial, tempMap);
                        }
                    }
                    mode = "start6";
                    break;
                case "start6":
                    break;

            }
        }
        return toRet;
    }*/

}
