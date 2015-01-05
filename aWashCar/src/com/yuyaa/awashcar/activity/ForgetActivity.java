package com.yuyaa.awashcar.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.StringUtils;
import com.yuyaa.awashcar.util.SysUtil;
import com.yuyaa.awashcar.util.ThreadPoolUtils;
import com.yuyaa.awashcar.widget.MyProgressDialog;

public class ForgetActivity extends BaseActivity {

	private AutoCompleteTextView email;
	private List<String> autoString = new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private ForgetHandler forgetHandler;
	private MyProgressDialog progressDialog;
	private static final int FORGET_RESULT = 0;
	private static final int TIME_OUT = 1;
	private static final int FORGET_ERROR = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forget);
		email = (AutoCompleteTextView) findViewById(R.id.forget_email);
		// 获取用户帐户用来提示输入
		autoString.add(MyApplication.getInstance().user.email);
		// 将可选内容与ArrayAdapter连接起来
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item,
				autoString.toArray(new String[autoString.size()]));
		email.setAdapter(adapter);

		forgetHandler = new ForgetHandler(this);
	}

	private String httpLogin(String username) throws Exception {
		String url = Const.SERVER_BASE_PATH + Const.FORGET_PASSWORD;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("email", username);
		JSONObject json = (JSONObject) request
				.execForJSONObject(ForgetActivity.this);
		return json.optString("message");

	}

	private boolean validate() {
		// TODO Auto-generated method stub
		String emailAddress = email.getText().toString();
		return StringUtils.isEmail(emailAddress);
	}

	public void forgetListener(View v) {
		// TODO Auto-generated method stub
		if (validate()) {
			progressDialog = new MyProgressDialog(this,
					R.style.CustomProgressDialog);
			progressDialog.setTitle("正在发送中...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			// 提交请求
			ThreadPoolUtils.execute(new Runnable() {

				public void run() {
					Message msg = Message.obtain();
					try {
						String result = ForgetActivity.this.httpLogin(email
								.getText().toString());
						msg.what = FORGET_RESULT;
						msg.obj = result;

					} catch (ConnectTimeoutException e) {
						e.printStackTrace();
						msg.what = TIME_OUT;
					} catch (Exception e) {
						e.printStackTrace();
						msg.what = FORGET_ERROR;
					} finally {
						forgetHandler.sendMessage(msg);
					}

				}
			});
		} else {
			AlertDialog.Builder builder = new Builder(ForgetActivity.this);
			builder.setMessage("邮箱地址不正确！");
			builder.setTitle("提示");
			builder.setPositiveButton("返回",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}

					});
			builder.create().show();
		}
	}

	public void clickCallback(View view) {
		switch (view.getId()) {
		case R.id.btn_merchant_detail_activity_back:
			SysUtil sys = new SysUtil(ForgetActivity.this);
			sys.forward(LoginActivity.class);
			break;
		default:
			break;
		}
	}

	private static class ForgetHandler extends Handler {
		WeakReference<ForgetActivity> mActivity;

		ForgetHandler(ForgetActivity activity) {
			mActivity = new WeakReference<ForgetActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ForgetActivity theActivity = mActivity.get();
			if (theActivity.progressDialog != null) {
				theActivity.progressDialog.dismiss();
			}
			String result = "";
			String title = "";
			switch (msg.what) {
			case FORGET_RESULT:
				result = (String) msg.obj;
				break;
			case TIME_OUT:
				title = "网络连接异常！";
				result = "请检查网络是否可用！";
				break;
			case FORGET_ERROR:
				title = "提示";
				result = "遭遇未知异常！";
				break;
			default:
				break;
			}			
			AlertDialog.Builder builder = new Builder(theActivity);
			builder.setMessage(result);
			builder.setTitle(title);
			builder.setCancelable(false);
			builder.setPositiveButton("返回",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}

					});
			builder.create().show();
			super.handleMessage(msg);
		}
	};
}
