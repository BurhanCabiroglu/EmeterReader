package com.example.mobit.mobitprobelibrary;

import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothDevice;

public interface MobitProbe extends AutoCloseable {
    void connect(BluetoothDevice device) throws Exception;

    InputStream getInputStream() throws Exception;

    OutputStream getOutputStream() throws Exception;

    int getBatteryLifePercent() throws Exception;

    void setEnabled(boolean enabled) throws Exception;

    void setBaudRate(int baudRateId) throws Exception;
}
