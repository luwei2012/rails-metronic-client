package com.yuyaa.awashcar.util;

import com.baidu.mapapi.model.LatLng;
import com.yuyaa.awashcar.MyApplication;

public class Const {
	public static final int TARGET_SDK = 8;

	public static final long doubleBackInteralTime = 2000;
	public static long touchTime = 0;

	public static boolean shook;

	public static LatLng test_latlng;

	// 服务器路径
	public static final String SERVER_BASE_PATH = "http://192.168.0.102:3000";// 服务器根目录
	
	public static final String DOWNLOAD = "/apk/aWashCar.apk";// 服务器根目录
	public static final String REGISTER = "/mobile/register";// 注册地址
	public static final String LOGIN = "/mobile/login";// 登录地址

	public static final String INFORMATION = "/mobile/information";// 个人详情
	public static final String ORDER = "/mobile/order";// 下訂單
	public static final String ORDER_DETAIL = "/mobile/order_detail";// 訂單详情
	public static final String CANCEL_ORDER = "/mobile/cancel";// 取消訂單
	public static final String COMMENT_ORDER = "/mobile/comment";// 评价訂單
	public static final String CAR_WASH_ORDER = "/mobile/car_wash_order";// 下訂單
	public static final String COMPLETE = "/mobile/complete";// 完善个人信息
	public static final String FOLLOW_SHOP = "/mobile/follow_shop";// 收藏店铺
	public static final String FOLLOW_BRAND = "/mobile/follow_brand";// 收藏品牌
	public static final String FOLLOW_DISCOUNT = "/mobile/follow_discount";// 收藏服务
	public static final String PRAISE_DIACOUNT = "/mobile/praise_discount";// 赞
	public static final String FEEDBACK = "/mobile/feedBack";// 取消訂單
	public static final String MODIFY_PASSWORD = "/mobile/modify_password";// 修改密码
	public static final String FORGET_PASSWORD = "/mobile/forget_password";// 忘记密码
	public static final String SHAKE = "/mobile/shake";// 摇一摇
	public static final String SHAKE_DISCOUNT = "/mobile/shake_discount";// 摇一摇详情

	public static final String BRAND = "/mobile/brand";// 品牌页面
	public static final String BRANDS = "/mobile/brands";// 品牌列表
	public static final String BRAND_SHOPS = "/mobile/brand_shops";// 品牌-商店列表，点击品牌后需要展示商店列表
	public static final String SHOP_DISCOUNTS = "/mobile/shop_discounts";// 品牌-商店列表，点击品牌后需要展示商店列表
	public static final String DISCOUNTS = "/mobile/discounts";// 我要洗车页面
																// 店铺服务信息列表
	public static final String MAP_DISCOUNTS = "/mobile/map_discounts";// 我要洗车页面店铺服务信息列表
	public static final String DISCOUNT = "/mobile/discount";// 查看服务信息详情
	public static final String CAR_WASH_DISCOUNT = "/mobile/car_wash_discount";// 查看服务信息详情
	public static final String RECOMMEND_DISCOUNTS = "/mobile/recommend_discounts";// 品牌服务页面
																					// 用户收藏列表
	public static final String DISCOUNT_TYPES = "/mobile/discount_types";// 获取服务信息类型
	public static final String DISCOUNT_TYPES_SONS = "/mobile/discount_types_sons";// 获取服务信息类型子类型
	public static final String DISCOUNT_TYPES_DISCOUNTS = "/mobile/discount_types_discounts";// 获取某些服务类型的服务信息
	public static final String BRAND_PATH = "/mobile/brand";// 品牌页面
	public static final String MY_ORDERS = "/mobile/my_orders";// 品牌页面
	public static final String USER_FAV_DISCOUNTS = "/mobile/user_fav_discounts";// 品牌页面
	public static final String USER_FAV_SHOPS = "/mobile/user_fav_shops";// 品牌页面

	// 服务器请求关键字
	public static final String KEY_SESSION = "session";
	public static final String KEY_ACCOUNT = "account"; // 登录、注册
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_TIME = "time"; // 摇一摇搜索
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE_TEST = "104.079";
	public static final String KEY_LATITUDE_TEST = "30.674";
	public static final String KEY_PAGE = "page";
	public static final String KEY_BIGCAR = "bigCar";
	public static final String KEY_NAME = "name";
	public static final String KEY_SEX = "sex";
	public static final String KEY_CAR_TYPE = "car_type";
	public static final String KEY_LICENCE_PLATE = "licence_plate";
	public static final String KEY_PHONE = "phone";
	public static final String KEY_INTEGRAL = "integral";
	public static final String KEY_INTEGRAL_TIP = "integral_tip";
	public static final String KEY_SHOP_ID = "shop_id";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_DISTANCE = "distance";
	public static final String KEY_BIG_CAR_PRICE = "big_car_price";
	public static final String KEY_BIG_CAR_SALE_PRICE = "big_car_sale_price";
	public static final String KEY_SMALL_CAR_PRICE = "small_car_price";
	public static final String KEY_SMALL_CAR_SALE_PRICE = "small_car_sale_price";

	// 服务器返回结果关键字
	public static final String KEY_STATUS = "status";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DATA = "data";

	// 网络请求结果
	public static final int HTTP_RESULT_FAILTRUE = -1;
	public static final int HTTP_RESULT_SUCCESS = 1;

	public static final int VIEW_PGAER_WIDTH = MyApplication.getInstance().screenWidth;
	public static final int VIEW_PGAER_HEIGHT = (int) (MyApplication
			.getInstance().screenHeight * 0.25);
	// // 获取天气信息URL
	// public static final String WEATHER_URL =
	// "http://m.weather.com.cn/data/101270101.html";

	public static final int PAGE_SIZE = 15;

}
