package com.yuyaa.awashcar.util;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.text.TextUtils;
import android.util.Log;

public class Util {
	public static long lastClickTime;

	// 判断是否有网络连接
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 判断是否使用的是3g网络
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWIFIor3GNetwork(Context context) {
		if (null != context) {
			ConnectivityManager conMan = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			// wifi
			State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
					.getState();
			if (wifi == State.CONNECTED) {
				return true;
			}
			// mobile 3G Data Network
			State mobile = conMan.getNetworkInfo(
					ConnectivityManager.TYPE_MOBILE).getState();
			if (mobile == State.CONNECTED) {
				return true;
			}
		}

		return false;
	}

	public static void saveLogin(Context context, JSONObject result) {
		SharedPreferences pref = context.getSharedPreferences("loginData",
				Context.MODE_PRIVATE);
		// String email = result.optString("Email");
		// String avatar = result.optString("Avatar");
		// int userId = result.optInt("Id");
		// int groupId = result.optInt("GroupId");
		// String nickName = result.optString("NickName");
		// String strDate = result.optString("LoginDate");
		// strDate = strDate.substring(strDate.indexOf("(") + 1,
		// strDate.indexOf(")"));
		// long loginDate = Long.parseLong(strDate);

		// String userName = result.optString("userName");
		// String passWord = result.optString("passWord");
		// Editor editor = pref.edit();
		// editor.putString("userName", userName);
		// editor.putString("passWord", passWord);
		// editor.putInt("userId", userId);
		// editor.putInt("groupId", groupId);
		// editor.putString("nickName", nickName);

		Editor editor = pref.edit();
		editor.putString(Const.KEY_NAME, result.optString(Const.KEY_NAME));
		editor.putString(Const.KEY_PHONE, result.optString(Const.KEY_PHONE));
		editor.putString(Const.KEY_LICENCE_PLATE,
				result.optString(Const.KEY_LICENCE_PLATE));
		editor.putString(Const.KEY_CAR_TYPE,
				result.optString(Const.KEY_CAR_TYPE));
		editor.putString(Const.KEY_INTEGRAL,
				result.optString(Const.KEY_INTEGRAL));
		editor.putString(Const.KEY_INTEGRAL_TIP,
				result.optString(Const.KEY_INTEGRAL_TIP));

		editor.commit();
	}

	public static void deleteLogin(Context context) {
		SharedPreferences pref = context.getSharedPreferences("loginData",
				Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		// editor.remove(Const.KEY_NAME);
		// editor.remove(Const.KEY_PHONE);
		// editor.remove(Const.KEY_LICENCE_PLATE);
		// editor.remove(Const.KEY_CAR_TYPE);
		// editor.remove(Const.KEY_INTEGRAL);
		// editor.remove(Const.KEY_INTEGRAL_TIP);
		editor.clear();
		editor.commit();
	}

	public static JSONObject getLogin(Context context) {
		SharedPreferences pref = context.getSharedPreferences("loginData",
				Context.MODE_PRIVATE);
		JSONObject result = new JSONObject();
		try {
			result.put(Const.KEY_NAME, pref.getString(Const.KEY_NAME, ""));
			result.put(Const.KEY_PHONE, pref.getString(Const.KEY_PHONE, ""));
			result.put(Const.KEY_LICENCE_PLATE,
					pref.getString(Const.KEY_LICENCE_PLATE, ""));
			result.put(Const.KEY_CAR_TYPE,
					pref.getString(Const.KEY_CAR_TYPE, ""));
			result.put(Const.KEY_INTEGRAL,
					pref.getString(Const.KEY_INTEGRAL, ""));
			result.put(Const.KEY_INTEGRAL_TIP,
					pref.getString(Const.KEY_INTEGRAL_TIP, ""));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean checkLogin(Context context) {
		boolean isLogin = false;
		SharedPreferences pref = context.getSharedPreferences("loginData",
				Context.MODE_PRIVATE);
		String userName = pref.getString(Const.KEY_PHONE, "");
		String passWord = pref.getString(Const.KEY_NAME, "");
		if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(passWord)) {
			isLogin = true;
		}
		return isLogin;
	}

	// 判断是否安装了某个APK
	public static boolean isInstalledAPK(Context context, String apkPackagename) {
		PackageInfo info = null;
		try {
			info = context.getPackageManager().getPackageInfo(apkPackagename,
					PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
			info = null;
		}
		if (null == info) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean isWrongClick() {
		long clickTime = System.currentTimeMillis();
		long timeD = clickTime - lastClickTime;
		if ((0 < timeD) && (1000 > timeD)) {
			return true;
		} else {
			lastClickTime = clickTime;
			return false;
		}
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	// 存取session
	public static String getSession(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				Const.KEY_SESSION, Context.MODE_PRIVATE);
		String sessionStr = pref.getString(Const.KEY_SESSION, "");
		return sessionStr;
	}

	public static void setSession(Context context, String sessionStr) {
		SharedPreferences pref = context.getSharedPreferences(
				Const.KEY_SESSION, Context.MODE_PRIVATE);
		Editor edit = pref.edit();
		edit.putString(Const.KEY_SESSION, sessionStr);
		edit.commit();
	}

	// 打印工具函数
	public static void print(String s) {
		if (s != null) {
			Log.i("+++++++++++++++++++++aaa", s);
		}
	}
}
