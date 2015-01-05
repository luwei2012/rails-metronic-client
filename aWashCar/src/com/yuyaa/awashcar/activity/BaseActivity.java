package com.yuyaa.awashcar.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView; 

import com.koushikdutta.ion.Ion;
import com.umeng.analytics.MobclickAgent;
import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.util.Const;

public class BaseActivity extends FragmentActivity {
	public static final int LOADING_FINISHED = 0;
	public static final int REFRESH = 1;
	public static final int RELOAD_DATA = 2;
	public static final int TIME_OUT = 3;
	public static final int FOLLOW_END = 4;
	public static final int TYPE_LOADING_FINISHED = 7;
	public static final int REFRESH_TYPE = 8;
	public static final int RELOAD_TYPE = 5;
	public static final int TYPE_TIME_OUT = 6;
	public static final int COMPLETE_SUCCESS = 9;
	public static final int COMPLETE_ERROR = 10;
	public static final int CANCEL_END = 11;
	public static final int COMMENT_END = 12;

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPageEnd(getClass().getName());
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart(getClass().getName());
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApplication.getInstance().addActivity(this);
		MobclickAgent.setDebugMode(true);
		MobclickAgent.openActivityDurationTrack(false);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		MyApplication.getInstance().removeActivity(this);
	}

	/**
	 * 
	 */
	public BaseActivity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void setViewImage(ImageView imageView, String url) {
		Ion.with(this).load(Const.SERVER_BASE_PATH + url).withBitmap()
				.placeholder(R.drawable.unload_image)
				.error(R.drawable.outofmemory).animateLoad(null)
				.animateIn(null).intoImageView(imageView);
	}

}
