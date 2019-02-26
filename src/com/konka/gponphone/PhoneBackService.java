package com.konka.gponphone;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

public class PhoneBackService extends Service {

    /**
     * 获取安卓设备当前的IP地址（有线或无线）
     *
     * @return
     */
    private String getClientIP() {
 
        try {
            // 获取本地设备的所有网络接口
            Enumeration<NetworkInterface> enumerationNi = NetworkInterface.getNetworkInterfaces();
            while (enumerationNi.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNi.nextElement();
                String interfaceName = networkInterface.getDisplayName();
                Log.i("lipan", "网络名字" + interfaceName);
 
                // 如果是有限网卡
                if (interfaceName.equals("eth0")) {
                    Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses();
 
                    while (enumIpAddr.hasMoreElements()) {
                        // 返回枚举集合中的下一个IP地址信息
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        // 不是回环地址，并且是ipv4的地址
                        //if (!inetAddress.isLoopbackAddress()&& inetAddress instanceof Inet4Address) {
                        if (!inetAddress.isLoopbackAddress()) {
                            Log.i("tag", inetAddress.getHostAddress() + "   ");
 
                            return inetAddress.getHostAddress();
                        }
                    }
                    //  如果是无限网卡
                } else if (interfaceName.equals("wlan0")) {
                    Enumeration<InetAddress> enumIpAddr = networkInterface
                            .getInetAddresses();
 
                    while (enumIpAddr.hasMoreElements()) {
                        // 返回枚举集合中的下一个IP地址信息
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        // 不是回环地址，并且是ipv4的地址   //&& inetAddress instanceof Inet4Address
                        if (!inetAddress.isLoopbackAddress() ) {
                            Log.i("tag", inetAddress.getHostAddress() + "   ");
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
 
    }

	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
	}
	Handler mHandler=new Handler(){
		public void handleMessage(Message msg) {
			
		};
	};
	public void initView(){
		mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		final WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        // 类型
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        // 设置flag
        int flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        // | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
        params.flags = flags;
        // 不设置这个弹出框的透明遮罩显示为黑色
        params.format = PixelFormat.TRANSLUCENT;
        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
        // 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
        // 不设置这个flag的话，home页的划屏会有问题

        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER_HORIZONTAL;
        
        
       // mWindowManager.addView(arg0, params);
	}
	Socket tcpsocket;
	WindowManager mWindowManager;
	private String serverIP = "192.168.55.1";
	private int serverPort = 5070;
	private PrintWriter pw=null;
	private OutputStream tcpos=null;
	private InputStream tcpis=null;
	private DataInputStream tcpdatais=null;
	private byte[] datatype = new byte[] { (byte) 0xEC, 0x6f, 0x59,
			(byte) 0x8a, 0x02, 0x50, 0, 0, 0, 0, 0, 0 };
	byte buff[] = new byte[172];
	
	Thread phoneliveThread=new Thread(){
		public void run() {
			while (tcpsocket == null) {
				try {
					tcpsocket = new Socket(serverIP, serverPort);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				SystemClock.sleep(1000);
			}
			if (tcpsocket != null) {
				try {
					tcpsocket.setSoTimeout(Integer.MAX_VALUE);
					tcpos = tcpsocket.getOutputStream();
					tcpis = tcpsocket.getInputStream();
					tcpdatais = new DataInputStream(tcpis);
					tcpos.write(datatype);
					while(true){
						int state=tcpdatais.read(buff);
						String datas = ToolDataChange.bytes2HexString(buff);
						String data = datas.substring(0, 64);
						String magic = data.substring(0, 8);
						String msgstate = "";
						String msglength = "";
						String msgvalue = "";
						int numlength = 0;
						if(!magic.equals("EC6F598A")){
							magic = "EC6F598A";
							msgstate = data.substring(6, 10);
							msglength = data.substring(10, 14);
							numlength = Integer.valueOf(msglength, 16);
							msgvalue = data.substring(22, 22 + numlength * 2);
						}else{
							msgstate = data.substring(8, 12);
							msglength = data.substring(12, 16);
							numlength = Integer.valueOf(msglength, 16);
							msgvalue = data.substring(24, 24 + numlength * 2);
						}
						String value3x = "";
						for (int i = 0; i < msgvalue.length(); i++) {
							if (i % 2 == 1) {
								value3x = value3x+ msgvalue.substring(i, i + 1);
							}
						}
						if (msgstate.endsWith("0152")) {
							datatype = new byte[] { (byte) 0xEC, 0x6f, 0x59,
									(byte) 0x8a, 0x02, 0x53, 0, 0, 0, 0, 0, 0 };
							tcpos.write(datatype);
						} else if (msgstate.endsWith("0153")) {
							datatype = new byte[] { (byte) 0xEC, 0x6f, 0x59,
									(byte) 0x8a, 0x02, 0x50, 0, 0, 0, 0, 0, 0 };
							tcpos.write(datatype);
						} else if (msgstate.endsWith("0108")) {

						} else if (msgstate.endsWith("0154")) {
							
						}
					}
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	};
	
}
