package com.yuyaa.awashcar.fragment;

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
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.activity.BrandShopActivity;
import com.yuyaa.awashcar.activity.BrandsActivity;
import com.yuyaa.awashcar.activity.DiscountDetailActivity;
import com.yuyaa.awashcar.activity.DiscountsActivity;
import com.yuyaa.awashcar.activity.ServiceTypeActivity;
import com.yuyaa.awashcar.adapter.ViewPageAdapter;
import com.yuyaa.awashcar.entity.Brand;
import com.yuyaa.awashcar.entity.Discount;
import com.yuyaa.awashcar.entity.DiscountType;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.ThreadPoolUtils;
import com.yuyaa.awashcar.widget.MyViewPager;

public class BrandServiceFragment extends BaseFragment {
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (viewPager != null) {
			viewPager.stopAutoScroll();
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (viewPager != null) {
			viewPager.startAutoScroll();
		}
	}

	private Context mContext;
	private static BrandServiceFragment mBrandServiceFragment;
	private View contentView;
	private BrandServiceHandler mHandler;
	private boolean isLoading = false;
	private MyViewPager viewPager;
	private RelativeLayout recommendLayout;
	private RelativeLayout brandLayout;
	private ViewPageAdapter viewPageAdapter;
	private List<View> imageViews; // 滑动的图片集合
	private List<Discount> recommendList;
	private List<Brand> brandList;
	private List<Discount> advertisementList;
	private List<DiscountType> discountTypeList;
	private List<View> dots;
	private RelativeLayout dotsLayout;
	private HorizontalScrollView brand_service_discount_types_container;
	private LinearLayout discount_types_container;
	private LayoutInflater layoutInflater;
	/** column number **/
	public static final int COLUMNS = 2;
	/** imageView default height **/
	public static final int IMAGEVIEW_DEFAULT_HEIGHT = 400;

	public static BrandServiceFragment newInstance() {
		if (mBrandServiceFragment == null) {
			mBrandServiceFragment = new BrandServiceFragment();
		}
		return mBrandServiceFragment;
	}

	public BrandServiceFragment() {
		// Required empty public constructor
		super();
		imageViews = new ArrayList<View>();
		dots = new ArrayList<View>();
		recommendList = new ArrayList<Discount>();
		brandList = new ArrayList<Brand>();
		advertisementList = new ArrayList<Discount>();
		discountTypeList = new ArrayList<DiscountType>();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			if (viewPager != null) {
				viewPager.startAutoScroll();
			}
		} else {
			if (viewPager != null) {
				viewPager.stopAutoScroll();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		layoutInflater = inflater;
		contentView = inflater.inflate(R.layout.activity_brand_service,
				container, false);
		findViews();
		init();
		startLoadingThread();
		return contentView;
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
		String url = Const.SERVER_BASE_PATH + Const.BRAND;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		JSONObject jsonObj = null;
		jsonObj = (JSONObject) request
				.execForJSONObject(BrandServiceFragment.this.getActivity());
		Message msgMessage = Message.obtain();
		msgMessage.what = RELOAD_DATA;
		msgMessage.obj = jsonObj;
		mHandler.sendMessage(msgMessage);
	}

	private void init() {
		viewPageAdapter = new ViewPageAdapter(imageViews);
		viewPager.setAdapter(viewPageAdapter);// 设置填充ViewPager页面的适配器
		// 设置一个监听器，当ViewPager中的页面改变时调用
		viewPager.setOnPageChangeListener(new MyPageChangeListener(dots));
		mHandler = new BrandServiceHandler(this);
	}

	public void findViews() {
		viewPager = (MyViewPager) contentView
				.findViewById(R.id.brand_advertisement_viewpager);
		viewPager.height = Const.VIEW_PGAER_HEIGHT;
		recommendLayout = (RelativeLayout) contentView
				.findViewById(R.id.brand_recomment_layout);
		brandLayout = (RelativeLayout) contentView
				.findViewById(R.id.brand_brands_layout);
		dotsLayout = (RelativeLayout) contentView
				.findViewById(R.id.viewpager_dots_container);
		brand_service_discount_types_container = (HorizontalScrollView) contentView
				.findViewById(R.id.brand_service_discount_types_container);
		discount_types_container = (LinearLayout) contentView
				.findViewById(R.id.discount_types_container);
	}

	private class MyPageChangeListener implements OnPageChangeListener {
		private List<View> dots;

		public MyPageChangeListener(List<View> dots) {
			// TODO Auto-generated constructor stub
			this.dots = dots;
		}

		public void resetDots() {
			for (View dotView : dots) {
				dotView.setBackgroundResource(R.drawable.dot_normal);
			}
		}

		/**
		 * This method will be invoked when a new page becomes selected.
		 * position: Position index of the new selected page.
		 */
		public void onPageSelected(int position) {
			resetDots();
			dots.get(position).setBackgroundResource(R.drawable.dot_focused);
		}

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}
	}

	@Override
	public void xmlClickMethod(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.layout_brand_service_activity_collect:
			startActivity(new Intent(mContext, DiscountsActivity.class));
			break;
		case R.id.layout_brand_service_activity_brand:
			startActivity(new Intent(mContext, BrandsActivity.class));
			break;
		default:
			break;
		}
	}

	private static class BrandServiceHandler extends Handler {
		WeakReference<BrandServiceFragment> mActivity;

		BrandServiceHandler(BrandServiceFragment activity) {
			mActivity = new WeakReference<BrandServiceFragment>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			BrandServiceFragment theActivity = mActivity.get();
			switch (msg.what) {

			case LOADING_FINISHED:
				// 更新显示的内容
				theActivity.isLoading = false;
				theActivity.refreshUI();
				break;

			case TIME_OUT:
				// 更新显示的内容
				if (theActivity.isVisible()) {
					Toast.makeText(
							theActivity.getActivity(),
							MyApplication.getInstance().getString(
									R.string.net_connect_time_out),
							Toast.LENGTH_SHORT).show();
				}

				break;

			case RELOAD_DATA:
				// 更新显示的内容
				JSONObject jsonObj = (JSONObject) msg.obj;
				try {
					if (jsonObj.optInt("status") == 1) {
						// 处理数据
						JSONArray brand_discount_array = jsonObj
								.optJSONArray("brand_discount_array");
						JSONArray discount_array = jsonObj
								.optJSONArray("discount_array");
						JSONArray brand_array = jsonObj
								.optJSONArray("brand_array");
						JSONArray discount_type_array = jsonObj
								.optJSONArray("discount_type_array");
						JSONObject tmpObject = null;
						for (int i = 0; i < brand_discount_array.length(); i++) {
							tmpObject = brand_discount_array.optJSONObject(i);
							Discount shopDiscount = theActivity
									.createDiscount(tmpObject);
							theActivity.advertisementList.add(shopDiscount);
						}
						for (int i = 0; i < discount_array.length(); i++) {
							tmpObject = discount_array.optJSONObject(i);
							Discount shopDiscount = theActivity
									.createDiscount(tmpObject);
							theActivity.recommendList.add(shopDiscount);
						}
						for (int i = 0; i < brand_array.length(); i++) {
							tmpObject = brand_array.optJSONObject(i);
							Brand brand = theActivity.createBrand(tmpObject);
							theActivity.brandList.add(brand);
						}
						for (int i = 0; i < discount_type_array.length(); i++) {
							tmpObject = discount_type_array.optJSONObject(i);
							DiscountType discountType = theActivity
									.createDiscountType(tmpObject);
							theActivity.discountTypeList.add(discountType);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();

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
		discount.setDiscount_id(item.optInt("discount_id"));
		JSONArray jsonArray = item.optJSONArray("discount_photo");
		List<String> imagesList = discount.getDiscount_photo();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject tmpJsonObject = jsonArray.optJSONObject(i);
			imagesList.add(tmpJsonObject.optString("original"));
		}
		discount.setShop_photo(item.optString("shop_photo"));
		discount.setOrder_count(item.optInt("order_count"));
		discount.setUpdated_at(item.optInt("updated_at"));
		discount.setShop_id(item.optInt("shop_id"));
		discount.setShop_name(item.optString("name"));
		discount.setSale_price(item.optString("sale_price"));
		discount.setAddress(item.optString("address"));
		discount.setPrice(item.optString("price"));
		discount.setMeet_needs(item.optBoolean("meet_needs"));
		discount.setCar_wash_bookable(item.optBoolean("car_wash_bookable"));
		discount.setIntegral(item.optInt("integral"));
		discount.setTelephone_area_code(item.optString("telephone_area_code"));
		discount.setTelephone_number(item.optString("telephone_number"));
		discount.setFollowed(item.optBoolean("followed"));
		discount.setPraised(item.optBoolean("praised"));
		return discount;
	}

	public Brand createBrand(JSONObject item) throws Exception {
		// TODO Auto-generated method stub
		Brand brand = new Brand();
		brand.setBrand_id(item.optInt("brand_id"));
		brand.setName(item.optString("name"));
		brand.setBrand_photo(item.optString("brand_photo"));
		brand.setFollow_count(item.optInt("follow_count"));
		brand.setShop_count(item.optInt("shop_count"));
		return brand;
	}

	public DiscountType createDiscountType(JSONObject item) throws Exception {
		// TODO Auto-generated method stub
		DiscountType discountType = new DiscountType();
		discountType.setDiscount_type_id(item.optInt("discount_type_id"));
		discountType.setIcon(item.optString("icon"));
		discountType.setName(item.optString("name"));
		return discountType;
	}

	private void refreshUI() {
		updateRecommentView();
		updateBrandView();
		updateViewPage();
		updateDiscountType();
		contentView.invalidate();
	}

	private void updateRecommentView() {
		recommendLayout.removeAllViews();
		if (recommendList != null && recommendList.size() > 0) {
			int viewId = 0x7F24FFF0;
			int size = recommendList.size();
			int horizontalSpacing = getResources().getDimensionPixelSize(
					R.dimen.padding_white_strip);
			int count = 0;
			for (Discount discount : recommendList) {
				count++;
				ImageView imageView = new ImageView(getActivity());
				imageView.setId(++viewId);
				imageView.setScaleType(ScaleType.CENTER_CROP);
				imageView.setBackgroundResource(R.drawable.image_border);
				final Discount tmpDiscount = discount;
				imageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(BrandServiceFragment.this
								.getActivity(), DiscountDetailActivity.class);
						Bundle bundle = new Bundle();
						bundle.putParcelable("discount", tmpDiscount);
						intent.putExtras(bundle);
						startActivity(intent);
					}
				});
				recommendLayout.addView(imageView);
				// set imageView layout params
				int width = (int) ((MyApplication.getInstance().screenWidth - horizontalSpacing
						* (size > 1 ? size - 1 : 0))
						/ size + 0.5);
				int height = (int) (width * 1.4 + 0.5);
				LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView
						.getLayoutParams();
				layoutParams.width = width;
				layoutParams.height = height;
				layoutParams.addRule(RelativeLayout.RIGHT_OF, viewId - 1);
				if (count > 0) {
					layoutParams.leftMargin = horizontalSpacing;
				}
				String imageString;
				if (discount.getDiscount_photo() != null
						&& discount.getDiscount_photo().size() > 0) {
					imageString = discount.getDiscount_photo().get(0);
				} else {
					imageString = discount.getShop_photo();
				}
				setViewImage(imageView, imageString);
			}
		}
		recommendLayout.invalidate();
	}

	@SuppressLint("InflateParams")
	private void updateBrandView() {
		brandLayout.removeAllViews();
		if (brandList != null && brandList.size() > 0) {
			int verticalSpacing, horizontalSpacing;
			verticalSpacing = horizontalSpacing = getResources()
					.getDimensionPixelSize(R.dimen.padding_white_strip);
			int count = 0, viewId = 0x7F24FFF0 + recommendList.size();
			int size = brandList.size();
			for (Brand brand : brandList) {
				View view = layoutInflater.inflate(
						R.layout.view_item_brand_service_activity_gridview,
						null);
				TextView textView = (TextView) view
						.findViewById(R.id.text_brand_name);
				textView.setText(brand.getName());
				ImageView imageView = (ImageView) view
						.findViewById(R.id.view_brand);
				view.setId(++viewId);
				imageView.setScaleType(ScaleType.CENTER_INSIDE);
				imageView.setBackgroundResource(R.drawable.image_border);
				final Brand tmpBrand = brand;
				imageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(BrandServiceFragment.this
								.getActivity(), BrandShopActivity.class);
						Bundle bundle = new Bundle();
						bundle.putParcelable("brand", tmpBrand);
						intent.putExtras(bundle);
						startActivity(intent);
					}
				});
				brandLayout.addView(view);
				// set imageView layout params
				// 减去留白
				int width = (MyApplication.getInstance().screenWidth - horizontalSpacing);
				int height = 0;
				LayoutParams layoutParams = (RelativeLayout.LayoutParams) view
						.getLayoutParams();
				switch (count) {
				case 0:
					if (size < 3) {
						width = (int) (width * 0.5 + 0.5);
						height = width;
					} else {
						width = (int) (width * 0.4 + 0.5);
						height = (int) (width * 1.4 + 0.5);
					}
					break;
				case 1:
					if (size < 3) {
						width = (int) (width * 0.5 + 0.5);
						height = width;
					} else {
						height = (int) ((width * 0.56 - verticalSpacing) * 0.5 + 0.5);
						width = (int) (width * 0.6 + 0.5);
					}
					layoutParams.leftMargin = horizontalSpacing;
					break;
				case 2:
				case 3:
					height = (int) ((width * 0.56 - verticalSpacing) * 0.5 + 0.5);
					if (size == 3) {
						width = (int) (width * 0.6 + 0.5);
					} else {
						width = (int) (width * 0.3 + 0.5);
					}
					layoutParams.leftMargin = horizontalSpacing;
					layoutParams.topMargin = verticalSpacing;
					break;
				}
				layoutParams.width = width;
				layoutParams.height = height;
				switch (count) {
				case 1:
					layoutParams.addRule(RelativeLayout.RIGHT_OF, viewId - 1);
					break;
				case 2:
					layoutParams.addRule(RelativeLayout.BELOW, viewId - 1);
					layoutParams.addRule(RelativeLayout.RIGHT_OF, viewId - 2);
					break;
				case 3:
					layoutParams.addRule(RelativeLayout.BELOW, viewId - 2);
					layoutParams.addRule(RelativeLayout.RIGHT_OF, viewId - 1);
					break;
				}
				setViewImage(imageView, brand.getBrand_photo());
				count++;
			}
		}
		brandLayout.invalidate();
	}

	private void updateViewPage() {
		viewPager.stopAutoScroll();
		imageViews.clear();
		dots.clear();
		dotsLayout.removeAllViews();
		int viewId = 0x7F24FFF0 + brandList.size() + recommendList.size();
		if (advertisementList == null || advertisementList.size() < 1) {
			// 隐藏ViewPager
			viewPager.setVisibility(View.GONE);
		} else {
			// 初始化图片资源
			int dotsize = getResources()
					.getDimensionPixelSize(R.dimen.dot_size);
			int horizontalSpacing = getResources().getDimensionPixelSize(
					R.dimen.dot_padding);
			for (int i = 0; i < advertisementList.size(); i++) {
				final Discount discount = advertisementList.get(i);
				ImageView imageView = new ImageView(this.getActivity());
				LayoutParams layoutParams = new LayoutParams(
						Const.VIEW_PGAER_WIDTH, Const.VIEW_PGAER_HEIGHT);
				final String image_path;
				if (discount.getDiscount_photo() != null
						&& discount.getDiscount_photo().size() > 0) {
					image_path = discount.getDiscount_photo().get(0);
				} else {
					image_path = discount.getShop_photo();
				}
				imageView.setLayoutParams(layoutParams);
				imageView.setScaleType(ScaleType.CENTER_CROP);
				imageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(BrandServiceFragment.this
								.getActivity(), DiscountDetailActivity.class);
						Bundle bundle = new Bundle();
						bundle.putParcelable("discount", discount);
						intent.putExtras(bundle);
						startActivity(intent);
					}
				});
				setViewImage(imageView, image_path);
				imageViews.add(imageView);
				View view = new View(getActivity());
				view.setId(++viewId);
				view.setBackgroundResource(i == 0 ? R.drawable.dot_focused
						: R.drawable.dot_normal);
				LayoutParams layoutParams1 = new LayoutParams(dotsize, dotsize);
				view.setLayoutParams(layoutParams1);
				layoutParams1.addRule(RelativeLayout.RIGHT_OF, viewId - 1);
				layoutParams1
						.setMargins(i == 0 ? horizontalSpacing : 0,
								horizontalSpacing, horizontalSpacing,
								horizontalSpacing);
				dots.add(view);
				dotsLayout.addView(view);
			}
			viewPager.setVisibility(View.VISIBLE);
		}
		dotsLayout.invalidate();
		viewPageAdapter.notifyDataSetChanged();
		viewPager.startAutoScroll();
	}

	@SuppressLint("InflateParams")
	private void updateDiscountType() {
		discount_types_container.removeAllViews();
		if (discountTypeList == null || discountTypeList.size() < 1) {
			// 隐藏
			brand_service_discount_types_container.setVisibility(View.GONE);
		} else {
			int horizontalSpacing = getResources().getDimensionPixelSize(
					R.dimen.padding_white_strip) * 4;
			int screenWidth = (int) ((MyApplication.getInstance().screenWidth - horizontalSpacing * 8) * 0.25 + 0.5);
			for (DiscountType discountType : discountTypeList) {
				View childView = getActivity().getLayoutInflater().inflate(
						R.layout.discount_type_item, null);
				discount_types_container.addView(childView);
				LinearLayout.LayoutParams layoutParams = (android.widget.LinearLayout.LayoutParams) childView
						.getLayoutParams();
				layoutParams.setMargins(horizontalSpacing, horizontalSpacing,
						horizontalSpacing, horizontalSpacing / 2);
				final int discount_type_id = discountType.getDiscount_type_id();
				final String name = discountType.getName();
				childView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent it = new Intent(mContext,
								ServiceTypeActivity.class);
						it.putExtra("serviceType", discount_type_id);
						it.putExtra("name", name);
						startActivity(it);
					}
				});
				ImageView imageView = (ImageView) childView
						.findViewById(R.id.discount_type_icon);
				LayoutParams layoutParams2 = new LayoutParams(screenWidth,
						screenWidth);
				String url = discountType.getIcon();
				imageView.setScaleType(ScaleType.CENTER_CROP);
				imageView.setLayoutParams(layoutParams2);
				setViewImage(imageView, url);
			}
			brand_service_discount_types_container.setVisibility(View.VISIBLE);
			discount_types_container.invalidate();

		}
		brand_service_discount_types_container.invalidate();
	}
}
