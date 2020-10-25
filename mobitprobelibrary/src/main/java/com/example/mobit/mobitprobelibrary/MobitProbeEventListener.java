package com.example.mobit.mobitprobelibrary;

public interface MobitProbeEventListener {
    void onConnectionReset();

    void onPowerStatusChanged();

    void onTriggerButtonPressed(int buttonId);
}
