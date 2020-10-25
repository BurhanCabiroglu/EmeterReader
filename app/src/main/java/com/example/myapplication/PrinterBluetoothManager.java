package com.example.myapplication;

import android.app.Activity;
//import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
//import android.widget.ArrayAdapter;
import android.os.Looper;
import android.widget.Toast;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class PrinterBluetoothManager {

    private BluetoothDevice mDevice;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private ArrayList<String> arrayList;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;
    private String connected_device="";

    public String getConnected_device() {
        return connected_device;
    }

    public PrinterBluetoothManager(BluetoothDevice device){
        this.mDevice=device;
        connect();
        listenData();
    }



    public ArrayList<String> getArrayList() {
        return arrayList;
    }

    public void choosePrinter(int position){
        mDevice=bluetoothDevices.get(position);
        connect();
        listenData();
    }

    public void findPrinter(Activity activity){
        try{
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(bluetoothAdapter == null){
                Toast.makeText(activity.getApplicationContext(),"Bluetooth'a bağlanamadı",Toast.LENGTH_LONG).show();
            }
            if(!bluetoothAdapter.isEnabled()){
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBluetooth,0);
            }
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if(pairedDevices.size()>0){
                arrayList=new ArrayList<>();
                bluetoothDevices=new ArrayList<>();
                for(BluetoothDevice device : pairedDevices){
                    //getBluetoothClass ile cihaz bağlanmak istediğimiz cihazın türüne karar verebiliriz.
                    String device_type=device.getBluetoothClass().toString();

                    if(device_type.equals("40680")){
                        bluetoothDevices.add(device);
                        arrayList.add(device.getName());
                    }
                }
            }
            else{
                Toast.makeText(activity.getApplicationContext(),"Cihazı Bulamadık!!",Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void connect(){
        try{
            //Standart Seri Port Servis UUID
//
            if(socket != null && socket.isConnected()){
                socket.close();
            }
            UUID uuid=UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            socket=mDevice.createRfcommSocketToServiceRecord(uuid);
            if(socket != null){
                socket.connect();
                outputStream=socket.getOutputStream();
                inputStream=socket.getInputStream();
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void connectOf() {
        try{
            stopWorker=true;
            outputStream.close();
            inputStream.close();;
            socket.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void listenData(){
        try{
            final Handler handler= new Handler();


            // for new line
            final byte delimeter =10;
            stopWorker=false;
            readBufferPosition=0;
            readBuffer=new byte[1024];

            Thread workerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {
                            int bytesAvailable = inputStream.available();

                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimeter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                connected_device = data;
                                            }
                                        },100);

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            workerThread.start();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void print(String params){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        byte[] mFormat=new byte[]{27,33,0};

        mFormat[2]=((byte) (0x1 | mFormat[2]));
        try {
            outputStream.write(params.getBytes());

        }catch (Exception e){
            e.printStackTrace();
        }
        /*
        try{
            StringFormat stringFormat=new StringFormat("deneme");
            String print="\n\n\n\n\n\n"+stringFormat.format1();
            outputStream.write(mFormat);
            for(int i = 0;i<3;i++){
                outputStream.write(print.getBytes());
                print=stringFormat.format1();
            }
            StringFormat stringFormat2=new StringFormat("deneme2","deneme2");

            StringFormat stringFormat3=new StringFormat("deneme3","deneme3","deneme3");
            String print3="\n\n\n\n\n\n\n\n\n\n"+stringFormat3.format3();
           for(int i = 0;i<5;i++){

                outputStream.write(print3.getBytes());
                print3=stringFormat3.format3();;
            }
        }catch (Exception e){
            e.printStackTrace();
        }*/

    }





}
