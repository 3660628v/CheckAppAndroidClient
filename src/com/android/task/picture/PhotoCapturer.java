package com.android.task.picture;

import com.android.task.tools.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.android.task.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


public class PhotoCapturer extends Activity implements SensorEventListener
{
	private final String TAG = PhotoCapturer.class.getName();
	SurfaceView sView;
	SurfaceHolder surfaceHolder;
	int imgWidth, imgHeight;
	// 定义系统所用的照相机
	Camera camera;
	//是否在浏览中
	boolean isPreview = false;
	SensorManager mSensorMgr; 
	//SensorEventListener mSensorEL;
	Camera.AutoFocusCallback mCameraAFC;
	boolean mAutofocusSupport = false;
	
	FrameLayout mLayout = null;
	TextView mTvInfo = null;
	
	// 记录加速度，判断是否该对焦
	float mX = 0.0f, mY = 0.0f, mZ = 0.0f;
	boolean isMoved = true;
	
	OrientationEventListener myOrientationEventListener;

	
	
    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// 设置全屏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.cam_pic_layout);
				
		myOrientationEventListener
		   = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL){

		    @Override
		    public void onOrientationChanged(int orientation) {
		    	
		     // TODO Auto-generated method stub
		    	int cameraId = 0;
		    	
		    	android.hardware.Camera.CameraInfo info =
		                new android.hardware.Camera.CameraInfo();
		         android.hardware.Camera.getCameraInfo(cameraId, info);
		         orientation = (orientation + 45) / 90 * 90;
		         int rotation = 0;
		         if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
		             rotation = (info.orientation - orientation + 360) % 360;
		         } else {  // back-facing camera
		             rotation = (info.orientation + orientation) % 360;
		         }
		         if (PhotoCapturer.this.camera != null){
//		        	 PhotoCapturer.this.camera.getParameters().setRotation(rotation);
		        	 Camera.Parameters parameters = camera.getParameters();
		        	 parameters.setRotation(rotation);
		        	 PhotoCapturer.this.camera.setParameters(parameters);
//				     Toast.makeText(PhotoCapturer.this, "旋转:"+String.valueOf(rotation), Toast.LENGTH_LONG).show();

		         }
		         
		         //Toast.makeText(PhotoCapturer.this, "rotation: "+rotation, Toast.LENGTH_SHORT).show();
		    }};
		    
	    if (myOrientationEventListener.canDetectOrientation()){
	    	Toast.makeText(this, "Can DetectOrientation", Toast.LENGTH_SHORT).show();
	    	// 先屏蔽检测旋转
	    	//myOrientationEventListener.enable();
	    } else {
	    	Toast.makeText(this, "Can't DetectOrientation", Toast.LENGTH_SHORT).show();
	    	finish();
	    }  
		
		sView = (SurfaceView) findViewById(R.id.pic_view);
		surfaceHolder = sView.getHolder();
		surfaceHolder.addCallback(new Callback()
		{
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height)
			{
				if ( camera != null ) {
					//获得相机参数对象
					android.hardware.Camera.Parameters parameters = camera.getParameters();
					//设置格式
					//parameters.setPreviewFormat(ImageFormat.RGB_565);
					parameters.setPictureFormat(ImageFormat.JPEG);
					//设置预览大小，取1280*720, 如果不支持就取最大值
					int maxVW = 0, maxVH = 0;
					for ( android.hardware.Camera.Size sz : parameters.getSupportedPreviewSizes() ) {
						if ( sz.width == 1280 ) {
							maxVW = sz.width;
							maxVH = sz.height;
							break;
						}
						if ( sz.width > maxVW ) {
							maxVW = sz.width;
							maxVH = sz.height;
						}
					}
					parameters.setPreviewSize(maxVW, maxVH);
					
					//设置自动对焦
					//parameters.setFocusMode("auto");
					//设置图片保存时的分辨率大小, 设置为640*480, 若不支持则取最小
					int minW=0, minH=0;
					for ( android.hardware.Camera.Size sz : parameters.getSupportedPictureSizes() ) {
						if ( sz.width == 640 ) {
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
					
					parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_AUTO);
					camera.setDisplayOrientation(90);
					camera.setParameters(parameters);
					
					camera.startPreview();
					String md = camera.getParameters().getFocusMode();
					if ( md.equals(Camera.Parameters.FOCUS_MODE_AUTO) ) {
						mAutofocusSupport = true;
						camera.autoFocus(mCameraAFC);
						Toast.makeText(PhotoCapturer.this, minW+"*"+minH+"|"+maxVW+"*"+maxVH+"|"+md,
										Toast.LENGTH_SHORT).show();
					}
					isPreview = true;
				}
			}

			public void surfaceCreated(SurfaceHolder holder)
			{
				// 打开摄像头
				initCamera();
				initSensor();
			}
			
			public void surfaceDestroyed(SurfaceHolder holder)
			{
				Log.d(TAG,"surface destroyed");
				
				mSensorMgr.unregisterListener(PhotoCapturer.this);

				// 如果camera不为null ,释放摄像头
				if (camera != null)
				{
					if (isPreview)
					{
						camera.stopPreview();
						isPreview = false;
					}
					camera.release();
					camera = null;
				}
			}		
		});
		// 设置该SurfaceView自己不维护缓冲    
		//surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		// set button event
		/*
		Button recBtn = (Button) findViewById(R.id.pic_rec_btn);
		recBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				if(isPreview){
					try {
						// take picture and save
						camera.takePicture(null, null, myPicture);
					} catch (Exception e) {
						// TODO: handle exception
						Log.e(TAG, "Error take picture: " + e.getMessage());
					}
				}
			}
		});
		
		Button exitBtn = (Button) findViewById(R.id.pic_exit_btn);
		exitBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
					UploadMessage.set_upload_message(null);
					PhotoCapturer.this.finish();
					return;
				}
		});
		*/
		mCameraAFC = new Camera.AutoFocusCallback() {
			public void onAutoFocus(boolean success, Camera camera) {
				 if ( success ) {
					 //Toast.makeText(PhotoCapturer.this, "autofocus",
					 //			Toast.LENGTH_SHORT).show();
					 mTvInfo.setText("已对焦，点击屏幕拍照");
				 }
			}
		};
		mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		this.mLayout = (FrameLayout)findViewById(R.id.pic_layout);
		this.mTvInfo = new TextView(this);
		mLayout.addView(mTvInfo);
		
		mTvInfo.setTextColor(Color.argb(155, 255, 255, 255));
		mTvInfo.setTextSize(20);
		
		mLayout.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// 拍照
				if ( ! isPreview ) return;
				
				try {
					// take picture and save
					camera.takePicture(null, null, myPicture);
				} catch (Exception e) {
					// TODO: handle exception
					Log.e(TAG, "Error take picture: " + e.getMessage());
				}
			}
		});
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    }

    private void initCamera()
	{
		if (!isPreview)
		{
			camera = Camera.open();
			try {
				//设置预览
				camera.setPreviewDisplay(surfaceHolder);
			} catch (IOException e) {
				// 释放相机资源并置空
				camera.release();
				camera = null;
			}

			Log.d(TAG,"open camera");
		}
	}
    
    private void initSensor() {
    	Sensor s = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	mSensorMgr.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
    }

	
	PictureCallback myPicture = new PictureCallback()
	{
		// 这里先不保存
		@SuppressWarnings("unused")
		public void _onPictureTaken(byte[] data, Camera camera)
		{
			final File picFile = Tools.getOutputMediaFile(Tools.MEDIA_TYPE_IMAGE);
			if( picFile == null ){
				Log.e(TAG, "Fail to generate image, check storage permission.");
				return;
			}
			
			// save to file
			try {
				FileOutputStream fos = new FileOutputStream(picFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.e(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, "Error accessing file: " + e.getMessage());
				Toast.makeText(PhotoCapturer.this, "无法保存图像，请检查存储容量", Toast.LENGTH_SHORT).show();
			}
			
			final Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);	
			if(bm == null) {
				Log.e(TAG, "bitmap is null.");
				return;
			}
			
			PhotoSaveDialog photo_save_dialog = new PhotoSaveDialog(PhotoCapturer.this,bm,picFile);
			photo_save_dialog.getPhotoSaveDialog().show();
			
			//重新浏览
			camera.stopPreview();
			camera.startPreview();
			isPreview = true;
		}

		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			final Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
			PhotoSaveDialog photo_save_dialog = new PhotoSaveDialog(PhotoCapturer.this,bm,null);
			photo_save_dialog.getPhotoSaveDialog().show();
			
			camera.stopPreview();
			camera.startPreview();
			isPreview = true;
		}
	};



	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// 空函数
	}

	public void onSensorChanged(SensorEvent ev) {
		// 判断是否稳定，稳定后对焦
		if ( ev.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float dx = Math.abs(ev.values[0] - mX);
			float dy = Math.abs(ev.values[1] - mY);
			float dz = Math.abs(ev.values[2] - mZ);
			
			mX = ev.values[0];
			mY = ev.values[1];
			mZ = ev.values[2];
			
			if ( dx<0.5 && dy<0.5 && dz<0.5 && isPreview==true ) {
				if ( isMoved ) {
					// 对焦
					camera.autoFocus(mCameraAFC);
					isMoved = false;
				}
			} else {
				isMoved = true;
			}
			//Toast.makeText(PhotoCapturer.this, "acc: "+x+"/"+y+"/"+z,
			//		Toast.LENGTH_SHORT).show();
			
			//this.mTvInfo.setText("acc: " + dx + "/" + dy + "/" + dz+"\n"+isMoved);
		}
	}
}


