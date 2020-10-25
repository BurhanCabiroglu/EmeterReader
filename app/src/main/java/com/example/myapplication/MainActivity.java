package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.mobit.mobitprobelibrary.MobitProbe;
import com.example.mobit.mobitprobelibrary.MobitProbeEventListener;
import com.noven.m2m.bluetooth.DeviceDiscoveryEvent;
import com.noven.m2m.dlms.DLMSMeterIdentityReadResult;
import com.noven.m2m.dlms.DLMSMeterLoadProfileReadResult;
import com.noven.m2m.dlms.DLMSMeterRegisterReadResult;
import com.noven.m2m.dlms.DLMSOperationResult;
import com.noven.m2mapi.NvNCommManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NvNCommManager.NovenApiListener, MobitProbeEventListener {

    Mobit mobit;
    Noven noven;


    MobitProbe mobitProbe;

    public BluetoothAdapter bluetoothAdapter;
    public static int REQUEST_ENABLE_BT=1;
    public BluetoothDevice printerDevice=null;
    public BluetoothDevice probeDevice=null;


    private List<BluetoothDevice> avaibleDevices;
    private List<String> devicesNames;


    /** Declaring Botton Buttons*/
    protected Button readButton;
    protected Button clearButton;
    protected Button printButton;
    /* ----- End ----- */


    /** Declaring Toolbar Elements*/
    protected ImageButton probeButton;
    protected ImageButton printerButton;
    protected TextView probeConnectionText;
    protected ProgressBar progressBar;
    protected TextView printerConnectionText;
    protected ImageView connectionImage;
    /* ----- End ----- */


    /** Declaring ListViews*/
    protected ListView probeListView;
    ArrayAdapter<String> probe_adapter;
    protected ListView printerListView;
    ArrayAdapter<String> printer_adapter;
    protected ListView dataListView;
    ArrayAdapter<String> data_adapter;
    List<String> obistlist;
    protected TextView textView;

    /* ----- End ----- */



    /** Declaring Printer*/
    public PrinterBluetoothManager printerBluetoothManager;
    /* ----- End ----- */



    /** Declaring executor service*/
    public ExecutorService executorService;
    /* ----- End ----- */



    public static String allresults="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        NvNCommManager.init(this, null, this);



        textView=findViewById(R.id.textView);
        textView.setVisibility(View.GONE);



        String[] permissions={Manifest.permission.BLUETOOTH_ADMIN,Manifest.permission.BLUETOOTH,Manifest.permission.ACCESS_COARSE_LOCATION};
        this.requestPermissions(permissions,1);

        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        avaibleDevices=new ArrayList<>();
        devicesNames=new ArrayList<>();
        obistlist=new ArrayList<>();


        connectionImage=findViewById(R.id.connectionImage);
        progressBar=findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        readButton=findViewById(R.id.readButton);
        printButton=findViewById(R.id.printButton);
        clearButton=findViewById(R.id.clearButton);


        probeButton=findViewById(R.id.probeButton);
        printerButton=findViewById(R.id.printerButton);
        probeConnectionText=findViewById(R.id.probeConnectionText);
        printerConnectionText=findViewById(R.id.printerConnectionText);

        probeListView=findViewById(R.id.probeList);
        probe_adapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,android.R.id.text1,devicesNames);
        probeListView.setAdapter(probe_adapter);
        probeListView.setVisibility(View.GONE);

        printerListView=findViewById(R.id.printerList);
        printer_adapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,android.R.id.text1,devicesNames);
        printerListView.setAdapter(printer_adapter);
        printerListView.setVisibility(View.GONE);


        dataListView=findViewById(R.id.dataList);
        data_adapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,android.R.id.text1,obistlist);
        dataListView.setAdapter(data_adapter);
        dataListView.setVisibility(View.GONE);


        printerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printerDevice=null;
                avaibleDevices.clear();
                devicesNames.clear();
                textView.setVisibility(View.GONE);
                printer_adapter.clear();


                printerListView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                probeListView.setVisibility(View.GONE);
                dataListView.setVisibility(View.GONE);

                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(printer_receiver, filter);
                System.out.println("Basıldı");
                bluetoothAdapter.startDiscovery();


                new CountDownTimer(20000,1000) {
                    @Override
                    public void onTick(long l) {}

                    @Override
                    public void onFinish() {
                        System.out.println("printer receiver sonlandırıldı...");
                        bluetoothAdapter.cancelDiscovery();

                        try{
                            unregisterReceiver(printer_receiver);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            System.out.println("printer probe register hali hazırda unregistered edilmis");
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }.start();
            }
        });


        probeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                probeDevice=null;
                avaibleDevices.clear();
                devicesNames.clear();
                probe_adapter.clear();

                probeListView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
                printerListView.setVisibility(View.GONE);
                dataListView.setVisibility(View.GONE);



                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(probe_receiver, filter);
                System.out.println("Basıldı");
                bluetoothAdapter.startDiscovery();

                new CountDownTimer(20000,1000) {
                    @Override
                    public void onTick(long l) {}

                    @Override
                    public void onFinish() {
                        System.out.println("probe receiver sonlandırıldı...");
                        bluetoothAdapter.cancelDiscovery();
                        try{
                            unregisterReceiver(probe_receiver);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            System.out.println("probe register hali hazırda unregistered edilmis");
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }.start();
            }
        });

        probeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                probeDevice= avaibleDevices.get(i);
                probeDevice.createBond();


                if(probeDevice.getBondState()==BluetoothDevice.BOND_BONDED){

                    connectionImage.setImageResource(R.drawable.conyes);
                    probeButton.setBackgroundResource(R.drawable.bluyes);
                    probeConnectionText.setText("Connection : "+ probeDevice.getName());
                    connectionImage.setBackgroundResource(R.drawable.conyes);
                    progressBar.setVisibility(View.GONE);
                    try{
                        unregisterReceiver(probe_receiver);
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                    executorService=Executors.newCachedThreadPool();
                    Thread t=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                if (probeDevice.getName().startsWith("M-")) {
                                    if(mobitProbe!=null){
                                        mobitProbe.close();
                                        mobitProbe=null;
                                    }
                                    initialMobit();
                                    mobit.connect(probeDevice);
                                    mobitProbe=mobit.getMobitProbe();



                                } else if (probeDevice.getName().contains("NOVEN")) {
                                    if(mobitProbe!=null){
                                        mobitProbe.close();
                                        mobitProbe=null;
                                    }
                                    initialNoven();
                                    noven.connect(probeDevice);

                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    executorService.submit(t);


                }
                else{
                    IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                    intentFilter.addAction(BluetoothDevice.EXTRA_BOND_STATE);
                    intentFilter.addAction(String.valueOf(BluetoothDevice.BOND_BONDING));
                    intentFilter.addAction(String.valueOf(BluetoothDevice.BOND_NONE));
                    registerReceiver(control_probe_connection,intentFilter);
                }



            }


        });

        printerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                printerDevice=avaibleDevices.get(i);
                printerDevice.createBond();


                if(printerDevice.getBondState()==BluetoothDevice.BOND_BONDED ){
                    printerButton.setBackgroundResource(R.drawable.printeryes);
                    printerConnectionText.setText(printerDevice.getName());
                    progressBar.setVisibility(View.GONE);



                    executorService=Executors.newCachedThreadPool();
                    Thread t=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            printerBluetoothManager=new PrinterBluetoothManager(printerDevice);
                        }
                    });
                    executorService.submit(t);
                    executorService.shutdown();
                    try{
                        unregisterReceiver(printer_receiver);
                    }catch (Exception e){
                        e.printStackTrace();
                    }



                }
                else{
                    IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                    intentFilter.addAction(BluetoothDevice.EXTRA_BOND_STATE);
                    intentFilter.addAction(String.valueOf(BluetoothDevice.BOND_BONDING));
                    intentFilter.addAction(String.valueOf(BluetoothDevice.BOND_NONE));
                    registerReceiver(control_printer_connection,intentFilter);
                }
            }
        });


        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(printerBluetoothManager!=null){
                    printerBluetoothManager.print(allresults);
                }

            }
        });


        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allresults+="";
                probeListView.setVisibility(View.GONE);

                printerListView.setVisibility(View.GONE);
                dataListView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                //obistlist.add("deneme");
                data_adapter.notifyDataSetChanged();
                textView.setText("");
                if(mobit != null && mobitProbe!=null){

                           readDataFromMobit();

                }
                else if(noven!=null){


                            noven.readData();


                    allresults+=textView.getText();
                }


            }
        });












    }















    private final BroadcastReceiver control_printer_connection=new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("control broadcasti çalışıyor printer...");

            try{
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
            finally {
                if(printerDevice.getBondState()==BluetoothDevice.BOND_BONDED && bluetoothAdapter.getState()==BluetoothAdapter.STATE_ON && devicesNames.contains(printerDevice.getName())){
                    printerConnectionText.setText(printerDevice.getName());
                    printerButton.setBackgroundResource(R.drawable.printeryes);
                    progressBar.setVisibility(View.GONE);


                    Thread t=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            printerBluetoothManager=new PrinterBluetoothManager(printerDevice);
                        }
                    });
                    executorService=Executors.newCachedThreadPool();
                    executorService.submit(t);
                    executorService.shutdown();


                    unregisterReceiver(printer_receiver);
                    unregisterReceiver(control_printer_connection);


                }
                else{
                    unregisterReceiver(control_printer_connection);
                    progressBar.setVisibility(View.GONE);
                    printerConnectionText.setText("No Connection");
                    printerButton.setBackgroundResource(R.drawable.printerno);

                }
            }

        }
    };



    private final BroadcastReceiver control_probe_connection=new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("control broadcasti çalışıyor probe...");
            System.out.println("broadcastbound state :"+probeDevice.getBondState());
            try{
                Thread.sleep(1000);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {

                if(probeDevice.getBondState()==BluetoothDevice.BOND_BONDED && bluetoothAdapter.getState()==BluetoothAdapter.STATE_ON && devicesNames.contains(probeDevice.getName())){

                    probeConnectionText.setText("Connection: "+probeDevice.getName());
                    probeButton.setBackgroundResource(R.drawable.bluyes);
                    progressBar.setVisibility(View.GONE);
                    connectionImage.setImageResource(R.drawable.conyes);


                    executorService=Executors.newCachedThreadPool();
                    Thread t=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                if (probeDevice.getName().startsWith("M-")) {

                                    initialMobit();
                                    mobit.connect(probeDevice);


                                } else if (probeDevice.getName().contains("NOVEN")) {
                                    initialNoven();
                                    noven.connect(probeDevice);

                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });

                    executorService.submit(t);
                    executorService.shutdown();

                    try{
                        unregisterReceiver(probe_receiver);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    unregisterReceiver(control_probe_connection);

                }
                else{

                    progressBar.setVisibility(View.GONE);
                    probeConnectionText.setText("No Connection");
                    connectionImage.setImageResource(R.drawable.conno);
                    probeButton.setBackgroundResource(R.drawable.bluno);
                    unregisterReceiver(control_probe_connection);

                }
            }


        }
    };


    private final BroadcastReceiver probe_receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            System.out.println("probe broadcast çalışıyor");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                if(!devicesNames.contains(deviceName)&&deviceName!=null&&device.getBluetoothClass().toString().equals("1f00")){
                    System.out.println(deviceName + "  "+device.getBluetoothClass());
                    devicesNames.add(device.getName());
                    avaibleDevices.add(device);
                    probe_adapter.notifyDataSetChanged();

                }
                else{
                    System.out.println("Hata zıkkımı");
                }

            }
        }
    };


    private final BroadcastReceiver printer_receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            System.out.println("printer broadcast çalışıyor");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                if(!devicesNames.contains(deviceName)&&deviceName!=null&&device.getBluetoothClass().toString().equals("40680")){
                    System.out.println(deviceName + "  "+device.getBluetoothClass());
                    devicesNames.add(device.getName());
                    avaibleDevices.add(device);
                    printer_adapter.notifyDataSetChanged();

                }
                else{
                    System.out.println("Hata zıkkımı");
                }

            }
        }
    };

    public void initialNoven() throws InstantiationException, IllegalAccessException {
        noven = (Noven) ProbeFactory.createProbe(Noven.class);
        noven.setResources(getResources());
        noven.setTxtResultLog(textView);

    }

    public void initialMobit() throws InstantiationException, IllegalAccessException {
        mobit = (Mobit) ProbeFactory.createProbe(Mobit.class);
    }
    @Override
    protected void onDestroy() {
        unregisterReceiver(control_printer_connection);
        unregisterReceiver(control_probe_connection);
        unregisterReceiver(printer_receiver);
        unregisterReceiver(probe_receiver);
        super.onDestroy();
    }





    public void readDataFromMobit(){
        Separator separator ;
        obistlist.clear();


        mobit.readData();
        textView.setVisibility(View.VISIBLE);
        obistlist= mobit.getObisList();
        if (obistlist != null) {
            ArrayList<String> result_list=new ArrayList<>();
            for(int i = 0;i<obistlist.size();i++){
                String result=obistlist.get(i);
                separator = new Separator(result);
                String sonuc = separator.separate();
                result_list.add(sonuc);
                System.out.println(result);
                textView.append(sonuc+"\n");
                allresults+=sonuc;
                allresults+="\n";


            }
        }



    }

    @Override
    public void onConnectionReset() {

    }

    @Override
    public void onPowerStatusChanged() {

    }

    @Override
    public void onTriggerButtonPressed(int buttonId) {

    }

    @Override
    public void onButtonPressed() {

    }

    @Override
    public void onStateChange(int oldState, int newState) {

    }

    @Override
    public void onProcessLogMessage(String msg) {

    }

    @Override
    public void onDLMSIdentityRead(DLMSMeterIdentityReadResult identityReadResult) {

    }

    @Override
    public void onDLMSRegisterDataRead(DLMSMeterRegisterReadResult registerReadResult) {
        final String val = registerReadResult.toString() + "\r\n";
        final int res = registerReadResult.getReadResult();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (res == DLMSMeterRegisterReadResult.EN_SUCCESSFULL)
                {
                    textView.append(val);
                }
            }

        });
    }

    @Override
    public void onDLMSLoadProfileDataRead(DLMSMeterLoadProfileReadResult loadProfileReadResult) {

    }

    @Override
    public void onDLMSOperationCompleted(DLMSOperationResult operationResult) {

    }

    @Override
    public void onDeviceDiscoveryEvent(DeviceDiscoveryEvent deviceDiscoveryEvent) {

    }
}