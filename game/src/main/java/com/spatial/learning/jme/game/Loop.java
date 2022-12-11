package com.admin.vwm.game.classes;

import java.util.ArrayList;
import java.util.List;

public class Loop {
    public String name;
    public List<Loop> children = new ArrayList<>();
    protected List<String> args = new ArrayList<>();
    protected Loop parent;

    public Loop(String name, List<String> args) {
        this.name = name;
        this.args.addAll(args);
    }

    public Loop addChild(Loop toAdd) {
        children.add(toAdd);
        toAdd.parent(this);
        return toAdd;
    }

    public Loop getChild(int index) {
        return children.get(index);
    }

    public List<Loop> getChildren() {
        return this.children;
    }

    public String addArg(String argument, int index) {
        args.add(index, argument);
        return argument;
    }

    public String addArg(String argument) {
        args.add(argument);
        return argument;
    }

    public List<String> addArgs(List<String> args) {
        this.args.addAll(args);
        return args;
    }

    public String getArg(int i) {
        return this.args.get(i);
    }

    public List<String> getArgs() {
        return args;
    }

    public Loop parent(Loop parent) {
        this.parent = parent;
        return parent;
    }

    public Loop parent() {
        return this.parent;
    }
}
