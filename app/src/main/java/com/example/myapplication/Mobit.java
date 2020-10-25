package com.example.myapplication;

import android.bluetooth.BluetoothDevice;
import com.example.mobit.mobitprobelibrary.MobitBtProbe;
import com.example.mobit.mobitprobelibrary.MobitProbe;
import com.example.mobit.mobitprobelibrary.MobitProbeEventListener;

import java.util.ArrayList;

public class Mobit implements Probe, MobitProbeEventListener {

    MobitProbe mobitProbe;
    ArrayList<String> obisList;

    public MobitProbe getMobitProbe() {
        return mobitProbe;
    }



    public ArrayList<String> getObisList() {
        return obisList;
    }

    @Override
    public void connect(BluetoothDevice device) {
        MobitProbe probe;
        probe = new MobitBtProbe(this);
        try {
            probe.connect(device);
            mobitProbe = probe;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readData() {
        obisList=new ArrayList<>();
        try {
            mobitProbe.setEnabled(true);

            try {
                mobitProbe.setBaudRate(0);
                IEC62056 iec = new IEC62056(mobitProbe);
                iec.resetIrInputStream();
                iec.writeRequest(null);

                IEC62056.Identification ident = iec.readIdentification();

                if (ident.mnfId.equals("ELM"))
                    Thread.sleep(200);
                iec.writeOptionSelect(0, ident.baudId, 0);

                if (ident.mnfId.equals("KHL") || ident.mnfId.equals("AEL")) {
                    //txtResultLog.append();
                    obisList.add(iec.queryDataBlock('2', "1.8.0()"));
                    obisList.add(iec.queryDataBlock('2', "1.8.1()"));
                    obisList.add(iec.queryDataBlock('2', "1.8.2()"));
                    obisList.add(iec.queryDataBlock('2', "1.8.3()"));

                } else {
                    mobitProbe.setBaudRate(ident.baudId);
                    //Eğer textView da göstermek istersek
                    /*ArrayList<String> list = iec.readDataMessageList();
                    for(int i = 0; i<list.size();i++){
                        txtResultLog.append(list.get(i));
                    }*/
                    obisList.addAll(iec.readDataMessageList());
                }

            } catch (Exception e) {
                e.printStackTrace();
            } /*finally
            {
                if (obisList != null) {
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dataListAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, obisList);
                            list_data.setAdapter(dataListAdapter);
                            dataListAdapter.notifyDataSetChanged();
                        }
                    });
                    thread.start();
                    mobitProbe.setEnabled(false);
                }

            }*/

        } catch(Exception e) {
            e.printStackTrace();
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
}
