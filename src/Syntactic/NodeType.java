package Syntactic;

public enum NodeType {
    PROGRAM,
    STMT,
    STMT_BLOCK,
    STMT_IF,
    STMT_WHILE,
    STMT_FOR,
    STMT_ASSIGN,
    STMT_DECLARE,
    STMT_READ,
    STMT_WRITE,
    VARIABLE,
    EXP,
    EXP_LOGICAL,
    EXP_ARITHMETICAL,
    TERM,
    FACTOR,
    OP_COMPARATIVE,
    OP_ADDITIVE,
    OP_MULTIPLICATIVE,
    OP_PLUS,
    OP_MINUS,
    OP_MULTIPLY,
    OP_DIVIDE,
    OP_MOD,
    OP_EQUAL,
    OP_UNEQUAL,
    OP_GT,
    OP_GE,
    OP_LT,
    OP_LE
}