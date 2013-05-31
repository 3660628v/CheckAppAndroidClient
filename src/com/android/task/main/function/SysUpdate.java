package com.android.task.main.function;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.android.task.tools.EquipmentId;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class SysUpdate 
{
	private final String TAG			 				= SysUpdate.class.getName();
	private final String LOCAL_PACKAGE_FILE				= ".checkapp.apk";
	private final String REMOTE_PACKAGE_FILE			= "check.apk";
	private Activity     a					= null;
	private UrlConfigure mUrlConfigure 		= null;
	private EquipmentId	mEquipmentId		= null;
	
	public SysUpdate(Activity a,UrlConfigure u)
	{
		this.mUrlConfigure			= 		u;
		this.a						=       a;
		this.mEquipmentId			=       new EquipmentId(a);
	}
	
	public void update()
	{
		/*
		if (this.downloadNewPackage()){
			this.installPackage();
			Toast.makeText(this.a, "��װ�°汾�ɹ���", Toast.LENGTH_SHORT).show();
			return;
		}
		*/
		downUpdate du = new downUpdate(a, this.getNewPackageUrl());
		du.doUpdate();
	}
	
	private class downUpdate extends GetInfoTask {
		private Context context;
		private String url;
		
		public downUpdate(Context con, String url) {
			context = con;
			this.url = url;
		}
		
		public void doUpdate() {
			this.execute(url, "android", "get");
		}
		
		@Override
		protected void onPostExecGet(Boolean succ) {
			if (succ) {
				if (this.getHttpCode() != 200) {
					Toast.makeText(context, "���������ļ�ʧ��, ����ϵϵͳ����Ա: "+this.getHttpCode(), Toast.LENGTH_SHORT).show();
					return;
				}
				// �����ļ�
				File SDCardRoot = Environment.getExternalStorageDirectory();
		        File file = new File(SDCardRoot,LOCAL_PACKAGE_FILE);
		        try {
					FileOutputStream fileOutput = new FileOutputStream(file);
					fileOutput.write(this.toByte());
					fileOutput.close();
					
					installPackage();
					Toast.makeText(context, "��װ�°汾�ɹ�", Toast.LENGTH_SHORT);
				} catch (FileNotFoundException e) {
					Toast.makeText(context, "�ļ����������ȷ��·����ȷ��"+SDCardRoot+"/"+LOCAL_PACKAGE_FILE, Toast.LENGTH_SHORT);
					//e.printStackTrace();
				} catch (IOException e) {
					//e.printStackTrace();
					Toast.makeText(context, "�ļ�����ʧ�ܣ�����洢�ռ�", Toast.LENGTH_SHORT);
				}
		        
			} else {
				Toast.makeText(context, "���������ļ�ʧ��, ����ϵϵͳ����Ա", Toast.LENGTH_SHORT).show();
			}
		}
		
		private void installPackage()
		{
			Toast.makeText(context, "��װ·����"+Environment.getExternalStorageDirectory()+"//"+LOCAL_PACKAGE_FILE, Toast.LENGTH_SHORT).show();
			File apkFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + LOCAL_PACKAGE_FILE);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
			context.startActivity(intent);
		}
	}
	
	/*
	private boolean downloadNewPackage() 
	{
		String packageUrl				 	= this.getNewPackageUrl();
		HttpURLConnection urlConnection 	= null;
        URL url;
		try {
			url = new URL(packageUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
	        urlConnection.setRequestMethod("GET");
	        urlConnection.setDoOutput(true);
	        urlConnection.connect();
	        File SDCardRoot = Environment.getExternalStorageDirectory();
	        File file = new File(SDCardRoot,LOCAL_PACKAGE_FILE);
	        FileOutputStream fileOutput = new FileOutputStream(file);
	        InputStream inputStream = urlConnection.getInputStream();

	        int totalSize = urlConnection.getContentLength();
	        Log.d(TAG, "Total size" + totalSize);
	        int downloadedSize = 0;

	        byte[] buffer = new byte[1024*10];
	        int bufferLength = 0; //used to store a temporary size of the buffer
	        while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
    	        Log.d(TAG, "Download size" + downloadedSize);
    	        
    			Toast.makeText(this.a, "��������У����Ժ�...", Toast.LENGTH_SHORT).show();

//    			Toast.makeText(this.a, String.valueOf(downloadedSize/(float)totalSize * 100) + "%", Toast.LENGTH_SHORT).show();
	        }
        fileOutput.close();

		} catch (MalformedURLException e) {
			Log.e(TAG,"package update error ! "+ packageUrl );
			Toast.makeText(this.a, "���µ�ַ����", Toast.LENGTH_LONG).show();
			return false;
		}  catch (IOException e) {
			Log.e(TAG,"net work error!");
			Toast.makeText(this.a, "���ӷ�����ʧ�ܣ�", Toast.LENGTH_LONG).show();
			return false;
		} catch ( Exception e ) {
			Toast.makeText(this.a, "download error: "+e.toString(), Toast.LENGTH_SHORT).show();
			return false;
		}
		Log.d(TAG,"DownLoad Success!");
		return true;
	}
	*/
	
	private String getNewPackageUrl()
	{
		return "http://"+mUrlConfigure.getHost() 			+ "/" +
				this.mEquipmentId.getAndroidVersion() 		+ "/" +
				REMOTE_PACKAGE_FILE;
	}
}
