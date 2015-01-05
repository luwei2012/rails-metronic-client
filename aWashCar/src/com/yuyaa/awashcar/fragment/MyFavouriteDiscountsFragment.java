package com.yuyaa.awashcar.fragment;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.yuyaa.awashcar.activity.DiscountDetailActivity;
import com.yuyaa.awashcar.adapter.DiscountsActivityAdapter;
import com.yuyaa.awashcar.entity.Discount;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.ThreadPoolUtils;

public class MyFavouriteDiscountsFragment extends BaseFragment {
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser && firstIn == 1) {
			refresh();
		}
	}

	public void refresh() {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mPullRefreshListView.setRefreshing();
			}
		}, 500); 
		
		currentPage = 0;
		firstIn = 2;
		startLoadingThread();
	}

	private ListView mListView;
	private TextView emptyView;
	private View contentView;
	private int currentPage = 0;
	private boolean has_more = true;
	private PullToRefreshListView mPullRefreshListView;
	private List<Discount> merchantList = new ArrayList<Discount>();
	private DiscountsActivityAdapter<Discount> adapter;
	private boolean isLoading = false;
	private MyFavouriteDiscountsFragmentHandler mHandler;
	private static MyFavouriteDiscountsFragment myFavouriteDiscountsFragment;

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

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
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

	public static synchronized MyFavouriteDiscountsFragment newInstance() {
		if (null == myFavouriteDiscountsFragment) {
			myFavouriteDiscountsFragment = new MyFavouriteDiscountsFragment();
		}
		return myFavouriteDiscountsFragment;
	}

	public MyFavouriteDiscountsFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		contentView = inflater.inflate(R.layout.fragment_myfavourite_discounts,
				container, false);
		init();
		if (isVisibleToUser) {
			refresh();
		}
		return contentView;
	}

	private void init() {
		findViews();
		// 定位初始化
		int[] textViewResourceId = { R.layout.view_item_discount_list };
		adapter = new DiscountsActivityAdapter<Discount>(
				MyFavouriteDiscountsFragment.this.getActivity(),
				textViewResourceId, merchantList);
		mListView.setAdapter(adapter);
		// 创建handler
		mHandler = new MyFavouriteDiscountsFragmentHandler(
				MyFavouriteDiscountsFragment.this);
		// 添加点击
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int selectedIndex, long id) {
				// TODO Auto-generated method stub
				selectedIndex = selectedIndex - 1;
				selectedIndex = selectedIndex < 0 ? 0 : selectedIndex;
				Discount discount = merchantList.get(selectedIndex);
				Intent intent = new Intent(MyFavouriteDiscountsFragment.this
						.getActivity(), DiscountDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("discount", discount);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	private void findViews() {
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
		String url = Const.SERVER_BASE_PATH + Const.USER_FAV_DISCOUNTS;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("page", "" + currentPage);
		request.setValueForKey("page_size", "" + Const.PAGE_SIZE);
		JSONObject jsonObject = (JSONObject) request
				.execForJSONObject(MyFavouriteDiscountsFragment.this
						.getActivity());
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

	private static class MyFavouriteDiscountsFragmentHandler extends Handler {
		WeakReference<MyFavouriteDiscountsFragment> mActivity;

		MyFavouriteDiscountsFragmentHandler(
				MyFavouriteDiscountsFragment activity) {
			mActivity = new WeakReference<MyFavouriteDiscountsFragment>(
					activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MyFavouriteDiscountsFragment theActivity = mActivity.get();
			switch (msg.what) {
			case REFRESH:
				// 更新显示的内容
				theActivity.merchantList.clear();
				theActivity.adapter.notifyDataSetChanged();
				break;

			case LOADING_FINISHED:
				theActivity.mPullRefreshListView.setEmptyView(theActivity.emptyView);
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
						theActivity.getActivity(),
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
		discount.setTitle(item.optString("title"));
		discount.setLongitude(item.optDouble("longitude"));
		discount.setLatitude(item.optDouble("latitude"));
		discount.setUpdated_at(item.optInt("updated_at"));
		discount.setMeet_needs(item.optBoolean("meet_needs"));
		discount.setCar_wash_bookable(item.optBoolean("car_wash_bookable"));
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

}
