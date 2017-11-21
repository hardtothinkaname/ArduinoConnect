package com.zc.arduinoconnect1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.Ch34xSerialDriver;
import com.hoho.android.usbserial.driver.Cp21xxSerialDriver;
import com.hoho.android.usbserial.driver.FtdiSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.ProlificSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private UsbSerialDriver driver;
    private UsbDeviceConnection connection;
    private UsbSerialPort port;
    private TextView tvShowText;
    private RadioButton rbDTR;
    private RadioButton rbDSR;

    private String mMsg;
    private ProbeTable customTable = new ProbeTable();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvShowText = (TextView) findViewById(R.id.tvShowMsg);
        rbDSR = (RadioButton)findViewById(R.id.rbDSR);
        rbDSR = (RadioButton)findViewById(R.id.rbDSR);
        customTable.addProduct(10755, 67, CdcAcmSerialDriver.class);

    }

    public void onClickStart(View view){
        // Find all available drivers from attached devices.
        UsbManager mUsbManager;
        List<UsbSerialDriver> mAvailableDrivers;
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        String mUsbDeviceNames = "";
        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            usbDevice.getDeviceName();
            mUsbDeviceNames += usbDevice.getDeviceName() + ", " + usbDevice.getVendorId() + ", " + usbDevice.getProductId();

        }

        UsbSerialProber prober = new UsbSerialProber(customTable);
        mAvailableDrivers = prober.findAllDrivers(mUsbManager);
        if (mAvailableDrivers.isEmpty()) {
            Toast.makeText(this,"available drivers is empty!" + mUsbDeviceNames, Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(this,"available drivers is not empty!" + mUsbDeviceNames, Toast.LENGTH_SHORT).show();
        }

        // Open a connection to the first available driver.
        driver = mAvailableDrivers.get(0);
        connection = mUsbManager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }
        Toast.makeText(this,"get driver: " + driver.getDevice().getDeviceName(), Toast.LENGTH_SHORT).show();

        port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            Toast.makeText(this, "falil to open port!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }


    }


    public void onClickClear(View view){
        mMsg = "";
    }


    SerialInputOutputManager serialInputOutputManager;
    public void onClickSend(View view){
        // Read some data! Most have just one port (port 0).
        serialInputOutputManager= new SerialInputOutputManager(port, new SerialInputOutputManager.Listener() {
            @Override
            public void onNewData(byte[] data) {
                mMsg = mMsg + new String(data);
                Toast.makeText(MainActivity.this, "receive,DSR" ,Toast.LENGTH_SHORT).show();
                tvShowText.setText(mMsg);
            }

            @Override
            public void onRunError(Exception e) {

            }
        });
        serialInputOutputManager.run();

//        try {
//            byte buffer[] = new byte[64];
//            int numBytesRead = port.read(buffer, 1000);
//            port.setRTS(true);
//            mMsg = mMsg + new String(buffer);
//            Toast.makeText(this, "receive,DSR" ,Toast.LENGTH_SHORT).show();
//            tvShowText.setText(mMsg);
//        } catch (IOException e) {
//            // Deal with error.
//            Toast.makeText(this, "connection error!", Toast.LENGTH_SHORT).show();
//        }

    }

    public void onClickStop(View view){
        serialInputOutputManager.stop();
//        try {
//            port.close();
////            mIsPortOpened = false;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }


}
