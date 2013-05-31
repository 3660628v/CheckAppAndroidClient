package com.android.task.main;

import com.android.task.R;
import com.android.task.main.function.CheckAppClientExit;
import com.android.task.main.function.LocTimerTask;
import com.android.task.main.function.LocationService;
import com.android.task.main.function.MainPage;
import com.android.task.main.function.UrlConfigure;
import com.android.task.picture.PhotoCapturer;
import com.android.task.tools.CustomExceptionHandler;
import com.android.task.tools.InsertFileToMediaStore;
import com.android.task.tools.ScaleBitmap;
import com.android.task.tools.UploadMessage;
import com.android.task.tools.EquipmentId;
import com.android.task.web.MyWebChromeClient;
import com.android.task.web.MyWebClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

/* Change Log */
/* 0.11 
 * 	   Bug Fix:
 * 	   1. 读取bitmap超过虚拟机内存限制的bug。
 * 	   2. 支持用后退键，取消上传数据或者网页加载
 * 0.12
 * 	   Feature:
 * 	   1. web不记录密码
 */
public class WebMainActivity extends Activity {
	
	public  static final String         VERSION		  = "0.12";
	public  ProgressDialog ProgressDialog = null;

	private final boolean	   APP_DEBUG    = false;
	private final String TAG = WebMainActivity.class.getName();
	
	private WebView mWebView;
	private UrlConfigure mUrlConf;
	private CheckAppClientExit mExit;
	private MainPage		   mMpage;
	//private SysSetting		   mSysSetting;
	private EquipmentId		   mEquipmentId;
	private String			   mSessKey;
	private int				   mLoginType;
	
	// 获取地理信息
	private Handler mLocHandler;
	private LocTimerTask mLocTask;
	private Intent mServiceIntent;
	
    @SuppressLint("SetJavaScriptEnabled")
	@SuppressWarnings("static-access")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
        super.onCreate(savedInstanceState);
        
        mSessKey = this.getIntent().getExtras().getString("remember_token");
        mLoginType = this.getIntent().getExtras().getInt("type");
        
        this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
        
        this.getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        this.setProgressBarVisibility(true);
        ProgressDialog = new ProgressDialog(this);
    	
        ProgressDialog.setCancelable(true);
        ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        ProgressDialog.setTitle("数据传输中...");
        mEquipmentId = new EquipmentId(this);

        setContentView(R.layout.web_main);
        
        mWebView = (WebView) findViewById(R.id.main_webview);
        
        mWebView.setFocusable(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUserAgentString(mEquipmentId.getId());
        mWebView.getSettings().setSavePassword(false);
        
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.setWebChromeClient(new MyWebChromeClient(this));
        mWebView.setWebViewClient(new MyWebClient(this, mSessKey));
        
        
        mUrlConf 		= new UrlConfigure(this,this.mWebView);
        //mSysSetting		= new SysSetting(this,this.mWebView);
        
        // 同步cookie
		CookieSyncManager.createInstance(mWebView.getContext());
		
	    //CookieSyncManager.createInstance(this);
		CookieManager mCM = CookieManager.getInstance();
		
        mCM.setAcceptCookie(true);
        mCM.removeSessionCookie();
        // 特别注意过期时间，否则会设置失败！！！
        //mCM.setCookie("peterwolf.cn.mu","remember_token="+mSessKey+"; path=/; expires=Thu, 23-Apr-2020 00:00:00 GMT");
        mCM.setCookie(mUrlConf.getHost(),"remember_token="+mSessKey+"; path=/; expires=Thu, 23-Apr-2020 00:00:00 GMT");
        CookieSyncManager.getInstance().sync();
        
        //mWebView.loadUrl(mUrlConf.getUrl());
        //mWebView.loadUrl("http://peterwolf.cn.mu/zone_supervisor_home.mobile");
        mWebView.loadUrl(mUrlConf.getUrl());
        
        //cookieManager = CookieManager.getInstance();
        //Toast.makeText(this, "cookie: "+cookieManager.getCookie("peterwolf.cn.mu"), Toast.LENGTH_SHORT).show();
        
        mMpage  = new MainPage(this);
        
        // 重登录按键
        TextView menu_text = (TextView)findViewById(R.id.main_menu_text);
        menu_text.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				if (WebMainActivity.this.APP_DEBUG){
					Log.d(TAG,"DEBUG模式");
					mMpage.getMainPageDialog().show();
				}else{
					// 直接返回登录界面
			        //mWebView.loadUrl(mUrlConf.getUrl());
					WebMainActivity.this.finish();
			        Toast.makeText(WebMainActivity.this, "返回登录界面...", Toast.LENGTH_SHORT).show();
//			        Toast.makeText(WebMainActivity.this, String.valueOf(getRequestedOrientation()) , Toast.LENGTH_LONG).show();
				}
			}
		});
        
        /*
        TextView config_text = (TextView)findViewById(R.id.main_config_text);
        config_text.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				mSysSetting.getSettingDialog().show();
//				mUrlConf.getUrlDialog().show();
			}
		});
		*/
        
        TextView exit_text = (TextView)findViewById(R.id.main_exit_text);
        exit_text.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				mExit.getCheckAppExitDialog().show();
			}
		});
        
        // 摄像头测试
        /*
        TextView camera_text = (TextView)findViewById(R.id.textView1);
        camera_text.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent ca = new Intent(WebMainActivity.this, PhotoCapturer.class);
				startActivity(ca);
			}
		});
        */
        /*
        // 定位测试
        ((TextView)findViewById(R.id.tv_loc)).setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if ( mLocTask.manualLocate() ) {
					LocationInfo info = mLocTask.getLocation();
					Toast.makeText(WebMainActivity.this.getApplicationContext(), 
        					"位置信息: "+info.mLatitude+", "+info.mLongitude, 
        					Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(WebMainActivity.this.getApplicationContext(), 
        					"位置信息获取失败", Toast.LENGTH_SHORT).show();
				}
			}
		});
		*/
        
        mLocHandler = new Handler() {
			@Override
        	public void handleMessage(Message msg) {
        		if (msg.what == LocTimerTask.EVENT_LOCATION_OK) {
        			// 显示地理位置信息
        			//LocationInfo info = mLocTask.getLocation();
        			Toast.makeText(WebMainActivity.this.getApplicationContext(), 
        					"位置信息成功上报！", Toast.LENGTH_SHORT).show();
        		} else if (msg.what == LocTimerTask.EVENT_LOCATION_FAIL) {
        			String err = mLocTask.getErrmsg();
        			Toast.makeText(WebMainActivity.this.getApplicationContext(),
        					"位置信息上报失败: "+err, Toast.LENGTH_SHORT).show();
        		}else if (msg.what == LocTimerTask.EVENT_LOCATION_ERROR ){
        			String err = mLocTask.getErrmsg();
        			Toast.makeText(WebMainActivity.this.getApplicationContext(),
        					"位置信息获取错误: "+err, Toast.LENGTH_SHORT).show();
        		} else if (msg.what == CheckAppClientExit.EVENT_WEBVIEW_EXIT) {
        			Toast.makeText(WebMainActivity.this, "退出主界面", Toast.LENGTH_SHORT).show();
        			WebMainActivity.this.finish();
        		} else {
        			Log.d(TAG, "got unknown msg: "+msg.what);
        		}
        	}
        };
        
        mExit	= new CheckAppClientExit(this, mLocHandler);
        
        // 启动位置定位服务
        mServiceIntent = new Intent(this, LocationService.class);
        Bundle bs = new Bundle();
        bs.putString("host", mUrlConf.getHost());
        bs.putString("sessid", mSessKey);
        bs.putString("eid", mEquipmentId.getId());
        mServiceIntent.putExtras(bs);
        this.startService(mServiceIntent);
        
        //Toast.makeText(this, mEquipmentId.getId(), Toast.LENGTH_LONG).show();
        Log.d(TAG, "EID: "+mEquipmentId.getId()+" cookie: "+mSessKey+" type: "+mLoginType);
    }
    
    @Override
    protected void onResume() {
    	if (mLocTask != null) {
    		mLocTask.clean();
    		mLocTask = null;
    	}
    	// 启动定时器
        //mLocTask = new LocTimerTask(mLocHandler, mUrlConf.getHost(), mSessKey, mEquipmentId.getId(), this.getApplicationContext());
        //mLocTask.Schedule();
        
        super.onResume();
    }
    
    protected void onActivityResult(int requestCode, int resultCode,  Intent intent) 
    {

    	if(requestCode== MyWebChromeClient.FILECHOOSER_IMAG_RESULTCODE)  
    	{  

    		Uri result = intent == null || resultCode != RESULT_OK ? null  : intent.getData();  
    		ScaleBitmap scale_bitmap = new ScaleBitmap(this,result);
    		Bitmap      bit_map      = scale_bitmap.scale();
    		InsertFileToMediaStore insert_file = new InsertFileToMediaStore(this,bit_map,"image/jpeg");
			Uri uri = insert_file.insert();
    		UploadMessage.set_upload_message(uri);
    		bit_map = null;
    		scale_bitmap.release();
    	}else if (requestCode == MyWebChromeClient.FILECHOOSER_VIDEO_RESULTCODE){
    		Uri result = intent == null || resultCode != RESULT_OK ? null  : intent.getData();  
    		UploadMessage.set_upload_message(result);
    	}else if (requestCode == MyWebChromeClient.CAPTURE_PICTURE_INTENT){
    		 if (resultCode != RESULT_OK  ){
    			 UploadMessage.set_upload_message(null);
    		 }else{
    			 if (UploadMessage.get_file_uri() != null){
    				 	try {
    				 		ScaleBitmap scale_bitmap = new ScaleBitmap(this,UploadMessage.get_file_uri());

    				 		Bitmap      bit_map      = scale_bitmap.scale();

    				 		if (bit_map == null){
    			    			 UploadMessage.set_upload_message(null);

    				 		}else{
    				 			InsertFileToMediaStore insert_file = new InsertFileToMediaStore(this,bit_map,"image/jpeg");
    				 			Uri uri = insert_file.insert();
    				 			UploadMessage.set_upload_message(uri);

    				 		}
    				 		bit_map = null;
    			    		scale_bitmap.release();
    				 	}catch (Exception e)
    				 	{
    				        Toast.makeText(WebMainActivity.this, "插入文件出错！", Toast.LENGTH_SHORT).show();
    		    			 UploadMessage.set_upload_message(null);
    				 	}
    			 }else{
    				 UploadMessage.set_upload_message();
    			 }
    		 }
    	}else if (requestCode == MyWebChromeClient.CAPTURE_VIDEO_INTENT){
    		if (resultCode != RESULT_OK  ){
   			 	UploadMessage.set_upload_message(null);
   		 	}else{
   		 		UploadMessage.set_upload_message(intent.getData());
   		 	}
    	}

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {   
    	if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {  
    		UploadMessage.set_upload_message(null);
			Log.d(TAG,"go back");
    		mWebView.goBack(); 
    		return true;
    	}
		Log.d(TAG,"can't go back");
    	UploadMessage.set_upload_message(null);
    	return super.onKeyDown(keyCode, event);
    }
    
    // 清理定时器
    @Override
    protected void onStop() {
    	//mLocTask.clean();
    	super.onStop();
    }

	@Override
	protected void onDestroy() {
		stopService(mServiceIntent);
		super.onDestroy();
	}
    
    /*
    private class MyReceiver extends BroadcastReceiver {  
    	  
        @Override  
        public void onReceive(Context context, Intent intent) {  
            Bundle bundle = intent.getExtras();  
            int progress = bundle.getInt("progress");  
        }  
    }
    */
}
