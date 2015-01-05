package com.yuyaa.awashcar.fragment;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.activity.ChangePSWActivity;
import com.yuyaa.awashcar.activity.CompleteActivity;
import com.yuyaa.awashcar.activity.LoginActivity;
import com.yuyaa.awashcar.entity.User;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.StringUtils;
import com.yuyaa.awashcar.util.ThreadPoolUtils;

public class PersonalInfoFragment extends BaseFragment {

	private View contentView;
	private TextView textNickname;
	private TextView textPhoneNum;
	private TextView textCarId;
	private TextView textCarType;
	private TextView textPoints, tips1, tips2;
	private Button btnLogout;
	private boolean isLoading = false;
	private Button btnCompleteInfo, modifyButton;
	private static PersonalInfoFragment personalInfoFragment;

	public static PersonalInfoFragment newInstance(String param1, String param2) {
		if (personalInfoFragment == null) {
			personalInfoFragment = new PersonalInfoFragment();
			Bundle args = new Bundle();
			personalInfoFragment.setArguments(args);
		}

		return personalInfoFragment;
	}

	public static PersonalInfoFragment newInstance() {
		return newInstance("", "");
	}

	public PersonalInfoFragment() {
		// Required empty public constructor
		super();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			if (MyApplication.getInstance()
					.getStringGlobalData("session", null) != null) {
				startLoadingThread();
			}
		}
	}

	private void startLoadingThread() {
		if (!isLoading) {
			isLoading = true;
			ThreadPoolUtils.execute(new Runnable() {
				public void run() {
					reloadData();
				}
			});
		}

	}

	private void reloadData() {
		String url = Const.SERVER_BASE_PATH + Const.INFORMATION;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		JSONObject jsonObj = null;
		jsonObj = (JSONObject) request.execForJSONObject(getActivity());
		try {
			if (jsonObj.optInt("status") == 1) {
				MyApplication.getInstance().user.updateUser(jsonObj);
				MyApplication.getInstance().saveLoginData();
				if (tips2 != null)
					tips2.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							refreshUI();
						}
					});
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			isLoading = false;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		contentView = inflater.inflate(R.layout.fragment_personal_info,
				container, false);
		init();
		return contentView;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		refreshUI();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	private void init() {
		findViews();
	}

	private void findViews() {
		textNickname = (TextView) contentView
				.findViewById(R.id.text_personal_info_nickname);
		textPhoneNum = (TextView) contentView
				.findViewById(R.id.text_personal_info_phone_number);
		textCarId = (TextView) contentView
				.findViewById(R.id.text_personal_info_car_id);
		textCarType = (TextView) contentView
				.findViewById(R.id.text_personal_info_car_model);
		textPoints = (TextView) contentView
				.findViewById(R.id.text_personal_info_points);
		btnLogout = (Button) contentView
				.findViewById(R.id.btn_personal_info_logout);
		btnCompleteInfo = (Button) contentView
				.findViewById(R.id.btn_personal_info_complete_info);
		modifyButton = (Button) contentView
				.findViewById(R.id.btn_personal_info_modify_password);
		tips1 = (TextView) contentView
				.findViewById(R.id.text_personal_info_points_desciption);
		tips2 = (TextView) contentView
				.findViewById(R.id.text_personal_info_reward_description);
	}

	private void refreshUI() {
		User user = MyApplication.getInstance().user;
		if (!StringUtils.isEmpty(user.name)) {
			textNickname.setText(user.name);
			textNickname.setVisibility(View.VISIBLE);
		} else {
			textNickname.setVisibility(View.GONE);
		}
		if (!StringUtils.isEmpty(user.phone)) {
			textPhoneNum.setText(user.phone);
			textPhoneNum.setVisibility(View.VISIBLE);
		} else {
			textPhoneNum.setVisibility(View.GONE);
		}

		if (!StringUtils.isEmpty(user.licence_plate)) {
			textCarId.setText(user.licence_plate);
			textCarId.setVisibility(View.VISIBLE);
		} else {
			textCarId.setVisibility(View.GONE);
		}

		if (!StringUtils.isEmpty(user.car_type)) {
			textCarType.setText(user.car_type);
			textCarType.setVisibility(View.VISIBLE);
		} else {
			textCarType.setVisibility(View.GONE);
		}

		textPoints.setText(user.integral + "分");
		textPoints.setVisibility(View.VISIBLE);
		if (MyApplication.getInstance().getStringGlobalData("session", null) != null) {
			if (user.percent == 1) {
				btnCompleteInfo.setVisibility(View.VISIBLE);
				btnCompleteInfo.setText("修改个人信息");
				tips2.setVisibility(View.GONE);
			} else {
				btnCompleteInfo.setVisibility(View.VISIBLE);
				tips2.setVisibility(View.VISIBLE);
			}
			modifyButton.setVisibility(View.VISIBLE);
			btnLogout.setText("退出登录");
		} else {
			btnCompleteInfo.setVisibility(View.GONE);
			tips2.setVisibility(View.GONE);
			btnLogout.setText("登录");
			modifyButton.setVisibility(View.GONE);
		}

		if (!StringUtils.isEmpty(user.integral_tip)) {
			tips1.setText(user.integral_tip);
			tips1.setVisibility(View.VISIBLE);
		} else {
			tips1.setVisibility(View.GONE);
		}

	}

	@Override
	public void xmlClickMethod(View view) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btn_personal_info_logout:
			if (MyApplication.getInstance()
					.getStringGlobalData("session", null) != null) {
				MyApplication.getInstance().removeLoginData();
				MyApplication.getInstance().removeTempGlobalData("session");
			} else {
				Intent intent = new Intent(
						PersonalInfoFragment.this.getActivity(),
						LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				PersonalInfoFragment.this.getActivity().startActivity(intent);
			}
			refreshUI();
			break;
		case R.id.btn_personal_info_complete_info:
			Intent intent = new Intent(PersonalInfoFragment.this.getActivity(),
					CompleteActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PersonalInfoFragment.this.getActivity().startActivity(intent);
			break;
		case R.id.btn_personal_info_modify_password:
			Intent intent1 = new Intent(
					PersonalInfoFragment.this.getActivity(),
					ChangePSWActivity.class);
			intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PersonalInfoFragment.this.getActivity().startActivity(intent1);
			break;
		default:
			break;
		}
	}
}
