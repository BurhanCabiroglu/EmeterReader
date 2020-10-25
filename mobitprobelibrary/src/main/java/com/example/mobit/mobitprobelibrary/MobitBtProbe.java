package com.example.mobit.mobitprobelibrary;

import java.util.UUID;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ArrayBlockingQueue;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class MobitBtProbe implements MobitProbe {
    private enum PACKET_CLASS {
        EVENT,
        COMMAND,
        RESULT
    }

    private enum PROBE_EVENT {
        POWER,
        TRIGGER
    }

    private enum PROBE_COMMAND {
        GETVERSION,
        GETBTVERSION,
        POWEROFF,
        SETLED,
        GETPOWERSTATUS,
        IRENABLE,
        IRSETBAUDRATE,
        SETAUTOPOWEROFFTIME
    }

    private enum PROBE_RESULT {
        SUCCESS,
        FAILED,
        NOTIMPLEMENTED
    }

    public enum IR_BAUDRATE {
        IRBR300,
        IRBR600,
        IRBR1200,
        IRBR2400,
        IRBR4800,
        IRBR9600,
        IRBR19200
    }

    private static final int RESULT_TIMEOUT = 2000;

    public class PowerStatus {
        public boolean charging;
        public int batteryVoltage;
        public int batteryLifePercent;
    }

    private class CommandResult {
        public int mCode;
        public byte[] mData;

        public CommandResult(int code, byte[] data) {
            mCode = code;
            mData = data;
        }
    }

    private Thread mInputThread;
    private BluetoothSocket mSocket;
    private CircularBuffer mCircularInputBuffer;
    private MobitProbeEventListener mEventListener;
    private BlockingQueue<CommandResult> mCommandResultQueue;
    private CircularBufferInputStream mCircularBufferInputStream;

    public MobitBtProbe(MobitProbeEventListener eventListener) {
        mEventListener = eventListener;
        mCircularInputBuffer = new CircularBuffer(1024);
        mCommandResultQueue = new ArrayBlockingQueue<CommandResult>(1);
        mCircularBufferInputStream = new CircularBufferInputStream(mCircularInputBuffer);
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
    public InputStream getInputStream() {
        return (mCircularBufferInputStream);
    }

    @Override
    public OutputStream getOutputStream() throws Exception {
        return (mSocket.getOutputStream());
    }

    @Override
    public int getBatteryLifePercent() throws Exception {
        return (cmdGetPowerStatus().batteryLifePercent);
    }

    @Override
    public void setEnabled(boolean enabled) throws Exception {
        cmdIrEnable(enabled);
    }

    @Override
    public void setBaudRate(int baudRateId) throws Exception {
        cmdIrSetBaudRate(convertToIrBaudRate(baudRateId));
    }

    @Override
    public void close() throws Exception {
        mSocket.close();
        mInputThread.interrupt();
        mInputThread.join();
    }

    @Override
    public void connect(BluetoothDevice device) throws Exception {
        // Standard SerialPortService ID
        mSocket = device.createInsecureRfcommSocketToServiceRecord(UUID
                .fromString("00001101-0000-1000-8000-00805f9b34fb"));

        mSocket.connect();
        Thread.sleep(1000);
        mSocket.getInputStream().skip(mSocket.getInputStream().available());

        mCircularInputBuffer.empty();

        mInputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                readInputLoop();
            }
        });
        mInputThread.start();

        cmdGetVersion();
    }

    private int cmdGetVersion() throws Exception {
        writeCommand(PROBE_COMMAND.GETVERSION, null);

        byte[] resultData = readResult();
        if (resultData.length != 2)
            throw new Exception("Invalid data");

        return ((resultData[1] << 8) & 0xff00) | (resultData[0] & 0xff);
    }

    private String cmdGetBtVersion() throws Exception {
        writeCommand(PROBE_COMMAND.GETBTVERSION, null);
        return (new String(readResult()));
    }

    private void cmdPowerOff() throws Exception {
        writeCommand(PROBE_COMMAND.POWEROFF, null);
        readResult();
    }

    private void cmdSetLed(boolean enable) throws Exception {
        writeCommand(PROBE_COMMAND.SETLED, new byte[]{(byte) (enable ? 1 : 0)});
        readResult();
    }

    private PowerStatus cmdGetPowerStatus() throws Exception {
        writeCommand(PROBE_COMMAND.GETPOWERSTATUS, null);
        byte[] result = readResult();
        if (result.length != 4)
            throw new Exception("Invalid data");

        PowerStatus status = new PowerStatus();
        status.charging = (result[0] != 0);
        status.batteryVoltage = ((result[2] << 8) & 0xff00) | (result[1] & 0xff);
        status.batteryLifePercent = result[3];
        return (status);
    }

    private void cmdIrEnable(boolean enable) throws Exception {
        writeCommand(PROBE_COMMAND.IRENABLE, new byte[]{(byte) (enable ? 1 : 0)});
        readResult();
    }

    private void cmdIrSetBaudRate(IR_BAUDRATE baudRate) throws Exception {
        writeCommand(PROBE_COMMAND.IRSETBAUDRATE, new byte[]{(byte) baudRate.ordinal()});
        readResult();
    }

    private void cmdSetAutoPowerOffTime(int duration) throws Exception {
        writeCommand(PROBE_COMMAND.SETAUTOPOWEROFFTIME, new byte[]{(byte) (duration & 0xff),
                (byte) ((duration >> 8) & 0xff)});
        readResult();
    }

    private void writeCommand(PROBE_COMMAND command, byte[] parameters) throws Exception {
        OutputStream outputStream = mSocket.getOutputStream();
        synchronized (outputStream) {
            outputStream.write((1 << 7) | (command.ordinal() << 2) | PACKET_CLASS.COMMAND.ordinal());
            outputStream.write(2 + ((parameters == null) ? 0 : parameters.length));
            if (parameters != null)
                outputStream.write(parameters);

            outputStream.flush();
        }
    }

    private byte[] readResult() throws Exception {
        CommandResult result = mCommandResultQueue.poll(RESULT_TIMEOUT, TimeUnit.MILLISECONDS);
        if (result == null)
            throw new TimeoutException();

        if (result.mCode == PROBE_RESULT.SUCCESS.ordinal())
            return (result.mData);
        else if (result.mCode == PROBE_RESULT.FAILED.ordinal())
            throw new Exception("The operarion failed");
        else if (result.mCode == PROBE_RESULT.NOTIMPLEMENTED.ordinal())
            throw new Exception("Command not implemented");
        else
            throw new Exception(String.format("Unknown result code(%d)", result.mCode));
    }

    private void handlePacket(int packetClass, int packetCode, byte[] packetBody) throws Exception {
        if (packetClass == PACKET_CLASS.EVENT.ordinal()) {
            if (packetCode == PROBE_EVENT.POWER.ordinal())
                mEventListener.onPowerStatusChanged();
            else if (packetCode == PROBE_EVENT.TRIGGER.ordinal())
                mEventListener.onTriggerButtonPressed(1);
        } else if (packetClass == PACKET_CLASS.RESULT.ordinal()) {
            mCommandResultQueue.clear();
            mCommandResultQueue.put(new CommandResult(packetCode, packetBody));
        }
    }

    private void readInputLoop() {
        try {
            int state = 0;
            int packetCode = 0;
            int packetClass = 0;
            int packetBodyIndex = 0;
            int packetBodyLength = 0;
            byte[] packetBody = null;

            while (!Thread.currentThread().isInterrupted()) {
                int data = mSocket.getInputStream().read();
                switch (state) {
                    case 0:
                        if ((data & (1 << 7)) == 0) {
                            mCircularInputBuffer.write((byte) data);
                        } else {
                            packetClass = (data & 0x03);
                            packetCode = (data >> 2) & 0x1f;
                            state = 1;
                        }
                        break;
                    case 1:
                        packetBodyLength = (data & 0xff) - 2;
                        if (packetBodyLength < 0) {
                            state = 0;
                        } else if (packetBodyLength == 0) {
                            handlePacket(packetClass, packetCode, null);
                            state = 0;
                        } else {
                            packetBody = new byte[packetBodyLength];
                            packetBodyIndex = 0;
                            state = 2;
                        }
                        break;
                    case 2:
                        packetBody[packetBodyIndex++] = (byte) data;
                        if (packetBodyIndex == packetBodyLength) {
                            handlePacket(packetClass, packetCode, packetBody);
                            state = 0;
                        }
                        break;
                }
            }
        } catch (IOException e) {
            if (mSocket.isConnected())
                mEventListener.onConnectionReset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IR_BAUDRATE convertToIrBaudRate(int baudRateId) {
        switch (baudRateId) {
            case 0:
                return (IR_BAUDRATE.IRBR300);
            case 1:
                return (IR_BAUDRATE.IRBR600);
            case 2:
                return (IR_BAUDRATE.IRBR1200);
            case 3:
                return (IR_BAUDRATE.IRBR2400);
            case 4:
                return (IR_BAUDRATE.IRBR4800);
            case 5:
                return (IR_BAUDRATE.IRBR9600);
            case 6:
                return (IR_BAUDRATE.IRBR19200);
            default:
                throw new IllegalArgumentException("Invalid baudrateId");
        }
    }
}