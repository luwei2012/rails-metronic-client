package com.yuyaa.awashcar.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMyLocationClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.adapter.ViewPageAdapter;
import com.yuyaa.awashcar.entity.Discount;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.StringUtils;
import com.yuyaa.awashcar.util.ThreadPoolUtils;
import com.yuyaa.awashcar.widget.MyProgressDialog;
import com.yuyaa.awashcar.widget.MyViewPager;

public class MapSearchActivity extends BaseActivity implements
		OnGetGeoCoderResultListener, OnGetSuggestionResultListener,
		OnGetRoutePlanResultListener {
	private Context mContext;
	private SuggestionSearch mSuggestionSearch;
	GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	private BaiduMap mBaiduMap;
	private MapSearchHandler mHandler;
	private boolean isLoading = false;
	private List<View> mViews; // 滑动的View集合
	private List<Discount> mList; // 滑动的图片集合
	private List<Marker> markerList;// 点的集合
	private ViewPageAdapter viewPageAdapter;
	private int viewPager_select_index;
	private AutoCompleteTextView mAutoCompleteTextView;
	private ArrayAdapter<String> sugAdapter;
	private PopupWindow mTimePopupWindow;
	private PopupWindow mPricePopupWindow;
	private PopupWindow mCarTypePopupWindow;
	private MyViewPager viewPager;
	private RelativeLayout viewPagerContainer;

	// 定位：
	private LocationClient mLocationClient;
	private BDLocationListener mBdLocationListener;
	private LocationMode mCurrentMode = LocationMode.NORMAL; // 普通、罗盘、跟随
	MapView mMapView;
	boolean isFirstLoc = true;
	private int price_condition;
	private JSONObject time_condition;
	private Boolean bigCar;
	private Double mLongitude;
	private Double mLatitude;
	private Boolean[][] time_zone = {
			{ false, false, false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false } };
	private Boolean[][] time_zone_tmp = {
			{ false, false, false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false } };
	private int time_zone_select_index;
	private long today;
	private long tomorrow;
	private String default_address;
	private MyProgressDialog progressDialog;
	private BitmapDescriptor[] bd;
	private BitmapDescriptor[] bd_focus;
	private Marker position_marker;
	OverlayManager routeOverlay = null;
	View timePopupView;
	View pricePopupView;
	View typePopupView;
	RadioGroup time_zone_index;
	RoutePlanSearch mRouteSearch = null; // 搜索模块，也可去掉地图模块独立使用

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_search);
		price_condition = 0;
		bigCar = false;
		mLatitude = 0.0;
		time_zone_select_index = 0;
		mLongitude = 0.0;
		time_condition = new JSONObject();
		Date todayDate = new Date();
		Date tomorrowDate = new Date(todayDate.getTime()
				+ (24 * 60 * 60 * 1000));
		today = todayDate.getTime() / 1000;
		tomorrow = tomorrowDate.getTime() / 1000;
		init();
		// 初始化搜索模块，注册事件监听
		mRouteSearch = RoutePlanSearch.newInstance();
		mRouteSearch.setOnGetRoutePlanResultListener(this);
	}

	private void findViews() {
		bd = new BitmapDescriptor[4];
		// 满足所有条件
		bd[0] = BitmapDescriptorFactory
				.fromResource(R.drawable.not_bookable_normal);
		// 支持预约但是不满足时间条件，这个在地图上不会显示出来。留着以后可能会用
		bd[1] = BitmapDescriptorFactory
				.fromResource(R.drawable.not_bookable_normal);
		// 不支持预约
		bd[2] = BitmapDescriptorFactory
				.fromResource(R.drawable.recommend_normal);
		// 地图选点 位置坐标
		bd[3] = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_openmap_mark);
		bd_focus = new BitmapDescriptor[4];
		// 满足所有条件 所中状态
		bd_focus[0] = BitmapDescriptorFactory
				.fromResource(R.drawable.not_bookable_select);
		// 支持预约但是不满足时间条件，这个在地图上不会显示出来。留着以后可能会用 所中状态
		bd_focus[1] = BitmapDescriptorFactory
				.fromResource(R.drawable.not_bookable_select);
		// 不支持预约 所中状态
		bd_focus[2] = BitmapDescriptorFactory
				.fromResource(R.drawable.recommend_select);
		// 地图选点 位置坐标 所中状态
		bd_focus[3] = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_openmap_focuse_mark);
		mMapView = (MapView) findViewById(R.id.mapview_map_search_activity);
		mBaiduMap = mMapView.getMap();
		mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.auto_text_map_search_activity);
		// 隐藏缩放控件
		int childCount = mMapView.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = mMapView.getChildAt(i);
			if (child instanceof ImageView || child instanceof ZoomControls) {
				child.setVisibility(View.GONE);
			}

		}
		viewPager = (MyViewPager) findViewById(R.id.map_search_viewpager);
		viewPager.setPageMargin(getResources().getDimensionPixelSize(
				R.dimen.page_margin));
		viewPagerContainer = (RelativeLayout) findViewById(R.id.map_search_viewpager_container);
		viewPagerContainer.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return viewPager.dispatchTouchEvent(event);
			}
		});
		mViews = new ArrayList<View>();
		viewPageAdapter = new ViewPageAdapter(mViews);
		// 设置一个监听器，当ViewPager中的页面改变时调用
		viewPager.setOnPageChangeListener(new MyPageChangeListener());
		viewPager.setAdapter(viewPageAdapter);// 设置填充ViewPager页面的适配器
		progressDialog = new MyProgressDialog(this,
				R.style.CustomProgressDialog);
		progressDialog.setCancelable(false);
		progressDialog.show();
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker arg0) {
				// TODO Auto-generated method stub
				// get index
				if (arg0 != position_marker) {
					int index = arg0.getZIndex() - 1;
					if (index >= markerList.size()) {
						index -= markerList.size();
					}
					viewPager.setCurrentItem(index);
					viewPagerContainer.setVisibility(View.VISIBLE);
				}
				return true;
			}
		});
		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onMapClick(LatLng arg0) {
				// TODO Auto-generated method stub
				mSearch.reverseGeoCode(new ReverseGeoCodeOption()
						.location(arg0));
				OverlayOptions ooA = new MarkerOptions().position(arg0)
						.icon(bd[3]).zIndex(0).draggable(true);
				if (position_marker != null) {
					position_marker.remove();
					mMapView.invalidate();
				}
				mBaiduMap.hideInfoWindow();
				position_marker = (Marker) (mBaiduMap.addOverlay(ooA));
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(arg0);
				mBaiduMap.animateMapStatus(u);
				mLatitude = arg0.latitude;
				mLongitude = arg0.longitude;
				startLoadingThread();
			}
		});
	}

	private void init() {
		mContext = this;
		markerList = new ArrayList<Marker>();
		mList = new ArrayList<Discount>();
		mHandler = new MapSearchHandler(this);
		findViews();
		// 定位初始化
		mLocationClient = new LocationClient(this);
		mBdLocationListener = new MyLocationListenner();
		mLocationClient.registerLocationListener(mBdLocationListener);
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				mCurrentMode, true, null)); // 设置定位marker
		mBaiduMap.setOnMyLocationClickListener(new OnMyLocationClickListener() {

			@Override
			public boolean onMyLocationClick() {
				// TODO Auto-generated method stub
				LatLng latLng = new LatLng(
						MyApplication.getInstance().user.mCurrentLantitude,
						MyApplication.getInstance().user.mCurrentLongitude);
				mLatitude = MyApplication.getInstance().user.mCurrentLantitude;
				mLongitude = MyApplication.getInstance().user.mCurrentLongitude;
				if (null != position_marker) {
					position_marker.remove();
					mMapView.invalidate();
					position_marker = null;
					mSearch.reverseGeoCode(new ReverseGeoCodeOption()
							.location(latLng));
					startLoadingThread();
				}
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
				mBaiduMap.animateMapStatus(u);
				return true;
			}
		});
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
		mBaiduMap.setMapStatus(msu);
		mBaiduMap.setMyLocationEnabled(true);
		LocationClientOption option = new LocationClientOption();
		option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度，默认值gcj02，如果后面要在百度地图上用此定位，使用"bd09ll"更精确
		option.setOpenGps(true); // 打开gps
		option.setScanSpan(10000); // 设置发起定位请求的间隔时间为1000ms
		option.setIsNeedAddress(true);
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this);
		sugAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line);
		mAutoCompleteTextView.setAdapter(sugAdapter);
		/**
		 * 当输入关键字变化时，动态更新建议列表
		 */
		mAutoCompleteTextView.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				if (cs.length() <= 0) {
					return;
				}
				mSuggestionSearch
						.requestSuggestion((new SuggestionSearchOption())
								.keyword(cs.toString()).city("成都"));
			}
		});

		mAutoCompleteTextView
				.setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView arg0, int actionId,
							KeyEvent arg2) {
						// TODO Auto-generated method stub
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							String address = mAutoCompleteTextView.getText()
									.toString();
							if (TextUtils.isEmpty(address)) {
								mAutoCompleteTextView.setText(default_address);
							} else {
								mSearch.geocode(new GeoCodeOption().city("成都")
										.address(
												mAutoCompleteTextView.getText()
														.toString()));
								// 设置新的坐标中心点
								if (progressDialog != null) {
									if (!progressDialog.isShowing()) {
										progressDialog.show();
									}
								} else {
									progressDialog = new MyProgressDialog(
											MapSearchActivity.this,
											R.style.CustomProgressDialog);
									progressDialog.setCancelable(false);
									progressDialog.show();
								}
							}
							((InputMethodManager) mContext
									.getSystemService(Context.INPUT_METHOD_SERVICE))
									.hideSoftInputFromWindow(
											mAutoCompleteTextView
													.getWindowToken(),
											InputMethodManager.HIDE_NOT_ALWAYS);
							return true;
						}
						return false;
					}
				});

	}

	public void clickCallback(View v) {
		switch (v.getId()) {
		case R.id.btn_map_search_activity_back:
			onBackPressed();
			break;
		case R.id.btn_map_search_activity_filter_time:
			resetButtons();
			if (mTimePopupWindow != null && mTimePopupWindow.isShowing()) {
				mTimePopupWindow.dismiss();
			} else {
				createTimePopupWindow(mContext,
						R.layout.view_popupwindow_select_time);
				v.setBackgroundResource(R.drawable.btn_filter_time_selected);
				mTimePopupWindow.showAsDropDown(v);
			}
			break;
		case R.id.btn_map_search_activity_filter_price:
			resetButtons();
			if (mPricePopupWindow != null && mPricePopupWindow.isShowing()) {
				mPricePopupWindow.dismiss();
			} else {
				createPricePopupWindow(mContext,
						R.layout.view_popupwindow_select_price);
				v.setBackgroundResource(R.drawable.btn_filter_price_selected);
				mPricePopupWindow.showAsDropDown(v);
			}
			break;
		case R.id.btn_map_search_activity_filter_model:
			resetButtons();
			if (mCarTypePopupWindow != null && mCarTypePopupWindow.isShowing()) {
				mCarTypePopupWindow.dismiss();
			} else {
				createCarTypePopupWindow(mContext,
						R.layout.view_popupwindow_select_model);
				v.setBackgroundResource(R.drawable.btn_filter_car_type_pressed);
				mCarTypePopupWindow.showAsDropDown(v);
			}
			break;
		default:
			resetButtons();
			break;
		}
	}

	public void resetButtons() {
		findViewById(R.id.btn_map_search_activity_filter_time)
				.setBackgroundResource(R.drawable.btn_filter_time_normal);
		findViewById(R.id.btn_map_search_activity_filter_price)
				.setBackgroundResource(R.drawable.btn_filter_price_normal);
		findViewById(R.id.btn_map_search_activity_filter_model)
				.setBackgroundResource(R.drawable.btn_filter_car_type_normal);
	}

	public OnCheckedChangeListener time_zone_index_click = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			if (R.id.rb_filter_date_day_0 == checkedId) {
				time_zone_select_index = 0;
				Date todayDate = new Date();
				Calendar calendar = GregorianCalendar.getInstance();
				calendar.setTime(todayDate); // assigns calendar
												// to
												// given date
				int default_time_zone = calendar.get(Calendar.HOUR_OF_DAY);
				default_time_zone = (default_time_zone - 9) > 0 ? default_time_zone - 9
						: 0;
				default_time_zone = default_time_zone <= 9 ? default_time_zone
						: 9;
				for (int i = 0; i < default_time_zone; i++) {
					int resID1 = getResources().getIdentifier(
							"cb_clock_popupwindow_select_time_" + i, "id",
							getPackageName());
					final CheckBox checkBox = (CheckBox) timePopupView
							.findViewById(resID1);
					checkBox.setClickable(false);
					checkBox.setEnabled(false);
				}
			} else {
				time_zone_select_index = 1;
				for (int i = 0; i < 9; i++) {
					int resID1 = getResources().getIdentifier(
							"cb_clock_popupwindow_select_time_" + i, "id",
							getPackageName());
					final CheckBox checkBox = (CheckBox) timePopupView
							.findViewById(resID1);
					checkBox.setClickable(true);
					checkBox.setEnabled(true);
				}
			}
			updateCheckBoxes(timePopupView);
		}

	};

	private void initTimePopupWindow() {
		// TODO Auto-generated method stub
		Date todayDate = new Date();
		Date tomorrowDate = new Date(todayDate.getTime()
				+ (24 * 60 * 60 * 1000));
		today = todayDate.getTime() / 1000;
		tomorrow = tomorrowDate.getTime() / 1000;
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(todayDate); // assigns calendar to given date
		int default_time_zone = calendar.get(Calendar.HOUR_OF_DAY);
		if (default_time_zone < 9) {
			// 还没到开门时间 do nothing
		} else {
			// 如果选中的是今天，需要将现在之前的时间段设置为不可选中
			if (time_zone_select_index == 0) {
				int tmp = default_time_zone - 9;
				tmp = tmp > 9 ? 9 : tmp;
				for (int i = 0; i < tmp; i++) {
					time_zone[0][i] = false;
					time_zone_tmp[0][i] = false;
				}
				if (default_time_zone > 17) {
					// 超出时间 切换到明天
					time_zone_select_index = 1;
					time_zone_index.check(R.id.rb_filter_date_day_1);
				} else {
					for (int i = 0; i < tmp; i++) {
						int resID1 = getResources().getIdentifier(
								"cb_clock_popupwindow_select_time_" + i, "id",
								getPackageName());
						CheckBox checkBox = (CheckBox) timePopupView
								.findViewById(resID1);
						checkBox.setClickable(false);
						checkBox.setEnabled(false);
					}
				}
			}

		}

	}

	private void createTimePopupWindow(Context context, int layoutId) {
		if (mTimePopupWindow == null) {
			timePopupView = ((Activity) mContext).getLayoutInflater().inflate(
					layoutId, null);
			timePopupView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mTimePopupWindow.dismiss();
				}
			});
			time_zone_index = (RadioGroup) timePopupView
					.findViewById(R.id.time_zone_index);
			time_zone_index.setOnCheckedChangeListener(time_zone_index_click);
			for (int i = 0; i < 9; i++) {
				int resID1 = getResources().getIdentifier(
						"cb_clock_popupwindow_select_time_" + i, "id",
						getPackageName());
				CheckBox checkBox = (CheckBox) timePopupView
						.findViewById(resID1);
				final int index = i;
				checkBox.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (((CheckBox) v).isChecked()) {
							time_zone_tmp[time_zone_select_index][index] = true;
						} else {
							time_zone_tmp[time_zone_select_index][index] = false;
						}
					}
				});
			}
			Button btnConfirm = (Button) timePopupView
					.findViewById(R.id.btn_popupwindow_confirm);
			Button checkAllButton = (Button) timePopupView
					.findViewById(R.id.btn_popupwindow_check_all);
			checkAllButton.setOnClickListener(checkAll);
			btnConfirm.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 记录选择
					try {
						time_condition = new JSONObject();
						JSONArray timeArray = new JSONArray();
						for (int i = 0; i < 9; i++) {
							time_zone[0][i] = time_zone_tmp[0][i];
							if (time_zone[0][i]) {
								timeArray.put(i + 9);
							}
						}
						if (timeArray.length() > 0)
							time_condition.put("" + today, timeArray);
						timeArray = new JSONArray();
						for (int i = 0; i < 9; i++) {
							time_zone[1][i] = time_zone_tmp[1][i];
							if (time_zone[1][i]) {
								timeArray.put(i + 9);
							}
						}
						if (timeArray.length() > 0)
							time_condition.put("" + tomorrow, timeArray);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					startLoadingThread();
					mTimePopupWindow.dismiss();
				}
			});
			mTimePopupWindow = new PopupWindow(timePopupView,
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
			ColorDrawable bg = new ColorDrawable(R.color.font_pink);
			bg.setAlpha(255);
			mTimePopupWindow.setBackgroundDrawable(bg);
			mTimePopupWindow.setTouchable(true);
			mTimePopupWindow.setOutsideTouchable(true);
			mTimePopupWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					// TODO Auto-generated method stub
					for (int i = 0; i < 9; i++) {
						time_zone_tmp[0][i] = time_zone[0][i];
					}
					for (int i = 0; i < 9; i++) {
						time_zone_tmp[1][i] = time_zone[1][i];
					}
					resetButtons();
				}
			});

		}
		initTimePopupWindow();
		updateCheckBoxes(mTimePopupWindow.getContentView());
	}

	public OnClickListener checkAll = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (time_zone_select_index == 0) {
				Date todayDate = new Date();
				Calendar calendar = GregorianCalendar.getInstance();
				calendar.setTime(todayDate); // assigns calendar to
												// given date
				int default_time_zone = calendar.get(Calendar.HOUR_OF_DAY);
				default_time_zone = (default_time_zone - 9) > 0 ? default_time_zone - 9
						: 0;
				default_time_zone = default_time_zone <= 9 ? default_time_zone
						: 9;
				for (int i = default_time_zone; i < 9; i++) {
					time_zone_tmp[time_zone_select_index][i] = true;
					updateCheckBoxes(timePopupView);
				}
			} else {
				for (int i = 0; i < 9; i++) {
					time_zone_tmp[time_zone_select_index][i] = true;
					updateCheckBoxes(timePopupView);
				}
			}
			((Button) v).setText("取消");
			v.setOnClickListener(cancelAll);
		}
	};

	public OnClickListener cancelAll = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (time_zone_select_index == 0) {
				Date todayDate = new Date();
				Calendar calendar = GregorianCalendar.getInstance();
				calendar.setTime(todayDate); // assigns calendar to
												// given date
				int default_time_zone = calendar.get(Calendar.HOUR_OF_DAY);
				default_time_zone = (default_time_zone - 9) > 0 ? default_time_zone - 9
						: 0;
				default_time_zone = default_time_zone <= 9 ? default_time_zone
						: 9;
				for (int i = default_time_zone; i < 9; i++) {
					time_zone_tmp[time_zone_select_index][i] = false;
					updateCheckBoxes(timePopupView);
				}
			} else {
				for (int i = 0; i < 9; i++) {
					time_zone_tmp[time_zone_select_index][i] = false;
					updateCheckBoxes(timePopupView);
				}
			}
			((Button) v).setText("全选");
			v.setOnClickListener(checkAll);
		}
	};

	private void updateCheckBoxes(View view) {
		for (int i = 0; i < 9; i++) {
			int resID = getResources().getIdentifier(
					"cb_clock_popupwindow_select_time_" + i, "id",
					getPackageName());
			CheckBox checkBox = (CheckBox) view.findViewById(resID);
			checkBox.setChecked(time_zone_tmp[time_zone_select_index][i]);
		}
	}

	private void createPricePopupWindow(Context context, int layoutId) {
		if (mPricePopupWindow == null) {
			pricePopupView = ((Activity) mContext).getLayoutInflater().inflate(
					layoutId, null);
			mPricePopupWindow = new PopupWindow(pricePopupView,
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
			ColorDrawable bg = new ColorDrawable(R.color.font_pink);
			bg.setAlpha(255);
			mPricePopupWindow.setBackgroundDrawable(bg);
			mPricePopupWindow.setTouchable(true);
			mPricePopupWindow.setOutsideTouchable(true);
			pricePopupView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mPricePopupWindow.dismiss();
				}
			});
			mPricePopupWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					// TODO Auto-generated method stub
					resetButtons();
				}
			});
			RadioGroup radioGroup = (RadioGroup) pricePopupView
					.findViewById(R.id.condition_price_group);
			radioGroup.check(radioGroup.getChildAt(price_condition).getId());
			radioGroup
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(RadioGroup group,
								int checkedId) {
							// TODO Auto-generated method stub
							price_condition = group.indexOfChild(group
									.findViewById(checkedId));
							startLoadingThread();
							mPricePopupWindow.dismiss();
						}
					});
		}
	}

	private void createCarTypePopupWindow(Context context, int layoutId) {
		if (mCarTypePopupWindow == null) {
			typePopupView = ((Activity) mContext).getLayoutInflater().inflate(
					layoutId, null);
			mCarTypePopupWindow = new PopupWindow(typePopupView,
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
			ColorDrawable bg = new ColorDrawable(R.color.font_pink);
			bg.setAlpha(255);
			mCarTypePopupWindow.setBackgroundDrawable(bg);
			mCarTypePopupWindow.setTouchable(true);
			mCarTypePopupWindow.setOutsideTouchable(true);
			typePopupView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mCarTypePopupWindow.dismiss();
				}
			});
			mCarTypePopupWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					// TODO Auto-generated method stub
					resetButtons();
				}
			});

			RadioGroup radioGroup = (RadioGroup) typePopupView
					.findViewById(R.id.conditon_car_type_group);
			radioGroup.check(radioGroup.getChildAt(bigCar ? 1 : 0).getId());
			radioGroup
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(RadioGroup group,
								int checkedId) {
							// TODO Auto-generated method stub
							bigCar = (group.indexOfChild(group
									.findViewById(checkedId))) == 1;
							startLoadingThread();
							mCarTypePopupWindow.dismiss();
						}
					});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();

	}

	@Override
	protected void onStart() {
		// 开启图层定位
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		// 关闭图层定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mLocationClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		mSearch.destroy();
		mSuggestionSearch.destroy();
		for (int i = 0; i < 4; i++) {
			bd[i].recycle();
			bd_focus[i].recycle();
		}
		mRouteSearch.destroy();
		super.onDestroy();
	}

	@Override
	public void onGetSuggestionResult(SuggestionResult res) {
		if (res == null || res.getAllSuggestions() == null) {
			return;
		}
		sugAdapter.clear();
		for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
			if (info.key != null)
				sugAdapter.add(info.key);
		}
		sugAdapter.notifyDataSetChanged();
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360，这里随便写100，通过location.getDirection()得不到？
					.direction(location.getDirection())
					.latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			mLatitude = MyApplication.getInstance().user.mCurrentLantitude = location
					.getLatitude();
			mLongitude = MyApplication.getInstance().user.mCurrentLongitude = location
					.getLongitude();
			mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
					mCurrentMode, true, null)); // 设置定位marker
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ll));
				startLoadingThread();
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
			mLocationClient.stop();
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@SuppressLint("InflateParams")
	private void updateViewPage() {
		viewPager.removeAllViews();
		int discount_index = mList.size();
		if (mList != null && mList.size() > 0) {
			for (int i = 0; i < discount_index; i++) {
				final Discount discount = mList.get(i);
				View view = ((Activity) mContext).getLayoutInflater().inflate(
						R.layout.map_viewpager_item, null);
				TextView name = (TextView) view
						.findViewById(R.id.text_map_search_activity_location_detail);
				TextView address = (TextView) view
						.findViewById(R.id.text_map_search_activity_distance);
				TextView detailView = (TextView) view
						.findViewById(R.id.text_map_search_activity_detail);
				ImageView discountImageView = (ImageView) view
						.findViewById(R.id.img_map_search_activity_tuan);
				ImageView bookableImageView = (ImageView) view
						.findViewById(R.id.img_map_search_activity_ding);
				ImageView[] stars = new ImageView[5];
				stars[0] = (ImageView) view
						.findViewById(R.id.img_map_search_activity_star0);
				stars[1] = (ImageView) view
						.findViewById(R.id.img_map_search_activity_star1);
				stars[2] = (ImageView) view
						.findViewById(R.id.img_map_search_activity_star2);
				stars[3] = (ImageView) view
						.findViewById(R.id.img_map_search_activity_star3);
				stars[4] = (ImageView) view
						.findViewById(R.id.img_map_search_activity_star4);
				TextView textPrice = (TextView) view
						.findViewById(R.id.text_map_search_activity_price);
				TextView textSalePrice = (TextView) view
						.findViewById(R.id.text_map_search_activity_sale_price);
				TextView routeButton = (TextView) view
						.findViewById(R.id.btn_map_search_activity_navigate);
				TextView textDistance = (TextView) view
						.findViewById(R.id.text_map_search_activity_distance);
				name.setText(i + 1 + ". " + discount.getShop_name());
				address.setText(discount.getAddress());
				discountImageView.setVisibility(View.GONE);
				textDistance.setText(StringUtils.friendly_distance(discount
						.getDistance()));
				int average_grade = discount.getAverage_grade();
				for (int j = 0; j < stars.length; j++) {
					stars[j].setBackgroundResource(R.drawable.img_star_dark_big);
				}
				for (int i1 = 0; i1 < average_grade; i1++) {
					stars[i1]
							.setBackgroundResource(R.drawable.img_star_light_big);
				}
				if (discount.getCar_wash_bookable()) {
					bookableImageView.setVisibility(View.VISIBLE);
				} else {
					bookableImageView.setVisibility(View.GONE);
				}
				OnClickListener onClickListener = new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(MapSearchActivity.this,
								CarWashDiscountDetailActivity.class);
						Bundle bundle = new Bundle();
						bundle.putParcelable("discount", discount);
						intent.putExtras(bundle);
						startActivity(intent);
					}
				};
				detailView.setOnClickListener(onClickListener);
				view.setOnClickListener(onClickListener);
				if (StringUtils.isEmpty(discount.getPrice())
						&& StringUtils.isEmpty(discount.getSale_price())) {
					textPrice.setVisibility(View.GONE);
					textSalePrice.setVisibility(View.GONE);
				} else if (StringUtils.isEmpty(discount.getPrice())) {
					discountImageView.setVisibility(View.VISIBLE);
					textSalePrice.setText("￥" + discount.getSale_price());
					textPrice.setVisibility(View.GONE);
					textSalePrice.setVisibility(View.VISIBLE);
				} else if (StringUtils.isEmpty(discount.getSale_price())) {
					textPrice.setText("￥" + discount.getPrice());
					textPrice.setVisibility(View.VISIBLE);
					textSalePrice.setVisibility(View.GONE);
				} else if (Float.parseFloat(discount.getPrice()) > Float
						.parseFloat(discount.getSale_price())) {
					discountImageView.setVisibility(View.VISIBLE);
					textPrice.setText("￥" + discount.getPrice());
					textSalePrice.setText("￥" + discount.getSale_price());
					textPrice.setVisibility(View.VISIBLE);
					textSalePrice.setVisibility(View.VISIBLE);
				} else {
					textSalePrice.setText("￥" + discount.getPrice());
					textPrice.setVisibility(View.GONE);
					textSalePrice.setVisibility(View.VISIBLE);
				}
				// 路劲规划
				routeButton.setOnClickListener(routingButtonOnClickListener);
				mViews.add(view);
			}
			viewPagerContainer.setVisibility(View.VISIBLE);
		} else {
			viewPagerContainer.setVisibility(View.GONE);
		}
		viewPageAdapter.notifyDataSetChanged();
	}

	public OnClickListener routingButtonOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Discount discount = mList.get(viewPager_select_index);
			// 获取输入的起终点
			startCalcRoute(discount);
		}
	};

	private void startCalcRoute(Discount discount) {
		if (routeOverlay != null) {
			routeOverlay.removeFromMap();
			mMapView.invalidate();
		}
		// 重置浏览节点的路线数据
		// 设置起终点信息，对于tranist search 来说，城市名无意义
		PlanNode stNode = PlanNode.withLocation(new LatLng(mLatitude,
				mLongitude));
		PlanNode enNode = PlanNode.withLocation(new LatLng(discount
				.getLatitude(), discount.getLongitude()));

		// 实际使用中请对起点终点城市进行正确的设定
		mRouteSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode)
				.to(enNode));
	}

	/**
	 * 当ViewPager中页面的状态发生改变时调用
	 * 
	 * @author Administrator
	 * 
	 */
	private class MyPageChangeListener implements OnPageChangeListener {
		/**
		 * This method will be invoked when a new page becomes selected.
		 * position: Position index of the new selected page.
		 */
		public void onPageSelected(int position) {

			if (mList.size() > 0) {
				Discount discount = mList.get(viewPager_select_index);
				int index = 0;
				if (discount.getMeet_needs()) {
					index = 0;
				} else if (discount.getCar_wash_bookable()) {
					index = 1;
				} else
					index = 2;
				markerList.get(viewPager_select_index).setIcon(bd[index]);
				if (viewPager_select_index != position) {
					markerList.get(viewPager_select_index).setZIndex(
							viewPager_select_index + 1);
					markerList.get(position).setZIndex(
							markerList.size() + viewPager_select_index + 1);
				}
				viewPager_select_index = position;
				discount = mList.get(viewPager_select_index);
				if (discount.getMeet_needs()) {
					index = 0;
				} else if (discount.getCar_wash_bookable()) {
					index = 1;
				} else
					index = 2;
				Marker marker = markerList.get(viewPager_select_index);
				marker.setIcon(bd_focus[index]);
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(marker
						.getPosition());
				mBaiduMap.animateMapStatus(u);
			}

		}

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
			if (viewPagerContainer != null) {
				viewPagerContainer.invalidate();
			}
		}
	}

	private void startLoadingThread() {
		if (!isLoading) {
			if (!isFirstLoc) {
				isLoading = true;
				if (progressDialog != null) {
					if (!progressDialog.isShowing()) {
						progressDialog.show();
					}
				} else {
					progressDialog = new MyProgressDialog(this,
							R.style.CustomProgressDialog);
					progressDialog.setCancelable(false);
					progressDialog.show();
				}

				ThreadPoolUtils.execute(new Runnable() {
					public void run() {
						reloadData();
					}
				});
			}
		}
	}

	private void reloadData() {
		String url = Const.SERVER_BASE_PATH + Const.MAP_DISCOUNTS;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("price_condition", "" + price_condition);
		request.setValueForKey("time_condition", time_condition.toString());
		request.setValueForKey("bigCar", "" + bigCar);
		request.setValueForKey("longitude", "" + mLongitude);
		request.setValueForKey("latitude", "" + mLatitude);
		JSONObject jsonObject = (JSONObject) request.execForJSONObject(this);

		Message msg = Message.obtain();
		msg.what = REFRESH;
		mHandler.sendMessage(msg);

		Message msgMessage = Message.obtain();
		msgMessage.what = RELOAD_DATA;
		msgMessage.obj = jsonObject;
		mHandler.sendMessage(msgMessage);
	}

	private static class MapSearchHandler extends Handler {
		WeakReference<MapSearchActivity> mActivity;

		MapSearchHandler(MapSearchActivity activity) {
			mActivity = new WeakReference<MapSearchActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			final MapSearchActivity theActivity = mActivity.get();
			if (theActivity.progressDialog != null) {
				theActivity.progressDialog.dismiss();
			}
			switch (msg.what) {
			case REFRESH:
				theActivity.viewPager_select_index = 0;
				theActivity.mViews.clear(); // 滑动的View集合
				theActivity.mList.clear(); // 滑动的图片集合
				theActivity.markerList.clear();// 点的集合
				theActivity.refreshUI();
				break;
			case LOADING_FINISHED:
				// 更新显示的内容
				theActivity.isLoading = false;
				theActivity.refreshUI();
				break;
			case TIME_OUT:
				Toast.makeText(
						theActivity,
						MyApplication.getInstance().getString(
								R.string.net_connect_time_out),
						Toast.LENGTH_SHORT).show();
				break;

			case RELOAD_DATA:
				// 更新显示的内容

				JSONObject jsonObject = (JSONObject) msg.obj;
				try {
					if (jsonObject.optInt("status") == 1) {
						JSONArray jsonArray = jsonObject.optJSONArray("data");
						int length = jsonArray == null ? 0 : jsonArray.length();
						// 实际翻页没有内容，因此页数不能变化

						JSONObject info;
						for (int i = 0; i < length; i++) {
							info = (JSONObject) jsonArray.get(i);
							Discount discount = theActivity
									.createDiscount(info);
							theActivity.mList.add(discount);
						}

					}
				} catch (JSONException e) {
					e.printStackTrace();
					Message msgMessage = Message.obtain();
					msgMessage.what = TIME_OUT;
					this.sendMessage(msgMessage);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Message msgMessage = Message.obtain();
					msgMessage.what = TIME_OUT;
					this.sendMessage(msgMessage);
				} finally {
					Message msgMessage = Message.obtain();
					msgMessage.what = LOADING_FINISHED;
					this.sendMessage(msgMessage);
				}

				break;

			default:
				break;
			}

			super.handleMessage(msg);

		}
	};

	private Discount createDiscount(JSONObject item) throws Exception {
		Discount discount = new Discount();
		discount.setShop_name(item.optString("name"));
		discount.setDiscount_id(item.optInt("discount_id"));
		discount.setUpdated_at(item.optInt("updated_at"));
		discount.setShop_photo(item.optString("shop_photo"));
		discount.setPrice(item.optString("price"));
		discount.setSale_price(item.optString("sale_price"));
		discount.setDistance(item.optInt("distance"));
		discount.setAddress(item.optString("address"));
		discount.setOrder_count(item.optInt("order_count"));
		discount.setTitle(item.optString("title"));
		discount.setShop_id(item.optInt("shop_id"));
		discount.setAverage_grade(item.optInt("average_grade"));
		discount.setLongitude(item.optDouble("longitude"));
		discount.setLatitude(item.optDouble("latitude"));
		discount.setMeet_needs(item.optBoolean("meet_needs"));
		discount.setTelephone_area_code(item.optString("telephone_area_code"));
		discount.setTelephone_number(item.optString("telephone_number"));
		discount.setPriority(item.optInt("priority"));
		discount.setCar_wash_bookable(item.optBoolean("car_wash_bookable"));
		return discount;
	}

	private Marker createMarker(Discount discount, int index) {
		LatLng llA = new LatLng(discount.getLatitude(), discount.getLongitude());
		OverlayOptions ooA;
		int bd_index = 0;
		if (discount.getMeet_needs()) {
			bd_index = 0;
		} else if (discount.getCar_wash_bookable()) {
			bd_index = 1;
		} else
			bd_index = 2;
		if (index == 0) {
			ooA = new MarkerOptions().position(llA).icon(bd_focus[bd_index])
					.zIndex(mList.size() + 1).draggable(false);
		} else {
			ooA = new MarkerOptions().position(llA).icon(bd[bd_index])
					.zIndex(index + 1).draggable(false);
		}
		Marker mMarker = (Marker) (mBaiduMap.addOverlay(ooA));
		return mMarker;
	}

	public void refreshUI() {
		// TODO Auto-generated method stub
		int mark_index = markerList.size();
		int discount_index = mList.size();
		if (viewPager_select_index == 0) {
			// 刷新
			if (mBaiduMap != null)
				mBaiduMap.clear();
			markerList.clear();
			if (position_marker != null) {
				OverlayOptions ooA = new MarkerOptions()
						.position(position_marker.getPosition()).icon(bd[3])
						.zIndex(0).draggable(false);
				position_marker.remove();
				position_marker = (Marker) (mBaiduMap.addOverlay(ooA));
			}
			for (int i = 0; i < discount_index; i++) {
				markerList.add(createMarker(mList.get(i), i));
			}

		} else {
			// 加载更多
			for (int i = mark_index; i < discount_index; i++) {
				markerList.add(createMarker(mList.get(i), i));
			}
		}
		updateViewPage();
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			return;
		}
		mLatitude = result.getLocation().latitude;
		mLongitude = result.getLocation().longitude;
		OverlayOptions ooA = new MarkerOptions().position(result.getLocation())
				.icon(bd[3]).zIndex(0).draggable(true);
		if (position_marker != null) {
			position_marker.remove();
			mMapView.invalidate();
		}
		mBaiduMap.hideInfoWindow();
		position_marker = (Marker) (mBaiduMap.addOverlay(ooA));
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(result
				.getLocation());
		mBaiduMap.animateMapStatus(u);
		startLoadingThread();
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			return;
		}
		default_address = result.getAddress();
		mAutoCompleteTextView.setText(result.getAddress());
	}

	public void startNavi(View v) {
		Discount discount = mList.get(viewPager_select_index);
		// 这里给出一个起终点示例，实际应用中可以通过POI检索、外部POI来源等方式获取起终点坐标
		BNaviPoint startPoint = new BNaviPoint(mLongitude, mLatitude,
				mAutoCompleteTextView.getText().toString(),
				BNaviPoint.CoordinateType.BD09_MC);
		BNaviPoint endPoint = new BNaviPoint(discount.getLongitude(),
				discount.getLatitude(), discount.getAddress(),
				BNaviPoint.CoordinateType.BD09_MC);
		BaiduNaviManager.getInstance().launchNavigator(this, startPoint, // 起点（可指定坐标系）
				endPoint, // 终点（可指定坐标系）
				NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME, // 算路方式
				true, // 真实导航
				BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, // 在离线策略
				new OnStartNavigationListener() { // 跳转监听

					@Override
					public void onJumpToNavigator(Bundle configParams) {
						Intent intent = new Intent(MapSearchActivity.this,
								BNavigatorActivity.class);
						intent.putExtras(configParams);
						startActivity(intent);
					}

					@Override
					public void onJumpToDownloader() {
					}
				});
	}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
			routeOverlay = overlay;
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}

	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult result) {

		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaiduMap);
			routeOverlay = overlay;
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}
	}

	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
			routeOverlay = overlay;
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}
	}

	// 定制RouteOverly
	private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

		public MyDrivingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {

			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {

			return null;
		}
	}

	private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

		public MyWalkingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {

			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {

			return null;
		}
	}

	private class MyTransitRouteOverlay extends TransitRouteOverlay {

		public MyTransitRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {

			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {

			return null;
		}
	}
}
