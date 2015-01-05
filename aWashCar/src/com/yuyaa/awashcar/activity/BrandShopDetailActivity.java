package com.yuyaa.awashcar.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.koushikdutta.ion.Ion;
import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.adapter.BrandShopDetailActivityAdapter;
import com.yuyaa.awashcar.entity.Discount;
import com.yuyaa.awashcar.entity.Shop;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.ThreadPoolUtils;
import com.yuyaa.awashcar.widget.StickyLayout;
import com.yuyaa.awashcar.widget.StickyLayout.OnStickyLayoutTouchEventListener;

public class BrandShopDetailActivity extends BaseActivity implements
		OnStickyLayoutTouchEventListener {

	private ListView mListView;
	private TextView emptyView;
	private Shop shop;
	private StickyLayout stickyLayout;
	private int currentPage = 0;
	private boolean has_more = true;
	private PullToRefreshListView mPullRefreshListView;
	private List<Discount> merchantList = new ArrayList<Discount>();
	private BrandShopDetailActivityAdapter<Discount> adapter;
	private boolean isLoading = false;
	private BrandShopDetailActivityHandler mHandler;
	ImageView followed;
	RelativeLayout brand_detail_shop_title;
	ImageView shop_photo;
	TextView shop_name, shop_name2, follow_count, order_count, average_grade;

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
		setContentView(R.layout.activity_brand_detail);
		init();
		refresh();
	}

	private void init() {
		findViews();
		Intent intent = getIntent();
		shop = intent.getExtras().getParcelable("shop");
		Ion.with(this).load(Const.SERVER_BASE_PATH + shop.getShop_photo())
				.withBitmap().placeholder(R.drawable.unload_image)
				.error(R.drawable.outofmemory).animateLoad(null)
				.animateIn(null).intoImageView(shop_photo);
		shop_name.setText(shop.getName());
		shop_name2.setText(shop.getName());
		follow_count.setText(shop.getFollow_count() + "");
		order_count.setText(shop.getOrder_count() + "");
		average_grade.setText(shop.getAverage_grade() + "");
		int[] textViewResourceId = { R.layout.view_item_shop_discount_list };
		adapter = new BrandShopDetailActivityAdapter<Discount>(this,
				textViewResourceId, merchantList, mListView);
		mListView.setAdapter(adapter);
		// 创建handler
		mHandler = new BrandShopDetailActivityHandler(this);
		// 添加点击
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int selectedIndex, long id) {
				// TODO Auto-generated method stub
				selectedIndex = selectedIndex - 1;
				selectedIndex = selectedIndex < 0 ? 0 : selectedIndex;
				Discount discount = merchantList.get(selectedIndex);
				Intent intent = new Intent(BrandShopDetailActivity.this,
						DiscountDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("discount", discount);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		followed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (MyApplication.getInstance().getStringGlobalData("session",
						null) != null) {
					final Boolean flagBoolean;
					if (shop.getFollowed()) {
						flagBoolean = false;
						((ImageView) v)
								.setImageResource(R.drawable.img_drawer_list_item_favourite_normal);
					} else {
						flagBoolean = true;
						((ImageView) v)
								.setImageResource(R.drawable.img_drawer_list_item_favourite_followed);
					}
					ThreadPoolUtils.execute(new Runnable() {
						public void run() {
							httpFollow(flagBoolean);
						}
					});
				} else {
					Toast toast = Toast.makeText(
							BrandShopDetailActivity.this,
							MyApplication.getInstance().getString(
									R.string.please_log), Toast.LENGTH_SHORT);
					toast.setMargin(0f, 0.05f);
					toast.show();
					Intent intent = new Intent(BrandShopDetailActivity.this,
							LoginActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					BrandShopDetailActivity.this.startActivity(intent);
				}
			}
		});
		refreshUI();
	}

	private void httpFollow(Boolean flag) {
		String url = Const.SERVER_BASE_PATH + Const.FOLLOW_SHOP;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("flag", "" + flag);
		request.setValueForKey("shop_id", "" + shop.getShop_id());
		JSONObject json = (JSONObject) request
				.execForJSONObject(BrandShopDetailActivity.this);
		Message msg = Message.obtain(mHandler, FOLLOW_END, 0, 0, json);
		mHandler.sendMessage(msg);
	}

	private void findViews() {
		stickyLayout = (StickyLayout) findViewById(R.id.sticky_layout);
		brand_detail_shop_title = (RelativeLayout) findViewById(R.id.brand_detail_shop_title);
		stickyLayout.offset = com.yuyaa.awashcar.util.Util.dip2px(this, 40);
		stickyLayout.setStickyLayoutTouchEventListener(this);
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
		followed = (ImageView) findViewById(R.id.followed);
		shop_photo = (ImageView) findViewById(R.id.shop_photo);
		LayoutParams layoutParams = (LayoutParams) shop_photo.getLayoutParams();
		layoutParams.width = Const.VIEW_PGAER_WIDTH;
		layoutParams.height = Const.VIEW_PGAER_HEIGHT;
		shop_name = (TextView) findViewById(R.id.shop_name);
		shop_name2 = (TextView) findViewById(R.id.shop_name2);
		follow_count = (TextView) findViewById(R.id.follow_count);
		order_count = (TextView) findViewById(R.id.order_count);
		average_grade = (TextView) findViewById(R.id.average_grade);
	}

	public void clickCallback(View view) {
		switch (view.getId()) {
		case R.id.btn_brand_detail_activity_back:
			finish();
			break;
		default:
			break;
		}
	}

	private static class BrandShopDetailActivityHandler extends Handler {
		WeakReference<BrandShopDetailActivity> mActivity;

		BrandShopDetailActivityHandler(BrandShopDetailActivity activity) {
			mActivity = new WeakReference<BrandShopDetailActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			BrandShopDetailActivity theActivity = mActivity.get();
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
			case FOLLOW_END:
				// 更新显示的内容
				try {
					JSONObject jsonObject = (JSONObject) msg.obj;
					if (jsonObject.optInt("status") == 1) {
						theActivity.shop.setFollowed(jsonObject
								.optBoolean("followed"));
						theActivity.shop.setFollow_count(jsonObject
								.optInt("follow_count"));
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					theActivity.refreshUI();
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
		discount.setTitle(item.optString("title"));
		discount.setUpdated_at(item.optInt("updated_at"));
		discount.setCar_wash_bookable(item.optBoolean("car_wash_bookable"));
		discount.setMeet_needs(item.optBoolean("meet_needs"));
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

	public void refreshUI() {
		// TODO Auto-generated method stub
		if (shop.getFollowed()) {
			followed.setImageResource(R.drawable.img_drawer_list_item_favourite_followed);
		} else {
			followed.setImageResource(R.drawable.img_drawer_list_item_favourite_normal);
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
		String url = Const.SERVER_BASE_PATH + Const.SHOP_DISCOUNTS;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("page", "" + currentPage);
		request.setValueForKey("shop_id", "" + shop.getShop_id());
		request.setValueForKey("page_size", "" + Const.PAGE_SIZE);
		JSONObject jsonObject = (JSONObject) request
				.execForJSONObject(BrandShopDetailActivity.this);
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
	public boolean giveUpTouchEvent(MotionEvent event) {
		if (mListView.getFirstVisiblePosition() == 0) {
			View view = mListView.getChildAt(0);
			if (view != null && view.getTop() >= 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onHeadChanged() {
		// TODO Auto-generated method stub
		int originalHeight = stickyLayout.mOriginalHeaderHeight;
		int currentHeight = stickyLayout.mHeaderHeight;
		int offset = stickyLayout.offset;
		if (originalHeight > offset) {
			int alpha;
			if (currentHeight >= originalHeight) {
				alpha = 0;
			} else {
				alpha = 255 - (int) (255 * (currentHeight - offset)
						/ (originalHeight - offset) + 0.5f);
				if (alpha >= 255) {
					alpha = 255;
				}
			}
			int color = Color.argb(alpha, 245, 243, 239);
			brand_detail_shop_title.setBackgroundColor(color);
		}
		if (stickyLayout.mStatus == StickyLayout.STATUS_COLLAPSED) {
			shop_name2.setVisibility(View.VISIBLE);
		} else {
			shop_name2.setVisibility(View.INVISIBLE);
		}
	}
}
