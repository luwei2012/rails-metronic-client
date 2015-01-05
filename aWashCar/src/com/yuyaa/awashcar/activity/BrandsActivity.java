package com.yuyaa.awashcar.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.koushikdutta.ion.Ion;
import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.entity.Brand;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.ThreadPoolUtils;

public class BrandsActivity extends BaseActivity {

	private GridView mGridView;
	private List<Brand> brands = new ArrayList<Brand>();
	private TextView emptyView;
	private PullToRefreshGridView mPullRefreshGridView;
	private int currentPage = 0;
	private BrandsGridViewAdapter adapter;
	private boolean isLoading = false;
	private boolean has_more = true;
	private BrandsActivityHandler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_brands);
		init();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mPullRefreshGridView.setRefreshing();
			}
		}, 500);
		startLoadingThread();
	}

	private void findViews() {
		mPullRefreshGridView = (PullToRefreshGridView) findViewById(R.id.gridview_brands_activity);
		mGridView = mPullRefreshGridView.getRefreshableView();
		emptyView = new TextView(this);
		emptyView.setGravity(Gravity.CENTER);
		mPullRefreshGridView
				.setOnRefreshListener(new OnRefreshListener2<GridView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<GridView> refreshView) {
						if (!isLoading) {
							currentPage = 0;
							startLoadingThread();
						}
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<GridView> refreshView) {
						if (!isLoading) {
							if (has_more) {
								startLoadingThread();
							} else {
								mPullRefreshGridView.onRefreshComplete();
							}

						} else {
							mPullRefreshGridView.onRefreshComplete();
						}
					}

				});
		emptyView.setText(getString(R.string.no));

		mPullRefreshGridView.setMode(Mode.BOTH);
	}

	private void init() {
		findViews();
		// 创建handler
		mHandler = new BrandsActivityHandler(this);
		adapter = new BrandsGridViewAdapter(this);
		mGridView.setAdapter(adapter);
		mGridView.setOnItemClickListener(new BrandsGridViewItemClickListener());
	}

	public void clickCallback(View view) {
		switch (view.getId()) {
		case R.id.btn_brands_activity_back:
			onBackPressed();
			break;
		default:
			break;
		}
	}

	private class BrandsGridViewAdapter extends BaseAdapter {
		private Context mContext;

		public BrandsGridViewAdapter(Context c) {
			this.mContext = c;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			// return commentListData.size();
			return brands.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return brands.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return brands.get(arg0).getBrand_id();
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.view_item_brand_service_activity_gridview,
						null);
				holder = new ViewHolder();
				holder.brandImg = (ImageView) convertView
						.findViewById(R.id.view_brand);
				holder.brand_name = (TextView) convertView
						.findViewById(R.id.text_brand_name);
				int w = (int) (MyApplication.getInstance().screenWidth - getResources()
						.getDimension(
								R.dimen.margin_brand_service_activity_gridview) * 3) / 2;
				LayoutParams params = (LayoutParams) holder.brandImg
						.getLayoutParams();
				if (params == null) {
					params = new LayoutParams(w, w);
				} else {
					params.width = w;
					params.height = w;
				}
				holder.brandImg.setLayoutParams(params);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			Brand brand = brands.get(position);
			holder.brand_name.setText(brand.getName());
			Ion.with(mContext)
					.load(Const.SERVER_BASE_PATH + brand.getBrand_photo())
					.withBitmap().placeholder(R.drawable.unload_image)
					.error(R.drawable.outofmemory).animateLoad(null)
					.animateIn(null).intoImageView(holder.brandImg);
			return convertView;
		}

		private final class ViewHolder {
			ImageView brandImg;
			TextView brand_name;
		}
	}

	private final class BrandsGridViewItemClickListener implements
			OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(BrandsActivity.this,
					BrandShopActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable("brand", brands.get(position));
			intent.putExtras(bundle);
			startActivity(intent);
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
		String url = Const.SERVER_BASE_PATH + Const.BRANDS;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("page", "" + currentPage);
		request.setValueForKey("page_size", "" + Const.PAGE_SIZE);
		JSONObject jsonObject = (JSONObject) request
				.execForJSONObject(BrandsActivity.this);
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

	private static class BrandsActivityHandler extends Handler {
		WeakReference<BrandsActivity> mActivity;

		BrandsActivityHandler(BrandsActivity activity) {
			mActivity = new WeakReference<BrandsActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			BrandsActivity theActivity = mActivity.get();
			switch (msg.what) {
			case REFRESH:
				// 更新显示的内容
				theActivity.brands.clear();
				theActivity.adapter.notifyDataSetChanged();
				break;

			case LOADING_FINISHED:
				if (msg.arg1 == 1) {
					theActivity.currentPage += 1;
				}
				theActivity.mPullRefreshGridView
						.setEmptyView(theActivity.emptyView);
				theActivity.mPullRefreshGridView.onRefreshComplete();
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
							Brand shopDiscount = theActivity
									.createBrand(tmpObject);
							theActivity.brands.add(shopDiscount);
						}
						if (discount_array.length() >= Const.PAGE_SIZE) {
							msgMessage.arg1 = 1;
						} else {
							theActivity.has_more = false;
							theActivity.mPullRefreshGridView
									.setMode(Mode.PULL_FROM_START);
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

	private Brand createBrand(JSONObject item) throws Exception {
		Brand brand = new Brand();
		brand.setBrand_id(item.optInt("brand_id"));
		brand.setName(item.optString("name"));
		brand.setBrand_photo(item.optString("brand_photo"));
		brand.setFollow_count(item.optInt("follow_count"));
		brand.setShop_count(item.optInt("shop_count"));
		brand.setUpdated_at(item.optInt("updated_at"));
		return brand;
	}

}
