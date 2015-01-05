package com.yuyaa.awashcar.fragment;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.activity.ShakeDetailActivity;
import com.yuyaa.awashcar.entity.ShakeDiscount;
import com.yuyaa.awashcar.entity.ShopDiscount;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.GetWeather;
import com.yuyaa.awashcar.util.StringUtils;
import com.yuyaa.awashcar.util.ThreadPoolUtils;
import com.yuyaa.awashcar.util.Util;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
public class MainFragment extends BaseFragment implements SensorEventListener {
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	float[] gravity = new float[3];
	float[] linear_acceleration = new float[3];
	private Context mContext;
	public View contentView;
	public HomeFragment parentFragment;
	private TextView text_temperature, text_weather, empty_number;
	private TextView text_number0, text_number1;
	private Button btnOrderService;
	private Button btnBrandService;
	public static MainFragment singtonMainFragment;
	// 摇一摇
	private SensorManager mSensorManager;
	private Vibrator mVibrator;
	private View currentView;
	private int page;
	private RelativeLayout numberLayout;
	private GradientDrawable mDrawable;
	int[] colors = { Color.rgb(39, 39, 50), Color.rgb(57, 57, 85),
			Color.rgb(49, 44, 59), Color.rgb(41, 35, 42) };

	public MainFragment() {
		super();
	}

	public static synchronized MainFragment newInstance(HomeFragment parent) {
		if (null == singtonMainFragment) {
			singtonMainFragment = new MainFragment();
			singtonMainFragment.parentFragment = parent;
		}
		return singtonMainFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		page = 0;
		currentView = null;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			if (null != mSensorManager) {
				mSensorManager.registerListener(this, mSensorManager
						.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_GAME);
			}
		} else {
			if (null != mSensorManager) {
				mSensorManager.unregisterListener(this);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (null != mSensorManager && isVisibleToUser) {
			mSensorManager.unregisterListener(this);
		}

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (null != mSensorManager && isVisibleToUser) {
			mSensorManager.registerListener(this,
					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_GAME);
		}

	}

	private void updateWeatherInfo() {

		ThreadPoolUtils.execute(new Runnable() {
			@Override
			public void run() {
				GetWeather.getweather();
				btnBrandService.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						text_temperature.setText(MyApplication.getInstance().weather
								.getTemperature());
						text_weather.setText(MyApplication.getInstance().weather
								.getStatus());
					}
				});
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		contentView = inflater
				.inflate(R.layout.fragment_main, container, false);
		findViews(contentView);
		init();
		updateWeatherInfo();
		return contentView;
	}

	private void findViews(View container) {
		text_temperature = (TextView) container.findViewById(R.id.temperature);
		text_weather = (TextView) container.findViewById(R.id.weather_info);
		text_number0 = (TextView) container.findViewById(R.id.car_number_0);
		text_number1 = (TextView) container.findViewById(R.id.car_number_1);
		btnBrandService = (Button) container
				.findViewById(R.id.btn_main_brand_service);
		btnOrderService = (Button) container
				.findViewById(R.id.btn_main_order_service);
		numberLayout = (RelativeLayout) container
				.findViewById(R.id.number_layout);
		empty_number = (TextView) container.findViewById(R.id.empty_number);
	}

	private void init() {
		mSensorManager = (SensorManager) mContext
				.getSystemService(Service.SENSOR_SERVICE);
		mVibrator = (Vibrator) mContext
				.getSystemService(Service.VIBRATOR_SERVICE);

		mDrawable = new GradientDrawable(Orientation.LEFT_RIGHT, colors);
		mDrawable.setShape(GradientDrawable.RECTANGLE);
		btnBrandService.setBackgroundDrawable(mDrawable);
		btnOrderService.setBackgroundDrawable(mDrawable);
		btnBrandService.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				parentFragment.mViewPager.setCurrentItem(0, true);
			}
		});
		btnOrderService.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				parentFragment.mViewPager.setCurrentItem(2, true);
			}
		});
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		if (day == 1 || day == 7) {
			numberLayout.setVisibility(View.GONE);
			empty_number.setVisibility(View.VISIBLE);
		} else {
			int number1 = day - 1;
			int number2 = (number1 + 5) % 10;
			text_number0.setText("" + number1);
			text_number1.setText("" + number2);
			empty_number.setVisibility(View.GONE);
			numberLayout.setVisibility(View.VISIBLE);
		}
		String status = MyApplication.getInstance().weather.getStatus();
		if (!StringUtils.isEmpty(status))
			text_weather.setText(status);
		String temperature = MyApplication.getInstance().weather
				.getTemperature();
		if (!StringUtils.isEmpty(status))
			text_temperature.setText(temperature);
	}

	private ShakeDiscount createDiscount(JSONObject item) throws Exception {
		ShakeDiscount shakeDiscount = new ShakeDiscount();
		shakeDiscount.setShop_name(item.optString("name"));
		shakeDiscount.setShop_photo(item.optString("shop_photo"));
		shakeDiscount.setAddress(item.optString("address"));
		shakeDiscount.setDistance(item.optInt("distance"));
		shakeDiscount.setOrder_count(item.optInt("order_count"));
		shakeDiscount.setShop_id(item.optInt("shop_id"));
		shakeDiscount.setAverage_grade(item.optInt("average_grade"));
		shakeDiscount.setLongitude(item.optDouble("longitude"));
		shakeDiscount.setLatitude(item.optDouble("latitude"));
		shakeDiscount
				.setCar_wash_bookable(item.optBoolean("car_wash_bookable"));
		shakeDiscount.setMeet_needs(item.optBoolean("meet_needs"));
		shakeDiscount.setTelephone_area_code(item
				.optString("telephone_area_code"));
		shakeDiscount.setTelephone_number(item.optString("telephone_number"));
		JSONObject big_carObject = item.optJSONObject("big_car");
		JSONObject small_carObject = item.optJSONObject("small_car");
		ShopDiscount big_carDiscount = new ShopDiscount();
		ShopDiscount small_carDiscount = new ShopDiscount();
		big_carDiscount.setPrice(big_carObject.optString("price"));
		big_carDiscount.setSale_price(big_carObject.optString("sale_price"));
		big_carDiscount.setContent(big_carObject.optString("content"));
		small_carDiscount.setPrice(small_carObject.optString("price"));
		small_carDiscount.setContent(small_carObject.optString("content"));
		small_carDiscount
				.setSale_price(small_carObject.optString("sale_price"));
		shakeDiscount.setBig_car(big_carDiscount);
		shakeDiscount.setSmall_car(small_carDiscount);
		return shakeDiscount;
	}

	private void createPopupView(JSONArray jsonArray, String message) {
		RelativeLayout root = (RelativeLayout) singtonMainFragment.contentView;
		float marginLR = 20;
		float marginTB = 2;
		View view = null;
		if (jsonArray != null && jsonArray.length() > 0) {
			page++;
			// 先移除原来的popup
			for (int i = 0; i < jsonArray.length(); ++i) {
				JSONObject item = jsonArray.optJSONObject(i);
				try {
					final ShakeDiscount shakeDiscount;
					shakeDiscount = createDiscount(item);
					view = LayoutInflater.from(mContext).inflate(
							R.layout.view_item_shake_popup, null);
					((TextView) view
							.findViewById(R.id.text_shake_popup_item_name))
							.setText(shakeDiscount.getShop_name());
					((TextView) view
							.findViewById(R.id.text_shake_popup_item_address))
							.setText("地址：" + shakeDiscount.getAddress());
					((TextView) view
							.findViewById(R.id.text_shake_popup_item_distance))
							.setText(StringUtils
									.friendly_distance(shakeDiscount
											.getDistance()));
					TextView big_car_text = (TextView) view
							.findViewById(R.id.no_big_car_discount);
					TextView small_car_text = (TextView) view
							.findViewById(R.id.no_small_car_discount);
					TextView textBigCarPrice = ((TextView) view
							.findViewById(R.id.text_shake_popup_item_big_car_price));
					TextView textBigCarDiscountPrice = ((TextView) view
							.findViewById(R.id.text_shake_popup_item_big_car_discount_price));
					TextView textSmallCarPrice = ((TextView) view
							.findViewById(R.id.text_shake_popup_item_small_car_price));
					TextView textSmallCarDiscountPrice = ((TextView) view
							.findViewById(R.id.text_shake_popup_item_small_car_discount_price));

					big_car_text.setVisibility(View.GONE);
					small_car_text.setVisibility(View.GONE);
					ImageView discountImageView = (ImageView) view
							.findViewById(R.id.img_shake_popup_item_discount);
					discountImageView.setVisibility(View.GONE);
					textBigCarPrice.setVisibility(View.GONE);
					textBigCarDiscountPrice.setVisibility(View.GONE);
					if (StringUtils.isEmpty(shakeDiscount.getBig_car()
							.getPrice())
							&& StringUtils.isEmpty(shakeDiscount.getBig_car()
									.getSale_price())) {
						big_car_text.setVisibility(View.VISIBLE);
					} else if (StringUtils.isEmpty(shakeDiscount.getBig_car()
							.getPrice())) {
						discountImageView.setVisibility(View.VISIBLE);
						textBigCarDiscountPrice.setText("￥"
								+ shakeDiscount.getBig_car().getSale_price());
						textBigCarDiscountPrice.setVisibility(View.VISIBLE);
					} else if (StringUtils.isEmpty(shakeDiscount.getBig_car()
							.getSale_price())) {
						textBigCarPrice.setText("￥"
								+ shakeDiscount.getBig_car().getPrice());
						textBigCarPrice.setVisibility(View.VISIBLE);
					} else if (Float.parseFloat(shakeDiscount.getBig_car()
							.getPrice()) > Float.parseFloat(shakeDiscount
							.getBig_car().getSale_price())) {
						discountImageView.setVisibility(View.VISIBLE);
						textBigCarPrice.setText("￥"
								+ shakeDiscount.getBig_car().getPrice());
						textBigCarDiscountPrice.setText("￥"
								+ shakeDiscount.getBig_car().getSale_price());
						textBigCarDiscountPrice.setVisibility(View.VISIBLE);
						textBigCarPrice.setVisibility(View.VISIBLE);
					} else {
						textBigCarDiscountPrice.setText("￥"
								+ shakeDiscount.getBig_car().getPrice());
						textBigCarDiscountPrice.setVisibility(View.VISIBLE);
					}
					textSmallCarPrice.setVisibility(View.GONE);
					textSmallCarDiscountPrice.setVisibility(View.GONE);
					if (StringUtils.isEmpty(shakeDiscount.getSmall_car()
							.getPrice())
							&& StringUtils.isEmpty(shakeDiscount.getSmall_car()
									.getSale_price())) {
						small_car_text.setVisibility(View.VISIBLE);
					} else if (StringUtils.isEmpty(shakeDiscount.getSmall_car()
							.getPrice())) {
						discountImageView.setVisibility(View.VISIBLE);
						textSmallCarDiscountPrice.setText("￥"
								+ shakeDiscount.getSmall_car().getSale_price());
						textSmallCarDiscountPrice.setVisibility(View.VISIBLE);
					} else if (StringUtils.isEmpty(shakeDiscount.getSmall_car()
							.getSale_price())) {
						textSmallCarPrice.setText("￥"
								+ shakeDiscount.getSmall_car().getPrice());
						textSmallCarPrice.setVisibility(View.VISIBLE);
					} else if (Float.parseFloat(shakeDiscount.getSmall_car()
							.getPrice()) > Float.parseFloat(shakeDiscount
							.getSmall_car().getSale_price())) {
						discountImageView.setVisibility(View.VISIBLE);
						textSmallCarPrice.setText("￥"
								+ shakeDiscount.getSmall_car().getPrice());
						textSmallCarDiscountPrice.setText("￥"
								+ shakeDiscount.getSmall_car().getSale_price());
						textSmallCarDiscountPrice.setVisibility(View.VISIBLE);
						textSmallCarPrice.setVisibility(View.VISIBLE);
					} else {
						textSmallCarDiscountPrice.setText("￥"
								+ shakeDiscount.getSmall_car().getPrice());
						textSmallCarDiscountPrice.setVisibility(View.VISIBLE);
					}
					String image_path = shakeDiscount.getShop_photo();
					ImageView imageView = (ImageView) view
							.findViewById(R.id.img_shake_popup_item_logo);
					setViewImage(imageView, image_path);
					RelativeLayout.LayoutParams params = new LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					params.addRule(RelativeLayout.CENTER_HORIZONTAL);
					params.setMargins(Util.dip2px(mContext, marginLR),
							Util.dip2px(mContext, marginTB),
							Util.dip2px(mContext, marginLR),
							Util.dip2px(mContext, 20));
					if (0 < i) {
						params.addRule(RelativeLayout.BELOW,
								R.id.main_fragment_shake_img);
					} else if (0 == i) {
						params.addRule(RelativeLayout.ABOVE,
								R.id.btn_main_brand_service);
					}
					view.setLayoutParams(params);
					view.setId(i + 1);
					view.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(mContext,
									ShakeDetailActivity.class);
							Bundle bundle = new Bundle();
							bundle.putParcelable("discount", shakeDiscount);
							intent.putExtras(bundle);
							startActivity(intent);
						}
					});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			view = LayoutInflater.from(mContext).inflate(
					R.layout.view_item_shake_popup_null, null);
			RelativeLayout.LayoutParams params = new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
			params.setMargins(Util.dip2px(mContext, marginLR),
					Util.dip2px(mContext, marginTB),
					Util.dip2px(mContext, marginLR), Util.dip2px(mContext, 20));
			params.addRule(RelativeLayout.ABOVE, R.id.btn_main_brand_service);
			view.setLayoutParams(params);
			((TextView) view.findViewById(R.id.message)).setText(message);
		}
		if (null != contentView && currentView != null) {
			Animation push_left_out = AnimationUtils.loadAnimation(
					getActivity(), R.anim.push_left_out);
			currentView.startAnimation(push_left_out);
			root.removeView(currentView);
		}
		root.addView(view, root.getChildCount());
		Animation push_left_in = AnimationUtils.loadAnimation(getActivity(),
				R.anim.push_left_in);
		view.startAnimation(push_left_in);
		currentView = view;

	}

	// 实现摇一摇函数
	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		// values[0]:X轴，values[1]：Y轴，values[2]：Z轴

		final float alpha = 0.8f;

		gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
		gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
		gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

		linear_acceleration[0] = event.values[0] - gravity[0];
		linear_acceleration[1] = event.values[1] - gravity[1];
		linear_acceleration[2] = event.values[2] - gravity[2];

		if (sensorType == Sensor.TYPE_ACCELEROMETER) {
			if ((Math.abs(linear_acceleration[0]) > 14)
					|| (Math.abs(linear_acceleration[1]) > 14)
					|| (Math.abs(linear_acceleration[2]) > 14)) {

				if (!MyApplication.getInstance().isFirstLoc) {
					if (Const.shook)
						return;
					// playSound(R.raw.voi_addstar);
					mVibrator.vibrate(1000);
					getShakeMerchants();
					Const.shook = true;
				} else {
					if (MainFragment.this.isVisible()) {
						contentView.post(new Runnable() {
							@Override
							public void run() {
								createPopupView(null, "正在定位中，请稍后再试！");
							}
						});
					}

				}

			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	private void getShakeMerchants() {
		final String uriString = Const.SERVER_BASE_PATH + Const.SHAKE;
		final Map<String, String> params = new HashMap<String, String>();
		params.put(Const.KEY_LONGITUDE, ""
				+ MyApplication.getInstance().user.mCurrentLongitude);
		params.put(Const.KEY_LATITUDE, ""
				+ MyApplication.getInstance().user.mCurrentLantitude);
		params.put(Const.KEY_PAGE, "" + page);

		ThreadPoolUtils.execute(new Runnable() {

			@Override
			public void run() {
				GOVHttp request = GOVHttp.requestWithURL(uriString, "POST",
						params);
				JSONObject jsonObject = (JSONObject) request
						.execForJSONObject(MainFragment.this.getActivity());
				try {
					if ((null != jsonObject)
							&& (Const.HTTP_RESULT_SUCCESS == jsonObject
									.optInt(Const.KEY_STATUS))) {
						final JSONArray jsonArray = jsonObject
								.optJSONArray(Const.KEY_DATA);
						final String message = jsonObject
								.optString(Const.KEY_MESSAGE);
						contentView.post(new Runnable() {
							@Override
							public void run() {
								createPopupView(jsonArray, message);
							}
						});
					} else if (Const.HTTP_RESULT_FAILTRUE == jsonObject
							.optInt(Const.KEY_STATUS)) {
						final String message = jsonObject
								.optString(Const.KEY_MESSAGE);
						contentView.post(new Runnable() {

							@Override
							public void run() {
								createPopupView(null, message);
							}
						});
					} else {
						contentView.post(new Runnable() {

							@Override
							public void run() {
								createPopupView(null, "附件没有搜到店铺！");
							}
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Const.shook = false;
				}
			}
		});
	}

	@Override
	public void xmlClickMethod(View v) {
		// TODO Auto-generated method stub

	}

}
