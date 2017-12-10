package com.cirnoteam.cmm.graphic;

import java.io.IOException;
import java.io.InputStream;

public class CustomInputStream extends InputStream {
    public Window window;

    public CustomInputStream(Window window) {
        this.window = window;
    }

    @Override
    public int read() throws IOException {
        return window.getChar();
    }
}
