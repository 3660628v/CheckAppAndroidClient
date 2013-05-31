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
	private final String TITLE	= "ȷ��Ҫ�˳���?";
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
		exit_diag.setPositiveButton("ȷ��", new OnClickListener() {
			public void onClick(DialogInterface dialog,
					int which) {
					// �������������login���ᵼ��login�޷�ͨ������һ��finish�˳�����Ϊ��activityջ�л��и�����ʱ��login
					//Intent loginintent = new Intent(mA, LoginActivity.class);
					//loginintent.putExtra("class", "CheckAppClientExit");
					//mA.startActivity(loginintent);
					
					// �Է�����Ϣ����ʽȡ��ֱ��finish�������쳣
					Message msg = mH.obtainMessage(EVENT_WEBVIEW_EXIT);
					msg.sendToTarget();
					//CheckAppClientExit.this.mA.finish();
					Log.d(TAG,"exit!");
					return;
				}
		});
		exit_diag.setNegativeButton("ȡ��", new OnClickListener()
		{
			public void onClick(DialogInterface dialog,
				int which) {	
			}
		});
		mCheckAppExitDialog = exit_diag.create();
	}
}
