package com.android.task.web;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MyWebClient extends WebViewClient{
	private Activity a;
	private ProgressDialog mLoadingBar;
	private String mCookie;
	public MyWebClient(Activity a, String cookie)
	{
		super();
		this.a  = a;
		mCookie = cookie;
	}
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		if (url.endsWith(".3gp")){
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(url),"video/3gp");
			a.startActivity(intent);
			return true;
		}else if(url.endsWith(".mp4")){
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(url),"video/mp4");
			a.startActivity(intent);
			return true;
		}
        view.loadUrl(url);
        return true;
    }
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) 
	{
		//mLoadingBar=ProgressDialog.show(this.a, null, "正在加载…");

		Toast.makeText(this.a, "访问服务器...", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onPageFinished(WebView view, String url) {

		super.onPageFinished(view, url);
    	Toast.makeText(this.a, "服务正常返回: "+mCookie, Toast.LENGTH_SHORT).show();
	}
	@Override
    public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
        Toast.makeText(this.a, "网络错误", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this.a);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) { 
            	
            }
        });
        builder.setTitle("发生错误");
        builder.setMessage(description);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
