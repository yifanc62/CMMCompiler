package VirtualMachine;

import static VirtualMachine.VariableType.DOUBLE;
import static VirtualMachine.VariableType.INT;

public class Variable {
    private String[] values;
    private int length;
    private VariableType type;


    public Variable(String value, VariableType type) {
        switch (type) {
            case INT:
                this.values = new String[]{value};
                break;
            case DOUBLE:
                this.values = new String[]{value};
                break;
            default:
                throw new LauncherException("Internal error: Invalid function call of 'Variable(String value, VariableType type)'");
        }
        length = -1;
        this.type = type;
    }

    public Variable(String[] values, VariableType type) {
        switch (type) {
            case ARRAY_INT:
                this.values = values;
                break;
            case ARRAY_DOUBLE:
                this.values = values;
                break;
            default:
                throw new LauncherException("Internal error: Invalid function call of 'Variable(String[] values, VariableType type)'");
        }
        this.length = values.length;
        this.type = type;
    }

    public Variable(Variable variable) {
        values = variable.values;
        length = variable.length;
        type = variable.type;
    }

    public void assign(Variable variable) {
        values = variable.values;
    }

    public void assignChild(String value, int index) {
        values[index] = value;
    }

    public int getIntValue() {
        if (type == INT)
            return Integer.valueOf(values[0]);
        throw new LauncherException("Internal error: Invalid function call of 'getIntValue()'");
    }

    public double getDoubleValue() {
        if (type == DOUBLE)
            return Double.valueOf(values[0]);
        throw new LauncherException("Internal error: Invalid function call of 'getDoubleValue()'");
    }

    public String getValue() {
        return values[0];
    }

    public String[] getValues() {
        return values;
    }

    public int getLength() {
        return length;
    }

    public VariableType getType() {
        return type;
    }
}
