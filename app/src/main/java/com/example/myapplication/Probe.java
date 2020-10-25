package com.example.myapplication;

import android.bluetooth.BluetoothDevice;

public interface Probe {

    void connect(BluetoothDevice device);
    void readData();


}
