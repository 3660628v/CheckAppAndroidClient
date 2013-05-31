package com.android.task.main.function;


import com.android.task.main.LoginActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CheckAppClientExit {
	public final static int EVENT_WEBVIEW_EXIT = 0x1000;
	private final String TAG 	= CheckAppClientExit.class.getName();
	private final String TITLE	= "确定要退出吗?";
	private Activity mA;
	private Handler mH;
	private AlertDialog mCheckAppExitDialog;
	
	
	public CheckAppClientExit(Activity a, Handler h)
	{
		this.mA = a;
		this.mH = h;
		init();
	}
	public AlertDialog getCheckAppExitDialog() 
	{
		return mCheckAppExitDialog;
	}
	private void init()
	{
		AlertDialog.Builder exit_diag = new AlertDialog.Builder(this.mA);
		exit_diag.setTitle(TITLE);
		exit_diag.setPositiveButton("确定", new OnClickListener() {
			public void onClick(DialogInterface dialog,
					int which) {
					// 这里如果又启动login，会导致login无法通过调用一次finish退出，因为在activity栈中还有个启动时的login
					//Intent loginintent = new Intent(mA, LoginActivity.class);
					//loginintent.putExtra("class", "CheckAppClientExit");
					//mA.startActivity(loginintent);
					
					// 以发送消息的形式取代直接finish，避免异常
					Message msg = mH.obtainMessage(EVENT_WEBVIEW_EXIT);
					msg.sendToTarget();
					//CheckAppClientExit.this.mA.finish();
					Log.d(TAG,"exit!");
					return;
				}
		});
		exit_diag.setNegativeButton("取消", new OnClickListener()
		{
			public void onClick(DialogInterface dialog,
				int which) {	
			}
		});
		mCheckAppExitDialog = exit_diag.create();
	}
}
