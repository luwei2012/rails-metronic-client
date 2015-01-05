package com.yuyaa.awashcar.util;

import org.json.JSONObject;

import com.yuyaa.awashcar.MyApplication;

public class GetWeather {
	// static String[] city = { "北京", "天津", "上海", "重庆", "石家庄", "太原", "沈阳", "长春",
	// "哈尔滨", "南京", "杭州", "合肥", "福州", "南昌", "济南", "郑州", "武汉", "长沙", "广州",
	// "海口", "成都", "贵阳", "昆明", "西安", "兰州", "西宁", "拉萨", "南宁", "呼和浩特", "银川",
	// "乌鲁木齐", "香港", "台北", "澳门" }; // 各个城市
	public static String[] city = { "成都" };

	public static void getweather() // 获取天气函数
	{
		String ur_temerature = "http://www.weather.com.cn/data/sk/101270101.html";
		String ur_status = "http://www.weather.com.cn/data/cityinfo/101270101.html";
		try {
			// 获取天气
			GOVHttp request = GOVHttp.requestWithURL(ur_status, "GET");
			JSONObject jsonObject = (JSONObject) request
					.execForJSONObject(MyApplication.getInstance());
			MyApplication.getInstance().weather.setStatus(jsonObject
					.getJSONObject("weatherinfo").getString("weather"));
			// 获取温度
			request = GOVHttp.requestWithURL(ur_temerature, "GET");
			jsonObject = (JSONObject) request.execForJSONObject(MyApplication
					.getInstance());
			MyApplication.getInstance().weather.setTemperature(jsonObject
					.getJSONObject("weatherinfo").getString("temp"));
			// 保存天气信息
			MyApplication.getInstance().saveWeatherData();
		} catch (Exception e) {
			System.out.println("获取天气失败:" + e);
		}
	}

}
