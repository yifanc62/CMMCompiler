package GUI;

import java.io.IOException;
import java.io.OutputStream;

public class CustomErrorStream extends OutputStream {
    private Window window;

    public CustomErrorStream(Window window) {
        this.window = window;
    }

    @Override
    public void write(int b) throws IOException {
        window.outputToError(b);
    }
}
