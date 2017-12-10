package com.cirnoteam.cmm.semantic;

public class CompilerException extends Exception {
    public CompilerException(String message, int line) {
        super(String.format("[line: %d]Compiler error: %s.", line, message));
    }
}
