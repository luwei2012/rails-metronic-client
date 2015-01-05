package com.yuyaa.awashcar.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuyaa.awashcar.R;

/**
 * @author XZQ
 * @version
 */
public class MyProgressDialog extends Dialog {

	@Override
	public void setTitle(CharSequence title) {
		// TODO Auto-generated method stub
		textView.setText(title);
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		img_loading.startAnimation(rotateAnimation);
		super.show();
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		img_loading.clearAnimation();
		super.hide();
	}

	public Context context;// 上下文
	public ImageView img_loading;
	public TextView textView;
	public RotateAnimation rotateAnimation;

	public MyProgressDialog(Context context) {
		super(context);
		this.context = context;
	}

	public MyProgressDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		this.context = context;
	}

	public MyProgressDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
		View view = LayoutInflater.from(context).inflate(
				R.layout.load_no_close, null); // 加载自己定义的布局
		img_loading = (ImageView) view.findViewById(R.id.img_load);
		textView = (TextView) view.findViewById(R.id.tv_msg);
		rotateAnimation = (RotateAnimation) AnimationUtils.loadAnimation(
				context, R.anim.refresh); // 加载XML文件中定义的动画
		setContentView(view);// 为Dialoge设置自己定义的布局
	}

}
