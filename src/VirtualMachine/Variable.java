package VirtualMachine;

import static VirtualMachine.VariableType.*;

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
                throw new LauncherException();
        }
        length = -1;
        this.type = type;
    }

    public Variable(String[] values, VariableType type) {
        switch (type) {
            case ARRAY_INT:
                break;
            case ARRAY_DOUBLE:
                break;
            default:
                throw new LauncherException();
        }
        this.length = values.length;
        this.type = type;
    }

    public int getIntValue() {
        if (type == INT)
            return Integer.valueOf(values[0]);
        throw new LauncherException();
    }

    public double getDoubleValue() {
        if (type == DOUBLE)
            return Double.valueOf(values[0]);
        throw new LauncherException();
    }

    public int[] getIntArray() {
        if (type == ARRAY_INT) {
            int[] result = new int[length];
            for (int i = 0; i < result.length; i++) {
                result[i] = Integer.valueOf(values[i]);
            }
            return result;
        }
        throw new LauncherException();
    }

    public double[] getDoubleArray() {
        if (type == ARRAY_DOUBLE) {
            double[] result = new double[length];
            for (int i = 0; i < result.length; i++) {
                result[i] = Double.valueOf(values[i]);
            }
            return result;
        }
        throw new LauncherException();
    }

    public String getValue() {
        return values[0];
    }

    public String[] getValues() {
        return values;
    }

    public VariableType getType() {
        return type;
    }
}
