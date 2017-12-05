package Lexic;

import java.util.Arrays;

public enum TokenType {
    K_STMT_IF,
    K_STMT_ELSE,
    K_STMT_WHILE,
    K_STMT_BREAK,
    K_STMT_FOR,
    K_IO_READ,
    K_IO_WRITE,
    K_TYPE_INT,
    K_TYPE_DOUBLE,
    V_INT,
    V_DOUBLE,
    V_BOOL_TRUE,
    V_BOOL_FALSE,
    V_VARIABLE,
    S_PLUS,
    S_MINUS,
    S_MULTIPLY,
    S_DIVIDE,
    S_MOD,
    S_EQUAL,
    S_UNEQUAL,
    S_GT,
    S_GE,
    S_LT,
    S_LE,
    S_ASSIGN,
    S_PARENTHESIS_L,
    S_PARENTHESIS_R,
    S_BRACKET_L,
    S_BRACKET_R,
    S_BRACE_L,
    S_BRACE_R,
    S_COMMA,
    S_SEMICOLON,
    C,
    E_UNRECOGNIZED,
    E_COMMENT;

    public boolean isStatement() {
        return Arrays.asList(K_STMT_IF, K_STMT_ELSE, K_STMT_WHILE, K_STMT_BREAK, K_STMT_FOR).contains(this);
    }

    public boolean isIO() {
        return Arrays.asList(K_IO_READ, K_IO_WRITE).contains(this);
    }

    public boolean isType() {
        return Arrays.asList(K_TYPE_INT, K_TYPE_DOUBLE).contains(this);
    }

    public boolean isKeyword() {
        return isStatement() || isIO() || isType();
    }

    public boolean isValue() {
        return Arrays.asList(V_INT, V_DOUBLE, V_BOOL_TRUE, V_BOOL_FALSE).contains(this);
    }

    public boolean isVariable() {
        return this == V_VARIABLE;
    }

    public boolean isSymbol() {
        return Arrays.asList(S_PLUS, S_MINUS, S_MULTIPLY, S_DIVIDE, S_MOD, S_EQUAL, S_UNEQUAL, S_GT, S_GE, S_LT, S_LE, S_ASSIGN, S_PARENTHESIS_L, S_PARENTHESIS_R, S_BRACKET_L, S_BRACKET_R, S_BRACE_L, S_BRACE_R, S_COMMA, S_SEMICOLON).contains(this);
    }

    public boolean isComment() {
        return this == C;
    }

    public boolean isUnrecognized() {
        return this == E_UNRECOGNIZED;
    }

    public boolean isCommentNotClosed() {
        return this == E_COMMENT;
    }

    public boolean isError() {
        return isUnrecognized() || isCommentNotClosed();
    }

    public boolean isOneOf(TokenType... types) {
        return Arrays.asList(types).contains(this);
    }
}

