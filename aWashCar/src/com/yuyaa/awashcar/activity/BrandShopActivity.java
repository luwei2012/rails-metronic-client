package com.yuyaa.awashcar.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.adapter.BrandShopActivityAdapter;
import com.yuyaa.awashcar.entity.Brand;
import com.yuyaa.awashcar.entity.Shop;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.ThreadPoolUtils;

public class BrandShopActivity extends BaseActivity {

	private ListView mListView;
	private TextView emptyView, text_service_type_activity_title;
	private int currentPage = 0;
	private boolean has_more = true;
	private PullToRefreshListView mPullRefreshListView;
	private List<Shop> merchantList = new ArrayList<Shop>();
	private BrandShopActivityAdapter<Shop> adapter;
	private boolean isLoading = false;
	private BrandShopActivityHandler mHandler;
	private Brand brand;

	public void refresh() {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mPullRefreshListView.setRefreshing();
			}
		}, 500);
		currentPage = 0;
		startLoadingThread();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_brand_shop);
		init();
		refresh();
	}

	private void init() {
		findViews();
		Intent intent = getIntent();
		brand = intent.getExtras().getParcelable("brand");
		text_service_type_activity_title.setText(brand.getName());
		int[] textViewResourceId = { R.layout.view_item_shop_list };
		adapter = new BrandShopActivityAdapter<Shop>(this, textViewResourceId,
				merchantList, mListView);
		mListView.setAdapter(adapter);
		// 创建handler
		mHandler = new BrandShopActivityHandler(this);
		// 添加点击
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int selectedIndex, long id) {
				// TODO Auto-generated method stub
				selectedIndex = selectedIndex - 1;
				selectedIndex = selectedIndex < 0 ? 0 : selectedIndex;
				Shop shop = merchantList.get(selectedIndex);
				Intent intent = new Intent(BrandShopActivity.this,
						BrandShopDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("shop", shop);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	private void findViews() {
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
		text_service_type_activity_title = (TextView) findViewById(R.id.text_service_type_activity_title);
	}

	public void clickCallback(View view) {
		switch (view.getId()) {
		case R.id.btn_service_type_activity_back:
			onBackPressed();
			break;

		default:
			break;
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
		String url = Const.SERVER_BASE_PATH + Const.BRAND_SHOPS;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("page", "" + currentPage);
		request.setValueForKey("page_size", "" + Const.PAGE_SIZE);
		request.setValueForKey("brand_id", "" + brand.getBrand_id());
		request.setValueForKey(Const.KEY_LONGITUDE,
				"" + MyApplication.getInstance().user.mCurrentLongitude);
		request.setValueForKey(Const.KEY_LATITUDE,
				"" + MyApplication.getInstance().user.mCurrentLantitude);
		JSONObject jsonObject = (JSONObject) request
				.execForJSONObject(BrandShopActivity.this);
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

	private static class BrandShopActivityHandler extends Handler {
		WeakReference<BrandShopActivity> mActivity;

		BrandShopActivityHandler(BrandShopActivity activity) {
			mActivity = new WeakReference<BrandShopActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			BrandShopActivity theActivity = mActivity.get();
			switch (msg.what) {
			case REFRESH:
				// 更新显示的内容
				theActivity.merchantList.clear();
				theActivity.adapter.notifyDataSetChanged();
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
							Shop shopShop = theActivity.createShop(tmpObject);
							theActivity.merchantList.add(shopShop);
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

	private Shop createShop(JSONObject item) throws Exception {
		Shop shop = new Shop();
		shop.setShop_id(item.optInt("shop_id"));
		shop.setShop_photo(item.optString("shop_photo"));
		shop.setName(item.optString("name"));
		shop.setAddress(item.optString("address"));
		shop.setOrder_count(item.optInt("order_count"));
		shop.setLongitude(item.optDouble("longitude"));
		shop.setLatitude(item.optDouble("latitude"));
		shop.setFollowed(item.optBoolean("followed"));
		shop.setUpdated_at(item.optInt("updated_at"));
		shop.setAverage_grade(item.optInt("average_grade"));
		shop.setDistance(item.optInt("distance"));
		shop.setFollow_count(item.optInt("follow_count"));
		return shop;
	}

}
