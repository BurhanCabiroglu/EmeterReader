package com.example.mobit.mobitprobelibrary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.LinkedBlockingQueue;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class MobitMopProbe implements MobitProbe {

    private BluetoothSocket mIrDataSocket;
    private Thread mAtCommandListenerThread;
    private BluetoothSocket mAtCommandSocket;
    private MobitProbeEventListener mEventListener;
    private BlockingQueue<String> mAtCmdResponseQueue;
    private static final int AT_CMD_RESPONSE_TIMEOUT = 1000;

    public MobitMopProbe(MobitProbeEventListener eventListener) {
        mEventListener = eventListener;
        mAtCmdResponseQueue = new LinkedBlockingQueue<String>();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    @Override
    public InputStream getInputStream() throws Exception {
        return (mIrDataSocket.getInputStream());
    }

    @Override
    public OutputStream getOutputStream() throws Exception {
        return (mIrDataSocket.getOutputStream());
    }

    @Override
    public int getBatteryLifePercent() throws Exception {
        mAtCmdResponseQueue.clear();
        mAtCommandSocket.getOutputStream().write(new String("AT+CBC\r\n").getBytes());
        if (!readAtCmdResponseLine().equals("AT+CBC"))
            return (-1);

        /* Skip empty new line */
        readAtCmdResponseLine();

        StringTokenizer st = new StringTokenizer(readAtCmdResponseLine(), ":,");
        if (!st.nextToken().equals("+CBC"))
            return (-1);

        st.nextToken();
        return (Integer.parseInt(st.nextToken()));
    }

    @Override
    public void setEnabled(boolean enabled) {
        /* Always enabled */
    }

    @Override
    public void setBaudRate(int baudRateId) {
        /* No need to adjust the baud rate */
    }

    @Override
    public void close() throws Exception {
        if (mIrDataSocket != null)
            mIrDataSocket.close();

        if (mAtCommandSocket != null)
            mAtCommandSocket.close();

        if (mAtCommandListenerThread != null) {
            mAtCommandListenerThread.interrupt();
            mAtCommandListenerThread.join();
        }

        mAtCommandListenerThread = null;
        mAtCommandSocket = null;
        mIrDataSocket = null;
    }

    @Override
    public void connect(BluetoothDevice device) throws Exception {
        Method m = device.getClass().getMethod("createRfcommSocket", int.class);

        /* Connect to infrared data channel (channel 1) */
        mIrDataSocket = (BluetoothSocket) m.invoke(device, 1);
        mIrDataSocket.connect();
        Thread.sleep(100);

        /* Connect to AT command channel (channel 2) */
        mAtCommandSocket = (BluetoothSocket) m.invoke(device, 2);
        mAtCommandSocket.connect();
        Thread.sleep(100);

        /* Start AT command listener thread */
        mAtCommandListenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                atCommandListener();
            }
        });
        mAtCommandListenerThread.start();
    }

    protected String readAtCmdResponseLine() throws Exception {
        String line = mAtCmdResponseQueue.poll(AT_CMD_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        if (line == null)
            throw new TimeoutException();
        return (line);
    }

    private void atCommandListener() {
        try {
            long bufferTime = System.currentTimeMillis();
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(
                    mAtCommandSocket.getInputStream()));
            while (!Thread.currentThread().isInterrupted()) {
                String line = bufferReader.readLine();
                if (line.startsWith("+BTN:")) {
                    mEventListener.onTriggerButtonPressed(Integer.parseInt(line.substring(5)));
                } else {
                    mAtCmdResponseQueue.put(line);
                }
            }
        } catch (IOException e) {
            if (mAtCommandSocket.isConnected())
                mEventListener.onConnectionReset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
