package com.cirnoteam.cmm.graphic;

import java.io.IOException;
import java.io.OutputStream;

public class CustomOutputStream extends OutputStream {
    private Window window;

    public CustomOutputStream(Window window) {
        this.window = window;
    }

    @Override
    public void write(int b) throws IOException {
        window.outputToResult(b);
    }
}
