package com.cirnoteam.cmm.machine;

import java.util.HashMap;
import java.util.Map;

public class VariableTable {
    private Map<String, Variable> varMap;
    private VariableTable child;
    private VariableTable parent;

    public VariableTable() {
        varMap = new HashMap<>();
        child = null;
        parent = null;
    }

    private VariableTable(VariableTable parent) {
        this.varMap = new HashMap<>();
        this.child = null;
        this.parent = parent;
    }

    public VariableTable createChild() {
        child = new VariableTable(this);
        return child;
    }

    public VariableTable returnParent() {
        return parent;
    }

    public void put(String name, String value, VariableType type, int line) {
        if (varMap.containsKey(name))
            throw new LauncherException(String.format("Redefined variable '%s'", name), line);
        varMap.put(name, new Variable(value, type));
    }

    public void put(String name, String[] values, VariableType type, int line) {
        if (varMap.containsKey(name))
            throw new LauncherException(String.format("Redefined variable '%s'", name), line);
        varMap.put(name, new Variable(values, type));
    }

    public Variable get(String name, int line) {
        if (varMap.containsKey(name))
            return varMap.get(name);
        if (parent != null)
            return parent.get(name, line);
        throw new LauncherException(String.format("Unknown variable '%s'", name), line);
    }
}
