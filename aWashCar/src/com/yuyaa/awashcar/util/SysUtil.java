package com.yuyaa.awashcar.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.activity.LoginActivity;
import com.yuyaa.awashcar.service.MaintainNoticeService;

public class SysUtil {
	public static final int EXIT_APPLICATION = 0x0001;
	public static final int LOGOUT_APPLICATION = 0x0002;
	public static final int TIME_OUT = 0x0003;

	private Context mContext;

	public SysUtil(Context context) {
		this.mContext = context;
	}

	public void exit() {
		MyApplication.getInstance().exit();
	}

	public void forward(Class<?> toContext) {

		Intent mIntent = new Intent();
		mIntent.setClass(mContext, toContext);
		mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mContext.startActivity(mIntent);
		((Activity) mContext).finish();
	}

	public void logout() {

	}

	public void Time_out() {
		Intent mIntent = new Intent();
		mIntent.setClass(mContext, LoginActivity.class);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mIntent.putExtra("flag", TIME_OUT);
		mContext.startActivity(mIntent);
	}

	public void Maintain() {
		// TODO Auto-generated method stub
		Intent mIntent = new Intent();
		mIntent.setClass(mContext, MaintainNoticeService.class);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startService(mIntent);

	}
}
