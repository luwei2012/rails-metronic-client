package com.yuyaa.awashcar.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.adapter.ServiceTypeAdapter;
import com.yuyaa.awashcar.entity.Discount;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.ThreadPoolUtils;

public class ServiceTypeActivity extends BaseActivity {

	private TextView textTitle;
	private PopupWindow mPopupWindow;
	private List<String> serviceItemNames = new ArrayList<String>();
	private List<Boolean> serviceItemBooleans = new ArrayList<Boolean>();
	private List<Boolean> serviceItemBooleans_tmp = new ArrayList<Boolean>();
	private ListView mListView;
	private TextView emptyView;
	private LinearLayout type_selected_container;
	private HorizontalScrollView service_type_container;
	private int serviceType;
	private String name;
	private Hashtable<Integer, String> typeList = new Hashtable<Integer, String>();
	private int currentPage = 0;
	private List<Discount> merchantList = new ArrayList<Discount>();
	private ServiceTypeAdapter<Discount> adapter;
	private boolean has_more = true;
	private PullToRefreshListView mPullRefreshListView;
	private boolean isLoading = false;
	private ServiceTypeActivityHandler mHandler;
	private JSONArray selectTypesArray = new JSONArray();
	private PopupWindowAdapter popupWindowAdapter;
	private Boolean type_changed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_service_type);
		init();
		startLoadingTypeThread();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mPullRefreshListView.setRefreshing();
			}
		}, 500);
		currentPage = 0;
		startLoadingThread();
	}

	public void refresh() {
		mPullRefreshListView.setRefreshing();
		currentPage = 0;
		startLoadingThread();
	}

	private void init() {
		findViews();
		Intent it = getIntent();
		serviceType = it.getIntExtra("serviceType", 0);
		name = it.getStringExtra("name");
		textTitle.setText(name);
		int[] textViewResourceId = { R.layout.view_item_discount_list };
		adapter = new ServiceTypeAdapter<Discount>(this, textViewResourceId,
				merchantList, mListView);
		mListView.setAdapter(adapter);
		// 创建handler
		mHandler = new ServiceTypeActivityHandler(this);
		// 添加点击
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int selectedIndex, long id) {
				// TODO Auto-generated method stub
				selectedIndex = selectedIndex - 1;
				selectedIndex = selectedIndex < 0 ? 0 : selectedIndex;
				Discount discount = merchantList.get(selectedIndex);
				Intent intent = new Intent(ServiceTypeActivity.this,
						DiscountDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("discount", discount);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		popupWindowAdapter = new PopupWindowAdapter(this);
	}

	@SuppressLint("InflateParams")
	private void createPopupWindow() {
		if (null == mPopupWindow) {
			View popupView = getLayoutInflater().inflate(
					R.layout.view_popupwindow_service_type_activity, null);
			ListView mListView = (ListView) popupView
					.findViewById(R.id.lv_service_type_popupwindow);
			mListView.setAdapter(popupWindowAdapter);
			mPopupWindow = new PopupWindow(popupView);
			ColorDrawable bg = new ColorDrawable(android.R.color.transparent);
			mPopupWindow.setBackgroundDrawable(bg);
			mPopupWindow.setWidth(Const.VIEW_PGAER_WIDTH / 2);
			mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
			mPopupWindow.setFocusable(true);
			mPopupWindow.setTouchable(true);
			mPopupWindow.setOutsideTouchable(true);
			Button confirmButton = (Button) popupView
					.findViewById(R.id.btn_service_type_activity_popupwindow_ok);
			confirmButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					updateCheckBoxes();
					updateLinearLayout();
					if (type_changed) {
						refresh();
					}
					mPopupWindow.dismiss();
				}
			});
			mPopupWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					// TODO Auto-generated method stub
					for (int i = 0; i < serviceItemBooleans_tmp.size(); i++) {
						serviceItemBooleans_tmp.set(i,
								serviceItemBooleans.get(i));
					}
					type_changed = false;
				}
			});
		}
	}

	@SuppressLint("InflateParams")
	void updateLinearLayout() {
		type_selected_container.removeAllViews();
		Boolean flagBoolean = false;
		int horizontalSpacing = getResources().getDimensionPixelSize(
				R.dimen.padding_white_strip);
		for (int i = 0; i < serviceItemBooleans.size(); i++) {
			if (serviceItemBooleans.get(i) == true) {
				TextView view = (TextView) getLayoutInflater().inflate(
						R.layout.service_type_select_item, null);
				view.setText(serviceItemNames.get(i));
				android.widget.LinearLayout.LayoutParams layoutParams = new android.widget.LinearLayout.LayoutParams(
						android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
						android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(0, horizontalSpacing,
						horizontalSpacing, horizontalSpacing);
				view.setLayoutParams(layoutParams);
				type_selected_container.addView(view);
				flagBoolean = true;
			}
		}
		if (flagBoolean) {
			service_type_container.invalidate();
			service_type_container.setVisibility(View.VISIBLE);
		} else {
			service_type_container.setVisibility(View.GONE);
		}

	}

	private void findViews() {
		textTitle = (TextView) findViewById(R.id.text_service_type_activity_title);
		service_type_container = (HorizontalScrollView) findViewById(R.id.service_type_selected);
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_service_type_activity);
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

		emptyView = new TextView(this);
		emptyView.setGravity(Gravity.CENTER);
		emptyView.setText(getString(R.string.no));
		mListView = mPullRefreshListView.getRefreshableView();
		type_selected_container = (LinearLayout) findViewById(R.id.type_selected_container);
	}

	public void clickCallback(View view) {
		switch (view.getId()) {
		case R.id.btn_service_type_activity_back:
			onBackPressed();
			break;
		case R.id.text_service_type_activity_title:
			if (mPopupWindow != null && mPopupWindow.isShowing()) {
				mPopupWindow.dismiss();
			} else {
				createPopupWindow();
				Rect frame = new Rect();
				getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
				int state_height = frame.top;// 状态栏的高度 --显示电量的区域
				int y = findViewById(R.id.service_type_title).getHeight()
						+ state_height;// title.height为title栏高度
				mPopupWindow.showAtLocation(
						findViewById(R.id.service_type_main),
						Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, y);// 需要指定Gravity，默认情况是center.
			}
			break;
		default:
			break;
		}
	}

	private final class PopupWindowAdapter extends BaseAdapter {
		private Context mContext;

		public PopupWindowAdapter(Context c) {
			mContext = c;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return serviceItemNames.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.view_item_service_type_popup_list, null);
				holder = new ViewHolder();
				holder.serviceItemName = (CheckBox) convertView;
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final int tmp = position;
			holder.serviceItemName
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							// TODO Auto-generated method stub
							serviceItemBooleans_tmp.set(tmp, isChecked);
						}
					});
			holder.serviceItemName.setText(serviceItemNames.get(position));
			holder.serviceItemName.setChecked(serviceItemBooleans_tmp
					.get(position));
			return convertView;
		}

		private final class ViewHolder {
			CheckBox serviceItemName;
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
		String url = Const.SERVER_BASE_PATH + Const.DISCOUNT_TYPES_DISCOUNTS;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		if (selectTypesArray.length() < 1) {
			selectTypesArray.put(serviceType);
		}
		request.setValueForKey("discount_types", selectTypesArray.toString());
		request.setValueForKey("page", "" + currentPage);
		request.setValueForKey("page_size", "" + Const.PAGE_SIZE);
		JSONObject jsonObject = (JSONObject) request
				.execForJSONObject(ServiceTypeActivity.this);
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

	private void updateCheckBoxes() {
		// do something
		selectTypesArray = new JSONArray();
		for (int i = 0; i < serviceItemBooleans.size(); i++) {
			if (serviceItemBooleans.get(i) != serviceItemBooleans_tmp.get(i)) {
				type_changed = true;
			}
			serviceItemBooleans.set(i, serviceItemBooleans_tmp.get(i));
			if (serviceItemBooleans.get(i)) {
				selectTypesArray.put(getType(serviceItemNames.get(i)));
			}
		}
	}

	public int getType(String typeName) {
		for (int key : typeList.keySet()) {
			if (typeList.get(key).equals(typeName)) {
				return key;
			}
		}
		return 0;
	}

	private static class ServiceTypeActivityHandler extends Handler {
		WeakReference<ServiceTypeActivity> mActivity;

		ServiceTypeActivityHandler(ServiceTypeActivity activity) {
			mActivity = new WeakReference<ServiceTypeActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ServiceTypeActivity theActivity = mActivity.get();
			switch (msg.what) {
			case REFRESH:
				// 更新显示的内容
				theActivity.merchantList.clear();
				theActivity.adapter.notifyDataSetChanged();
				break;
			case RELOAD_TYPE:
				// 更新显示的内容
				JSONObject jsonObject = (JSONObject) msg.obj;
				try {
					if (jsonObject.optInt("status") == 1) {
						JSONArray jsonArray = jsonObject
								.optJSONArray("discount_type_array");
						int length = jsonArray.length();
						for (int i = 0; i < length; i++) {
							JSONObject mJsonObject = jsonArray.optJSONObject(i);
							String name = mJsonObject.optString("name");
							theActivity.typeList.put(
									mJsonObject.optInt("discount_type_id"),
									name);
							theActivity.serviceItemNames.add(name);
							theActivity.serviceItemBooleans.add(false);
							theActivity.serviceItemBooleans_tmp.add(false);
						}
					}

				} catch (NullPointerException e) {
					e.printStackTrace();
					Message msgMessage = Message.obtain();
					msgMessage.what = TIME_OUT;
					this.sendMessage(msgMessage);
				} finally {
					Message msgMessage = Message.obtain();
					msgMessage.what = TYPE_LOADING_FINISHED;
					this.sendMessage(msgMessage);
				}
				break;
			case REFRESH_TYPE:
				// 更新显示的内容
				theActivity.typeList.clear();
				theActivity.serviceItemBooleans.clear();
				theActivity.serviceItemBooleans_tmp.clear();
				theActivity.selectTypesArray = new JSONArray();
				break;
			case TYPE_LOADING_FINISHED:
				// 更新显示的内容
				theActivity.isLoading = false;
				if (theActivity.popupWindowAdapter != null) {
					theActivity.popupWindowAdapter.notifyDataSetChanged();
				}
				theActivity.refresh();
				break;
			case LOADING_FINISHED:
				theActivity.mPullRefreshListView
						.setEmptyView(theActivity.emptyView);
				theActivity.mPullRefreshListView.onRefreshComplete();
				if (msg.arg1 == 1) {
					theActivity.currentPage += 1;
				}
				if (theActivity.merchantList.size() == 0) {
					theActivity.emptyView.setText(MyApplication.getInstance()
							.getString(R.string.no));
					theActivity.emptyView.setVisibility(View.VISIBLE);
				} else {
					theActivity.emptyView.setVisibility(View.GONE);
				}
				theActivity.adapter.notifyDataSetChanged();
				theActivity.isLoading = false;
				break;

			case TIME_OUT:
				// 更新显示的内容
				Toast.makeText(
						theActivity,
						MyApplication.getInstance().getString(
								R.string.net_connect_time_out),
						Toast.LENGTH_SHORT).show();
				break;
			case RELOAD_DATA:
				// 更新显示的内容
				JSONObject jsonObj = (JSONObject) msg.obj;
				Message msgMessage = Message.obtain();
				msgMessage.arg1 = 0;
				try {
					if (jsonObj.optInt("status") == 1) {
						// 处理数据
						JSONArray discount_array = jsonObj.optJSONArray("data");
						JSONObject tmpObject = null;
						for (int i = 0; i < discount_array.length(); i++) {
							tmpObject = discount_array.optJSONObject(i);
							Discount shopDiscount = theActivity
									.createDiscount(tmpObject);
							theActivity.merchantList.add(shopDiscount);
						}
						if (discount_array.length() >= Const.PAGE_SIZE) {
							msgMessage.arg1 = 1;
						} else {
							theActivity.has_more = false;
						}
					}

				} catch (Exception e) {
					e.printStackTrace();

				} finally {

					msgMessage.what = LOADING_FINISHED;
					this.sendMessage(msgMessage);
				}

				break;

			default:
				break;
			}

			super.handleMessage(msg);

		}
	}

	private Discount createDiscount(JSONObject item) throws Exception {
		Discount discount = new Discount();
		discount.setShop_name(item.optString("name"));
		discount.setShop_photo(item.optString("shop_photo"));
		discount.setAddress(item.optString("address"));
		discount.setDiscount_id(item.optInt("discount_id"));
		discount.setOrder_count(item.optInt("order_count"));
		discount.setShop_id(item.optInt("shop_id"));
		discount.setAverage_grade(item.optInt("average_grade"));
		discount.setLongitude(item.optDouble("longitude"));
		discount.setLatitude(item.optDouble("latitude"));
		discount.setUpdated_at(item.optInt("updated_at"));
		discount.setMeet_needs(item.optBoolean("meet_needs"));
		discount.setCar_wash_bookable(item.optBoolean("car_wash_bookable"));
		discount.setTitle(item.optString("title"));
		discount.setTelephone_area_code(item.optString("telephone_area_code"));
		discount.setTelephone_number(item.optString("telephone_number"));
		discount.setPrice(item.optString("price"));
		discount.setSale_price(item.optString("sale_price"));
		discount.setFollowed(item.optBoolean("followed"));
		discount.setPraised(item.optBoolean("praised"));
		discount.setFollow_count(item.optInt("follow_count"));
		discount.setContent(item.optString("content"));
		discount.setPraise_count(item.optInt("praise_count"));
		JSONArray jsonArray = item.optJSONArray("discount_photo");
		List<String> imagesList = discount.getDiscount_photo();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject tmpJsonObject = jsonArray.optJSONObject(i);
			imagesList.add(tmpJsonObject.optString("original"));
		}
		jsonArray = item.optJSONArray("discount_types");
		List<String> discount_types = discount.getDiscount_types();
		for (int i = 0; i < jsonArray.length(); i++) {
			discount_types.add(jsonArray.optString(i));
		}
		discount.setDiscount_photo(imagesList);
		discount.setDiscount_types(discount_types);
		return discount;
	}

	private void reloadType() {
		String url = Const.SERVER_BASE_PATH + Const.DISCOUNT_TYPES_SONS;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("id", "" + serviceType);
		JSONObject jsonObject = (JSONObject) request
				.execForJSONObject(ServiceTypeActivity.this);

		Message msg = Message.obtain();
		msg.what = REFRESH_TYPE;
		mHandler.sendMessage(msg);
		Message msgMessage = Message.obtain();
		msgMessage.what = RELOAD_TYPE;
		msgMessage.obj = jsonObject;
		mHandler.sendMessage(msgMessage);
	}

	private void startLoadingTypeThread() {

		if (!isLoading) {
			isLoading = true;
			ThreadPoolUtils.execute(new Runnable() {
				public void run() {
					reloadType();
				}
			});
		}

	}
}
