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
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.StringUtils;
import com.yuyaa.awashcar.util.ThreadPoolUtils;
import com.yuyaa.awashcar.widget.MyProgressDialog;

public class CompleteActivity extends BaseActivity {

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		refreshUI();
	}

	private CompleteHandler CompleteHandler;

	private MyProgressDialog progressDialog;

	private TextView account;
	private EditText name;
	private EditText email;
	private EditText lience_number, car_type;
	private RadioGroup sexRadioGroup;
	private String message;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_complete_personal_info);
		CompleteHandler = new CompleteHandler(this);
		account = (TextView) findViewById(R.id.account);
		name = (EditText) findViewById(R.id.name);
		email = (EditText) findViewById(R.id.email);
		lience_number = (EditText) findViewById(R.id.lience_number);
		car_type = (EditText) findViewById(R.id.car_type);
		sexRadioGroup = (RadioGroup) findViewById(R.id.sex_group);
		sexRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if (R.id.male == checkedId) {
					MyApplication.getInstance().user.sex = 1;

				} else {
					MyApplication.getInstance().user.sex = 0;
				}
			}
		});

	}

	// 注册按钮事件

	private void refreshUI() {
		// TODO Auto-generated method stub
		if (!StringUtils.isEmpty(MyApplication.getInstance().user.getAccount())) {
			account.setText(MyApplication.getInstance().user.getAccount());
		} else {
			account.setText("");
		}
		if (!StringUtils.isEmpty(MyApplication.getInstance().user.getName())) {
			name.setText(MyApplication.getInstance().user.getName());
		} else {
			name.setText("");
		}
		if (!StringUtils.isEmpty(MyApplication.getInstance().user.getEmail())) {
			email.setText(MyApplication.getInstance().user.getEmail());
		} else {
			email.setText("");
		}
		if (!StringUtils.isEmpty(MyApplication.getInstance().user
				.getLicence_plate())) {
			lience_number.setText(MyApplication.getInstance().user
					.getLicence_plate());
		} else {
			lience_number.setText("");
		}
		if (!StringUtils
				.isEmpty(MyApplication.getInstance().user.getCar_type())) {
			car_type.setText(MyApplication.getInstance().user.getCar_type());
		} else {
			car_type.setText("");
		}
		sexRadioGroup
				.check(MyApplication.getInstance().user.getSex() == 1 ? R.id.male
						: R.id.female);
	}

	public void CompleteListener(View arg0) {

		if (validateInput()) {
			progressDialog = new MyProgressDialog(this,
					R.style.CustomProgressDialog);
			progressDialog.setTitle("提交信息中...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			ThreadPoolUtils.execute(new Runnable() {

				public void run() {
					Message msg = Message.obtain();
					try {
						boolean result = CompleteActivity.this.httpComplete();
						// loginHandler.post(r)
						if (result) {
							msg.arg1 = COMPLETE_SUCCESS;
						} else {
							msg.arg1 = COMPLETE_ERROR;
						}

					} catch (ConnectTimeoutException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}

					CompleteHandler.sendMessage(msg);
				}
			});

		}

	}

	public void clickCallback(View view) {
		switch (view.getId()) {
		case R.id.btn_merchant_detail_activity_back:
			finish();
			break;
		default:
			break;
		}
	}

	private boolean validateInput() {
		String msg = null;
		boolean result = true;
		String stremailPattern = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
		Pattern pEmail = Pattern.compile(stremailPattern);
		Matcher mEmail = pEmail.matcher(email.getText().toString());
		if (StringUtils.isEmpty(email.getText().toString())) {
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

	private boolean httpComplete() throws Exception {
		String url = Const.SERVER_BASE_PATH + Const.COMPLETE;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("name", name.getText().toString());
		request.setValueForKey("sex", MyApplication.getInstance().user.getSex()
				+ "");
		request.setValueForKey("email", email.getText().toString());
		request.setValueForKey("car_type", car_type.getText().toString());
		request.setValueForKey("licence_plate", lience_number.getText()
				.toString());
		JSONObject json = (JSONObject) request
				.execForJSONObject(CompleteActivity.this);
		message = json.getString("message");
		if (json.optInt("status") == 1) {
			updateUser(json);
			return true;
		} else {
			return false;
		}
	}

	private static class CompleteHandler extends Handler {
		WeakReference<CompleteActivity> mActivity;

		CompleteHandler(CompleteActivity activity) {
			mActivity = new WeakReference<CompleteActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			CompleteActivity theActivity = mActivity.get();
			if (theActivity.progressDialog != null) {
				theActivity.progressDialog.dismiss();
			}
			if (msg.arg1 == COMPLETE_SUCCESS) {
				Toast toast = Toast.makeText(theActivity,
						MyApplication.getInstance().user.integral_tip,
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				theActivity.finish();
			} else if (msg.arg1 == COMPLETE_ERROR) {
				Toast toast = Toast.makeText(theActivity, theActivity.message,
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			theActivity.refreshUI();
			super.handleMessage(msg);

		}
	};

	public void updateUser(JSONObject item) throws JSONException {
		MyApplication.getInstance().user.updateUser(item);
		MyApplication.getInstance().saveLoginData();
	}
}
