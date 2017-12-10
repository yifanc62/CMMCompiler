package com.cirnoteam.cmm.syntactic;

public class UnexpectedEOFException extends Exception {
    public UnexpectedEOFException() {
        super("Unexpected end of file.");
    }
}
