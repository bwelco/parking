package com.example.administrator.parking;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;


public class StopCarActivity extends ActionBarActivity {

    AutoCompleteTextView email;
    EditText password;
    Button surebutton;
    Button usebutton;
    private ProgressDialog dialog;
    Boolean okbutton = false;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_car);

        settings = getSharedPreferences("account", Activity.MODE_PRIVATE);

        email = (AutoCompleteTextView) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        surebutton = (Button) findViewById(R.id.email_sign_in_button);
        usebutton = (Button) findViewById(R.id.usebutton);

        usebutton.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {
                String acc;
                String pass;
                acc = settings.getString("ACCOUNT", "");
                pass = settings.getString("PASSWORD", "");
                if(acc.equals("") && pass.equals(""))
                    Toast.makeText(getApplicationContext(), "抱歉，没有预置密码。",
                            Toast.LENGTH_SHORT).show();
                else
                {
                    email.setText(acc);
                    password.setText(pass);
                    Toast.makeText(getApplicationContext(), "已经使用缓存面预置密码。",
                            Toast.LENGTH_SHORT).show();
                }
            }
            });

            surebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String InputPassWord = password.getText().toString().trim();
                    final String InputEmail = email.getText().toString().trim();
                    if (InputPassWord.equals("") && InputEmail.equals(""))
                        Toast.makeText(getApplicationContext(), "请输入帐号和密码",
                                Toast.LENGTH_SHORT).show();
                    else if (InputPassWord.         equals(""))
                        Toast.makeText(getApplicationContext(), "请输入密码",
                                Toast.LENGTH_SHORT).show();
                    else if (InputEmail.equals(""))
                        Toast.makeText(getApplicationContext(), "请输入帐号",
                                Toast.LENGTH_SHORT).show();

                    else {
                        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(StopCarActivity.this.getCurrentFocus().getWindowToken()
                                ,InputMethodManager.HIDE_NOT_ALWAYS);

                        new AlertDialog.Builder(StopCarActivity.this).setTitle("确认你的帐号密码")//设置对话框标题
                                .setMessage("帐号 ：" + InputEmail + "\n" + "密码 ：" + InputPassWord)//设置显示的内容
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                        okbutton = true;
                                        setgetcar(okbutton, InputPassWord, InputEmail);
                                    }

                                }).setNegativeButton("返回", new DialogInterface.OnClickListener() {


                            @Override

                            public void onClick(DialogInterface dialog, int which) {
                                okbutton = false;
                            }

                        }).show();
                    }

                }
            });
    }

    public void setgetcar(Boolean okbutton, String InputPassWord, String InputEmail)
    {
        if(okbutton)
        {
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(StopCarActivity.this.getCurrentFocus().getWindowToken()
                    ,InputMethodManager.HIDE_NOT_ALWAYS);
            dialog = ProgressDialog.show(StopCarActivity.this, null, "请稍候，正在取车中");

            Thread t = new Thread(new Connect(MainActivity.mydevice, StopCarActivity.this,
                    InputEmail,InputPassWord));
            t.start();
        }
    }

    class Connect implements Runnable {
        private BluetoothSocket cwjSocket = null;
        private BluetoothDevice cwjDevice = null;
        Message message = new Message();
        private Context mainc;
        private String send_account;
        private String send_password;
        private String readbuf;
        BufferedReader in = null;
        PrintWriter out = null;

        public Connect(BluetoothDevice device,Context mainc, String send_account, String send_password) {

            BluetoothSocket tmp = null;
            cwjDevice = device;
            this.mainc = mainc;
            this.send_account = send_account;
            this.send_password = send_password;
            UUID uuid = UUID.fromString(MainActivity.SPP_UUID); // SPP协议
            try {
                tmp = device.createRfcommSocketToServiceRecord(uuid); // 客户端创建
            } catch (IOException e) {
            }
            cwjSocket = tmp;
        }


        @Override
        public void run() {

            try {
                cwjSocket.connect();
                in = new BufferedReader(new InputStreamReader(
                        cwjSocket.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                        cwjSocket.getOutputStream())), true);

            } catch (IOException ex) {
                ex.printStackTrace();
                message.what = 2;
            }

            out.write("GET_CAR&" + send_account + "&" + send_password + "*");
            out.flush();
            try {

                while(true) {
                    readbuf = in.readLine();
                    if (readbuf.startsWith("CHECKED")) {
                        while(true)
                        {
                            readbuf = in.readLine();
                            if(readbuf.startsWith("GETOK"))
                            {
                                message.what = 3;
                                StopCarActivity.this.handler.sendMessage(message);
                                break;
                            }
                        }
                        break;
                    }
                    if(readbuf.startsWith("NOTCHECKED"))
                    {
                        message.what = 5;
                        StopCarActivity.this.handler.sendMessage(message);
                        break;
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                message.what = 5;
                StopCarActivity.this.handler.sendMessage(message);
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            out.close();
            try {
                cwjSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 3: {
                    dialog.cancel();
                    Toast.makeText(getApplicationContext(), "成功取车!",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case 4: {
                    dialog.cancel();
                    break;
                }
                case 5: {
                    dialog.cancel();
                    Toast.makeText(getApplicationContext(), "账户名或者密码错误!",
                            Toast.LENGTH_SHORT).show();
                    break;
                }

            }
        }

    };


}
