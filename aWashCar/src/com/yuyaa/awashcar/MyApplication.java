package com.yuyaa.awashcar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.util.Base64;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.yuyaa.awashcar.entity.User;
import com.yuyaa.awashcar.entity.Weather;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.ThreadPoolUtils;

public class MyApplication extends Application {
	public List<Activity> activityList = new LinkedList<Activity>();
	public List<Fragment> fragementList = new LinkedList<Fragment>();
	private static MyApplication instance;
	public int screenWidth;
	public Boolean readyToUpdate;
	public int screenHeight;
	public int devDpi;
	public User user;
	public Weather weather;
	private boolean isLoading = false;
	public boolean isFirstLoc = true;
	private LocationClient mLocationClient;
	private MyLocationListenner mBdLocationListener;

	public MyApplication() {
		instance = this;
	}

	public static MyApplication getInstance() {
		if (instance == null) {
			instance = new MyApplication();
		}
		return instance;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		readyToUpdate = true;
		// 百度demo中的初始化
		SDKInitializer.initialize(this);
		screenWidth = getResources().getDisplayMetrics().widthPixels;
		screenHeight = getResources().getDisplayMetrics().heightPixels;
		devDpi = getResources().getDisplayMetrics().densityDpi;
		user = new User();
		weather = new Weather();
		// 定位初始化
		mLocationClient = new LocationClient(getApplicationContext());
		mBdLocationListener = new MyLocationListenner();
		mLocationClient.registerLocationListener(mBdLocationListener);
		LocationClientOption option = new LocationClientOption();
		option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度，默认值gcj02，如果后面要在百度地图上用此定位，使用"bd09ll"更精确
		option.setOpenGps(true); // 打开gps
		option.setScanSpan(1000); // 设置发起定位请求的间隔时间为1000ms
		option.setIsNeedAddress(true);
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
		// 加载本地保存的用户信息
		getLoginData();
		getWeatherData();
		updateMyPosition();
	}

	private void updateMyPosition() {
		// TODO Auto-generated method stub
		if (!mLocationClient.isStarted())
			mLocationClient.start();
		else
			mLocationClient.requestLocation();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		if (mLocationClient.isStarted()) {
			mLocationClient.stop();
		}
		exit();
		super.onTerminate();
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
		jsonObj = (JSONObject) request.execForJSONObject(this);
		try {
			if (jsonObj.optInt("status") == 1) {
				user.updateUser(jsonObj);
				saveLoginData();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			isLoading = false;
		}
	}

	public void addActivity(Activity activity) {
		activityList.add(activity);
	}

	public void removeActivity(Activity activity) {
		activityList.remove(activity);
	}

	public void exit() {
		for (Activity activity : activityList) {
			activity.finish();
		}
		activityList.clear();
		fragementList.clear();
		System.exit(0);
	}

	public String getStringGlobalData(String key, String defaultValue) {
		SharedPreferences sharedPreferences = getSharedPreferences(
				"TempGlobalData", Context.MODE_PRIVATE);
		return sharedPreferences.getString(Const.SERVER_BASE_PATH + key,
				defaultValue);
	}

	public void setStringGlobalData(String key, String value) {
		SharedPreferences sharedPreferences = getSharedPreferences(
				"TempGlobalData", Context.MODE_PRIVATE);
		sharedPreferences.edit().putString(Const.SERVER_BASE_PATH + key, value)
				.commit();
	}

	public void removeTempGlobalData(String key) {
		SharedPreferences sharedPreferences = getSharedPreferences(
				"TempGlobalData", Context.MODE_PRIVATE);
		sharedPreferences.edit().remove(Const.SERVER_BASE_PATH + key).commit();
	}

	public void getLoginData() {
		SharedPreferences sharedPreferences = MyApplication.getInstance()
				.getSharedPreferences("UserData", Context.MODE_PRIVATE);
		String userString = sharedPreferences.getString(Const.SERVER_BASE_PATH
				+ "user_info", null);
		if (userString != null) {
			try {
				byte[] base64Bytes = Base64.decode(userString.getBytes(),
						Base64.DEFAULT);
				ByteArrayInputStream bais = new ByteArrayInputStream(
						base64Bytes);
				ObjectInputStream ois;
				ois = new ObjectInputStream(bais);
				user = (User) ois.readObject();
				// 更新用户信息
				updateLoginData();
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void saveLoginData() {
		try {
			SharedPreferences sharedPreferences = getSharedPreferences(
					"UserData", Context.MODE_PRIVATE);
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream;
			ooStream = new ObjectOutputStream(baoStream);
			ooStream.writeObject(user);
			String userString = new String(Base64.encode(
					baoStream.toByteArray(), Base64.DEFAULT));
			sharedPreferences
					.edit()
					.putString(Const.SERVER_BASE_PATH + "user_info", userString)
					.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removeLoginData() {
		SharedPreferences sharedPreferences = getSharedPreferences("UserData",
				Context.MODE_PRIVATE);
		sharedPreferences.edit().remove(Const.SERVER_BASE_PATH + "user_info")
				.commit();
		user = new User();
	}

	public void updateLoginData() {
		if (getStringGlobalData("session", null) != null) {
			startLoadingThread();
		}
	}

	public void saveWeatherData() {
		try {
			SharedPreferences sharedPreferences = getSharedPreferences(
					"Weather", Context.MODE_PRIVATE);
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream;
			ooStream = new ObjectOutputStream(baoStream);
			ooStream.writeObject(weather);
			String userString = new String(Base64.encode(
					baoStream.toByteArray(), Base64.DEFAULT));
			sharedPreferences.edit().putString("today", userString).commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removeWeatherData() {
		SharedPreferences sharedPreferences = getSharedPreferences("Weather",
				Context.MODE_PRIVATE);
		sharedPreferences.edit().remove("today").commit();
		weather = new Weather();
	}

	public void getWeatherData() {
		SharedPreferences sharedPreferences = MyApplication.getInstance()
				.getSharedPreferences("Weather", Context.MODE_PRIVATE);
		String userString = sharedPreferences.getString("today", null);
		if (userString != null) {
			try {
				byte[] base64Bytes = Base64.decode(userString.getBytes(),
						Base64.DEFAULT);
				ByteArrayInputStream bais = new ByteArrayInputStream(
						base64Bytes);
				ObjectInputStream ois;
				ois = new ObjectInputStream(bais);
				weather = (Weather) ois.readObject();
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null)
				return;
			MyApplication.getInstance().user.mCurrentLantitude = location
					.getLatitude();
			MyApplication.getInstance().user.mCurrentLongitude = location
					.getLongitude();
			if (MyApplication.getInstance().isFirstLoc) {
				MyApplication.getInstance().isFirstLoc = false;
			}
			mLocationClient.stop();
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

}
