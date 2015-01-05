package com.yuyaa.awashcar.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.ThreadPoolUtils;
import com.yuyaa.awashcar.widget.MyProgressDialog;

public class ChangePSWActivity extends BaseActivity {

	private Context mContext;
	private EditText editOriginalPSW;
	private EditText editNewPSW;
	private EditText editNewPSWAgain;
	private MyProgressDialog progressDialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_psw);
		init();
	}

	private void init() {
		mContext = this;
		findViews();
	}

	private void findViews() {
		editOriginalPSW = (EditText) findViewById(R.id.edit_change_psw_activity_original_psw);
		editNewPSW = (EditText) findViewById(R.id.edit_change_psw_activity_new_psw);
		editNewPSWAgain = (EditText) findViewById(R.id.edit_change_psw_activity_new_psw_again);
	}

	public void clickCallback(View view) {
		switch (view.getId()) {
		case R.id.btn_change_psw_activity_back:
			onBackPressed();
			break;
		case R.id.btn_change_psw_activity_change:
			changePSW();
			break;
		default:
			break;
		}
	}

	private void changePSW() {
		final String originalPSW = editOriginalPSW.getText().toString();
		final String newPSW = editNewPSW.getText().toString();
		final String newPSWAgain = editNewPSWAgain.getText().toString();
		if (TextUtils.isEmpty(originalPSW) || TextUtils.isEmpty(newPSW)
				|| TextUtils.isEmpty(newPSWAgain)) {
			Toast.makeText(mContext, "密码不能为空！", Toast.LENGTH_SHORT).show();
			return;
		}
		if (!newPSW.equals(newPSWAgain)) {
			Toast.makeText(mContext, "与新密码不一致！", Toast.LENGTH_SHORT).show();
			return;
		}
		if (progressDialog == null) {
			progressDialog = new MyProgressDialog(this,
					R.style.CustomProgressDialog);
			progressDialog.setTitle("正在提交...");
			progressDialog.setCancelable(false);
		}
		progressDialog.show();
		ThreadPoolUtils.execute(new Runnable() {

			@Override
			public void run() {
				final String uriString = Const.SERVER_BASE_PATH
						+ Const.MODIFY_PASSWORD;
				Map<String, String> paramMap = new HashMap<String, String>();
				paramMap.put(Const.KEY_PASSWORD, originalPSW);
				paramMap.put("new_password", newPSW);
				GOVHttp request = GOVHttp.requestWithURL(uriString, "POST",
						paramMap);
				JSONObject jsonObject = (JSONObject) request
						.execForJSONObject(ChangePSWActivity.this);
				try {
					final String messageString = jsonObject
							.getString("message");
					if (jsonObject.optInt("status") == 1) {
						MyApplication.getInstance().user.password = newPSW;
						MyApplication.getInstance().saveLoginData();
					}
					editOriginalPSW.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(ChangePSWActivity.this,
									messageString, Toast.LENGTH_SHORT).show();
						}
					});
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					editOriginalPSW.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (ChangePSWActivity.this.progressDialog != null) {
								ChangePSWActivity.this.progressDialog.dismiss();
							}
						}
					});
				}

			}
		});

	}
}
