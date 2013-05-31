package com.android.task.picture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.android.task.R;
import com.android.task.tools.InsertFileToMediaStore;
import com.android.task.tools.Tools;
import com.android.task.tools.UploadMessage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class PhotoSaveDialog {
	private final String TAG = PhotoCapturer.class.getName();
	
	private Activity mA;
	private AlertDialog mPhotoSaveDialog;
	
	private View mPhotoSaveView;
	private Bitmap mBitmap, mRoBitmap;
	private File mPicFile;
	private ImageView mImgView;
	private int mInitDegree = 0;
	
	public PhotoSaveDialog(Activity a,Bitmap b,File f)
	{
		this.mA 		= a;
		this.mBitmap	= b;
		this.mPicFile	= f;
		
		init();
		Toast.makeText(mA, "点击图片可旋转", Toast.LENGTH_SHORT).show();
	}
	public AlertDialog getPhotoSaveDialog() {
		return mPhotoSaveDialog;
	}
	
	private void init()
	{
		this.mPhotoSaveView = this.mA.getLayoutInflater().inflate(R.layout.cam_pic_save_layout, null);
		mImgView = (ImageView)mPhotoSaveView.findViewById(R.id.pic_save_view);
		mImgView.setImageBitmap(this.mBitmap);
		AlertDialog.Builder save_diaglog_builder = new AlertDialog.Builder(this.mA);
		save_diaglog_builder.setView(this.mPhotoSaveView );
		save_diaglog_builder.setPositiveButton("保存", new OnClickListener()
		{
			public void onClick(DialogInterface dialog,
				int which)
			{
				if ( mPicFile == null ) {
					mPicFile = Tools.getOutputMediaFile(Tools.MEDIA_TYPE_IMAGE);
					if( mPicFile == null ){
						Log.e(TAG, "Fail to generate image, check storage permission.");
						return;
					}
					// save to file
					try {
						FileOutputStream fos = new FileOutputStream(mPicFile);
						if ( mRoBitmap != null ) {
							mRoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
						} else {
							mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
						}
						fos.flush();
						fos.close();
					} catch (FileNotFoundException e) {
						Log.e(TAG, "File not found: " + e.getMessage());
					} catch (IOException e) {
						Log.e(TAG, "Error accessing file: " + e.getMessage());
						Toast.makeText(mA, "无法保存图像，请检查存储容量", Toast.LENGTH_SHORT).show();
					}
				}
				
				InsertFileToMediaStore insert_file = new InsertFileToMediaStore(PhotoSaveDialog.this.mA,PhotoSaveDialog.this.mPicFile,"image/jpeg");
				Uri uri = insert_file.insert();
				if ( uri != null ) {
					Toast.makeText(mA, uri.toString(), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mA, "文件保存失败", Toast.LENGTH_SHORT).show();
				}
				mPicFile.delete();
				
				UploadMessage.set_upload_message(uri);
				PhotoSaveDialog.this.mA.finish();
			}
		});
		save_diaglog_builder.setNegativeButton("取消", new OnClickListener()
		{
			public void onClick(DialogInterface dialog,
				int which)
			{
				// delete saved file
				if(PhotoSaveDialog.this.mPicFile != null) {
					PhotoSaveDialog.this.mPicFile.delete();
				}
			}
		});
		
		mImgView.setOnClickListener(new View.OnClickListener() {	
			public void onClick(View v) {
				// TODO Auto-generated method stub
				imageRotate();
			}
		});
		
		mPhotoSaveDialog = save_diaglog_builder.create();
	}
	
	void imageRotate() {
		int w = mBitmap.getWidth(), h = mBitmap.getHeight();
		mInitDegree = (mInitDegree+90)%360;
		
		if ( mInitDegree == 90 || mInitDegree == 270 ) {
			w = mBitmap.getHeight();
			h = mBitmap.getWidth();
		}
		
		Matrix m = new Matrix();
		m.postScale((float)w/mBitmap.getWidth(), (float)h/mBitmap.getHeight());
		m.postRotate(mInitDegree);
		
		mRoBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), m, true);
		mImgView.setImageBitmap(mRoBitmap);
		
		Toast.makeText(mA, "img("+w+"*"+h+") rotate: "+mInitDegree, Toast.LENGTH_SHORT).show();
		
	}
}
