package com.yuyaa.awashcar.activity;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
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

public class RegisterActivity extends BaseActivity {

	private static final int REGISTER_SUCCESS = 0;
	private static final int REGISTER_ERROR = 1;
	private static final int GO_LOGIN = 4;

	private RegisterHandler registerHandler;

	private MyProgressDialog progressDialog;

	private EditText phone;
	private EditText emailEditText;
	private EditText password;
	private EditText rePassword;
	private String message;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		getSharedPreferences("UserInfo", 0);
		phone = (EditText) this.findViewById(R.id.phone);
		password = (EditText) this.findViewById(R.id.password);
		rePassword = (EditText) this.findViewById(R.id.password2);
		emailEditText = (EditText) findViewById(R.id.email);
		registerHandler = new RegisterHandler(this);
	}

	// 注册按钮事件

	public void RegisterListener(View arg0) {

		if (validateInput()) {
			progressDialog = new MyProgressDialog(this,
					R.style.CustomProgressDialog);
			progressDialog.setTitle("注册中...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			ThreadPoolUtils.execute(new Runnable() {

				public void run() {
					Message msg = Message.obtain();
					try {
						boolean result = RegisterActivity.this.httpRegister(
								phone.getText().toString(), password.getText()
										.toString());
						// loginHandler.post(r)
						if (result) {
							msg.arg1 = REGISTER_SUCCESS;
						} else {
							msg.arg1 = REGISTER_ERROR;
						}

					} catch (ConnectTimeoutException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}

					registerHandler.sendMessage(msg);
				}
			});

		}

	}

	public void clickCallback(View view) {
		switch (view.getId()) {
		case R.id.btn_merchant_detail_activity_back:
			Message msg = Message.obtain();
			msg.arg1 = GO_LOGIN;
			registerHandler.sendMessage(msg);
			break;
		default:
			break;
		}
	}

	private boolean validateInput() {
		String msg = null;
		boolean result = true;
		String strPattern = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
		String stremailPattern = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(phone.getText().toString());
		Pattern pEmail = Pattern.compile(stremailPattern);
		Matcher mEmail = pEmail.matcher(emailEditText.getText().toString());
		if (phone.getText().toString().equals("")) {
			msg = "手机号不能为空！";
			result = false;
		} else if (!m.matches()) {
			msg = "手机号不正确！";
			result = false;
		} else if (StringUtils.isEmpty(password.getText().toString())) {
			msg = "密码不能为空！";
			result = false;
		} else if (!password.getText().toString()
				.equals(rePassword.getText().toString())) {
			msg = "前后密码不一致！";
			result = false;
		} else if (emailEditText.getText().toString().equals("")) {
			msg = "邮箱不能为空！";
			result = false;
		} else if (!mEmail.matches()) {
			msg = "邮箱地址不正确！";
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

	private boolean httpRegister(String username, String password)
			throws Exception {
		String url = Const.SERVER_BASE_PATH + Const.REGISTER;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("account", username);
		request.setValueForKey("password", password);
		request.setValueForKey("email", emailEditText.getText().toString());
		JSONObject json = (JSONObject) request
				.execForJSONObject(RegisterActivity.this);

		message = json.getString("message");
		if (json.optInt("status") == 1) {
			updateUser(json);
			return true;
		} else {
			return false;
		}
	}

	private static class RegisterHandler extends Handler {
		WeakReference<RegisterActivity> mActivity;

		RegisterHandler(RegisterActivity activity) {
			mActivity = new WeakReference<RegisterActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			RegisterActivity theActivity = mActivity.get();
			if (theActivity.progressDialog != null) {
				theActivity.progressDialog.dismiss();
			}
			if (msg.arg1 == REGISTER_SUCCESS) {
				Toast toast = Toast.makeText(theActivity,
						MyApplication.getInstance().user.integral_tip,
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				theActivity.finish();
			} else if (msg.arg1 == GO_LOGIN) {
				// 记录用户配置信息
				SysUtil sys = new SysUtil(theActivity);
				sys.forward(LoginActivity.class);
			} else if (msg.arg1 == REGISTER_ERROR) {
				Toast toast = Toast.makeText(theActivity, theActivity.message,
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
