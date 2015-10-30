package com.example.administrator.parking;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import java.net.UnknownHostException;
import java.util.UUID;


public class GetCarActicity extends ActionBarActivity {

    AutoCompleteTextView email;
    EditText password;
    Button surebutton;
    Button clearbutton;
    private ProgressDialog dialog;
    Boolean okbutton = false;

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_get_car_acticity);

        settings = getSharedPreferences("account", Activity.MODE_PRIVATE);

        editor = settings.edit();;

        email = (AutoCompleteTextView) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        surebutton = (Button) findViewById(R.id.email_sign_in_button);
        clearbutton = (Button) findViewById(R.id.clearbutton);

        clearbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.clear();
                editor.commit();
                Toast.makeText(getApplicationContext(), "清除缓存成功",
                        Toast.LENGTH_SHORT).show();
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
                else if (InputPassWord.equals(""))
                    Toast.makeText(getApplicationContext(), "请输入密码",
                            Toast.LENGTH_SHORT).show();
                else if (InputEmail.equals(""))
                    Toast.makeText(getApplicationContext(), "请输入帐号",
                            Toast.LENGTH_SHORT).show();

                else {
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(GetCarActicity.this.getCurrentFocus().getWindowToken()
                            ,InputMethodManager.HIDE_NOT_ALWAYS);

                    new AlertDialog.Builder(GetCarActicity.this).setTitle("确认你的帐号密码")//设置对话框标题
                            .setMessage("帐号 ：" + InputEmail + "\n" + "密码 ：" + InputPassWord)//设置显示的内容
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮

                                @Override
                                public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                    okbutton = true;
                                   setstopcar(okbutton, InputPassWord, InputEmail);
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

    public void setstopcar(Boolean okbutton, String InputPassWord, String InputEmail)
    {
        if(okbutton)
        {
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(GetCarActicity.this.getCurrentFocus().getWindowToken()
                    ,InputMethodManager.HIDE_NOT_ALWAYS);

            dialog = ProgressDialog.show(GetCarActicity.this, null, "请将车停到指定位置，并将车锁上，之后该对话框将消失");

            Thread t = new Thread(new Connect(MainActivity.mydevice, GetCarActicity.this,
                    InputEmail, InputPassWord));
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

              //  GetCarActicity.this.handler.sendMessage(message);

            }

            out.write("STOP_CAR&" + send_account + "&" + send_password + "&1*");
            out.flush();
            try {
                while(true) {
                    String temp = in.readLine();
                    if (temp.startsWith("STOPOK")) {
                        message.what = 3;
                        GetCarActicity.this.handler.sendMessage(message);
                        break;
                    }
                    if(temp.startsWith("FULL"))
                    {
                        message.what = 5;
                        GetCarActicity.this.handler.sendMessage(message);
                        break;
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                message.what = 5;
                GetCarActicity.this.handler.sendMessage(message);
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
                    final String InputPassWord = password.getText().toString().trim();
                    final String InputEmail = email.getText().toString().trim();
                    dialog.cancel();
                    editor.putString("PASSWORD", InputPassWord);
                    editor.putString("ACCOUNT",InputEmail);
                    editor.commit();
                    Toast.makeText(getApplicationContext(), "成功存车!",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case 4: {
                    dialog.cancel();
                    break;
                }
                case 5: {
                    dialog.cancel();
                    new AlertDialog.Builder(GetCarActicity.this).setTitle("抱歉，停车位已满。")//设置对话框标题

                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }

                            }).setNegativeButton("返回", new DialogInterface.OnClickListener() {


                        @Override

                        public void onClick(DialogInterface dialog, int which) {
                            okbutton = false;
                        }

                    }).show();
                    break;
                }
            }
        }

    };

}
