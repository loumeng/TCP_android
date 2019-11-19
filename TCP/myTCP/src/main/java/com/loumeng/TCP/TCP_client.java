package com.loumeng.TCP;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;


/**
 * Created by Administrator on 2017/2/15.
 */
public class TCP_client extends Thread{
    private static final String TAG_1 = "TCPChat";
    private Handler mhandler;
    private Socket socket;
    private boolean isruning;
    public InputStream inputStream;
    public OutputStream outputStream;
    private InetAddress inetAddress;                             //IP地址
    private int port;                                            //端口号
    public static int  CLIENT_STATE_CORRECT_READ=7;
    public static int  CLIENT_STATE_CORRECT_WRITE=8;               //正常通信信息
    public static int  CLIENT_STATE_ERROR=9;                 //发生错误异常信息
    public static int  CLIENT_STATE_IOFO=10;                  //发送SOCKET信息
    public TCP_client(Handler mhandler) {
        this.mhandler=mhandler;
        isruning=true;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }
    public void setPort(int port) {
        this.port = port;
    }
    @Override
    public void run() {
   if(socket == null){
       try {
           Log.e(TAG_1,"启动连接线程");
           socket=new Socket(inetAddress,port);
           new Receive_Thread(socket).start();
           getadress();
       } catch (IOException e) {
           e.printStackTrace();
           senderror();
       }
   }
    }
    public void getadress(){
        String[] strings = new String[2];
        strings[0]=socket.getInetAddress().getHostAddress();
        strings[1]=socket.getInetAddress().getHostName();
        Message message = mhandler.obtainMessage(CLIENT_STATE_IOFO,-1,-1,strings);
        mhandler.sendMessage(message);
    }

  public  void close(){
      if (socket !=null){
          try {
              socket.close();
              socket=null;
              isruning=false;
          } catch (IOException e) {
          }
          }else if (socket ==null){
          Log.e(TAG_1, "未建立连接");
      }
  }
    class Receive_Thread extends Thread{
        private  Socket msocket;
   public Receive_Thread (Socket msocket){
      this.msocket =msocket;
    }
        @Override
        public void run() {
            try {
                while (isruning) {
                    inputStream = msocket.getInputStream();
                    while (inputStream.available()==0){
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final byte[] buffer = new byte[1024];//创建接收缓冲区

                    final int len = inputStream.read(buffer);//数据读出来，并且数据的长度
                    mhandler.sendMessage(mhandler.
                            obtainMessage(CLIENT_STATE_CORRECT_READ,len,-1,buffer));
                }
            }catch (IOException e) {
                    e.printStackTrace();
                   senderror();
                }finally {
                if(msocket!=null){
                    try {
                        msocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                if(inputStream!=null){
                    inputStream.close();
                }
                if (outputStream!=null){
                    outputStream.close();
                }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e(TAG_1,"关闭连接，释放资源");
            }
        }
    }
   public void sendmessage(byte[] message){
       try {
           outputStream =socket.getOutputStream();
           mhandler.sendMessage(mhandler.
                   obtainMessage(CLIENT_STATE_CORRECT_WRITE,-1,-1,message));
           outputStream.write(message);

       } catch (IOException e) {
               senderror();
       }
   }

    void senderror(){
        mhandler.sendMessage(mhandler.obtainMessage(CLIENT_STATE_ERROR));
}
}
