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
import com.yuyaa.awashcar.activity.OrderDetailActivity;
import com.yuyaa.awashcar.adapter.OrdersFragmentAdapter;
import com.yuyaa.awashcar.entity.Order;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.ThreadPoolUtils;

public class MyOrderDiscountsEndFragment extends BaseFragment {
	private ListView mListView;
	private TextView emptyView;
	private View contentView;
	private int currentPage = 0;
	private List<Order> merchantList = new ArrayList<Order>();
	private OrdersFragmentAdapter<Order> adapter;
	private PullToRefreshListView mPullRefreshListView;
	private boolean isLoading = false;
	private boolean has_more = true;
	private MyOrderDiscountsEndFragmentHandler mHandler;
	private static MyOrderDiscountsEndFragment myFavouriteDiscountsFragment;

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

	public static synchronized MyOrderDiscountsEndFragment newInstance() {
		if (null == myFavouriteDiscountsFragment) {
			myFavouriteDiscountsFragment = new MyOrderDiscountsEndFragment();
		}
		return myFavouriteDiscountsFragment;
	}

	public MyOrderDiscountsEndFragment() {
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
		contentView = inflater.inflate(R.layout.fragment_myorder_discounts_all,
				container, false);
		init();
		if (isVisibleToUser) {
			refresh();
		}
		return contentView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser && firstIn == 1) {
			refresh();

		}
	}

	private void init() {
		findViews();
		// 定位初始化
		int[] textViewResourceId = { R.layout.view_item_order_list };
		adapter = new OrdersFragmentAdapter<Order>(
				MyOrderDiscountsEndFragment.this.getActivity(),
				textViewResourceId, merchantList);
		mListView.setAdapter(adapter);
		// 创建handler
		mHandler = new MyOrderDiscountsEndFragmentHandler(
				MyOrderDiscountsEndFragment.this);
		// 添加点击
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int selectedIndex, long id) {
				// TODO Auto-generated method stub
				selectedIndex = selectedIndex - 1;
				selectedIndex = selectedIndex < 0 ? 0 : selectedIndex;
				Order discount = merchantList.get(selectedIndex);
				Intent intent = new Intent(MyOrderDiscountsEndFragment.this
						.getActivity(), OrderDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("order", discount);
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
		String url = Const.SERVER_BASE_PATH + Const.MY_ORDERS;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("page", "" + currentPage);
		request.setValueForKey("page_size", "" + Const.PAGE_SIZE);
		request.setValueForKey("status", "" + 2);
		JSONObject jsonObject = (JSONObject) request
				.execForJSONObject(MyOrderDiscountsEndFragment.this
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
	 

	private static class MyOrderDiscountsEndFragmentHandler extends Handler {
		WeakReference<MyOrderDiscountsEndFragment> mActivity;

		MyOrderDiscountsEndFragmentHandler(MyOrderDiscountsEndFragment activity) {
			mActivity = new WeakReference<MyOrderDiscountsEndFragment>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MyOrderDiscountsEndFragment theActivity = mActivity.get();
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
							Order order = theActivity.createOrder(tmpObject);
							theActivity.merchantList.add(order);
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

	private Order createOrder(JSONObject item) throws Exception {
		Order order = new Order();
		order.setOrder_id(item.optInt("order_id"));
		order.setOrder_number(item.optString("order_number"));
		order.setStatus(item.optInt("status"));
		order.setPrice(item.optString("price"));
		order.setRemark(item.optString("remark"));
		order.setUpdated_at(item.optInt("updated_at"));
		order.setDiscount_id(item.optInt("discount_id"));
		order.setShop_name(item.optString("shop_name"));
		order.setDiscount_photo(item.optString("discount_photo"));
		order.setTitle(item.optString("title"));
		order.setIntegral(item.optInt("integral"));
		order.setAverage_grade(item.optInt("average_grade"));
		order.setIs_graded(item.optBoolean("is_graded"));
		order.setBook_time(item.optInt("book_time"));
		order.setIs_car_wash(item.optBoolean("is_car_wash"));
		return order;
	}

}
