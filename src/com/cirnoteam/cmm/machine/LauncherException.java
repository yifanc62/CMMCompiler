package com.cirnoteam.cmm.machine;

public class LauncherException extends RuntimeException {
    public LauncherException(String message, int line) {
        super(String.format("[line: %d]Runtime error: %s.", line, message));
    }

    public LauncherException(String message) {
        super(message);
    }
}
