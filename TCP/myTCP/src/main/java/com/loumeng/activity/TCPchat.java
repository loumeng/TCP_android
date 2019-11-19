package com.loumeng.activity;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.loumeng.Bluetooth.R;
import com.loumeng.TCP.Data_syn;
import com.loumeng.TCP.TCP_client;
import com.loumeng.TCP.TCP_service;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * Created by Administrator on 2017/1/12.
 */
public class TCPchat extends Activity {
    private static final String TAG_1 = "TCPChat";

    //控件按钮
    private Button start;
    private  Button stop;
    private  Button clear_;
    private  Button send;
    //复选按钮控件
    private CheckBox  hex_show;
    private  CheckBox auto_huang;
    private  CheckBox hex_send;
    private  CheckBox auto_send;
    //文本显示控件
    private TextView ip_mode;
    private TextView port_mode;
    private TextView de_state;                        //设置状态
    private TextView ip_show;                        //连接的对象IP 显示
    private TextView name_show;                      //连接的对象主机名号 显示
    private TextView re_count;                       //接收字节数
    private TextView se_count;                       //发送字节数
    private TextView re_data_show;                   //接收字节显示
    //编辑框控件
    private EditText edit_ip;
    private EditText edit_port;
    private EditText edit_time;
    private EditText edit_data;
    //下拉控件
    private Spinner link_mode;       //连接模式
    //
    private boolean exit;
    //网络连接模式选择
    public final static int MODE_TCP_SERVER=0;
    public final static int MODE_TCP_CLIENT=1;
    public final static int MODE_UDP=2;
    private int ch_mode=0;
    //TCP服务器通信模式下
    private TCP_service tcp_service =null;
    private int ser_port;
    private boolean ser_islink=false;
    public final static int  SERVER_STATE_CORRECT_READ=3;
    public final static int  SERVER_STATE_CORRECT_WRITE=4;               //正常通信信息
    public final static int  SERVER_STATE_ERROR=5;                 //发生错误异常信息
    public final  static int  SERVER_STATE_IOFO=6;                  //发送SOCKET信息
   // TCP客户端通信模式下
    private TCP_client tcp_client =null;
    private final static int  CLIENT_STATE_CORRECT_READ=7;
    public final static int  CLIENT_STATE_CORRECT_WRITE=8;               //正常通信信息
    public final static int  CLIENT_STATE_ERROR=9;                 //发生错误异常信息
    public final static int  CLIENT_STATE_IOFO=10;                  //发送SOCKET信息
    private boolean client_islink =false;

    //复选状态信息
    private boolean  Hex_show =false;
    private boolean  Auto_huang =false;
    private boolean  Hex_send =false;
    private boolean  Auto_send =false;
    //计数用
    private int  countin =0;

    private  int countout=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ly_tcpchat);
        SlidingMenuinit();                    //侧滑菜单初始化
        init();
        link_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ch_mode = position;
                if (ch_mode == MODE_TCP_SERVER) {
                    ip_mode.setText("本地 I P");
                    port_mode.setText("本地端口");
                    start.setText("启动");
                    de_state.setText("");
                    ip_show.setHint("对象IP");
                    name_show.setHint("对象主机名");
                    clear();
                }
                if (ch_mode == MODE_TCP_CLIENT) {
                    ip_mode.setText("目的 I P");
                    port_mode.setText("目的端口");
                    start.setText("连接");
                   de_state.setText("");
                    ip_show.setHint("对象IP");
                    name_show.setHint("对象主机名");
                    clear();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        edit_ip.setText(getLocalIpAddress());   //获取本地IP地址显示
        edit_port.setText(8080+"");             //设置默认端口号
        start.setOnClickListener(startlistener);
        stop.setOnClickListener(stoplistener);
        send.setOnClickListener(sendlistener);
        clear_.setOnClickListener(clearlistener);

        hex_send.setOnCheckedChangeListener(listener);
        hex_show.setOnCheckedChangeListener(listener);
        auto_huang.setOnCheckedChangeListener(listener);
        auto_send.setOnCheckedChangeListener(listener);
}
    //初始化控件函数
    private void init() {
        link_mode= (Spinner) findViewById(R.id.ch_mode);
        ip_mode= (TextView) findViewById(R.id.ip_mode);
        port_mode= (TextView) findViewById(R.id.port_mode);

        start= (Button) findViewById(R.id.start);
        stop= (Button) findViewById(R.id.stop);
        clear_= (Button) findViewById(R.id.de_clear);
        send= (Button) findViewById(R.id.de_send);

        de_state= (TextView) findViewById(R.id.de_action);
        ip_show= (TextView) findViewById(R.id.de_ip);
        name_show= (TextView) findViewById(R.id.de_sport);
        re_count= (TextView) findViewById(R.id.receive_count);
        se_count= (TextView) findViewById(R.id.send_count);
        re_data_show= (TextView) findViewById(R.id.receive);
        re_data_show.setMovementMethod(ScrollingMovementMethod
                .getInstance());// 使TextView接收区可以滚动

        edit_ip= (EditText) findViewById(R.id.ip_edit);
        edit_port= (EditText) findViewById(R.id.port_edit);
        edit_time= (EditText) findViewById(R.id.edi_auto);
        edit_data= (EditText) findViewById(R.id.send_data);

        hex_show= (CheckBox) findViewById(R.id.hex_show);
        auto_huang= (CheckBox) findViewById(R.id.autohuang);
        hex_send= (CheckBox) findViewById(R.id.hex_send);
        auto_send= (CheckBox) findViewById(R.id.auto_send);

    }
  private OnCheckedChangeListener listener =new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      switch (buttonView.getId()){
          case R.id.hex_show:
                  if (isChecked) {
                      Toast.makeText(TCPchat.this, "16进制显示",
                              Toast.LENGTH_SHORT).show();
                      Hex_show = true;
                  } else
                      Hex_show = false;

          break;
          case R.id.autohuang:
              if (isChecked) {
                  Toast.makeText(TCPchat.this, "自动换行",
                          Toast.LENGTH_SHORT).show();
                  Auto_huang = true;
              } else
                  Auto_huang = false;
              break;
          case R.id.hex_send:
              if (isChecked) {
                  Toast.makeText(TCPchat.this, "16进制发送",
                          Toast.LENGTH_SHORT).show();
                  Hex_send = true;
              } else
                  Hex_send = false;

              break;
          case R.id.auto_send:
              if (isChecked) {
                  Toast.makeText(TCPchat.this, "16进制发送",
                          Toast.LENGTH_SHORT).show();
                  Auto_send = true;
              } else
                  Auto_send = false;

              break;
      }
      }
  };

    private View.OnClickListener startlistener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ch_mode==MODE_TCP_SERVER){
                if(tcp_service == null){
                    ser_port=Integer.valueOf(edit_port.getText().toString());    //获取设置的端口号 默认8080
                tcp_service =new TCP_service(ser_handler,ser_port);
                tcp_service.start();
                de_state.setText("TCP服务器模式  启动");
                    stop.setEnabled(true);
                    edit_ip.setEnabled(false);
                    edit_port.setEnabled(false);
                }
                else{
                    Log.e(TAG_1, "断开连接监听 释放资源");
                    de_state.setText("TCP服务器模式  出错");
                }

            }
            if(ch_mode==MODE_TCP_CLIENT){
               if(tcp_client == null) {
                  tcp_client =new TCP_client(cli_handler);
                   try {
                       InetAddress ipAddress = InetAddress.getByName
                               (edit_ip.getText().toString());
                       int port =Integer.valueOf(edit_port.getText().toString());//获取端口号
                       tcp_client.setInetAddress(ipAddress);
                       tcp_client.setPort(port);

                   } catch (UnknownHostException e) {
                       e.printStackTrace();
                   }
                   edit_ip.setEnabled(false);
                   edit_port.setEnabled(false);
                   tcp_client.start();
               }
                  stop.setEnabled(true);
            }
           }
    };

    private View.OnClickListener clearlistener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            clear();
        }
    };

    private View.OnClickListener stoplistener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ch_mode==MODE_TCP_SERVER){
                tcp_service.setis_start(false);
                if(tcp_service!=null)
                {
                tcp_service.close();
                tcp_service=null;
                }
                de_state.setText("TCP服务器模式  关闭");
                Ip_clear();
                edit_ip.setEnabled(true);
                edit_port.setEnabled(true);
        }
            if(ch_mode == MODE_TCP_CLIENT){
                if(tcp_client != null){
                    tcp_client.close();
                    tcp_client=null;
                }
                Ip_clear();
                edit_ip.setEnabled(true);
                edit_port.setEnabled(true);
                stop.setEnabled(false);

            }

        }

    };
    //发送响应函数
    private View.OnClickListener sendlistener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ch_mode==MODE_TCP_SERVER){
                if(ser_islink==true){
                  String message=  edit_data.getText().toString().replaceAll(" ","");
                    if(message.equals("")){
                        Toast.makeText(TCPchat.this,"发送内容不能为空",
                                Toast.LENGTH_SHORT).show();
                    }
                 sendmessage(message);
            }else{
                    Toast.makeText(TCPchat.this,"连接未建立",
                            Toast.LENGTH_SHORT).show();
                }
        }
            if (ch_mode==MODE_TCP_CLIENT){
                if(client_islink==true){
                    String message=  edit_data.getText().toString().replaceAll(" ","");
                    if(message.equals("")){
                        Toast.makeText(TCPchat.this,"发送内容不能为空",
                                Toast.LENGTH_SHORT).show();
                    }
                    sendmessage(message);
                }else{
                    Toast.makeText(TCPchat.this,"连接未建立",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(tcp_service!=null)
        {
            tcp_service.setis_start(false);
            tcp_service.close();
            tcp_service=null;
        }
        if(tcp_client != null){
            tcp_client.close();
            tcp_client=null;
        }
    }

    private void SlidingMenuinit(){
        SlidingMenu menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        // 设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.35f);
        //把滑动菜单添加进所有的Activity中，可选值SLIDING_CONTENT ， SLIDING_WINDOW
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        //为侧滑菜单设置布局
        menu.setMenu(R.layout.ly_tcpchat_left);

    }
    private Handler ser_handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==SERVER_STATE_ERROR){
              Toast.makeText(TCPchat.this,"连接异常"
                      ,Toast.LENGTH_SHORT).show();
                de_state.setText("TCP服务器模式 连接异常");
                ip_show.setHint("对象IP");
                name_show.setHint("对象主机名");
                ser_islink=false;
            }
            //发送数据
            if (msg.what==SERVER_STATE_CORRECT_WRITE){
                Handler_send(msg);
            }
            //接收数据
            if(msg.what==SERVER_STATE_CORRECT_READ){
                Handler_receive(msg);
            }
             if(msg.what==SERVER_STATE_IOFO){
                  ser_islink=true;
                 de_state.setText("TCP服务器模式  建立连接");
                 stop.setEnabled(true);
                 String[] strings= (String[]) msg.obj;
                 ip_show.append(strings[0]+"\n");
                 name_show.append(strings[1]+"\n");
            }
        }
    };
//客户端通信模式下
    private  Handler cli_handler =new Handler() {
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch( msg.what){
            case CLIENT_STATE_ERROR :
                Toast.makeText(TCPchat.this,"连接异常"
                        ,Toast.LENGTH_SHORT).show();
                de_state.setText("TCP客户端模式 连接异常");
                ip_show.setHint("对象IP");
                name_show.setHint("对象主机名");
                client_islink=false;
                break;
            case CLIENT_STATE_IOFO :
                client_islink  =true;
                de_state.setText("TCP客户端模式  建立连接");
                String[] strings= (String[]) msg.obj;
                ip_show.append(strings[0]+"\n");
                name_show.append(strings[1]+"\n");
                break;
            //接收数据
            case CLIENT_STATE_CORRECT_READ :
                Handler_receive(msg);
              break;
            //发送数据
            case CLIENT_STATE_CORRECT_WRITE:
                Handler_send(msg);
                break;
        }
    }
};
    @Override
    public void onBackPressed() {
        exit();
    }
    //获取wifi本地IP和主机名
    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // 获取32位整型IP地址
        int ipAddress = wifiInfo.getIpAddress();

        //返回整型地址转换成“*.*.*.*”地址
        return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }
    //发送数据函数
    public void  sendmessage(String message){
   if(Hex_send == true){
       byte[] send = Data_syn.hexStr2Bytes(message);
       if(ch_mode==MODE_TCP_SERVER)
       { tcp_service.write(send);
       }else if (ch_mode==MODE_TCP_CLIENT){
        tcp_client.sendmessage(send);
       }
   }else{
       byte[] send = message.getBytes();
       if(ch_mode==MODE_TCP_SERVER)
       { tcp_service.write(send);
       }else if (ch_mode==MODE_TCP_CLIENT){
           tcp_client.sendmessage(send);
       }
   }
    }
    //页面退出函数
    public void exit(){
        if(exit  ==  true){
           this.finish();
        }
        exit = true;
        Toast.makeText(this,"再按一次，返回上一页",Toast.LENGTH_SHORT).show();
    }
    //定时返回函数
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String message = edit_data.getText().toString();
            sendmessage(message);
        }
    };
    //清除函数
    private void clear() {
        countin=0;
        countout=0;
        re_count.setText("0个");
        se_count.setText("0个");
        re_data_show.setText("");
    }
    // 接收数据处理分析函数，通过handler从子线程回传到主线程
    private  void Handler_receive(Message msg){
        byte[]  buffer= (byte[]) msg.obj;
        if (Hex_show == true) {
            String readMessage = " "
                    + Data_syn.bytesToHexString(buffer, msg.arg1);
            re_data_show.append(readMessage);
            if(Auto_huang==true){
                re_data_show.append("\n");
            }
            countin += readMessage.length() / 2;                               // 接收计数
            re_count.setText("" + countin+"个");
        } else if (Hex_show == false) {
            String readMessage = null;
            try {
                readMessage = new String(buffer, 0, msg.arg1, "GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            re_data_show.append(readMessage);
            if(Auto_huang==true){
                re_data_show.append("\n");
            }
            countin += readMessage.length();                                   // 接收计数
            re_count.setText("" + countin+"个");
        }
    }
      //发送数据处理分析函数，通过handler从子线程回传主线程
    private void Handler_send(Message msg){
        byte[] writeBuf = (byte[]) msg.obj;
        if (Auto_send == true) {
            String s = edit_time.getText().toString();
            long t = Long.parseLong(s);
            ser_handler.postDelayed(runnable, t);
        } else if (Auto_send == false) {
            ser_handler.removeCallbacks(runnable);
        }

        if (Hex_send == true) {
            String writeMessage = Data_syn.Bytes2HexString(writeBuf);
            countout += writeMessage.length() / 2;
            se_count.setText("" + countout+"个");
        } else if (Hex_send == false) {
            String writeMessage = null;
            try {
                writeMessage = new String(writeBuf, "GBK");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            countout += writeMessage.length();
            se_count.setText("" + countout+"个");
        }
    }
    //目的地址和目的主机名清空函数
    private void Ip_clear(){
       ip_show.setText("");
        name_show.setText("");
    }
}

