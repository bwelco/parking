package com.example.administrator.parking;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by Administrator on 2015/10/28.
 */
public class BlueToothSocket implements Runnable {

    static private BluetoothSocket cwjSocket = null;
    static private BluetoothDevice cwjDevice = null;

    static BufferedReader in = null;
    static PrintWriter out = null;

    public BlueToothSocket(BluetoothDevice device) {

        BluetoothSocket tmp = null;
        cwjDevice = device;

        UUID uuid = UUID.fromString(MainActivity.SPP_UUID); // SPP协议
        try {
            tmp = device.createRfcommSocketToServiceRecord(uuid); // 客户端创建
        } catch (IOException e) {
        }
        cwjSocket = tmp;
    }
    @Override
    public void run() {

    }
}
