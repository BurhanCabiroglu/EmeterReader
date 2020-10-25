package com.example.mobit.mobitprobelibrary;

import java.io.IOException;
import java.io.InputStream;

public class CircularBufferInputStream extends InputStream {
    private CircularBuffer mBuffer;

    public CircularBufferInputStream(CircularBuffer buffer) {
        mBuffer = buffer;
    }

    @Override
    public int available() {
        return (mBuffer.isEmpty() ? 0 : 1);
    }

    @Override
    public int read() throws IOException {
        try {
            while (mBuffer.isEmpty())
                Thread.sleep(10);

            return (mBuffer.read());
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}
