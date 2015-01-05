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
import com.yuyaa.awashcar.adapter.MyGridAdapter;
import com.yuyaa.awashcar.adapter.ViewPageAdapter;
import com.yuyaa.awashcar.entity.Discount;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.StringUtils;
import com.yuyaa.awashcar.util.ThreadPoolUtils;
import com.yuyaa.awashcar.view.NestGridView;
import com.yuyaa.awashcar.widget.MyProgressDialog;
import com.yuyaa.awashcar.widget.MyViewPager;

public class CarWashDiscountDetailActivity extends BaseActivity {
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

	private Context mContext;
	private MyViewPager viewPager;
	protected static final int LOADING_FINISHED = 0;
	protected static final int RELOAD_DATA = 1;
	protected static final int SUPPORT_REPLY = 2;
	protected static final int ORDER_END = 4;
	protected static final int TIME_OUT = 3;
	private DiscountDetailHandler mHandler;
	private TextView textName;
	private TextView textPrice;
	private TextView textSalePrice, title_shop_name;
	private TextView textAddress;
	private TextView textDistance, discount_content;
	private TextView textTel;
	private TextView text_price, order_count;
	private ImageView discountImageView;
	private ImageView bookableImageView;
	private ImageView[] stars;
	private ImageView followImageView;
	private boolean isLoading = false;
	private List<View> imageViews; // 滑动的图片集合
	private Discount discount;
	private ViewPageAdapter viewPageAdapter;
	private List<View> dots;
	private List<String> imageList = new ArrayList<String>();
	private RelativeLayout dotsLayout;
	private Button orderButton;
	private ViewGroup popupWindow;
	private int time_index = -1;
	private int time_zone_select_index = 0;
	ImageView logoImageView;
	TextView priceTextView;
	TextView lefTextView, chose_time;
	private NestGridView nestGridView;
	TextView need_ji_fenTextView;
	TextView left_ji_fenTextView;
	RadioGroup time_zone_index;
	MyGridAdapter myGridAdapter;
	String time_desc = null;
	int[][] book_left = new int[2][9];
	private MyProgressDialog progressDialog;
	Button confirm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_merchant_detail);
		init();
		mHandler = new DiscountDetailHandler(this);
		startLoadingThread();
		MyApplication.getInstance().updateLoginData();
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
		discount = intent.getExtras().getParcelable("discount");
		refreshUI();
	}

	private void refreshUI() {
		if (discount.getMeet_needs()) {
			orderButton.setVisibility(View.VISIBLE);
		} else {
			orderButton.setVisibility(View.GONE);
		}
		textName.setText(discount.getTitle());
		title_shop_name.setText(discount.getShop_name());
		textAddress.setText(discount.getAddress());
		textDistance.setText(StringUtils.friendly_distance(discount
				.getDistance()));
		if (StringUtils.isEmpty(discount.getContent())) {
			discount_content.setText(discount.getContent());
		} else {
			discount_content.setText("这个商家太懒啦，什么都没写");
		}
		discountImageView.setVisibility(View.GONE);
		text_price.setVisibility(View.GONE);
		order_count.setText("总订单量:" + discount.getOrder_count());
		if (StringUtils.isEmpty(discount.getPrice())
				&& StringUtils.isEmpty(discount.getSale_price())) {
			textPrice.setVisibility(View.GONE);
			textSalePrice.setVisibility(View.GONE);
		} else if (StringUtils.isEmpty(discount.getPrice())) {
			discountImageView.setVisibility(View.VISIBLE);
			textSalePrice.setText("￥" + discount.getSale_price());
			textPrice.setVisibility(View.GONE);
			textSalePrice.setVisibility(View.VISIBLE);
			text_price.setVisibility(View.VISIBLE);
		} else if (StringUtils.isEmpty(discount.getSale_price())) {
			textPrice.setText("￥" + discount.getPrice());
			textPrice.setVisibility(View.VISIBLE);
			textSalePrice.setVisibility(View.GONE);
			text_price.setVisibility(View.VISIBLE);
		} else if (Float.parseFloat(discount.getPrice()) > Float
				.parseFloat(discount.getSale_price())) {
			discountImageView.setVisibility(View.VISIBLE);
			textPrice.setText("￥" + discount.getPrice());
			textSalePrice.setText("￥" + discount.getSale_price());
			textPrice.setVisibility(View.VISIBLE);
			textSalePrice.setVisibility(View.VISIBLE);
			text_price.setVisibility(View.VISIBLE);
		} else {
			text_price.setVisibility(View.VISIBLE);
			textSalePrice.setText("￥" + discount.getPrice());
			textPrice.setVisibility(View.GONE);
			textSalePrice.setVisibility(View.VISIBLE);
		}

		int average_grade = discount.getAverage_grade();
		for (int i = 0; i < stars.length; i++) {
			stars[i].setBackgroundResource(R.drawable.img_star_dark_big);
		}
		for (int i = 0; i < average_grade; i++) {
			stars[i].setBackgroundResource(R.drawable.img_star_light_big);
		}
		textTel.setText(discount.getTelephone_area_code() + "-"
				+ discount.getTelephone_number());
		if (discount.getCar_wash_bookable()) {
			bookableImageView.setVisibility(View.VISIBLE);
		} else {
			bookableImageView.setVisibility(View.GONE);
		}
		followImageView
				.setImageResource(discount.getFollowed() ? R.drawable.img_drawer_list_item_favourite_followed
						: R.drawable.img_drawer_list_item_favourite_normal);
		updateViewPage();
	}

	private void updateViewPage() {
		viewPager.stopAutoScroll();
		imageViews.clear();
		dots.clear();
		dotsLayout.removeAllViews();
		imageList = discount.getDiscount_photo();
		int viewId = 0x7F24FFF0;
		if (imageList == null || imageList.size() < 1) {
			ImageView imageView = new ImageView(this);
			LayoutParams layoutParams = new LayoutParams(
					Const.VIEW_PGAER_WIDTH, Const.VIEW_PGAER_HEIGHT);
			final String image_path = discount.getShop_photo();
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

	private void findViews() {
		viewPager = (MyViewPager) findViewById(R.id.vp);
		viewPager.height = Const.VIEW_PGAER_HEIGHT;
		textName = (TextView) findViewById(R.id.text_merchant_detail_activity_name);
		textSalePrice = (TextView) findViewById(R.id.text_merchant_detail_activity_big_car_discount_price);
		textPrice = (TextView) findViewById(R.id.text_merchant_detail_activity_big_car_price);
		textAddress = (TextView) findViewById(R.id.text_merchant_detail_activity_address);
		textDistance = (TextView) findViewById(R.id.text_merchant_detail_activity_distance);
		discount_content = (TextView) findViewById(R.id.car_content);
		textTel = (TextView) findViewById(R.id.text_merchant_detail_activity_tel);
		discountImageView = (ImageView) findViewById(R.id.img_merchant_detail_activity_tuan);
		bookableImageView = (ImageView) findViewById(R.id.img_merchant_detail_activity_ding);
		text_price = (TextView) findViewById(R.id.text_price);
		order_count = (TextView) findViewById(R.id.text_merchant_detail_activity_popularity);
		stars = new ImageView[5];
		dotsLayout = (RelativeLayout) findViewById(R.id.viewpager_dots_container);
		imageViews = new ArrayList<View>();
		dots = new ArrayList<View>();
		stars[0] = (ImageView) findViewById(R.id.img_merchant_detail_activity_star0);
		stars[1] = (ImageView) findViewById(R.id.img_merchant_detail_activity_star1);
		stars[2] = (ImageView) findViewById(R.id.img_merchant_detail_activity_star2);
		stars[3] = (ImageView) findViewById(R.id.img_merchant_detail_activity_star3);
		stars[4] = (ImageView) findViewById(R.id.img_merchant_detail_activity_star4);
		orderButton = (Button) findViewById(R.id.btn_merchant_detail_activity_order);
		followImageView = (ImageView) findViewById(R.id.btn_merchant_detail_activity_favourite);
		title_shop_name = (TextView) findViewById(R.id.title_shop_name);
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
				final Boolean boolean1 = !discount.getFollowed();
				discount.setFollowed(boolean1);
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

	private void httpFollow(Boolean flag) {
		String url = Const.SERVER_BASE_PATH + Const.FOLLOW_SHOP;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("flag", "" + flag);
		request.setValueForKey("shop_id", "" + discount.getShop_id());
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
			oks.setTitle(discount.getShop_name());
			// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
			// oks.setTitleUrl("http://sharesdk.cn");
			// text是分享文本，所有平台都需要这个字段
			oks.setText(discount.getShop_name() + "," + discount.getTitle()
					+ " 更多优惠服务尽车粒子洗车平台。车粒子下载地址：" + Const.SERVER_BASE_PATH
					+ Const.DOWNLOAD);
			// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
			oks.setImageUrl(Const.SERVER_BASE_PATH + discount.getShop_photo());
			oks.setImageArray(getImageArray());
			oks.setUrl(Const.SERVER_BASE_PATH + Const.DOWNLOAD);
			// comment是我对这条分享的评论，仅在人人网和QQ空间使用
			// oks.setComment("我是测试评论文本");
			// site是分享此内容的网站名称，仅在QQ空间使用
			oks.setSite(getString(R.string.app_name));
			// siteUrl是分享此内容的网站地址，仅在QQ空间使用
			oks.setSiteUrl(Const.SERVER_BASE_PATH + Const.DOWNLOAD);
			// url仅在微信（包括好友和朋友圈）中使用
			// 令编辑页面显示为Dialog模式
			oks.setDialogMode();
			// 启动分享GUI
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
		List<String> images = discount.getDiscount_photo();
		String[] imageArray = new String[images.size()];
		int count = 0;
		for (String imageUrl : images) {
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
		String url = Const.SERVER_BASE_PATH + Const.CAR_WASH_DISCOUNT;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("discount_id", "" + discount.getDiscount_id());
		request.setValueForKey(Const.KEY_LONGITUDE,
				"" + MyApplication.getInstance().user.mCurrentLongitude);
		request.setValueForKey(Const.KEY_LATITUDE,
				"" + MyApplication.getInstance().user.mCurrentLantitude);
		JSONObject jsonObj = null;
		jsonObj = (JSONObject) request
				.execForJSONObject(CarWashDiscountDetailActivity.this);
		Message msgMessage = Message.obtain();
		msgMessage.what = RELOAD_DATA;
		msgMessage.obj = jsonObj;
		mHandler.sendMessage(msgMessage);
	}

	private static class DiscountDetailHandler extends Handler {
		WeakReference<CarWashDiscountDetailActivity> mActivity;

		DiscountDetailHandler(CarWashDiscountDetailActivity activity) {
			mActivity = new WeakReference<CarWashDiscountDetailActivity>(
					activity);
		}

		@Override
		public void handleMessage(Message msg) {
			CarWashDiscountDetailActivity theActivity = mActivity.get();
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
					Discount discount = theActivity.discount;
					JSONObject jsonObject = (JSONObject) msg.obj;
					if (jsonObject.optInt("status") == 1) {
						Boolean fBoolean = jsonObject.optBoolean("followed");
						discount.setFollowed(fBoolean);
						discount.setFollow_count(jsonObject
								.optInt("follow_count"));
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

	private Discount createDiscount(JSONObject item) throws Exception {
		discount.setShop_name(item.optString("name"));
		discount.setShop_photo(item.optString("shop_photo"));
		discount.setAddress(item.optString("address"));
		discount.setDiscount_id(item.optInt("discount_id"));
		discount.setDistance(item.optInt("distance"));
		discount.setOrder_count(item.optInt("order_count"));
		discount.setShop_id(item.optInt("shop_id"));
		discount.setTitle(item.optString("title"));
		discount.setAverage_grade(item.optInt("average_grade"));
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
		discount.setIntegral(item.optInt("integral"));
		JSONArray jsonArray = item.optJSONArray("discount_photo");
		List<String> imagesList = discount.getDiscount_photo();
		imagesList.clear();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject tmpJsonObject = jsonArray.optJSONObject(i);
			imagesList.add(tmpJsonObject.optString("original"));
		}
		jsonArray = item.optJSONArray("discount_types");
		List<String> discount_types = discount.getDiscount_types();
		discount_types.clear();
		for (int i = 0; i < jsonArray.length(); i++) {
			discount_types.add(jsonArray.optString(i));
		}
		JSONObject book_left_JsonObject = item.optJSONObject("book_left");
		JSONArray tmp = book_left_JsonObject.optJSONArray("today");
		for (int i = 0; i < 9; i++) {
			book_left[0][i] = tmp.optInt(i);
		}
		tmp = book_left_JsonObject.optJSONArray("tomorrow");
		for (int i = 0; i < 9; i++) {
			book_left[1][i] = tmp.optInt(i);
		}
		return discount;
	}

	public void goBack(View view) {
		onBackPressed();
	}

	public void addressOnClick(View view) {
		Intent intent = new Intent(this, RoutePlanActivity.class);
		intent.putExtra("latitude", discount.getLatitude());
		intent.putExtra("longitude", discount.getLongitude());
		intent.putExtra("address", discount.getAddress());
		startActivity(intent);
	}

	public void phoneOnClick(View view) {
		Intent intent = new Intent();

		// 系统默认的action，用来打开默认的电话界面
		intent.setAction(Intent.ACTION_DIAL);

		// 需要拨打的号码
		String telephone_areq_code = discount.getTelephone_area_code();
		String telephone_number = discount.getTelephone_number();
		intent.setData(Uri.parse("tel:" + telephone_areq_code
				+ telephone_number));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

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
							CarWashDiscountDetailActivity.this,
							R.anim.base_slide_bottom_out);
					popupWindow.startAnimation(push_left_in);
					root.removeView(popupWindow);
				}
			});

			View view = LayoutInflater.from(mContext).inflate(
					R.layout.car_wash_discount_popup_view_layout, null);
			ImageView close_button = (ImageView) view
					.findViewById(R.id.close_button);
			close_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Animation push_left_in = AnimationUtils.loadAnimation(
							CarWashDiscountDetailActivity.this,
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
								CarWashDiscountDetailActivity.this,
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
						String result = CarWashDiscountDetailActivity.this
								.httpOrder();
						msg.obj = result;

					} catch (ConnectTimeoutException e) {
						e.printStackTrace();
						msg.what = TIME_OUT;
					} catch (Exception e) {
						e.printStackTrace();
						msg.what = TIME_OUT;
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

	protected String httpOrder() throws Exception {
		// TODO Auto-generated method stub
		String url = Const.SERVER_BASE_PATH + Const.CAR_WASH_ORDER;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("discount_id", "" + discount.getDiscount_id());
		request.setValueForKey("day_index", "" + time_zone_select_index);
		request.setValueForKey("time_zone", "" + (time_index + 9));
		JSONObject json = (JSONObject) request
				.execForJSONObject(CarWashDiscountDetailActivity.this);
		if (json.optInt("status") == 1) {
			// 更新积分
			MyApplication.getInstance().user.integral -= discount.getIntegral();
			MyApplication.getInstance().saveLoginData();
			// 更新剩余预约量
			if (json.has("left_number") && time_index != -1) {
				book_left[time_zone_select_index][time_index] = json
						.optInt("left_number");

			}
			// 更新历史订单量
			discount.setOrder_count(discount.getOrder_count() + 1);
			order_count.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					order_count.setText("总订单量:" + discount.getOrder_count());
				}
			});
		}
		return json.getString("message");
	}

	private void updatePopuoView() {
		// TODO Auto-generated method stub
		setViewImage(logoImageView, imageList.get(0));
		if (StringUtils.isEmpty(discount.getSale_price())) {
			priceTextView.setText("价格电话详谈");
		} else {
			priceTextView.setText("￥" + discount.getSale_price());
		}
		need_ji_fenTextView.setText(discount.getIntegral() + "");
		left_ji_fenTextView.setText(MyApplication.getInstance().user.integral
				+ "");
		updateLeftNumber();
	}

	private void updateLeftNumber() {
		if (time_index != -1) {
			lefTextView.setText(""
					+ book_left[time_zone_select_index][time_index]);
		} else {
			lefTextView.setText("");
		}
		if (time_desc != null) {
			chose_time.setText(time_desc);
		} else {
			chose_time.setText("请选择时间");
		}
	}

	private void initPopupView(final View view) {
		// TODO Auto-generated method stub
		logoImageView = (ImageView) view.findViewById(R.id.image);
		priceTextView = (TextView) view.findViewById(R.id.price);
		nestGridView = (NestGridView) view.findViewById(R.id.type_grid);
		lefTextView = (TextView) view.findViewById(R.id.left_number);
		myGridAdapter = new MyGridAdapter(this, discount);
		nestGridView.setAdapter(myGridAdapter);
		need_ji_fenTextView = (TextView) view.findViewById(R.id.need_ji_fen);
		left_ji_fenTextView = (TextView) view.findViewById(R.id.left_ji_fen);
		time_zone_index = (RadioGroup) view.findViewById(R.id.time_zone_index);
		chose_time = (TextView) view.findViewById(R.id.chose_time);

		for (int i = 0; i < 9; i++) {
			int resID1 = getResources().getIdentifier(
					"cb_clock_popupwindow_select_time_" + i, "id",
					getPackageName());
			CheckBox checkBox = (CheckBox) view.findViewById(resID1);
			final int index = i;
			checkBox.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (((CheckBox) v).isChecked()) {
						if (time_index != -1) {
							int resID1 = getResources().getIdentifier(
									"cb_clock_popupwindow_select_time_"
											+ time_index, "id",
									getPackageName());
							CheckBox checkBox = (CheckBox) view
									.findViewById(resID1);
							checkBox.setChecked(false);
						}
						time_index = index;
						time_desc = ((CheckBox) v).getText().toString();
					} else {
						time_index = -1;
						time_desc = null;
					}
					updateLeftNumber();
				}
			});
		}
		time_zone_index
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						if (R.id.rb_filter_date_day_0 == checkedId) {
							time_zone_select_index = 0;
							Date todayDate = new Date();
							Calendar calendar = GregorianCalendar.getInstance();
							calendar.setTime(todayDate); // assigns calendar to
															// given date
							int default_time_zone = calendar
									.get(Calendar.HOUR_OF_DAY);
							default_time_zone = (default_time_zone - 9) > 0 ? default_time_zone - 9
									: 0;
							default_time_zone = default_time_zone <= 9 ? default_time_zone
									: 9;
							for (int i = 0; i < default_time_zone; i++) {
								int resID1 = getResources()
										.getIdentifier(
												"cb_clock_popupwindow_select_time_"
														+ i, "id",
												getPackageName());
								final CheckBox checkBox = (CheckBox) view
										.findViewById(resID1);
								checkBox.setClickable(false);
								checkBox.setEnabled(false);
							}

						} else {
							time_zone_select_index = 1;
							for (int i = 0; i < 9; i++) {
								int resID1 = getResources()
										.getIdentifier(
												"cb_clock_popupwindow_select_time_"
														+ i, "id",
												getPackageName());
								final CheckBox checkBox = (CheckBox) view
										.findViewById(resID1);
								checkBox.setClickable(true);
								checkBox.setEnabled(true);
							}
						}
						if (time_index != -1) {
							int resID1 = getResources().getIdentifier(
									"cb_clock_popupwindow_select_time_"
											+ time_index, "id",
									getPackageName());
							CheckBox checkBox = (CheckBox) view
									.findViewById(resID1);
							checkBox.setChecked(false);
						}
					}
				});
		Date todayDate = new Date();
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(todayDate); // assigns calendar to
										// given date
		int default_time_zone = calendar.get(Calendar.HOUR_OF_DAY);
		default_time_zone = (default_time_zone - 9) > 0 ? default_time_zone - 9
				: 0;
		default_time_zone = default_time_zone <= 9 ? default_time_zone : 9;
		for (int i = 0; i < default_time_zone; i++) {
			int resID1 = getResources().getIdentifier(
					"cb_clock_popupwindow_select_time_" + i, "id",
					getPackageName());
			final CheckBox checkBox = (CheckBox) view.findViewById(resID1);
			checkBox.setClickable(false);
			checkBox.setEnabled(false);
		}

	}

	private boolean validateInput() {
		String msg = null;
		boolean result = true;
		if (time_index == -1) {
			result = false;
			msg = "请先选择预约时间!";
		}
		if (discount.getIntegral() > MyApplication.getInstance().user.integral) {
			result = false;
			msg = "剩余积分不足!";
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
