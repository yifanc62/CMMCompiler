package Semantic;

import java.util.HashMap;
import java.util.Map;

public class VariableTable {
    private Map<String, VariableType> varMap;
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

    public void put(String name, VariableType type) {
        varMap.put(name, type);
    }

    public VariableType get(String name) {
        if (varMap.containsKey(name))
            return varMap.get(name);
        else if (parent != null)
            return parent.get(name);
        return null;
    }

    public boolean existCurrent(String name) {
        return varMap.containsKey(name);
    }

    public boolean existAll(String name) {
        if (varMap.containsKey(name))
            return true;
        else if (parent != null)
            return parent.existAll(name);
        return false;
    }
}

