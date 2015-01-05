package com.yuyaa.awashcar.service;

import java.lang.ref.WeakReference;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.WindowManager;

import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.util.SysUtil;

public class MaintainNoticeService extends Service {
	// 提示语
	private String maintainMsg;
	@SuppressWarnings("unused")
	private MaintainNoticeHandler mHandler;
	private HandlerThread mHandlerThread;
	private AlertDialog noticeDialog;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mHandlerThread = new HandlerThread("UpdateService");
		mHandlerThread.start();
		mHandler = new MaintainNoticeHandler(mHandlerThread.getLooper(), this);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		super.onRebind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		maintainMsg = MyApplication.getInstance().getString(
				R.string.visit_later);
		if (intent.hasExtra("msg")) {
			maintainMsg = intent.getStringExtra("msg");
		}
		showNoticeDialog();
		return super.onStartCommand(intent, flags, startId);
	}

	public MaintainNoticeService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private static class MaintainNoticeHandler extends Handler {
		WeakReference<MaintainNoticeService> mService;

		public MaintainNoticeHandler(Looper looper,
				MaintainNoticeService service) {
			super(looper);
			mService = new WeakReference<MaintainNoticeService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			@SuppressWarnings("unused")
			MaintainNoticeService theService = mService.get();

			// TODO Auto-generated method stub
			switch (msg.what) {
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void showNoticeDialog() {
		AlertDialog.Builder builder = new Builder(MaintainNoticeService.this);
		builder.setTitle(MyApplication.getInstance().getString(
				R.string.maintenance_notification));
		builder.setMessage(maintainMsg);
		builder.setPositiveButton(
				MyApplication.getInstance().getString(R.string.vis_later),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						SysUtil sysUtil = new SysUtil(
								MaintainNoticeService.this);
						sysUtil.exit();
						MaintainNoticeService.this.stopSelf();
					}
				});
		noticeDialog = builder.create();
		noticeDialog.getWindow().setType(
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		noticeDialog.show();
	}

}
