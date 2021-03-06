package com.cirnoteam.cmm.semantic;

public class Command {
    private CommandType type;
    private String arg0;
    private String arg1;
    private String arg2;
    private int line;

    public Command(CommandType type, String arg0, String arg1, String arg2, int line) {
        this.type = type;
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.line = line;
    }

    public Command(CommandType type, String arg0, String arg1, String arg2) {
        this.type = type;
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.line = -1;
    }

    public static Command decode(String encodedStr) {
        try {
            encodedStr = encodedStr.trim();
            int i = encodedStr.lastIndexOf('.');
            int line = Integer.parseInt(encodedStr.substring(i + 1));
            encodedStr = encodedStr.substring(0, i).trim();
            if (!(encodedStr.startsWith("(") && encodedStr.endsWith(")")))
                throw new Exception();
            encodedStr = encodedStr.substring(1, encodedStr.length() - 1);
            String[] results = encodedStr.split(",");
            CommandType type = CommandType.valueOf(results[0].trim().toUpperCase());
            String arg0 = results[1].trim();
            String arg1 = results[2].trim();
            String arg2 = results[3].trim();
            return new Command(type, arg0.equals("") ? null : arg0, arg1.equals("") ? null : arg1, arg2.equals("") ? null : arg2, line);
        } catch (Exception e) {
            return null;
        }
    }

    public CommandType getType() {
        return this.type;
    }

    public Command setType(CommandType type) {
        this.type = type;
        return this;
    }

    public String getArg0() {
        return this.arg0;
    }

    public Command setArg0(String arg0) {
        this.arg0 = arg0;
        return this;
    }

    public String getArg1() {
        return this.arg1;
    }

    public Command setArg1(String arg1) {
        this.arg1 = arg1;
        return this;
    }

    public String getArg2() {
        return this.arg2;
    }

    public Command setArg2(String arg2) {
        this.arg2 = arg2;
        return this;
    }

    public int getLine() {
        return line;
    }

    public Command setLine(int line) {
        this.line = line;
        return this;
    }

    public Command addressPlus(int n) {
        return setArg0(Integer.toString(Integer.valueOf(getArg0()) + n));
    }

    public Command setAddress(int n) {
        return setArg0(Integer.toString(n));
    }

    public String encode() {
        return String.format("(%s,%s,%s,%s).%d", type.toString(), arg0 == null ? " " : arg0, arg1 == null ? " " : arg1, arg2 == null ? " " : arg2, line);
    }
}
