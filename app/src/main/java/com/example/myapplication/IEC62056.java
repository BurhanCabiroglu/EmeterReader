package com.example.myapplication;

import com.example.mobit.mobitprobelibrary.MobitProbe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class IEC62056 {
    public class Identification {
        public String devId;
        public String mnfId;
        public int baudId;
    }

    private MobitProbe mMobitProbe;
    private static final int SOH = 0x01;
    private static final int STX = 0x02;
    private static final int ETX = 0x03;
    private static final int EOT = 0x04;
    private static final int ACK = 0x06;
    private static final int NAK = 0x15;
    private static final int IR_DATA_TIMEOUT = 3000;

    public IEC62056(MobitProbe mobitProbe) {
        mMobitProbe = mobitProbe;
    }

    public void writeRequest(String devAddr) throws Exception {
        OutputStream outputStream = mMobitProbe.getOutputStream();
        outputStream.write(String.format("/?%s!\r\n", (devAddr == null) ? "" : devAddr).getBytes());
        outputStream.flush();
    }

    public Identification readIdentification() throws Exception {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        int chr1 = readIrInputStream(dataStream, new int[]{'\r', '\n'});
        int chr2 = readIrInputStream();
        if ((chr1 != '\r' || chr2 != '\n') && (chr1 != '\n' || chr2 != '\r'))
            throw new IOException("Invalid data");

        if (dataStream.size() < (1 + 3 + 1))
            throw new IOException("Invalid data");

        byte[] data = dataStream.toByteArray();
        if (data[0] != '/')
            throw new IOException("Invalid data");

        Identification ident = new Identification();
        ident.devId = new String(data, 5, data.length - 5);
        ident.mnfId = new String(data, 1, 3);
        ident.baudId = data[4] - '0';
        return (ident);
    }

    public void writeOptionSelect(int protocol, int baudId, int mode) throws Exception {
        OutputStream outputStream = mMobitProbe.getOutputStream();
        outputStream.write(new byte[]{(byte) ACK, (byte) ('0' + protocol), (byte) ('0' + baudId),
                (byte) ('0' + mode), (byte) '\r', (byte) '\n'});
        outputStream.flush();
    }

    public void readAcknowledgement() throws Exception {
        if (readIrInputStream() != ACK)
            throw new Exception("ACK not found");
    }

    public void writeProgrammingCommand(int cmi, int cti, byte[] dataSet) throws Exception {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        dataStream.write((byte) SOH);
        dataStream.write((byte) cmi);
        dataStream.write((byte) cti);
        dataStream.write((byte) STX);
        dataStream.write(dataSet, 0, dataSet.length);
        dataStream.write((byte) ETX);
        dataStream.write(calculateBcc((byte) 0, dataStream.toByteArray(),
                1, dataStream.size() - 1));

        OutputStream outputStream = mMobitProbe.getOutputStream();
        outputStream.write(dataStream.toByteArray());
        outputStream.flush();
    }

    public void writeBreak() throws Exception {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        dataStream.write((byte) SOH);
        dataStream.write((byte) 'B');
        dataStream.write((byte) '0');
        dataStream.write((byte) ETX);
        dataStream.write(calculateBcc((byte) 0, dataStream.toByteArray(),
                1, dataStream.size() - 1));

        OutputStream outputStream = mMobitProbe.getOutputStream();
        outputStream.write(dataStream.toByteArray());
        outputStream.flush();
    }

    public byte[] readDataMessage() throws Exception {
        if (readIrInputStream() != STX)
            throw new IOException("STX not found");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        readIrInputStream(baos, new int[]{ETX});

        @SuppressWarnings("unused")
        int bcc = readIrInputStream();
        /* You shoud check the BCC here */

        return (baos.toByteArray());
    }

    public String queryDataBlock(int cti, String address) throws Exception {
        return (new String(queryDataBlock(cti, address.getBytes())));
    }

    public byte[] queryDataBlock(int cti, byte[] address) throws Exception {
        writeProgrammingCommand('R', cti, address);
        return (readDataMessage());
    }

    public ArrayList<String> readDataMessageList() throws Exception {
        if (readIrInputStream() != STX)
            throw new IOException("STX not found");

        ArrayList<String> dataList = new ArrayList<String>();
        while (true) {
            ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            int chr1 = readIrInputStream(dataStream, new int[]{'\r', '\n', ETX});
            if (chr1 == ETX)
                break;

            int chr2 = readIrInputStream();
            if ((chr1 != '\r' || chr2 != '\n') && (chr1 != '\n' || chr2 != '\r'))
                throw new IOException("Invalid data");

            dataList.add(new String(dataStream.toByteArray()));
        }

        @SuppressWarnings("unused")
        int bcc = readIrInputStream();
        /* You shoud check the BCC here */

        return (dataList);
    }

    public void resetIrInputStream() throws Exception {
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 500) {
            if (mMobitProbe.getInputStream().available() != 0) {
                mMobitProbe.getInputStream().read();
                startTime = System.currentTimeMillis();
            }
        }
    }

    public int readIrInputStream() throws Exception {
        long startTime = System.currentTimeMillis();
        while (mMobitProbe.getInputStream().available() == 0) {
            if ((System.currentTimeMillis() - startTime) > IR_DATA_TIMEOUT)
                throw new TimeoutException();
        }
        return (mMobitProbe.getInputStream().read());
    }

    public int readIrInputStream(ByteArrayOutputStream dataStream, int[] stopChars) throws Exception {
        while (true) {
            int value = readIrInputStream();
            for (int stopChar : stopChars) {
                if (value == stopChar)
                    return ((byte) value);
            }
            dataStream.write(value);
        }
    }

    private byte calculateBcc(byte bcc, byte data) {
        return (bcc ^= data);
    }

    private byte calculateBcc(byte bcc, byte[] data, int start, int length) {
        for (int index = 0; index < length; index++)
            bcc = calculateBcc(bcc, data[start + index]);
        return (bcc);
    }
}

