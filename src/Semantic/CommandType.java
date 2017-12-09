package Semantic;

import java.util.Arrays;

public enum CommandType {
    //register: '<'->eax '>'->ebx
    //type: 'int'->int 'double'->double
    //address: int
    //name: String
    //length: int
    //value: int/double
    CMP, //(cmp, register, register, null)
    JMP, //(jmp, address, null, null)
    JE,  //(je, address, null, null)
    JNE, //(jne, address, null, null)
    JG,  //(jg, address, null, null)
    JGE, //(jge, address, null, null)
    JL,  //(jl, address, null, null)
    JLE, //(jle, address, null, null)
    DEF, //(def, name, register, type)/(def, name, length, type)/(def, name, null, type)
    MOV, //(mov, name, null, register)/(mov, name, register, register)/(mov, register, name, null)/(mov, register, name, register)/(mov, register, value, type)
    IN,  //(in, null, null, null)
    OUT, //(out, null, null, null)
    PUSH,//(push, null, value, type)/(push, register, null, null)
    POP, //(pop, register, null, null)
    ADD, //(add, null, null, null)
    SUB, //(sub, null, null, null)
    MUL, //(mul, null, null, null)
    DIV, //(div, null, null, null)
    MOD, //(mod, null, null, null)
    SC,  //(sc, register, null, null)
    PR,  //(pr, register, null, null)
    EXIT;//(exit, null, null, null)

    public boolean isJump() {
        return Arrays.asList(JMP, JE, JNE, JG, JGE, JL, JLE).contains(this);
    }
}
