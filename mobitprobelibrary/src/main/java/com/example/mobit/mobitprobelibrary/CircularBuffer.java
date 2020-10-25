package com.example.mobit.mobitprobelibrary;

public class CircularBuffer {
    private byte[] mBuffer;
    private int mStartPos, mEndPos;

    public CircularBuffer(int size) {
        mStartPos = mEndPos = 0;
        mBuffer = new byte[size];
    }

    public void empty() {
        mStartPos = mEndPos;
    }

    public boolean isEmpty() {
        return (mStartPos == mEndPos);
    }

    public boolean isFull() {
        return (mStartPos == ((mEndPos + 1) % mBuffer.length));
    }

    public byte read() throws InterruptedException {
        while (isEmpty())
            Thread.sleep(10);

        byte data = mBuffer[mStartPos];
        mStartPos = (mStartPos + 1) % mBuffer.length;
        return (data);
    }

    public void write(byte data) throws InterruptedException {
        while (isFull())
            Thread.sleep(10);

        mBuffer[mEndPos] = data;
        mEndPos = (mEndPos + 1) & (mBuffer.length - 1);
    }
}
