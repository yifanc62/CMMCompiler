package Semantic;

import java.util.Arrays;

public enum CommandType {
    CMP, //(cmp, value, value, null) value: name/register(<,>)
    JMP, //(jmp, address, null, null)
    JE,  //(je, address, null, null)
    JNE, //(jne, address, null, null)
    JG,  //(jg, address, null, null)
    JGE, //(jge, address, null, null)
    JL,  //(jl, address, null, null)
    JLE, //(jle, address, null, null)
    DEF, //(def, name, value, type) value:int/double/register(<,>) type:'int'/'double'/null
    MOV, //(mov, name, value, type) value:int/double/register(<,>) type:'int'/'double'/null
    IN,  //(in, null, null, null)
    OUT, //(out, null, null, null)
    PUSH,//(push, null, value, type)/(push, name, null, null) value:int/double/register(<,>) type:'int'/'double'/null
    POP, //(pop, value, null, null) value: name/register(<,>)
    INC, //(inc, target, value, type) target:name/register(<,>) value:int/double/register(<,>) type:'int'/'double'/null
    DEC, //(dec, target, value, type) target:name/register(<,>) value:int/double/register(<,>) type:'int'/'double'/null
    ADD, //(add, null, null, null)
    SUB, //(sub, null, null, null)
    MUL, //(mul, null, null, null)
    DIV, //(div, null, null, null)
    MOD, //(div, null, null, null)
    SC,  //(sc, target, null, null) target:name/register(<,>)
    PR;   //(pr, source, null, null) source:name/register(<,>)

    public boolean isJump() {
        return Arrays.asList(JMP, JE, JNE, JG, JGE, JL, JLE).contains(this);
    }
}
