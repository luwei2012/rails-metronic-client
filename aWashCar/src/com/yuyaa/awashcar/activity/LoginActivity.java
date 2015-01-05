package com.yuyaa.awashcar.activity;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.navisdk.util.common.StringUtils;
import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.SysUtil;
import com.yuyaa.awashcar.util.ThreadPoolUtils;
import com.yuyaa.awashcar.widget.MyProgressDialog;

public class LoginActivity extends BaseActivity {

	private static final int LOGIN_SUCCESS = 0;
	private static final int LOGIN_ERROR = 1;
	private static final int TIME_OUT = 2;
	private static final int UNKNOW_ERROR = 3;
	private static final int GO_REGISTER = 4;

	private LoginHandler loginHandler;
	private MyProgressDialog progressDialog;
	private EditText phone;
	private EditText password;
	private String message;
	private SharedPreferences sp;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
		phone = (EditText) this.findViewById(R.id.phone);
		password = (EditText) this.findViewById(R.id.password);
		Button login = (Button) this.findViewById(R.id.login_btn);
		loginHandler = new LoginHandler(this);
		if (!StringUtils.isEmpty(MyApplication.getInstance().user.account)
				&& StringUtils
						.isEmpty(MyApplication.getInstance().user.password)) {
			phone.setText(MyApplication.getInstance().user.account);
			password.setText(MyApplication.getInstance().user.password);
			login.performClick();
		}

	}

	// 这里用来接受退出程序的指令

	protected void onStart() {
		int flag = getIntent().getIntExtra("flag", 0);
		if (flag == SysUtil.TIME_OUT) {
			Toast toast = Toast.makeText(LoginActivity.this, "连接超时，请重新登录！",
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
		super.onStart();
	}

	// 登录按钮事件
	public void loginListener(View arg0) {
		if (!sp.getString("phone", "").equals(phone.getText().toString())) {
			sp.edit().putString("phone", phone.getText().toString()).commit();
		}
		if (!sp.getString("password", "").equals(password.getText().toString())) {
			sp.edit().putString("password", password.getText().toString())
					.commit();
		}
		if (validateInput()) {
			progressDialog = new MyProgressDialog(this,
					R.style.CustomProgressDialog);
			progressDialog.setTitle("正在登录中...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			// 提交登录
			ThreadPoolUtils.execute(new Runnable() {

				public void run() {
					Message msg = Message.obtain();
					try {
						boolean result = LoginActivity.this.httpLogin(phone
								.getText().toString(), password.getText()
								.toString());
						// loginHandler.post(r)
						if (result) {
							msg.arg1 = LOGIN_SUCCESS;
						} else {
							msg.arg1 = LOGIN_ERROR;
						}

					} catch (ConnectTimeoutException e) {
						e.printStackTrace();
						msg.arg1 = TIME_OUT;
					} catch (NullPointerException e) {
						e.printStackTrace();
						msg.arg1 = UNKNOW_ERROR;
					} catch (JSONException e) {
						e.printStackTrace();
						msg.arg1 = UNKNOW_ERROR;
					} catch (Exception e) {
						e.printStackTrace();
						// msg.arg1=LOGIN_ERROR;
					} finally {
						loginHandler.sendMessage(msg);
					}

				}
			});
		}
	}

	// 注册按钮事件
	public void GoRegisterListener(View arg0) {
		Message msg = Message.obtain();
		msg.arg1 = GO_REGISTER;
		loginHandler.sendMessage(msg);
	}

	// 注册按钮事件
	public void GoForgetListener(View arg0) {
		// 记录用户配置信息
		SysUtil sys = new SysUtil(LoginActivity.this);
		sys.forward(ForgetActivity.class);

	}

	/**
	 * 登录
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	private boolean httpLogin(String username, String password)
			throws Exception {

		String url = Const.SERVER_BASE_PATH + Const.LOGIN;
		System.out.println(url);
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("account", username);
		request.setValueForKey("password", password);
		request.setValueForKey("android_udid", getAndroid_udid());
		JSONObject json = (JSONObject) request
				.execForJSONObject(LoginActivity.this);
		message = json.getString("message");
		if (json.optInt("status") == 1) {
			updateUser(json);
			return true;
		} else {
			return false;
		}
	}

	public void clickCallback(View view) {
		switch (view.getId()) {
		case R.id.btn_merchant_detail_activity_back:
			onBackPressed();
			break;
		default:
			break;
		}
	}

	private boolean validateInput() {
		String msg = null;
		boolean result = true;
		String strPattern = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(phone.getText().toString());
		if (phone.getText().toString().equals("")) {
			msg = "手机号不能为空！";
			result = false;
		} else if (!m.matches()) {
			msg = "手机号不正确！";
			result = false;
		} else if (password.getText().toString().equals("")) {
			msg = "密码不能为空！";
			result = false;
		}
		if (!result) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setMessage(msg);
			builder.setTitle("提示");
			builder.setPositiveButton("返回", new OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
		}
		return result;
	}

	// add by luwei
	// 工具函数，用来得到android设备的唯一标识ID
	public String getAndroid_udid() {
		final TelephonyManager tm = (TelephonyManager) getBaseContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		final String tmDevice, tmSerial, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = ""
				+ android.provider.Settings.Secure.getString(
						getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);
		UUID deviceUuid = new UUID(androidId.hashCode(),
				((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
		String uniqueId = deviceUuid.toString();
		return uniqueId;
	}

	private static class LoginHandler extends Handler {
		WeakReference<LoginActivity> mActivity;

		LoginHandler(LoginActivity activity) {
			mActivity = new WeakReference<LoginActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			LoginActivity theActivity = mActivity.get();
			if (theActivity.progressDialog != null) {
				theActivity.progressDialog.dismiss();
			}
			if (msg.arg1 == LOGIN_SUCCESS) {

				Toast toast = Toast.makeText(theActivity,
						MyApplication.getInstance().user.integral_tip,
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				theActivity.finish();
			} else if (msg.arg1 == GO_REGISTER) {
				// 记录用户配置信息
				SysUtil sys = new SysUtil(theActivity);
				sys.forward(RegisterActivity.class);

			} else if (msg.arg1 == LOGIN_ERROR) {

				Toast toast = Toast.makeText(theActivity, theActivity.message,
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			} else if (msg.arg1 == TIME_OUT) {

				Toast toast = Toast.makeText(
						theActivity.getApplicationContext(), "登录超时！",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			} else if (msg.arg1 == UNKNOW_ERROR) {

				Toast toast = Toast.makeText(
						theActivity.getApplicationContext(), "服务器繁忙，请重试！",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}

			super.handleMessage(msg);
		}
	};

	public void updateUser(JSONObject item) throws JSONException {
		MyApplication.getInstance().user.updateUser(item);
		MyApplication.getInstance().user.account = phone.getText().toString();
		MyApplication.getInstance().user.phone = phone.getText().toString();
		MyApplication.getInstance().saveLoginData();
		MyApplication.getInstance().setStringGlobalData("session",
				GOVHttp.urlSessionIDMap.get(GOVHttp.url));
	}
}