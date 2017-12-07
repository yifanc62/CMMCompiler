package VirtualMachine;

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

    public VariableTable(VariableTable parent) {
        varMap = new HashMap<>();
        child = null;
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
        if (!varMap.containsKey(name))
            varMap.put(name, new Variable(value, type));
        throw new LauncherException(String.format("[line: %d]Runtime error: Duplication variable name '%s'", line, name));
    }

    public Variable get(String name, int line) {
        if (varMap.containsKey(name))
            return varMap.get(name);
        if (parent != null)
            return parent.get(name, line);
        throw new LauncherException(String.format("[line: %d]Runtime error: Variable '%s' not found", line, name));
    }
}
