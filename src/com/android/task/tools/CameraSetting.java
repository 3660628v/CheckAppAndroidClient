package com.android.task.tools;

import java.io.IOException;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

public class CameraSetting 
{
	private final static int DISPLAY_DEGREE = 90;
	private final static String TAG 		 = CameraSetting.class.getName();
	private final static int PIC_WIDTH		= 640;
	private final static int PIC_HEIGHT	    = 480;
	public  static void setCameraPicParameter(Camera camera,SurfaceHolder surface_holder) throws IOException
	{
		Log.d(TAG,"Seting Camera Parameter");
		
		android.hardware.Camera.Parameters parameters = camera.getParameters();
		//设置格式
		int maxPW = 0, maxPH = 0;
		//parameters.setPreviewFormat(ImageFormat.RGB_565);
		parameters.setPictureFormat(ImageFormat.JPEG);
		for ( android.hardware.Camera.Size sz : parameters.getSupportedPreviewSizes() ) {
			if (sz.width > maxPW ) {
				maxPW = sz.width;
				maxPH = sz.height;
			}
		}
		parameters.setPreviewSize(maxPW, maxPH);
		
		//设置图片保存时的分辨率大小
		int minW=0, minH=0;
		for ( android.hardware.Camera.Size sz : parameters.getSupportedPictureSizes() ) {
			if ( sz.width == PIC_WIDTH ) {
				minW = sz.width;
				minH = sz.height;
				break;
			}
			
			if ( minW == 0 || minW > sz.width ) {
				minW = sz.width;
				minH = sz.height;
			}
		}
		parameters.setPictureSize(minW, minH);

		//设置自动对焦
		parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_AUTO);
				
		camera.setDisplayOrientation(DISPLAY_DEGREE);
		camera.setParameters(parameters);
		camera.setPreviewDisplay(surface_holder);
	}
}
