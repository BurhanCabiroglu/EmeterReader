package com.example.myapplication;

public class ProbeFactory {

    public static Probe createProbe(Class aClass) throws IllegalAccessException, InstantiationException{
        return (Probe) aClass.newInstance();
    }
}
