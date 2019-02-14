package com.konka.gponphone;

import android.util.Log;

public class Logcat {
	public static final String TAG = "GPON";
	public static void e(String str){
		Log.e(TAG, str);
	}
	public static void d(String str){
		Log.d(TAG, str);
	}
	public static void v(String str){
		Log.v(TAG, str);
	}
}
