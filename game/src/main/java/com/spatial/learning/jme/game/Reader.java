package com.spatial.learning.jme.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reader {
    ProjectManager parent;

    public Reader(ProjectManager parent) {
        this.parent = parent;
    }

    public com.admin.vwm.game.classes.Loop stringToData(String dataString) {
        Map<String, Map<String, String>> tempData = new HashMap<>();
        String mode = "startH",
                subMode;
        int temp1 = 0,
                temp2 = 0;
        String tempStr1 = "",
                tempStr2 = "";
        List<String> tempList = new ArrayList<>();
        com.admin.vwm.game.classes.Loop bodyLoop = new com.admin.vwm.game.classes.Loop("Body", new ArrayList<>()),
                curLoop = new com.admin.vwm.game.classes.Loop("", new ArrayList<>(0)),
                parentLoop = bodyLoop;
        boolean slash = false, comment = false;
        for (char c : dataString.toCharArray()) {
            if (c == '/') {
                comment = slash;
                slash = !slash;
                if (comment) {
                    tempStr2 = mode;
                    mode = "false";
                }
            } else if (comment && c == '\n') {
                comment = false;
                mode = tempStr2;
                tempStr2 = "";
            }
            switch (mode) {
                case "startH":
                    temp1++;
                    tempStr1 += c;
                    if (temp1 == 35) {
                        if (tempStr1.equals("VWM/Virtual Water Maze Data File - ")) {
                            mode = "startV";
                            temp1 = 0;
                            tempStr1 = "";
                        } else {
                            return new com.admin.vwm.game.classes.Loop("Invalid Header: " + tempStr1, new ArrayList<>());
                        }
                    }
                    break;
                case "startV":
                    temp1++;
                    tempStr1 += c;
                    if (temp1 == 5) {
                        mode = "body";
                        temp1 = 0;
                        tempStr1 = "";
                    }
                    break;
                case "body":
                    subMode = "newA";
                    switch (subMode) {
                        case "newA":
                            switch (c) {
                                case '#':
                                    subMode = "loopStart";
                                    break;
                                case ' ':
                                    subMode = "newItem";
                                    break;
                                case '\n':
                                case '\t':
                                    break;
                                default:
                                    return new com.admin.vwm.game.classes.Loop("Error: Unexpected Character '" + c + "'", new ArrayList<>());
                            }
                            break;
                        case "newItem":
                            switch (c) {
                                case '#':
                                    subMode = "loopStart";
                                    break;
                                case ' ':
                                case '\n':
                                case '\t':
                                    break;
                                case ';':
                                    curLoop = parentLoop;
                                    parentLoop = parentLoop.parent();
                                default:
                                    subMode = "item";
                            }
                            break;
                        case "item":
                            if (c == ' ') {
                                subMode = "newItem";
                                tempList.add(tempStr1);
                                tempStr1 = "";
                                break;
                            } else if (c == ';') {
                                subMode = "newItem";
                                tempList.add(tempStr1);
                                tempStr1 = "";
                                curLoop.addArgs(tempList);
                                tempList.clear();
                                curLoop = parentLoop;
                                parentLoop = parentLoop.parent();
                            }
                            tempStr1 += c;
                            break;
                        case "loopStart":
                            if (c == ' ') {
                                parentLoop = curLoop;
                                curLoop = new com.admin.vwm.game.classes.Loop(tempStr1, new ArrayList<>());
                                parentLoop.addChild(curLoop);
                                subMode = "newItem";
                                tempStr1 = "";
                                if (curLoop.name.equals("End")) {
                                    mode = "";
                                    break;
                                }
                            }
                            tempStr1 += c;
                    }
                    break;
                case "":
                case "false":
                    break;
                default:
                    return new com.admin.vwm.game.classes.Loop("Logic Error: Incorrect mode " + mode, new ArrayList<>());
            }
        }
        return bodyLoop;
    }

    public Map<String, Map<String, String>> rawToGameData(com.admin.vwm.game.classes.Loop mainLoop) {
        if (!mainLoop.name.equals("Body")) {
            return null;
        }
        Map<String, Map<String, String>> toRet = new HashMap<>();

        toRet.put("data", new HashMap<>());
        com.admin.vwm.game.classes.Loop tempLoop;
        Map<String, String> tempMap = new HashMap<>();

        String mode = "start1";
        for (com.admin.vwm.game.classes.Loop loop : mainLoop.children) {
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
                    for (int i = 0, n = loop.getArgs().size() / 2; i < n; i++) {
                        String name = loop.getArg(i * 2);
                        String val = loop.getArg(i * 2 + 1);
                        switch (name) {
                            case "no_of_sessions":
                                toRet.get("data").put("sessions", String.valueOf(Integer.parseInt(val)));
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
                            while (k < o) {
                                switch (trial.getArg(k)) {
                                    case "probe":
                                        tempMap.put("probe", trial.getArg(k + 1));
                                        k++;
                                    case "start":
                                        tempMap.put("start", trial.getArg(k + 1) + " " + trial.getArg(k + 2) + " " + trial.getArg(k + 3) + " ");
                                        k += 3;
                                    case "end":
                                        tempMap.put("start", trial.getArg(k + 1) + " " + trial.getArg(k + 2) + " " + trial.getArg(k + 3) + " " + trial.getArg(k + 4) + " " + trial.getArg(k + 5));
                                        k += 5;
                                    case "cue":
                                        tempMap.put("cue" + trial.getArg(k + 1), trial.getArg(k + 2) + " " + trial.getArg(k + 3) + " " + trial.getArg(k + 4) + " " + trial.getArg(k + 5));
                                        k += 5;
                                }
                                k++;
                            }
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
    }

}
