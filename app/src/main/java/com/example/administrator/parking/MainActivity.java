package com.example.administrator.parking;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Set;


public class MainActivity extends ActionBarActivity {

    Button getcar;
    Button stopcar;
    TextView textone;
    Button parkingpoint;
    ProgressDialog dialog;
    public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    static BluetoothDevice mydevice;
    BlueToothSocket mysocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }


        getcar = (Button) findViewById(R.id.getcar);
        stopcar = (Button) findViewById(R.id.stopcar);
        textone = (TextView) findViewById(R.id.textone);
        textone.setBackgroundResource(R.drawable.backmain);
        parkingpoint = (Button) findViewById(R.id.parkingpoint);

        stopcar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, GetCarActicity.class);
                startActivity(intent);
            }
        });

        getcar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, StopCarActivity.class);
                startActivity(intent);
            }
        });


        parkingpoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, NotFound.class);
                startActivity(intent);
            }
        });
        dialog = ProgressDialog.show(MainActivity.this, null, "正在连接中...");
        bloothinit();
    }

    public void bloothinit()
    {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return;
        }
        if(!adapter.isEnabled()){	//蓝牙未开启，则开启蓝牙
            adapter.enable();

            try {
                while(true) {
                    Thread.sleep(1000);
                    if(adapter.isEnabled())
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        BluetoothAdapter adapter2 = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = adapter2.getBondedDevices();
        Iterator<BluetoothDevice> iterator = devices.iterator();

        while (iterator.hasNext()) {

            BluetoothDevice dev = iterator.next();

            if (dev.getName().contains("HC-05")) {
                mydevice = dev;
                break;
            }
        }

        if (mydevice == null) {
            Toast.makeText(getApplicationContext(), "连接错误",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            dialog.cancel();
        }
    }

}
