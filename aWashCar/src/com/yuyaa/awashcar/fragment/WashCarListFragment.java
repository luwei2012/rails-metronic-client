package com.yuyaa.awashcar.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.activity.CarWashDiscountDetailActivity;
import com.yuyaa.awashcar.activity.MapSearchActivity;
import com.yuyaa.awashcar.adapter.WashCarListFragmentAdapter;
import com.yuyaa.awashcar.entity.Discount;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.ThreadPoolUtils;

public class WashCarListFragment extends BaseFragment implements
		OnGetGeoCoderResultListener, OnGetSuggestionResultListener {

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
		super.onStart();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		mLocationClient.stop();
		super.onStop();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		mSearch.destroy();
		mSuggestionSearch.destroy();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	public void refresh() {
		if (firstIn == 1) {
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					mPullRefreshListView.setRefreshing();
				}
			}, 500);
		} else {
			mPullRefreshListView.setRefreshing();
		}
		currentPage = 0;
		firstIn = 2;
		startLoadingThread();
	}

	private Context mContext;
	private View contentView;
	private SuggestionSearch mSuggestionSearch;
	GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	private AutoCompleteTextView keyWorldsView;
	private ArrayAdapter<String> sugAdapter;
	private static WashCarListFragment washCarListFragment;
	private PullToRefreshListView mPullRefreshListView;
	private ListView mListView;
	private TextView emptyView;
	private List<Discount> merchantList = new ArrayList<Discount>();
	private WashCarListFragmentAdapter<Discount> adapter;
	private PopupWindow mTimePopupWindow;
	private PopupWindow mPricePopupWindow;
	private PopupWindow mCarTypePopupWindow;
	private WashCarListFragmentHandler mHandler;
	private boolean has_more = true;
	private int currentPage = 0;
	private LocationClient mLocationClient;
	private MyLocationListenner mBdLocationListener;
	private boolean isLoading = false;
	private int price_condition;
	private JSONObject time_condition;
	private Boolean bigCar;
	int[] colors = { Color.rgb(39, 39, 50), Color.rgb(57, 57, 85),
			Color.rgb(49, 44, 59), Color.rgb(41, 35, 42) };

	private Double mLongitude;
	private RelativeLayout search_title;
	private LinearLayout layout_filter_btns;
	View timePopupView;
	View pricePopupView;
	View typePopupView;
	RadioGroup time_zone_index;
	private Double mLatitude;
	private Boolean[][] time_zone = {
			{ false, false, false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false } };
	private Boolean[][] time_zone_tmp = {
			{ false, false, false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false } };
	private int time_zone_select_index;// 今天还是明天
	private long today;
	private long tomorrow;
	private String default_address;

	public static WashCarListFragment newInstance() {
		if (null == washCarListFragment) {
			washCarListFragment = new WashCarListFragment();
		}
		return washCarListFragment;
	}

	public WashCarListFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		contentView = inflater.inflate(R.layout.fragment_wash_car_list,
				container, false);
		init();
		if (isVisibleToUser) {
			refresh();
		}
		return contentView;
	}

	private void init() {
		findViews();
		GradientDrawable drawable = new GradientDrawable(
				Orientation.LEFT_RIGHT, colors);
		drawable.setShape(GradientDrawable.RECTANGLE);
		search_title.setBackgroundDrawable(drawable);
		drawable = new GradientDrawable(Orientation.LEFT_RIGHT, colors);
		drawable.setShape(GradientDrawable.RECTANGLE);
		layout_filter_btns.setBackgroundDrawable(drawable);
		// 定位初始化
		mLocationClient = new LocationClient(getActivity()
				.getApplicationContext());
		mBdLocationListener = new MyLocationListenner();
		mLocationClient.registerLocationListener(mBdLocationListener);
		LocationClientOption option = new LocationClientOption();
		option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度，默认值gcj02，如果后面要在百度地图上用此定位，使用"bd09ll"更精确
		option.setOpenGps(true); // 打开gps
		option.setScanSpan(1000); // 设置发起定位请求的间隔时间为1000ms
		option.setIsNeedAddress(true);
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
		mLocationClient.start();
		keyWorldsView.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int actionId,
					KeyEvent arg2) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					String address = keyWorldsView.getText().toString();
					if (TextUtils.isEmpty(address)) {
						keyWorldsView.setText(default_address);
					} else {
						mSearch.geocode(new GeoCodeOption().city("成都").address(
								keyWorldsView.getText().toString()));
					}
					// 搜索
					// Geo搜索
					((InputMethodManager) mContext
							.getSystemService(Context.INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(
									keyWorldsView.getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
					return true;
				}
				return false;
			}
		});

	}

	private void findViews() {
		layout_filter_btns = (LinearLayout) contentView
				.findViewById(R.id.layout_filter_btns);
		search_title = (RelativeLayout) contentView
				.findViewById(R.id.search_title);
		keyWorldsView = (AutoCompleteTextView) contentView
				.findViewById(R.id.edit_wash_car_map_fragment_input_address);

		mPullRefreshListView = (PullToRefreshListView) contentView
				.findViewById(R.id.lv_wash_car_list_fragment);
		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						String label = DateUtils.formatDateTime(MyApplication
								.getInstance().getApplicationContext(), System
								.currentTimeMillis(),
								DateUtils.FORMAT_SHOW_TIME
										| DateUtils.FORMAT_SHOW_DATE
										| DateUtils.FORMAT_ABBREV_ALL);
						refreshView.getLoadingLayoutProxy()
								.setLastUpdatedLabel(label);
						currentPage = 0;
						startLoadingThread();
					}
				});

		// Add an end-of-list listener
		mPullRefreshListView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

					@Override
					public void onLastItemVisible() {
						if (!isLoading) {
							if (has_more) {
								startLoadingThread();
							}
						}
					}
				});

		emptyView = new TextView(getActivity());
		emptyView.setGravity(Gravity.CENTER);
		emptyView.setText(getString(R.string.no));

		mListView = mPullRefreshListView.getRefreshableView();
		int[] textViewResourceId = { R.layout.view_item_merchant_list };
		adapter = new WashCarListFragmentAdapter<Discount>(getActivity(),
				textViewResourceId, merchantList);
		mListView.setAdapter(adapter);
		// 创建handler
		mHandler = new WashCarListFragmentHandler(this);
		// 添加点击
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View what,
					int selectedIndex, long arg3) {
				selectedIndex = selectedIndex - 1;
				selectedIndex = selectedIndex < 0 ? 0 : selectedIndex;
				Discount discount = merchantList.get(selectedIndex);
				Intent intent = new Intent(WashCarListFragment.this
						.getActivity(), CarWashDiscountDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("discount", discount);
				intent.putExtras(bundle);
				startActivity(intent);

			}
		});

		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this);
		sugAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_dropdown_item_1line);
		keyWorldsView.setAdapter(sugAdapter);
		/**
		 * 当输入关键字变化时，动态更新建议列表
		 */
		keyWorldsView.addTextChangedListener(new TextWatcher() {

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
				/**
				 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
				 */
				mSuggestionSearch
						.requestSuggestion((new SuggestionSearchOption())
								.keyword(cs.toString()).city("成都"));
			}
		});
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser && firstIn == 1) {
			refresh();
		}
	}

	private void startLoadingThread() {
		if (!isLoading) {
			if (!MyApplication.getInstance().isFirstLoc) {
				isLoading = true;
				ThreadPoolUtils.execute(new Runnable() {
					public void run() {
						reloadData();
					}
				});
			}
		}

	}

	private void reloadData() {
		String url = Const.SERVER_BASE_PATH + Const.DISCOUNTS;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("price_condition", "" + price_condition);
		request.setValueForKey("time_condition", time_condition.toString());
		request.setValueForKey("bigCar", "" + bigCar);
		request.setValueForKey("longitude", "" + mLongitude);
		request.setValueForKey("latitude", "" + mLatitude);
		request.setValueForKey("page", "" + currentPage);
		request.setValueForKey("page_size", "" + Const.PAGE_SIZE);
		JSONObject jsonObject = (JSONObject) request
				.execForJSONObject(WashCarListFragment.this.getActivity());
		if (currentPage == 0) {
			Message msg = Message.obtain();
			msg.what = REFRESH;
			mHandler.sendMessage(msg);
		}
		Message msgMessage = Message.obtain();
		msgMessage.what = RELOAD_DATA;
		msgMessage.obj = jsonObject;
		mHandler.sendMessage(msgMessage);

	}

	@Override
	public void xmlClickMethod(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.btn_filter_time_wash_car_list_fragment:
			if (mTimePopupWindow != null && mTimePopupWindow.isShowing()) {
				mTimePopupWindow.dismiss();
			} else {
				createTimePopupWindow(mContext,
						R.layout.view_popupwindow_select_time);
				mTimePopupWindow.showAsDropDown(v);
			}
			break;
		case R.id.btn_filter_price_wash_car_list_fragment:
			if (mPricePopupWindow != null && mPricePopupWindow.isShowing()) {
				mPricePopupWindow.dismiss();
			} else {
				createPricePopupWindow(mContext,
						R.layout.view_popupwindow_select_price);
				mPricePopupWindow.showAsDropDown(v);
			}
			break;
		case R.id.btn_filter_model_wash_car_list_fragment:
			if (mCarTypePopupWindow != null && mCarTypePopupWindow.isShowing()) {
				mCarTypePopupWindow.dismiss();
			} else {
				createCarTypePopupWindow(mContext,
						R.layout.view_popupwindow_select_model);
				mCarTypePopupWindow.showAsDropDown(v);
			}
			break;
		case R.id.btn_filter_map_wash_car_list_fragment:
			if (mCarTypePopupWindow != null && mCarTypePopupWindow.isShowing()) {
				mCarTypePopupWindow.dismiss();
			}
			if (mPricePopupWindow != null && mPricePopupWindow.isShowing()) {
				mPricePopupWindow.dismiss();
			}
			if (mTimePopupWindow != null && mTimePopupWindow.isShowing()) {
				mTimePopupWindow.dismiss();
			}
			Intent it = new Intent(mContext, MapSearchActivity.class);
			startActivity(it);
			break;
		default:
			break;
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
						getActivity().getPackageName());
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
					refresh();
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
				}
			});

		}
		initTimePopupWindow();
		updateCheckBoxes(mTimePopupWindow.getContentView());
	}

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
								getActivity().getPackageName());
						CheckBox checkBox = (CheckBox) timePopupView
								.findViewById(resID1);
						checkBox.setClickable(false);
						checkBox.setEnabled(false);
					}
				}
			}

		}

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
							getActivity().getPackageName());
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
							getActivity().getPackageName());
					final CheckBox checkBox = (CheckBox) timePopupView
							.findViewById(resID1);
					checkBox.setClickable(true);
					checkBox.setEnabled(true);
				}
			}
			updateCheckBoxes(timePopupView);
		}

	};

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
					getActivity().getPackageName());
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
							refresh();
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
							refresh();
							mCarTypePopupWindow.dismiss();
						}
					});
		}
	}

	private static class WashCarListFragmentHandler extends Handler {
		WeakReference<WashCarListFragment> mActivity;

		WashCarListFragmentHandler(WashCarListFragment activity) {
			mActivity = new WeakReference<WashCarListFragment>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			final WashCarListFragment theActivity = mActivity.get();
			if (msg.what == REFRESH) {
				theActivity.merchantList.clear();
				theActivity.adapter.notifyDataSetChanged();
			} else if (msg.what == TIME_OUT) {
				if (theActivity.isVisible()) {
					Toast toast = Toast.makeText(
							theActivity.getActivity(),
							MyApplication.getInstance().getString(
									R.string.net_abnormal), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			} else if (msg.what == LOADING_FINISHED) {
				theActivity.mPullRefreshListView
						.setEmptyView(theActivity.emptyView);
				theActivity.mPullRefreshListView.onRefreshComplete();
				if (msg.arg1 == 1) {
					theActivity.currentPage += 1;
				}
				theActivity.adapter.notifyDataSetChanged();
				theActivity.isLoading = false;
			} else if (msg.what == RELOAD_DATA) {
				JSONObject jsonObject = (JSONObject) msg.obj;
				Message msgMessage = Message.obtain();
				msgMessage.arg1 = 0;
				try {
					if (jsonObject.optInt("status") == 1) {
						JSONArray jsonArray = jsonObject.optJSONArray("data");
						int length = jsonArray == null ? 0 : jsonArray.length();
						// 实际翻页没有内容，因此页数不能变化
						if (length >= Const.PAGE_SIZE) {
							msgMessage.arg1 = 1;
						} else {
							theActivity.has_more = false;
						}
						JSONObject info;
						for (int i = 0; i < length; i++) {
							info = (JSONObject) jsonArray.get(i);
							Discount discount = theActivity
									.createDiscount(info);
							theActivity.merchantList.add(discount);
						}

					}
				} catch (JSONException e) {
					e.printStackTrace();
					msgMessage.what = TIME_OUT;
					this.sendMessage(msgMessage);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					msgMessage.what = TIME_OUT;
					this.sendMessage(msgMessage);
				} finally {
					msgMessage.what = LOADING_FINISHED;
					this.sendMessage(msgMessage);
				}

			}
			super.handleMessage(msg);

		}
	}

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
		discount.setShop_id(item.optInt("shop_id"));
		discount.setAverage_grade(item.optInt("average_grade"));
		discount.setTitle(item.optString("title"));
		discount.setLongitude(item.optDouble("longitude"));
		discount.setLatitude(item.optDouble("latitude"));
		discount.setMeet_needs(item.optBoolean("meet_needs"));
		discount.setCar_wash_bookable(item.optBoolean("car_wash_bookable"));
		discount.setTelephone_area_code(item.optString("telephone_area_code"));
		discount.setTelephone_number(item.optString("telephone_number"));
		discount.setFollowed(item.optBoolean("followed"));
		discount.setPraised(item.optBoolean("praised"));
		discount.setTitle(item.optString("title"));
		discount.setContent(item.optString("content"));
		JSONArray jsonArray = item.optJSONArray("discount_photo");
		List<String> imagesList = discount.getDiscount_photo();
		imagesList.clear();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject tmpJsonObject = jsonArray.optJSONObject(i);
			imagesList.add(tmpJsonObject.optString("original"));
		}
		return discount;
	}

	@Override
	public void onGetSuggestionResult(SuggestionResult res) {
		// TODO Auto-generated method stub
		if (res == null || res.getAllSuggestions() == null) {
			return;
		}
		sugAdapter.clear();
		for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
			sugAdapter.add(info.key);
		}
		sugAdapter.notifyDataSetChanged();
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			return;
		}
		mLatitude = result.getLocation().latitude;
		mLongitude = result.getLocation().longitude;
		refresh();
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			return;
		}
		default_address = result.getAddress();
		keyWorldsView.setText(result.getAddress());

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
			if (mLatitude == 0.0) {
				mLatitude = location.getLatitude();
				mLongitude = location.getLongitude();
			}
			MyApplication.getInstance().user.mCurrentLantitude = location
					.getLatitude();
			MyApplication.getInstance().user.mCurrentLongitude = location
					.getLongitude();
			if (MyApplication.getInstance().isFirstLoc) {
				MyApplication.getInstance().isFirstLoc = false;
				if (isVisibleToUser) {
					refresh();
				}
			}
			mLocationClient.stop();
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}
}
