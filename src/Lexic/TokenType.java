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

    public boolean isComparativeOperator() {
        return Arrays.asList(S_EQUAL, S_UNEQUAL, S_GT, S_GE, S_LT, S_LE).contains(this);
    }

    public boolean isAdditiveOperator() {
        return Arrays.asList(S_PLUS, S_MINUS).contains(this);
    }

    public boolean isMultiplicativeOperator() {
        return Arrays.asList(S_MULTIPLY, S_DIVIDE, S_MOD).contains(this);
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
        return types.length != 0 && Arrays.asList(types).contains(this);
    }

    public String toString() {
        switch (this) {
            case K_STMT_IF:
                return "'if'";
            case K_STMT_ELSE:
                return "'else'";
            case K_STMT_WHILE:
                return "'while'";
            case K_STMT_BREAK:
                return "'break'";
            case K_STMT_FOR:
                return "'for'";
            case K_IO_READ:
                return "'read'";
            case K_IO_WRITE:
                return "'write'";
            case K_TYPE_INT:
                return "'int'";
            case K_TYPE_DOUBLE:
                return "'double'";
            case V_INT:
                return "integer";
            case V_DOUBLE:
                return "decimal";
            case V_BOOL_TRUE:
                return "'true'";
            case V_BOOL_FALSE:
                return "'false'";
            case V_VARIABLE:
                return "variable";
            case S_PLUS:
                return "'+'";
            case S_MINUS:
                return "'-'";
            case S_MULTIPLY:
                return "'*'";
            case S_DIVIDE:
                return "'/'";
            case S_MOD:
                return "'%'";
            case S_EQUAL:
                return "'=='";
            case S_UNEQUAL:
                return "'!='";
            case S_GT:
                return "'>'";
            case S_GE:
                return "'>='";
            case S_LT:
                return "'<'";
            case S_LE:
                return "'<='";
            case S_ASSIGN:
                return "'='";
            case S_PARENTHESIS_L:
                return "'('";
            case S_PARENTHESIS_R:
                return "')'";
            case S_BRACKET_L:
                return "'['";
            case S_BRACKET_R:
                return "']'";
            case S_BRACE_L:
                return "'{'";
            case S_BRACE_R:
                return "'}'";
            case S_COMMA:
                return "','";
            case S_SEMICOLON:
                return "';'";
            case C:
                return "comment";
            default:
                return "";
        }
    }
}

