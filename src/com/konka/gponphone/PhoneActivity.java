package com.konka.gponphone;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PhoneActivity extends Activity implements OnClickListener {

	Button btn_0, btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7, btn_8,
			btn_9, btn_11, btn_10;
	Button btn_del, btn_call, btn_connect_call, btn_disconnect_call;
	LinearLayout layout_call, layout_number;
	EditText input_phone;
	TextView tv_phone_state;
	byte[] data = new byte[160 * 20];
	Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				tv_phone_state.setText("Incoming call: "+(String) msg.obj);
				btn_connect_call.setVisibility(View.VISIBLE);
				layout_call.setVisibility(View.VISIBLE);
				layout_number.setVisibility(View.INVISIBLE);
				break;
			case 1:
				modeNum = 0;
				IsThreadDisable = false;
				isRecording = false;
				tv_phone_state.setText("");
				if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
					mMediaPlayer.stop();
					mMediaPlayer=null;
				}
				layout_call.setVisibility(View.GONE);
				layout_number.setVisibility(View.VISIBLE);
				break;
			case 2:
				layout_call.setVisibility(View.VISIBLE);
				layout_number.setVisibility(View.INVISIBLE);
				btn_connect_call.setVisibility(View.GONE);
				break;
			case -1:
				String text = input_phone.getText().toString();
				tv_phone_state.setText("Outgoing call: "+text);
				break;
			case -2:
				Log.e("EE", "-------------------");
//				int buffersize = AudioRecord.getMinBufferSize(8000,
//						channelInConfig, audioEncoding);
//				audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
//						8000, channelInConfig, audioEncoding, data.length);
				audioRecord.startRecording();
				handler.sendEmptyMessageDelayed(-2, 31000);
				break;
			case 10:
				if (audioRecord != null) {
					audioRecord.stop();
					Log.e("pan", "audioRecord   stop");
					handler.removeMessages(-2);
				}
				int error = Integer.parseInt((String) msg.obj);
				String title = "";
				if (error == 1) {
					title = "SIP no regsiter,can't make outgoing call";
				} else if (error == 2) {
					title = "FXS aleady usage SIP,can't make outgoing call";
				} else if (error == 3) {
					title = "dial error number,sip server response error";
				} else if (error == 4) {
					title = "SIP server return error..";
				} else if (error == 5) {
					title = "other error,will add the error code";
				} else {
					title = "unknown";
				}
				tv_phone_state.setText(title);
				IsThreadDisable = false;
				isRecording = false;
				handler.sendEmptyMessageDelayed(1, 5000);
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_phone);
		tv_phone_state = (TextView) findViewById(R.id.tv_phone_state);
		btn_0 = (Button) findViewById(R.id.btn_0);
		btn_1 = (Button) findViewById(R.id.btn_1);
		btn_2 = (Button) findViewById(R.id.btn_2);
		btn_3 = (Button) findViewById(R.id.btn_3);
		btn_4 = (Button) findViewById(R.id.btn_4);
		btn_5 = (Button) findViewById(R.id.btn_5);
		btn_6 = (Button) findViewById(R.id.btn_6);
		btn_7 = (Button) findViewById(R.id.btn_7);
		btn_8 = (Button) findViewById(R.id.btn_8);
		btn_9 = (Button) findViewById(R.id.btn_9);
		btn_10 = (Button) findViewById(R.id.btn_10);
		btn_11 = (Button) findViewById(R.id.btn_11);
		btn_call = (Button) findViewById(R.id.btn_call);
		btn_del = (Button) findViewById(R.id.btn_del);

		input_phone = (EditText) findViewById(R.id.input_phone);
		btn_connect_call = (Button) findViewById(R.id.btn_connect_call);
		btn_disconnect_call = (Button) findViewById(R.id.btn_disconnect_call);
		layout_call = (LinearLayout) findViewById(R.id.layout_call);
		layout_number = (LinearLayout) findViewById(R.id.layout_number);

		layout_call.setVisibility(View.GONE);
		layout_number.setVisibility(View.VISIBLE);
		btn_0.setOnClickListener(this);
		btn_1.setOnClickListener(this);
		btn_2.setOnClickListener(this);
		btn_3.setOnClickListener(this);
		btn_4.setOnClickListener(this);
		btn_5.setOnClickListener(this);
		btn_6.setOnClickListener(this);
		btn_7.setOnClickListener(this);
		btn_8.setOnClickListener(this);
		btn_9.setOnClickListener(this);
		btn_10.setOnClickListener(this);
		btn_11.setOnClickListener(this);
		btn_call.setOnClickListener(this);
		btn_del.setOnClickListener(this);
		btn_connect_call.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
					   mMediaPlayer.stop();
					   Log.e("error", "stop   music (connect)");
					   mMediaPlayer=null;
					}
				new Thread() {
					@Override
					public void run() {
						modeNum = 3;
						byte[] data = new byte[] { (byte) 0xEC, 0x6f, 0x59,
								(byte) 0x8a, 0x02, 0x02, 0, 0, 0, 0, 0, 0 };
						try {
							if (tcpos != null)
								tcpos.write(data);
						} catch (IOException e) {
							e.printStackTrace();
						}
						handler.sendEmptyMessage(2);
					}
				}.start();
			}
		});
		btn_disconnect_call.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
				   mMediaPlayer.stop();
				   mMediaPlayer=null;
				   Log.e("error", "stop   music (disconnect)");
				 }
				new Thread() {
					@Override
					public void run() {
						byte[] data = new byte[] { (byte) 0xEC, 0x6f, 0x59,
								(byte) 0x8a, 0x02, 0x01, 0, 0, 0, 0, 0, 0 };
						try {
							if (tcpos != null)
								tcpos.write(data);
						} catch (IOException e) {
							e.printStackTrace();
						}
						handler.sendEmptyMessage(1);
					}
				}.start();
			}
		});
		btn_call.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread() {
					@Override
					public void run() {
						modeNum = 1;
						datazhuce = new byte[] { (byte) 0xEC, 0x6f, 0x59,
								(byte) 0x8a, 0x01, 0x08, 0, 0, 0, 0, 0, 0 };
						try {
							tcpos.write(datazhuce);
						} catch (IOException e) {
							e.printStackTrace();
						}
						Log.i(TAG, "send  TYPE_SIGNAL_KEEP_ALIVE   0108");
						handler.sendEmptyMessage(2);
					};
				}.start();
			}
		});
		// initAudioTrack();
		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		lock = manager.createMulticastLock("UDPwifi");
		btn_call.requestFocus();
		new Thread(phone_keeplive).start();
		// new Thread(AudioPlayThread).start();
		// new Thread(AudioRecordThread).start();
//		playmusic();
	}
	MediaPlayer mMediaPlayer=null;
    public void playmusic(){
    	try {
	    		mMediaPlayer=MediaPlayer.create(this, R.raw.music);
	        	mMediaPlayer.setLooping(true);
				mMediaPlayer.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			Log.e("error", e.getMessage());
		} 
    }
	@Override
	public void onClick(View v) {
		String text = input_phone.getText().toString();
		switch (v.getId()) {
		case R.id.btn_0:
			text = text + 0;
			break;
		case R.id.btn_1:
			text = text + 1;
			break;
		case R.id.btn_2:
			text = text + 2;
			break;
		case R.id.btn_3:
			text = text + 3;
			break;
		case R.id.btn_4:
			text = text + 4;
			break;
		case R.id.btn_5:
			text = text + 5;
			break;
		case R.id.btn_6:
			text = text + 6;
			break;
		case R.id.btn_7:
			text = text + 7;
			break;
		case R.id.btn_8:
			text = text + 8;
			break;
		case R.id.btn_9:
			text = text + 9;
			break;
		case R.id.btn_10:
			text = text + "*";
			break;
		case R.id.btn_11:
			text = text + "#";
			break;
		case R.id.btn_del:
			if (text.length() > 0) {
				text = text.substring(0, text.length() - 1);
			}
			// File file = new File(RECORDFILE);
			// startPlay(file);
			break;
		// case R.id.btn_call:
		//
		// break;
		}
		input_phone.setText(text);
	}

	boolean iscallstate = true;

	public byte[] byteadd(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}

	// int �? byte[]
	public static byte[] intToBytes2(int n) {
		byte[] b = new byte[4];

		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (n >> (24 - i * 8));

		}
		byte[] num = new byte[2];
		num[0] = b[2];
		num[1] = b[3];
		return num;
	}

	// byte转换为int
	public static int byteToInt2(byte[] b) {

		int mask = 0xff;
		int temp = 0;
		int n = 0;
		for (int i = 0; i < b.length; i++) {
			n <<= 8;
			temp = b[i] & mask;
			n |= temp;
		}
		return n;
	}

	boolean isplay = false;
	private String TAG = "TcpClient";
	private String serverIP = "192.168.55.1";
	private int serverPort = 5070;
	private PrintWriter pw;
	private InputStream is;
	private DataInputStream dis;
	private boolean isRun = true;
	private Socket tcpsocket = null;
	byte buff[] = new byte[172];
	private String rcvMsg;
	private int rcvLen;
	Context context;
	OutputStream tcpos = null;
	private byte[] datazhuce = new byte[] { (byte) 0xEC, 0x6f, 0x59,
			(byte) 0x8a, 0x02, 0x50, 0, 0, 0, 0, 0, 0 };
	int modeNum = 0; // 0 ,普�?�的 1,摘机 2
	int waitnum = 0;
	Thread phone_keeplive = new Thread() {
		@Override
		public void run() {
			try {
				while (tcpsocket == null) {
					tcpsocket = new Socket(serverIP, serverPort);
					SystemClock.sleep(1000);
				}
				if (tcpsocket != null) {
					tcpsocket.setSoTimeout(Integer.MAX_VALUE);
					Log.i(TAG,
							"--------------------------------------------------");
					pw = new PrintWriter(tcpsocket.getOutputStream(), true);
					tcpos = tcpsocket.getOutputStream();
					is = tcpsocket.getInputStream();
					dis = new DataInputStream(is);
					// TYPE_BT_PHONE_REGISTER_TO_ONT
					tcpos.write(datazhuce);
					Log.i(TAG, "TYPE_BT_PHONE_REGISTER_TO_ONT :"
							+ bytes2HexString(datazhuce));
					while (isRun) {
						rcvLen = dis.read(buff);
						// Log.i(TAG, "dis.readBoolean() :"
						// +dis.readBoolean()+"      "+rcvLen);
						String datas = bytes2HexString(buff);
						String data = datas.substring(0, 64);
						String magic = data.substring(0, 8);
						String msgstate = "";
						String msglength = "";
						String msgvalue = "";
						int numlength = 0;
						if (!magic.equals("EC6F598A")) {
							magic = "EC6F598A";
							msgstate = data.substring(6, 10);
							msglength = data.substring(10, 14);
							numlength = Integer.valueOf(msglength, 16);
							msgvalue = data.substring(22, 22 + numlength * 2);
						} else {
							msgstate = data.substring(8, 12);
							msglength = data.substring(12, 16);
							numlength = Integer.valueOf(msglength, 16);
							msgvalue = data.substring(24, 24 + numlength * 2);
						}
						String value3x = "";
						for (int i = 0; i < msgvalue.length(); i++) {
							if (i % 2 == 1) {
								value3x = value3x
										+ msgvalue.substring(i, i + 1);
							}
						}

						// Log.i(TAG, datas+"      "+"data :" + data + " " +
						// magic + "  "+
						// msgstate+"  "+msglength+"  "+numlength+" "+msgvalue+" "+value3x);
						if (msgstate.endsWith("0152")) {
							Log.i(TAG, "TYPE_BT_REG_SUCCES");
							datazhuce = new byte[] { (byte) 0xEC, 0x6f, 0x59,
									(byte) 0x8a, 0x02, 0x53, 0, 0, 0, 0, 0, 0 };
							tcpos.write(datazhuce);
							Log.i(TAG, "send  TYPE_QUERY_STB_ALIVE   0253");
						} else if (msgstate.endsWith("0153")) {
							Log.i(TAG, "TYPE_BT_REG_Fail");
							datazhuce = new byte[] { (byte) 0xEC, 0x6f, 0x59,
									(byte) 0x8a, 0x02, 0x50, 0, 0, 0, 0, 0, 0 };
							tcpos.write(datazhuce);
						} else if (msgstate.endsWith("0108")) {

						} else if (msgstate.endsWith("0154")) {
							Log.i(TAG, "TYPE_ONT_ACK");
							if (modeNum == 0) {
								// datazhuce = new byte[] { (byte) 0xEC, 0x6f,
								// 0x59, (byte) 0x8a, 0x01, 0x08, 0, 0, 0,
								// 0, 0, 0 };
								// tcpos.write(datazhuce);
								// Log.i(TAG,"send  TYPE_SIGNAL_KEEP_ALIVE   0108");
								// SystemClock.sleep(1000);
								// modeNum = 5;
								// continue;
							}
							if (modeNum == 1) {
								datazhuce = new byte[] { (byte) 0xEC, 0x6f,
										0x59, (byte) 0x8a, 0x02, 0x02, 0, 0, 0,
										0, 0, 0 };
								tcpos.write(datazhuce);
								Log.i(TAG,"send  TYPE_SIGNAL_KEEP_ALIVE   0202");
								modeNum = 2;
								continue;
							}
							if (modeNum == 2) {// 拨打电话
								String text = input_phone.getText().toString();
								handler.sendEmptyMessage(-1); //显示拨打电话
								int length = text.length();
								if (length > 20 || length == 0) {
									Toast.makeText(PhoneActivity.this,
											"plz input phone number", 2000)
											.show();
									handler.sendEmptyMessage(1);
									modeNum = 0;
								} else {
									String hex = Integer.toHexString(length);
									byte[] phonenumlength = intToBytes2(length);
									byte[] head = new byte[] { (byte) 0xEC,
											0x6f, 0x59, (byte) 0x8a, 0x02, 0x05 };
									// byte[] callout=new byte[]{(byte)
									// 0xEC,0x6f,0x59,(byte)
									// 0x8a,0x02,0x05,0x00,0x03,0,0,0,0,0x31,0x30,0x30};
									// //100
									byte[] head_length = byteadd(head,
											phonenumlength);
									byte[] type = new byte[] { 0, 0, 0, 0 };
									byte[] head_length_type = byteadd(
											head_length, type);
									byte[] phonenum = new byte[length];
									for (int i = 0; i < length; i++) {
										int num = Integer.parseInt(text
												.substring(i, i + 1));
										phonenum[i] = (byte) (num + '0');
									}
									byte[] callout = byteadd(head_length_type,
											phonenum);
									Log.i(TAG, "+++++++++++++++++++++"
											+ bytes2HexString(callout));
									tcpos.write(callout);
									Log.i(TAG,
											"send  TYPE_EVENT_OUTCALL   0205");
									modeNum = 3;
									continue;
								}
							}
							if (modeNum == 3) {
								IsThreadDisable = true;
								isRecording = true;
								new Thread(AudioPlayThread).start();
								new Thread(AudioRecordThread).start();
								// new Thread(AudioRecordFileThread).start();

								Log.i(TAG,
										"set  TYPE_EVENT_OUTCALL 0205->0102 ----------------------------- tonghua ");
								modeNum = 0;
							}
						} else if (msgstate.endsWith("0252")) {
							Log.i(TAG, "TYPE_STB_ACK");
						} else if (msgstate.endsWith("0202")) {
							Log.i(TAG, "TYPE_EVENT_HOOK_OFF");
						} else if (msgstate.endsWith("0201")) {
							// 挂机
							Log.i(TAG, "TYPE_EVENT_HOOK_ON");
						} else if (msgstate.endsWith("0205")) {
							Log.i(TAG, "TYPE_EVENT_OUTCALL");
						} else if (msgstate.endsWith("0102")) {
							Log.i(TAG, "TYPE_SINAL_CALL_PROGESS"); // 响铃
							Log.i(TAG,
									"---------------------------------------------------------响铃");
						} else if (msgstate.endsWith("0101")) {
							Log.i(TAG, "TYPE_SINAL_CALL_INCOMING");
							Log.i(TAG,
									"---------------------------------------------------------来电话了"
											+ value3x);
							Message msg = handler.obtainMessage();
							msg.what = 0;
							msg.obj = value3x;
							handler.sendMessage(msg);
							playmusic();
						} else if (msgstate.endsWith("0302")) {
							Log.i(TAG, "TYPE_AUDIO_ONT_TO_STB");
						} else if (msgstate.endsWith("0104")) {
							Log.i(TAG, "0104 -------------");
							handler.sendEmptyMessage(1);
						} else if (msgstate.endsWith("0109")) {
							Log.i(TAG, "0109 ------------- " + value3x);
							Message msg = handler.obtainMessage();
							msg.what = 10;
							msg.obj = value3x;
							handler.sendMessage(msg);
							// handler.sendEmptyMessage(1);
						} else if (msgstate.endsWith("0106")) {
							Log.i(TAG, "0106 -------------");
							handler.sendEmptyMessage(1);
						} else if (msgstate.endsWith("0103")) {
							Log.i(TAG, "0103 -------------");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.i(TAG, "Exception: " + e.getMessage());
				handler.sendEmptyMessage(-1);
			}

			try {
				if (pw != null && is != null && dis != null) {
					pw.close();
					is.close();
					dis.close();
					tcpsocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	};

	// AudioPlayer mAudioPlayer;
	AudioTrack audioTrk;
	Handler mHandler;

	public static byte[] hexToByteArray(String inHex) {
		int hexlen = inHex.length();
		byte[] result;
		if (hexlen % 2 == 1) {
			hexlen++;
			result = new byte[(hexlen / 2)];
			inHex = "0" + inHex;
		} else {
			result = new byte[(hexlen / 2)];
		}
		int j = 0;
		for (int i = 0; i < hexlen; i += 2) {
			result[j] = hexToByte(inHex.substring(i, i + 2));
			j++;
		}
		return result;
	}

	private static byte hexToByte(String inHex) {
		return (byte) Integer.parseInt(inHex, 16);
	}

	public static String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	DatagramSocket udpSocket = null;
	int udpport = 5071;
	boolean IsThreadDisable = true;
	private static WifiManager.MulticastLock lock;
	int stopnum = 0;
	boolean isRecording = true;
	private AudioRecord audioRecord;
	private String RECORDFILE = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/recordfile.pcm";
	private String RECORDFILE2 = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/recordfile2.pcm";
	Thread AudioRecordFileThread = new Thread() {
		@Override
		public void run() {
			try {
				DatagramPacket pack = null;
				InetAddress serverAddress = null;
				try {
					if (udpSocket == null) {
						udpSocket = new DatagramSocket(udpport);
					}
					serverAddress = InetAddress.getByName(serverIP);
				} catch (SocketException e1) {
					e1.printStackTrace();
					Log.e("lipan", "end  1 " + e1.getMessage());
				} catch (UnknownHostException e) {
					e.printStackTrace();
					Log.e("lipan", "end   2  " + e.getMessage());
				}
				// File cfile = new File(RECORDFILE2);
				File cfile = new File(FILEPATH);

				Log.e("lipan", "1");
				int musicLength = 160;
				byte[] music = new byte[musicLength];
				Log.e("lipan", "2");
				InputStream is = new FileInputStream(cfile);
				Log.e("lipan", "3");
				BufferedInputStream bis = new BufferedInputStream(is);
				Log.e("lipan", "4");
				DataInputStream dis = new DataInputStream(bis);
				Log.e("lipan", "5");

				while (-1 != dis.read(music)) {
					Log.e("lipan", "6");
					// audioTrack.write(music, 0, musicLength);

					byte[] head = new byte[] { (byte) 0xEC, 0x6f, 0x59,
							(byte) 0x8a, 0x03, 0x01, 0, (byte) 0xa0 };
					msgsequence++;
					byte[] msgsquencetype = hexToByteArray(numToHex16(msgsequence));
					byte[] msghead = byteadd(head, msgsquencetype);
					byte[] msg = byteadd(msghead, music);
					pack = new DatagramPacket(msg, msg.length, serverAddress,
							5071);
					udpSocket.send(pack);
					SystemClock.sleep(10);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("lipan", "end   " + e.getMessage());
			}
		}
	};
	int filenum = 0;
	Thread AudioRecordThread = new Thread() {
		@Override
		public void run() {
			Log.e("pan", "thread start ^^^^^^^");
			DatagramPacket pack = null;
			InetAddress serverAddress = null;
			try {
				if (udpSocket == null) {
					udpSocket = new DatagramSocket(udpport);
				}
				serverAddress = InetAddress.getByName(serverIP);
			} catch (SocketException e1) {
				e1.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			Log.i(TAG," start udp server");
			try {
				Log.e("pan", "start udp server");
				// byte[] head = new byte[] { (byte) 0xEC, 0x6f, 0x59,
				// (byte) 0x8a, 0x03, 0x01, 0, (byte) 0xa0 };
				if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
					Log.e("error", "stop music 2");
					mMediaPlayer.stop();
					mMediaPlayer=null;
				}
				int buffersize = AudioRecord.getMinBufferSize(8000,
				channelInConfig, audioEncoding);
				audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
				8000, channelInConfig, audioEncoding, data.length);
				audioRecord.startRecording();
				Log.e("pan", "audiorecord blue state:"+audioRecord.getRecordingState()+"   "+audioRecord.getState());
//				handler.sendEmptyMessage(-2);
				filenum = 0;
				final String sdpath = getCacheDir().getAbsolutePath();
				while (isRecording) {
//					Log.e("pan", "start write file "+filenum);
					msgsequence = 1;
					File file = new File(sdpath + "/voioc_" + filenum + ".pcm");
					if (file.exists()) {
						file.delete();
					}
					file.createNewFile();
					OutputStream cos = new FileOutputStream(file);
					// if (msgsequence == 1) {
					byte[] tou = new byte[] { (byte) 0xfa };
					byte[] onedata = new byte[data.length - 1];
					int bufferReadResult = audioRecord.read(onedata, 0,
							onedata.length);
					data = byteadd(tou, onedata);
					cos.write(data);
					cos.flush();
					cos.close();
//					Log.e("pan", "end  write file "+filenum+" ,start send udp");
					InputStream is = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(is);
					DataInputStream dis = new DataInputStream(bis);
					int musicLength = 160;
					byte[] music = new byte[musicLength];
					while (-1 != dis.read(music)) {
						byte[] head = new byte[] { (byte) 0xEC, 0x6f, 0x59,
								(byte) 0x8a, 0x03, 0x01, 0, (byte) 0xa0 };
						byte[] msgsquencetype = hexToByteArray(numToHex16(msgsequence));
						byte[] msghead = byteadd(head, msgsquencetype);
						byte[] msg = byteadd(msghead, music);
						pack = new DatagramPacket(msg, msg.length,
								serverAddress, 5071);
						udpSocket.send(pack);
						msgsequence++;
						SystemClock.sleep(9);
					}
					filenum++;

					dis.close();
					bis.close();
				}
				// os.close();
				// cos.close();
				audioRecord.stop();
				udpSocket.close();
				udpSocket=null;
				SystemClock.sleep(1000);
				deletefile(sdpath);
				Log.e("pan", "end udp  service ");
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("pan", "end   " + e.getMessage());
			}
		};
	};
	public void deletefile(String path){
		File file=new File(path);
		String[] templist=file.list();
		File temp=null;
		for (int i = 0; i < templist.length; i++) {
//			Log.e("pan", "delete file name:"+path+"/"+templist[i] );
			  temp=new File(path+"/"+templist[i]);
			  if (temp.isFile()) {
		          temp.delete();
		      }
		}
	}
	private final String FILEPATH = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/voice_record_rtk.pcm";

	public void open(int musicLength) {
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
				channelOutConfig, AudioFormat.ENCODING_PCM_16BIT,
				musicLength * 2, AudioTrack.MODE_STREAM);
	}

	int msgsequence = 1;
	byte[] olddata = null;
	byte[] servicedata = null;

	private String numToHex16(int b) {
		return String.format("%08x", b);
	}

	private int frequence = 8000;// 采样�? 8000
	private int channelInConfig = AudioFormat.CHANNEL_IN_MONO;// 定义采样通道
	private int channelOutConfig = AudioFormat.CHANNEL_OUT_MONO;// 定义采样通道
	// private int channelInConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;// 定义音频编码�?16位）
	private int bufferSize = -1;// 播放缓冲大小
	Thread AudioPlayThread = new Thread() {
		@Override
		public void run() {
			bufferSize = AudioTrack.getMinBufferSize(frequence,
					channelOutConfig, audioEncoding);
			// 实例AudioTrack
			audioTrk = new AudioTrack(AudioManager.STREAM_MUSIC, frequence,
					channelOutConfig, audioEncoding, bufferSize,
					AudioTrack.MODE_STREAM);
			if (udpSocket == null) {
				try {
					udpSocket = new DatagramSocket(udpport);
				} catch (SocketException e) {
					e.printStackTrace();
				}
			}
			if (udpSocket != null) {
				byte data[] = new byte[172];
				try {
					udpSocket.setBroadcast(true);
					DatagramPacket datagramPacket;// = new
													// DatagramPacket(data,data.length);
					audioTrk.play();
					Log.i(TAG, "get udp player");
					while (IsThreadDisable) {
						lock.acquire();
						// datagramSocket.receive(datagramPacket);
						// String strMsg=new
						// String(datagramPacket.getData()).trim();
						// String datas =
						// bytes2HexString(datagramPacket.getData());

						// String magic = datas.substring(0, 8);
						// String msgstate = datas.substring(8, 12);
						// String msglength = datas.substring(12, 16);
						// String msgsn = datas.substring(16, 24);

						// Logcat.d(datas+"\n"+magic+"  "+msgstate+" "+msglength+" "+msgsn);

						// audioTrack.write(datagramPacket.getData(),12,
						// datagramPacket.getLength());

						// byte[] data = getPCMData();

						// byte[]
						// datam=subByte(datagramPacket.getData(),12,datagramPacket.getLength()-12);
						// OutputStream p=new FileOutputStream(file);
						// p.write(datam, 0,datam.length);
						// p.close();

						// if (mSocket == null)
						// return;
						// 从文件流读数�?
						
						datagramPacket = new DatagramPacket(data, data.length);
						udpSocket.receive(datagramPacket);
						byte[] datam = subByte(datagramPacket.getData(), 11,
								datagramPacket.getLength() - 12);
						audioTrk.write(datam, 0, datam.length);
						// byte[] datam=datagramPacket.getData();
						// audioTrack.write(datam,11, datam.length-12);
						Log.i(TAG, " -----------------------");
						// audioTrk.stop();

						// Logcat.d("================="+bytes2HexString(datam));
						// mAudioPlayer.setDataSource(datam);
						// byte[]
						// datam=subByte(datagramPacket.getData(),12,datagramPacket.getLength()-12);
						// if(olddata==null){
						// olddata=datam;
						// }else{
						// olddata=byteadd(olddata,datam);
						// }
						// if(servicedata==null){
						// servicedata=datagramPacket.getData();
						// }else{
						// servicedata=byteadd(servicedata,datagramPacket.getData());
						// }

						// 音频源就�?
						// mAudioPlayer.prepare();
						// mAudioPlayer.play();
						// Logcat.e("@@@@@@@@@@@@@@@@@@@@@@@"+datagramPacket.getAddress().getHostAddress().toString()
						// + ":" +strMsg );
						lock.release();
						// SystemClock.sleep(1000);
						stopnum++;
					}
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
					Logcat.d("------------" + e.getMessage());
				}

			}
		};
	};
	MediaPlayer mediaplayer;

	public void startPlay(File mFile) {
		mediaplayer = new MediaPlayer();
		try {
			mediaplayer.setDataSource(mFile.getAbsolutePath());
			mediaplayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {

				}
			});
			mediaplayer.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					return false;
				}
			});
			mediaplayer.setVolume(1, 1);
			mediaplayer.setLooping(false);
			mediaplayer.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void playfile(byte[] fileSoundByteArray) {
		try {
			// create temp file that will hold byte array
			File tempMp3 = File
					.createTempFile("yuanshi", ".mp3", getCacheDir());
			Logcat.e("s::::::::::" + tempMp3.getAbsolutePath());
			tempMp3.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(tempMp3);
			fos.write(fileSoundByteArray);
			fos.close();
			// // Tried reusing instance of media player
			// // but that resulted in system crashes...
			// MediaPlayer mediaPlayer = new MediaPlayer();
			// // Tried passing path directly, but kept getting
			// // "Prepare failed.: status=0x1"
			// // so using file descriptor instead
			// FileInputStream fis = new FileInputStream(tempMp3);
			// mediaPlayer.setDataSource(fis.getFD());
			// mediaPlayer.prepare();
			// mediaPlayer.start();
		} catch (Exception ex) {
			String s = ex.toString();
			Logcat.e("s::::::::::" + s);
			ex.printStackTrace();
		}
	}

	private void playMp3(byte[] mp3SoundByteArray) {
		try {
			// create temp file that will hold byte array
			File tempMp3 = File.createTempFile("gequba", ".mp3", getCacheDir());
			Logcat.e("s::::::::::" + tempMp3.getAbsolutePath());
			tempMp3.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(tempMp3);
			fos.write(mp3SoundByteArray);
			fos.close();
			// // Tried reusing instance of media player
			// // but that resulted in system crashes...
			// MediaPlayer mediaPlayer = new MediaPlayer();
			// // Tried passing path directly, but kept getting
			// // "Prepare failed.: status=0x1"
			// // so using file descriptor instead
			// FileInputStream fis = new FileInputStream(tempMp3);
			// mediaPlayer.setDataSource(fis.getFD());
			// mediaPlayer.prepare();
			// mediaPlayer.start();
		} catch (Exception ex) {
			String s = ex.toString();
			Logcat.e("s::::::::::" + s);
			ex.printStackTrace();
		}
	}

	public byte[] subByte(byte[] b, int off, int length) {

		byte[] b1 = new byte[length];

		System.arraycopy(b, off, b1, 0, length);

		return b1;

	}

	// public void initAudioTrack() {
	// // 配置播放�?
	// // 扬声器播�?
	// int streamType = AudioManager.STREAM_MUSIC;
	// // 播放的采样频�? 和录制的采样频率�?�?
	// int sampleRate = 8000;
	//
	// // 和录制的�?样的
	// int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	//
	// // 流模�?
	// int mode = AudioTrack.MODE_STREAM;
	//
	// // 录音用输入单声道 播放用输出单声道
	// int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
	//
	// int minBufferSize = AudioTrack.getMinBufferSize(sampleRate,
	// channelConfig, audioFormat);
	//
	// audioTrack = new AudioTrack(streamType, sampleRate, channelConfig,
	// audioFormat, Math.max(minBufferSize, 512), mode);
	// // audioTrack.play();
	//
	// // mAudioPlayer = new AudioPlayer(mHandler);
	//
	// // / // 获取音频参数
	// // AudioParam audioParam = getAudioParam();
	// // mAudioPlayer.setAudioParam(audioParam);
	// }

	public AudioParam getAudioParam() {
		AudioParam audioParam = new AudioParam();
		audioParam.mFrequency = 8000;
		audioParam.mChannel = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
		audioParam.mSampBit = AudioFormat.ENCODING_PCM_16BIT;

		return audioParam;
	}

	AudioTrack audioTrack;
    @Override
    protected void onPause() {
    	super.onPause();
    	if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
			mMediaPlayer.stop();
			mMediaPlayer=null;
		}
    }
	@Override
	protected void onStop() {
		super.onStop();
		new Thread() {
			@Override
			public void run() {
				byte[] datazhuce = new byte[] { (byte) 0xEC, 0x6f, 0x59,
						(byte) 0x8a, 0x02, 0x01, 0, 0, 0, 0, 0, 0 };
				try {
					if (tcpos != null)
						tcpos.write(datazhuce);
				} catch (Exception e) {
					e.printStackTrace();
					Log.i(TAG, "send  挂机   0201      " + e.getMessage());
				}
			}
		}.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			isRun = false;
			IsThreadDisable = false;
			if (tcpsocket != null) {
				tcpsocket.close();
			}
			if (udpSocket != null) {
				udpSocket.disconnect();
				udpSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.e("keycode", "onkeyup :"+keyCode);
		if (keyCode == 231) {
			return false;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.e("keycode", "onkeydown :"+keyCode);
		if (keyCode == 231) {
			return false;
		}
		int p = KeyEvent.KEYCODE_ENTER;
		return super.onKeyDown(keyCode, event);
	}

}
