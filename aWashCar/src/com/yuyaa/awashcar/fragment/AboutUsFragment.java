package com.yuyaa.awashcar.fragment;

import java.lang.ref.WeakReference;
import java.util.Date;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.activity.LoginActivity;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.ThreadPoolUtils;
import com.yuyaa.awashcar.widget.MyProgressDialog;

public class AboutUsFragment extends BaseFragment {
	private EditText mEditText;
	public AlertDialog dlg;
	private String commentString;
	private AboutUSHandler mHandler;
	public int today;
	public MyProgressDialog progressDialog;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);
		mHandler = new AboutUSHandler(this);
		return inflater.inflate(R.layout.fragment_about_us, container, false);
	}

	private static AboutUsFragment mAboutUsFragment;

	public static synchronized AboutUsFragment newInstance() {
		if (mAboutUsFragment == null) {
			mAboutUsFragment = new AboutUsFragment();
		}
		return mAboutUsFragment;
	}

	public AboutUsFragment() {
		// Required empty public constructor
		super();
		Date todayDate = new Date();
		today = (int) (todayDate.getTime() / 86400000);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void xmlClickMethod(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.feedBack) {
			if (MyApplication.getInstance()
					.getStringGlobalData("session", null) != null) {

				if (today <= MyApplication.getInstance().user
						.getLast_comment_time()) {
					Toast.makeText(getActivity(), "每个用户每天只能评论一次！",
							Toast.LENGTH_SHORT).show();
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							AboutUsFragment.this.getActivity());
					mEditText = new EditText(AboutUsFragment.this.getActivity());
					mEditText.setHeight(200);
					mEditText.setGravity(0);
					builder.setTitle("用户反馈");
					builder.setView(mEditText).setNegativeButton("取消", null);
					builder.setPositiveButton("提交",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									commentString = mEditText.getText()
											.toString();
									if (com.yuyaa.awashcar.util.StringUtils
											.isEmpty(commentString)) {
										Toast toast = Toast.makeText(
												AboutUsFragment.this
														.getActivity(),
												"反馈不能为空!", Toast.LENGTH_SHORT);
										toast.setMargin(0f, 0.05f);
										toast.show();
										return;
									}
									progressDialog = new MyProgressDialog(
											AboutUsFragment.this.getActivity(),
											R.style.CustomProgressDialog);
									progressDialog.setTitle("正在提交反馈...");
									progressDialog.setCancelable(false);
									progressDialog.show();
									ThreadPoolUtils.execute(new Runnable() {

										@Override
										public void run() {
											try {
												AboutUsFragment.this
														.httpFeedBack();

											} catch (ConnectTimeoutException e) {
												e.printStackTrace();
												mHandler.sendEmptyMessage(TIME_OUT);
											} catch (NullPointerException e) {
												e.printStackTrace();
											} catch (JSONException e) {
												e.printStackTrace();
											} catch (Exception e) {
												e.printStackTrace();
												// msg.what=LOGIN_ERROR;
											}
										}
									});
								}
							});
					dlg = builder.create();
					dlg.show();
				}

			} else {
				Toast toast = Toast.makeText(
						AboutUsFragment.this.getActivity(), MyApplication
								.getInstance().getString(R.string.please_log),
						Toast.LENGTH_SHORT);
				toast.setMargin(0f, 0.05f);
				toast.show();
				Intent intent = new Intent(AboutUsFragment.this.getActivity(),
						LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		}
	}

	private void httpFeedBack() throws Exception {

		String url = Const.SERVER_BASE_PATH + Const.FEEDBACK;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("comment", commentString);
		JSONObject json = (JSONObject) request
				.execForJSONObject(AboutUsFragment.this.getActivity());
		Message msg;
		if (json.optInt("status") == 1) {
			msg = Message.obtain(mHandler, COMMENT_SUCCESS, 0, 0,
					json.optString("message"));
			MyApplication.getInstance().user.setLast_comment_time(today);
			MyApplication.getInstance().saveLoginData();
		} else {
			msg = Message.obtain(mHandler, COMMENT_FAIL, 0, 0,
					json.optString("message"));
		}
		mHandler.sendMessage(msg);
	}

	private static class AboutUSHandler extends Handler {
		WeakReference<AboutUsFragment> mActivity;

		AboutUSHandler(AboutUsFragment activity) {
			mActivity = new WeakReference<AboutUsFragment>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			AboutUsFragment theActivity = mActivity.get();
			if (theActivity.progressDialog != null) {
				theActivity.progressDialog.dismiss();
			}
			switch (msg.what) {

			case TIME_OUT:
				// 更新显示的内容
				Toast.makeText(
						theActivity.getActivity(),
						MyApplication.getInstance().getString(
								R.string.net_connect_time_out),
						Toast.LENGTH_SHORT).show();
				break;

			case COMMENT_FAIL:
				// 更新显示的内容
				Toast.makeText(theActivity.getActivity(), (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				break;

			case COMMENT_SUCCESS:
				// 更新显示的内容
				// 更新上次评论时间
				Toast.makeText(theActivity.getActivity(), (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}

			super.handleMessage(msg);

		}
	};

}
