package com.yuyaa.awashcar.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.adapter.ViewPageAdapter;
import com.yuyaa.awashcar.entity.ShakeDiscount;
import com.yuyaa.awashcar.entity.ShopDiscount;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.StringUtils;
import com.yuyaa.awashcar.util.ThreadPoolUtils;
import com.yuyaa.awashcar.widget.MyProgressDialog;
import com.yuyaa.awashcar.widget.MyViewPager;

public class ShakeDetailActivity extends BaseActivity {
	private Context mContext;
	private MyViewPager viewPager;
	protected static final int LOADING_FINISHED = 0;
	public AlertDialog dlg;
	protected static final int RELOAD_DATA = 1;
	protected static final int ORDER_END = 4;
	protected static final int SUPPORT_REPLY = 2;
	protected static final int TIME_OUT = 3;
	private ShakeDetailHandler mHandler;
	private TextView textName, order_count;
	private TextView textBigCarPrice;
	private TextView textBigCarDiscountPrice;
	private TextView textSmallCarPrice, small_car_content, big_car_content;
	private TextView textSmallCarDiscountPrice;
	private TextView textAddress;
	private TextView textDistance;
	private TextView textTel, big_car_text, small_car_text;
	private ImageView[] stars;
	private RelativeLayout dotsLayout;
	private boolean isLoading = false;
	private List<View> imageViews; // 滑动的图片集合
	private List<String> imageList = new ArrayList<String>();
	private ShakeDiscount shakeDiscount;
	private Button orderButton;
	private ImageView discountImageView;
	private ImageView bookableImageView;
	private ViewPageAdapter viewPageAdapter;
	private ViewGroup popupWindow;
	private int car_typeIndex = 0;
	private List<View> dots;
	ImageView logoImageView;
	private ImageView followImageView;
	TextView priceTextView;
	TextView typeTextView;
	RadioGroup typeGroup;
	TextView need_ji_fenTextView;
	private MyProgressDialog progressDialog;
	TextView left_ji_fenTextView;
	Button confirm;
	CheckBox cb_clock_popupwindow_select_time;
	int default_time_zone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_shake_detail);
		init();
		mHandler = new ShakeDetailHandler(this);
		startLoadingThread();
		MyApplication.getInstance().updateLoginData();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// TODO Auto-generated method stub
		if (viewPager.isShown()) {
			viewPager.stopAutoScroll();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		// TODO Auto-generated method stub
		if (viewPager.isShown()) {
			viewPager.startAutoScroll();
		}

	}

	public void addressOnClick(View view) {
		Intent intent = new Intent(this, RoutePlanActivity.class);
		intent.putExtra("latitude", shakeDiscount.getLatitude());
		intent.putExtra("longitude", shakeDiscount.getLongitude());
		intent.putExtra("address", shakeDiscount.getAddress());
		startActivity(intent);
	}

	private void init() {
		ShareSDK.initSDK(this);
		mContext = this;
		findViews();
		viewPageAdapter = new ViewPageAdapter(imageViews);
		viewPager.setAdapter(viewPageAdapter);// 设置填充ViewPager页面的适配器
		// 设置一个监听器，当ViewPager中的页面改变时调用
		viewPager.setOnPageChangeListener(new MyPageChangeListener(dots));
		Intent intent = getIntent();
		shakeDiscount = intent.getExtras().getParcelable("discount");
		refreshUI();
	}

	private void findViews() {
		big_car_text = (TextView) findViewById(R.id.no_big_car_discount);
		small_car_text = (TextView) findViewById(R.id.no_small_car_discount);
		viewPager = (MyViewPager) findViewById(R.id.vp);
		imageViews = new ArrayList<View>();
		small_car_content = (TextView) findViewById(R.id.small_car_content);
		big_car_content = (TextView) findViewById(R.id.big_car_content);
		textName = (TextView) findViewById(R.id.text_merchant_detail_activity_name);
		textBigCarPrice = (TextView) findViewById(R.id.text_merchant_detail_activity_big_car_price);
		textBigCarDiscountPrice = (TextView) findViewById(R.id.text_merchant_detail_activity_big_car_discount_price);
		textSmallCarPrice = (TextView) findViewById(R.id.text_merchant_detail_activity_small_car_price);
		textSmallCarDiscountPrice = (TextView) findViewById(R.id.text_merchant_detail_activity_small_car_discount_price);
		textAddress = (TextView) findViewById(R.id.text_merchant_detail_activity_address);
		textDistance = (TextView) findViewById(R.id.text_merchant_detail_activity_distance);
		textTel = (TextView) findViewById(R.id.text_merchant_detail_activity_tel);
		discountImageView = (ImageView) findViewById(R.id.img_merchant_detail_activity_tuan);
		bookableImageView = (ImageView) findViewById(R.id.img_merchant_detail_activity_ding);
		stars = new ImageView[5];
		order_count = (TextView) findViewById(R.id.text_merchant_detail_activity_popularity);
		dotsLayout = (RelativeLayout) findViewById(R.id.viewpager_dots_container);
		dots = new ArrayList<View>();
		stars[0] = (ImageView) findViewById(R.id.img_merchant_detail_activity_star0);
		stars[1] = (ImageView) findViewById(R.id.img_merchant_detail_activity_star1);
		stars[2] = (ImageView) findViewById(R.id.img_merchant_detail_activity_star2);
		stars[3] = (ImageView) findViewById(R.id.img_merchant_detail_activity_star3);
		stars[4] = (ImageView) findViewById(R.id.img_merchant_detail_activity_star4);
		orderButton = (Button) findViewById(R.id.btn_merchant_detail_activity_order);
		followImageView = (ImageView) findViewById(R.id.btn_merchant_detail_activity_favourite);
	}

	private void updateViewPage() {
		viewPager.stopAutoScroll();
		imageViews.clear();
		dots.clear();
		dotsLayout.removeAllViews();
		imageList = shakeDiscount.getBig_car().getDiscount_photo();
		for (String photo : shakeDiscount.getSmall_car().getDiscount_photo()) {
			imageList.add(photo);
		}
		int viewId = 0x7F24FFF0;
		if (imageList == null || imageList.size() < 1) {
			ImageView imageView = new ImageView(this);
			LayoutParams layoutParams = new LayoutParams(
					Const.VIEW_PGAER_WIDTH, Const.VIEW_PGAER_HEIGHT);
			final String image_path = shakeDiscount.getShop_photo();
			imageView.setLayoutParams(layoutParams);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			setViewImage(imageView, image_path);
			imageList.add(image_path);
			imageViews.add(imageView);
		} else {
			// 初始化图片资源
			int dotsize = getResources()
					.getDimensionPixelSize(R.dimen.dot_size);
			int horizontalSpacing = getResources().getDimensionPixelSize(
					R.dimen.dot_padding);
			for (int i = 0; i < imageList.size(); i++) {
				ImageView imageView = new ImageView(this);
				LayoutParams layoutParams = new LayoutParams(
						Const.VIEW_PGAER_WIDTH, Const.VIEW_PGAER_HEIGHT);
				final String image_path = imageList.get(i);
				imageView.setLayoutParams(layoutParams);
				imageView.setScaleType(ScaleType.CENTER_CROP);
				setViewImage(imageView, image_path);
				imageViews.add(imageView);
				View view = new View(this);
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
		}
		if (imageViews == null || imageViews.size() < 1) {
			// 隐藏ViewPager
			viewPager.setVisibility(View.GONE);
		} else {
			viewPager.setVisibility(View.VISIBLE);
		}
		dotsLayout.invalidate();
		viewPageAdapter.notifyDataSetChanged();
		viewPager.startAutoScroll();
	}

	/**
	 * 当ViewPager中页面的状态发生改变时调用
	 * 
	 * @author Administrator
	 * 
	 */
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

		public void onPageSelected(int position) {
			resetDots();
			dots.get(position).setBackgroundResource(R.drawable.dot_focused);
		}

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}
	}

	private void refreshUI() {
		if (shakeDiscount.getMeet_needs()) {
			orderButton.setVisibility(View.VISIBLE);
		} else {
			orderButton.setVisibility(View.GONE);
		}
		textName.setText(shakeDiscount.getShop_name());
		textAddress.setText(shakeDiscount.getAddress());
		textDistance.setText(StringUtils.friendly_distance(shakeDiscount
				.getDistance()));
		if (StringUtils.isEmpty(shakeDiscount.getBig_car().getContent())) {
			big_car_content.setText(shakeDiscount.getBig_car().getContent());
		} else {
			big_car_content.setText("这个商家太懒啦，什么都没写");
		}
		if (StringUtils.isEmpty(shakeDiscount.getSmall_car().getContent())) {
			small_car_content
					.setText(shakeDiscount.getSmall_car().getContent());
		} else {
			small_car_content.setText("这个商家太懒啦，什么都没写");
		}
		big_car_text.setVisibility(View.GONE);
		small_car_text.setVisibility(View.GONE);
		order_count.setText("总订单量:" + shakeDiscount.getOrder_count());
		discountImageView.setVisibility(View.GONE);
		textBigCarPrice.setVisibility(View.GONE);
		textBigCarDiscountPrice.setVisibility(View.GONE);
		if (StringUtils.isEmpty(shakeDiscount.getBig_car().getPrice())
				&& StringUtils.isEmpty(shakeDiscount.getBig_car()
						.getSale_price())) {
			big_car_text.setVisibility(View.VISIBLE);
		} else if (StringUtils.isEmpty(shakeDiscount.getBig_car().getPrice())) {
			discountImageView.setVisibility(View.VISIBLE);
			textBigCarDiscountPrice.setText("￥"
					+ shakeDiscount.getBig_car().getSale_price());
			textBigCarDiscountPrice.setVisibility(View.VISIBLE);
		} else if (StringUtils.isEmpty(shakeDiscount.getBig_car()
				.getSale_price())) {
			textBigCarPrice
					.setText("￥" + shakeDiscount.getBig_car().getPrice());
			textBigCarPrice.setVisibility(View.VISIBLE);
		} else if (Float.parseFloat(shakeDiscount.getBig_car().getPrice()) > Float
				.parseFloat(shakeDiscount.getBig_car().getSale_price())) {
			discountImageView.setVisibility(View.VISIBLE);
			textBigCarPrice
					.setText("￥" + shakeDiscount.getBig_car().getPrice());
			textBigCarDiscountPrice.setText("￥"
					+ shakeDiscount.getBig_car().getSale_price());
			textBigCarDiscountPrice.setVisibility(View.VISIBLE);
			textBigCarPrice.setVisibility(View.VISIBLE);
		} else {
			textBigCarDiscountPrice.setText("￥"
					+ shakeDiscount.getBig_car().getPrice());
			textBigCarDiscountPrice.setVisibility(View.VISIBLE);
		}
		textSmallCarPrice.setVisibility(View.GONE);
		textSmallCarDiscountPrice.setVisibility(View.GONE);
		if (StringUtils.isEmpty(shakeDiscount.getSmall_car().getPrice())
				&& StringUtils.isEmpty(shakeDiscount.getSmall_car()
						.getSale_price())) {
			small_car_text.setVisibility(View.VISIBLE);
		} else if (StringUtils.isEmpty(shakeDiscount.getSmall_car().getPrice())) {
			discountImageView.setVisibility(View.VISIBLE);
			textSmallCarDiscountPrice.setText("￥"
					+ shakeDiscount.getSmall_car().getSale_price());
			textSmallCarDiscountPrice.setVisibility(View.VISIBLE);
		} else if (StringUtils.isEmpty(shakeDiscount.getSmall_car()
				.getSale_price())) {
			textSmallCarPrice.setText("￥"
					+ shakeDiscount.getSmall_car().getPrice());
			textSmallCarPrice.setVisibility(View.VISIBLE);
		} else if (Float.parseFloat(shakeDiscount.getSmall_car().getPrice()) > Float
				.parseFloat(shakeDiscount.getSmall_car().getSale_price())) {
			discountImageView.setVisibility(View.VISIBLE);
			textSmallCarPrice.setText("￥"
					+ shakeDiscount.getSmall_car().getPrice());
			textSmallCarDiscountPrice.setText("￥"
					+ shakeDiscount.getSmall_car().getSale_price());
			textSmallCarDiscountPrice.setVisibility(View.VISIBLE);
			textSmallCarPrice.setVisibility(View.VISIBLE);
		} else {
			textSmallCarDiscountPrice.setText("￥"
					+ shakeDiscount.getSmall_car().getPrice());
			textSmallCarDiscountPrice.setVisibility(View.VISIBLE);
		}

		int average_grade = shakeDiscount.getAverage_grade();
		for (int i = 0; i < stars.length; i++) {
			stars[i].setBackgroundResource(R.drawable.img_star_dark_big);
		}
		for (int i = 0; i < average_grade; i++) {
			stars[i].setBackgroundResource(R.drawable.img_star_light_big);
		}
		textTel.setText(shakeDiscount.getTelephone_area_code() + "-"
				+ shakeDiscount.getTelephone_number());
		if (shakeDiscount.getCar_wash_bookable()) {
			bookableImageView.setVisibility(View.VISIBLE);
		} else {
			bookableImageView.setVisibility(View.GONE);
		}
		followImageView
				.setImageResource(shakeDiscount.getFollowed() ? R.drawable.img_drawer_list_item_favourite_followed
						: R.drawable.img_drawer_list_item_favourite_normal);
		updateViewPage();
	}

	public void clickCallback(View view) {
		switch (view.getId()) {
		case R.id.btn_merchant_detail_activity_back:
			onBackPressed();
			break;
		case R.id.btn_merchant_detail_activity_order:
			if (MyApplication.getInstance()
					.getStringGlobalData("session", null) != null) {
				book();
			} else {
				startActivity(new Intent(mContext, LoginActivity.class));
			}
			break;
		case R.id.btn_merchant_detail_activity_share:
			showShare();
			break;
		case R.id.btn_merchant_detail_activity_favourite:
			if (MyApplication.getInstance()
					.getStringGlobalData("session", null) != null) {
				final Boolean boolean1 = !shakeDiscount.getFollowed();
				shakeDiscount.setFollowed(boolean1);
				ThreadPoolUtils.execute(new Runnable() {
					public void run() {
						httpFollow(boolean1);
					}
				});
				followImageView
						.setImageResource(boolean1 ? R.drawable.img_drawer_list_item_favourite_followed
								: R.drawable.img_drawer_list_item_favourite_normal);

			} else {
				Toast toast = Toast.makeText(this, MyApplication.getInstance()
						.getString(R.string.please_log), Toast.LENGTH_SHORT);
				toast.setMargin(0f, 0.05f);
				toast.show();
				Intent intent = new Intent(this, LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}

			break;
		default:
			break;
		}
	}

	private ShakeDiscount createDiscount(JSONObject item) {
		shakeDiscount = new ShakeDiscount();
		shakeDiscount.setShop_name(item.optString("name"));
		shakeDiscount.setShop_photo(item.optString("shop_photo"));
		shakeDiscount.setAddress(item.optString("address"));
		shakeDiscount.setDistance(item.optInt("distance"));
		shakeDiscount.setOrder_count(item.optInt("order_count"));
		shakeDiscount.setShop_id(item.optInt("shop_id"));
		shakeDiscount.setAverage_grade(item.optInt("average_grade"));
		shakeDiscount.setLongitude(item.optDouble("longitude"));
		shakeDiscount.setLatitude(item.optDouble("latitude"));
		shakeDiscount
				.setCar_wash_bookable(item.optBoolean("car_wash_bookable"));
		shakeDiscount.setMeet_needs(item.optBoolean("meet_needs"));
		shakeDiscount.setTelephone_area_code(item
				.optString("telephone_area_code"));
		shakeDiscount.setFollowed(item.optBoolean("followed"));
		shakeDiscount.setTelephone_number(item.optString("telephone_number"));
		JSONObject big_carObject = item.optJSONObject("big_car");
		JSONObject small_carObject = item.optJSONObject("small_car");
		ShopDiscount big_carDiscount = new ShopDiscount();
		ShopDiscount small_carDiscount = new ShopDiscount();
		big_carDiscount.setPrice(big_carObject.optString("price"));
		big_carDiscount.setContent(big_carObject.optString("content"));
		big_carDiscount.setIntegral(big_carObject.optInt("integral"));
		big_carDiscount.setSale_price(big_carObject.optString("sale_price"));
		big_carDiscount.setDiscount_id(big_carObject.optInt("discount_id"));
		small_carDiscount.setPrice(small_carObject.optString("price"));
		small_carDiscount.setContent(small_carObject.optString("content"));
		small_carDiscount.setIntegral(small_carObject.optInt("integral"));
		small_carDiscount
				.setSale_price(small_carObject.optString("sale_price"));
		small_carDiscount.setDiscount_id(small_carObject.optInt("discount_id"));
		List<String> photoList = big_carDiscount.getDiscount_photo();
		photoList.clear();
		if (big_carObject.has("discount_photo")) {
			JSONArray big_car_photos = big_carObject
					.optJSONArray("discount_photo");
			for (int i = 0; i < big_car_photos.length(); i++) {
				photoList.add(big_car_photos.optJSONObject(i).optString(
						"original"));
			}
		}
		photoList = small_carDiscount.getDiscount_photo();
		photoList.clear();
		if (small_carObject.has("discount_photo")) {
			JSONArray small_car_photos = small_carObject
					.optJSONArray("discount_photo");
			for (int i = 0; i < small_car_photos.length(); i++) {
				photoList.add(small_car_photos.optJSONObject(i).optString(
						"original"));
			}
		}
		shakeDiscount.setBig_car(big_carDiscount);
		shakeDiscount.setSmall_car(small_carDiscount);
		return shakeDiscount;
	}

	private void httpFollow(Boolean flag) {
		String url = Const.SERVER_BASE_PATH + Const.FOLLOW_SHOP;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("flag", "" + flag);
		request.setValueForKey("shop_id", "" + shakeDiscount.getShop_id());
		JSONObject json = (JSONObject) request.execForJSONObject(this);
		Message msg = Message.obtain(mHandler, SUPPORT_REPLY, 0, 0, json);
		mHandler.sendMessage(msg);
	}

	private void book() {
		createPopupWindow();
	}

	// 分享接口
	private void showShare() {
		if (MyApplication.getInstance().getStringGlobalData("session", null) != null) {

			OnekeyShare oks = new OnekeyShare();
			// 关闭sso授权
			oks.disableSSOWhenAuthorize();

			// 分享时Notification的图标和文字
			oks.setNotification(R.drawable.ic_launcher,
					getString(R.string.app_name));
			// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
			oks.setTitle(shakeDiscount.getShop_name());
			// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
			// oks.setTitleUrl("http://sharesdk.cn");
			// text是分享文本，所有平台都需要这个字段
			oks.setText(shakeDiscount.getShop_name() + ","
					+ shakeDiscount.getAddress() + ",更多优惠服务尽车粒子洗车平台。车粒子下载地址："
					+ Const.SERVER_BASE_PATH + Const.DOWNLOAD);
			// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
			oks.setImageUrl(Const.SERVER_BASE_PATH
					+ shakeDiscount.getShop_photo());
			oks.setImageArray(getImageArray());
			oks.setUrl(Const.SERVER_BASE_PATH + Const.DOWNLOAD);
			// url仅在微信（包括好友和朋友圈）中使用
			// oks.setUrl("http://sharesdk.cn");
			// comment是我对这条分享的评论，仅在人人网和QQ空间使用
			// oks.setComment("我是测试评论文本");
			// site是分享此内容的网站名称，仅在QQ空间使用
			oks.setSite(getString(R.string.app_name));
			// siteUrl是分享此内容的网站地址，仅在QQ空间使用
			oks.setSiteUrl(Const.SERVER_BASE_PATH + Const.DOWNLOAD);
			// url仅在微信（包括好友和朋友圈）中使用
			// 启动分享GUI
			// 令编辑页面显示为Dialog模式
			oks.setDialogMode();
			oks.show(this);
		} else {
			Toast toast = Toast.makeText(this, MyApplication.getInstance()
					.getString(R.string.please_log), Toast.LENGTH_SHORT);
			toast.setMargin(0f, 0.05f);
			toast.show();
			Intent intent = new Intent(this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}

	private String[] getImageArray() {
		// TODO Auto-generated method stub
		String[] imageArray = new String[imageList.size()];
		int count = 0;
		for (String imageUrl : imageList) {
			imageArray[count++] = imageUrl;
		}
		return imageArray;
	}

	private void startLoadingThread() {
		if (!isLoading) {
			progressDialog = new MyProgressDialog(this,
					R.style.CustomProgressDialog);
			progressDialog.setTitle("正在加载...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			isLoading = true;
			ThreadPoolUtils.execute(new Runnable() {
				public void run() {
					reloadData();
				}
			});
		}

	}

	private void reloadData() {
		String url = Const.SERVER_BASE_PATH + Const.SHAKE_DISCOUNT;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("shop_id", "" + shakeDiscount.getShop_id());
		request.setValueForKey(Const.KEY_LONGITUDE,
				"" + MyApplication.getInstance().user.mCurrentLongitude);
		request.setValueForKey(Const.KEY_LATITUDE,
				"" + MyApplication.getInstance().user.mCurrentLantitude);
		JSONObject jsonObj = null;
		jsonObj = (JSONObject) request
				.execForJSONObject(ShakeDetailActivity.this);
		Message msgMessage = Message.obtain();
		msgMessage.what = RELOAD_DATA;
		msgMessage.obj = jsonObj;
		mHandler.sendMessage(msgMessage);
	}

	private static class ShakeDetailHandler extends Handler {
		WeakReference<ShakeDetailActivity> mActivity;

		ShakeDetailHandler(ShakeDetailActivity activity) {
			mActivity = new WeakReference<ShakeDetailActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ShakeDetailActivity theActivity = mActivity.get();
			if (theActivity.progressDialog != null) {
				theActivity.progressDialog.dismiss();
			}
			switch (msg.what) {
			case LOADING_FINISHED:
				// 更新显示的内容
				theActivity.isLoading = false;
				theActivity.refreshUI();
				break;
			case SUPPORT_REPLY:
				// 更新显示的内容
				try {
					ShakeDiscount discount = theActivity.shakeDiscount;
					JSONObject jsonObject = (JSONObject) msg.obj;
					if (jsonObject.optInt("status") == 1) {
						Boolean fBoolean = jsonObject.optBoolean("followed");
						discount.setFollowed(fBoolean);
						theActivity.followImageView
								.setImageResource(fBoolean ? R.drawable.img_drawer_list_item_favourite_followed
										: R.drawable.img_drawer_list_item_favourite_normal);
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
				try {
					if (jsonObj.optInt("status") == 1) {
						theActivity.createDiscount(jsonObj
								.optJSONObject("data"));
					}
				} catch (Exception e) {
					e.printStackTrace();

				} finally {
					Message msgMessage = Message.obtain();
					msgMessage.what = LOADING_FINISHED;
					this.sendMessage(msgMessage);
				}

				break;
			case ORDER_END:
				Toast.makeText(theActivity, (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}

			super.handleMessage(msg);

		}
	};

	public void goBack(View view) {
		onBackPressed();
	}

	public void phoneOnClick(View view) {
		Intent intent = new Intent();

		// 系统默认的action，用来打开默认的电话界面
		intent.setAction(Intent.ACTION_DIAL);

		// 需要拨打的号码
		String telephone_areq_code = shakeDiscount.getTelephone_area_code();
		String telephone_number = shakeDiscount.getTelephone_number();
		intent.setData(Uri.parse("tel:" + telephone_areq_code
				+ telephone_number));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	@SuppressLint("InflateParams")
	@SuppressWarnings("deprecation")
	public void createPopupWindow() {
		final RelativeLayout root = (RelativeLayout) findViewById(R.id.my_container);
		if (popupWindow == null) {
			popupWindow = new RelativeLayout(this);
			RelativeLayout.LayoutParams params = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			popupWindow.setLayoutParams(params);
			ColorDrawable bg = new ColorDrawable(R.color.font_pink);
			bg.setAlpha(255);
			popupWindow.setBackgroundDrawable(bg);
			popupWindow.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Animation push_left_in = AnimationUtils.loadAnimation(
							ShakeDetailActivity.this,
							R.anim.base_slide_bottom_out);
					popupWindow.startAnimation(push_left_in);
					root.removeView(popupWindow);
				}
			});

			View view = LayoutInflater.from(mContext).inflate(
					R.layout.shake_order_popup_view_layout, null);
			ImageView close_button = (ImageView) view
					.findViewById(R.id.close_button);
			close_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Animation push_left_in = AnimationUtils.loadAnimation(
							ShakeDetailActivity.this,
							R.anim.base_slide_bottom_out);
					popupWindow.startAnimation(push_left_in);
					root.removeView(popupWindow);
				}
			});
			confirm = (Button) view.findViewById(R.id.confirm);
			confirm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (validateInput()) {
						Animation push_left_in = AnimationUtils.loadAnimation(
								ShakeDetailActivity.this,
								R.anim.base_slide_bottom_out);
						popupWindow.startAnimation(push_left_in);
						root.removeView(popupWindow);
						Order();
					}
				}
			});
			WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			params = new LayoutParams(LayoutParams.MATCH_PARENT,
					(int) (display.getHeight() * 0.65));
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			view.setLayoutParams(params);
			initPopupView(view);
			popupWindow.addView(view);
		}
		updatePopuoView();
		root.addView(popupWindow, root.getChildCount());
		Animation push_left_in = AnimationUtils.loadAnimation(this,
				R.anim.base_slide_bottom_in);
		popupWindow.startAnimation(push_left_in);

	}

	private void updatePopuoView() {
		// TODO Auto-generated method stub
		setViewImage(logoImageView, imageList.get(0));
		// priceTextView.setText("￥" + discount.getSale_price());
		Date todayDate = new Date();
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(todayDate); // assigns calendar to given date
		default_time_zone = calendar.get(Calendar.HOUR_OF_DAY);
		String time_conditionString = default_time_zone + ":00-"
				+ (default_time_zone + 1) + ":00";
		cb_clock_popupwindow_select_time.setText(time_conditionString);
		if (default_time_zone < 9 && default_time_zone > 17) {
			cb_clock_popupwindow_select_time.setEnabled(false);
		}
		typeTextView.setText(car_typeIndex == 0 ? "5座以内" : "7座及SUV");
		priceTextView.setText("￥"
				+ (car_typeIndex == 0 ? shakeDiscount.getSmall_car()
						.getSale_price() : shakeDiscount.getBig_car()
						.getSale_price()));
		need_ji_fenTextView.setText(""
				+ (car_typeIndex == 0 ? shakeDiscount.getSmall_car()
						.getIntegral() : shakeDiscount.getBig_car()
						.getIntegral()));
		left_ji_fenTextView.setText(MyApplication.getInstance().user.integral
				+ "");
	}

	private void initPopupView(final View view) {
		// TODO Auto-generated method stub
		logoImageView = (ImageView) view.findViewById(R.id.image);
		priceTextView = (TextView) view.findViewById(R.id.price);
		typeTextView = (TextView) view.findViewById(R.id.type);
		typeGroup = (RadioGroup) view.findViewById(R.id.radio_button);
		need_ji_fenTextView = (TextView) view.findViewById(R.id.need_ji_fen);
		typeGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if (R.id.test_0 == checkedId) {
					car_typeIndex = 0;
				} else {
					car_typeIndex = 1;
				}
				typeTextView.setText(car_typeIndex == 0 ? "5座以内" : "7座及SUV");
				priceTextView.setText("￥"
						+ (car_typeIndex == 0 ? shakeDiscount.getSmall_car()
								.getSale_price() : shakeDiscount.getBig_car()
								.getSale_price()));
				need_ji_fenTextView.setText(""
						+ (car_typeIndex == 0 ? shakeDiscount.getSmall_car()
								.getIntegral() : shakeDiscount.getBig_car()
								.getIntegral()));
			}
		});

		left_ji_fenTextView = (TextView) view.findViewById(R.id.left_ji_fen);
		cb_clock_popupwindow_select_time = (CheckBox) view
				.findViewById(R.id.cb_clock_popupwindow_select_time);
	}

	protected String httpOrder() throws Exception {
		// TODO Auto-generated method stub
		String url = Const.SERVER_BASE_PATH + Const.CAR_WASH_ORDER;
		ShopDiscount shopDiscount;
		if (car_typeIndex == 0) {
			shopDiscount = shakeDiscount.getSmall_car();
		} else {
			shopDiscount = shakeDiscount.getBig_car();
		}
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("discount_id",
				"" + shopDiscount.getDiscount_id());
		request.setValueForKey("day_index", "" + 0);
		request.setValueForKey("time_zone", "" + default_time_zone);
		JSONObject json = (JSONObject) request
				.execForJSONObject(ShakeDetailActivity.this);
		if (json.optInt("status") == 1) {
			MyApplication.getInstance().user.integral -= shopDiscount
					.getIntegral();
			MyApplication.getInstance().saveLoginData();
			// 更新历史订单量
			shakeDiscount.setOrder_count(shakeDiscount.getOrder_count() + 1);
			order_count.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					order_count.setText("总订单量:"
							+ shakeDiscount.getOrder_count());
				}
			});
		}
		return json.optString("message");
	}

	protected void Order() {
		// TODO Auto-generated method stub
		if (MyApplication.getInstance().getStringGlobalData("session", null) != null) {
			progressDialog = new MyProgressDialog(this,
					R.style.CustomProgressDialog);
			progressDialog.setTitle("正在提交订单...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			// 提交请求
			ThreadPoolUtils.execute(new Runnable() {

				public void run() {
					Message msg = Message.obtain();
					try {
						msg.what = ORDER_END;
						String result = ShakeDetailActivity.this.httpOrder();
						msg.obj = result;

					} catch (ConnectTimeoutException e) {
						e.printStackTrace();
						msg.what = TIME_OUT;
					} catch (Exception e) {
						e.printStackTrace();
						msg.what = ORDER_END;
					} finally {
						mHandler.sendMessage(msg);
					}

				}
			});
		} else {
			Toast toast = Toast.makeText(this, MyApplication.getInstance()
					.getString(R.string.please_log), Toast.LENGTH_SHORT);
			toast.setMargin(0f, 0.05f);
			toast.show();
			Intent intent = new Intent(this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}

	}

	private boolean validateInput() {
		String msg = null;
		boolean result = true;
		ShopDiscount shopDiscount;
		if (car_typeIndex == 0) {
			shopDiscount = shakeDiscount.getSmall_car();
		} else {
			shopDiscount = shakeDiscount.getBig_car();
		}
		if (shopDiscount.getIntegral() > MyApplication.getInstance().user.integral) {
			result = false;
			msg = "剩余积分不足!";
		}
		if (default_time_zone < 9 && default_time_zone > 17) {
			result = false;
			msg = "洗车店都关门啦!";
		}
		if (!result) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setMessage(msg);
			builder.setTitle("提示");
			builder.setPositiveButton("返回",
					new android.content.DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.create().show();
		}
		return result;
	}
}
