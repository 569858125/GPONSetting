package com.konka.gponphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Log.e("lipan", "BootBroadcastReceiver......");
		if(arg1.getAction().equals("android.intent.action.BOOT_COMPLETED")){
			Intent phoneService=new Intent(arg0,PhoneBackService.class);
            arg0.startService(phoneService);
		}
	}
}
