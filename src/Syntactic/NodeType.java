package Syntactic;

public enum NodeType {
    PROGRAM,             //所有子节点为statement
    STMT_BLOCK,          //所有子节点为statement
    STMT_IF,             //[0]为条件@EXP_LOGICAL [1]为语句@STMT_BLOCK [2]为else语句@STMT_BLOCK&null
    STMT_WHILE,          //[0]为条件@EXP_LOGICAL [1]为语句@STMT_BLOCK
    STMT_FOR,            //[0]为初始化@STMT_ASSIGN&STMT_DECLARE&null [1]为条件@EXP_LOGICAL&null [2]为赋值@STMT_ASSIGN&null [3]为语句@STMT_BLOCK
    STMT_ASSIGN,         //[0]为变量@VARIABLE [1]为值@EXP_ARITHMETICAL
    STMT_DECLARE,        //[0]为类型@TYPE_INT&TYPE_DOUBLE [1]及以后为变量名及初值@IDENTIFIER
    STMT_READ,           //[0]为变量@VARIABLE
    STMT_WRITE,          //[0]为值@EXP_ARITHMETICAL
    TYPE_INT,            //value若不为null则为数组元素个数
    TYPE_DOUBLE,         //value若不为null则为数组元素个数
    VARIABLE,            //[0]为变量名@NAME [1]为变量中的元素索引@INDEX&null
    NAME,                //value为变量名
    INDEX,               //value为索引
    IDENTIFIER,          //[0]为变量名@NAME [1]为初值@V_INT&V_DOUBLE&null
    EXP_LOGICAL,         //[0]为左式或固定值@EXP_ARITHMETICAL&V_BOOL [1]为运算符@OP_EQUAL&OP_UNEQUAL&OP_GT&OP_GE&OP_LT&OP_LE&null [2]为右式@EXP_ARITHMETICAL&null
    EXP_ARITHMETICAL,    //
    TERM,                //
    FACTOR,              //
    V_INT,               //value为整数值
    V_DOUBLE,            //value为浮点数值
    V_BOOL,              //value为"true"或"false"
    OP_PLUS,             //没有子节点或value
    OP_MINUS,            //没有子节点或value
    OP_MULTIPLY,         //没有子节点或value
    OP_DIVIDE,           //没有子节点或value
    OP_MOD,              //没有子节点或value
    OP_EQUAL,            //没有子节点或value
    OP_UNEQUAL,          //没有子节点或value
    OP_GT,               //没有子节点或value
    OP_GE,               //没有子节点或value
    OP_LT,               //没有子节点或value
    OP_LE                //没有子节点或value
}
