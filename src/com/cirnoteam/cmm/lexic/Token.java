package com.cirnoteam.cmm.lexic;

public class Token {
    private int line;
    private int position;
    private int index;
    private TokenType type;
    private String value;
    private int length;

    public Token(int line, int position, int index, TokenType type, String value) {
        this.line = line;
        this.position = position;
        this.index = index;
        this.type = type;
        this.value = value;
        this.length = value.length();
    }

    public int getLine() {
        return line;
    }

    public int getPosition() {
        return position;
    }

    public int getIndex() {
        return index;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLength() {
        return length;
    }

    public Token setValueWithoutLength(String value) {
        this.value = value;
        return this;
    }
}
