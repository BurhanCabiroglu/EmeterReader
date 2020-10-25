package com.example.myapplication;


import android.bluetooth.BluetoothDevice;
import android.content.res.Resources;
import android.widget.TextView;

import com.noven.m2m.dlms.DLMSOperationParameters;
import com.noven.m2m.dlms.DLMSOperationParametersSDRO;
import com.noven.m2mapi.NvNCommManager;

public class Noven implements Probe {
    TextView txtResultLog;
    Resources resources;

    public void setTxtResultLog(TextView txtResultLog) {
        this.txtResultLog = txtResultLog;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    @Override
    public void connect(BluetoothDevice device) {
        String name = device.getName();
        String address = device.getAddress();
        NvNCommManager.Connect(name,address);
    }

    @Override
    public void readData() {
        DLMSOperationParametersSDRO params = new DLMSOperationParametersSDRO();
        trigExecuteOperation(params);
    }
    private void trigExecuteOperation(DLMSOperationParameters params)
    {
        txtResultLog.setText("");
        //txtResultLog.setBackgroundColor(resources.getColor(R.color.colorINPROGRESS));
        NvNCommManager.ExecuteOperation(params);
    }
}
