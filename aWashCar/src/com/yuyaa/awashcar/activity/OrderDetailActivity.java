package com.yuyaa.awashcar.activity;

import java.lang.ref.WeakReference;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;
import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.entity.Discount;
import com.yuyaa.awashcar.entity.Order;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.StringUtils;
import com.yuyaa.awashcar.util.ThreadPoolUtils;
import com.yuyaa.awashcar.widget.MyProgressDialog;

public class OrderDetailActivity extends BaseActivity {
	public Order order;
	public ImageView discount_photo;
	public TextView title, price, status, order_number, updated_at, book_time;
	public ImageView[] stars = new ImageView[5];
	public RelativeLayout book_time_layout, grade_layout;
	public Button comment, cancel;
	public MyProgressDialog progressDialog;
	public Boolean isLoading = false;
	private OrderDetailHandler mHandler;
	RelativeLayout headLayout;
	private int average_grade;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_detail);
		init();
		mHandler = new OrderDetailHandler(this);
		startLoadingThread();
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
		String url = Const.SERVER_BASE_PATH + Const.ORDER_DETAIL;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("order_id", "" + order.getOrder_id());
		JSONObject jsonObj = null;
		jsonObj = (JSONObject) request
				.execForJSONObject(OrderDetailActivity.this);
		Message msgMessage = Message.obtain();
		msgMessage.what = RELOAD_DATA;
		msgMessage.obj = jsonObj;
		mHandler.sendMessage(msgMessage);
	}

	private void findViews() {
		discount_photo = (ImageView) findViewById(R.id.discount_photo);
		title = (TextView) findViewById(R.id.discount_title);
		price = (TextView) findViewById(R.id.merchant_list_item_price);
		status = (TextView) findViewById(R.id.merchant_list_item_status);
		order_number = (TextView) findViewById(R.id.order_number);
		updated_at = (TextView) findViewById(R.id.updated_at);
		stars[0] = (ImageView) findViewById(R.id.merchant_list_item_star0);
		stars[1] = (ImageView) findViewById(R.id.merchant_list_item_star1);
		stars[2] = (ImageView) findViewById(R.id.merchant_list_item_star2);
		stars[3] = (ImageView) findViewById(R.id.merchant_list_item_star3);
		stars[4] = (ImageView) findViewById(R.id.merchant_list_item_star4);
		comment = (Button) findViewById(R.id.grade_button);
		cancel = (Button) findViewById(R.id.cancel_button);
		book_time_layout = (RelativeLayout) findViewById(R.id.book_time_layout);
		grade_layout = (RelativeLayout) findViewById(R.id.grade_layout);
		book_time = (TextView) findViewById(R.id.book_time);
		headLayout = (RelativeLayout) findViewById(R.id.head);
	}

	public void refreshUI() {
		Ion.with(OrderDetailActivity.this)
				.load(Const.SERVER_BASE_PATH + order.getDiscount_photo())
				.withBitmap().placeholder(R.drawable.unload_image)
				.error(R.drawable.outofmemory).animateLoad(null)
				.animateIn(null).intoImageView(discount_photo);
		if (order.getIs_car_wash()) {
			book_time_layout.setVisibility(View.VISIBLE);
			book_time.setText(StringUtils.toDate(order.getBook_time()));
			title.setText(order.getShop_name());
		} else {
			book_time_layout.setVisibility(View.GONE);
			title.setText(order.getTitle());
		}
		order_number.setText(order.getOrder_number());
		if (StringUtils.isEmpty(order.getPrice())) {
			price.setText("未设置");
		} else {
			price.setText("￥" + order.getPrice());
		}
		if (order.getStatus() == 0) {
			status.setText("未消费");
			grade_layout.setVisibility(View.GONE);
			cancel.setVisibility(View.VISIBLE);
		} else if (order.getStatus() == 1) {
			status.setText("等待确认");
			grade_layout.setVisibility(View.GONE);
			cancel.setVisibility(View.VISIBLE);
		} else if (order.getStatus() == 2) {
			status.setText("已消费");
			grade_layout.setVisibility(View.VISIBLE);
			cancel.setVisibility(View.GONE);
		} else if (order.getStatus() == 3) {
			status.setText("已取消");
			grade_layout.setVisibility(View.GONE);
			cancel.setVisibility(View.GONE);
		} else {
			status.setText("已关闭");
			grade_layout.setVisibility(View.GONE);
			cancel.setVisibility(View.GONE);
		}
		updated_at.setText(StringUtils.friendly_time(order.getUpdated_at()));
		Boolean flag = false;
		if (!order.getIs_graded() && order.getStatus() == 2) {
			flag = true;
			comment.setVisibility(View.VISIBLE);

		} else {
			comment.setVisibility(View.GONE);
		}
		for (int i = 0; i < stars.length; i++) {
			stars[i].setImageResource(R.drawable.img_star_dark_big);
			stars[i].setClickable(flag);
		}
		for (int i = 0; i < order.getAverage_grade(); i++) {
			stars[i].setImageResource(R.drawable.img_star_light_big);
		}
	}

	private void init() {
		findViews();
		Intent intent = getIntent();
		order = intent.getExtras().getParcelable("order");
		LayoutParams layoutParams = (LayoutParams) discount_photo
				.getLayoutParams();
		int width = (int) (Const.VIEW_PGAER_WIDTH * 0.3);
		int height = (int) (width * 0.75);
		if (layoutParams == null) {
			layoutParams = new LayoutParams(width, height);
			discount_photo.setLayoutParams(layoutParams);
		} else {
			layoutParams.width = width;
			layoutParams.height = height;
		}
		headLayout.invalidate();
		average_grade = order.getAverage_grade();
		refreshUI();
	}

	public void clickCallback(View view) {
		switch (view.getId()) {
		case R.id.btn_title_bar_back:
			onBackPressed();
			break;
		case R.id.merchant_list_item_star0:
			average_grade = 1;
			updateStars();

			break;
		case R.id.merchant_list_item_star1:
			average_grade = 2;
			updateStars();

			break;
		case R.id.merchant_list_item_star2:
			average_grade = 3;
			updateStars();

			break;
		case R.id.merchant_list_item_star3:
			average_grade = 4;
			updateStars();

			break;
		case R.id.merchant_list_item_star4:
			average_grade = 5;
			updateStars();

			break;
		default:
			break;
		}

	}

	private void updateStars() {
		// TODO Auto-generated method stub
		for (int i = 0; i < stars.length; i++) {
			stars[i].setImageResource(R.drawable.img_star_dark_big);
		}
		for (int i = 0; i < average_grade; i++) {
			stars[i].setImageResource(R.drawable.img_star_light_big);
		}
	}

	public void go_to_discount(View view) {
		if (order.getDiscount_id() != 0) {
			Discount discount = new Discount();
			discount.setDiscount_id(order.getDiscount_id());
			discount.setPrice(order.getPrice());
			discount.setShop_name(order.getShop_name());
			discount.setTitle(order.getTitle());
			discount.setIntegral(order.getIntegral());
			discount.setAverage_grade(order.getAverage_grade());
			discount.setShop_photo(order.getDiscount_photo());
			Intent intent;
			if (order.getIs_car_wash()) {
				intent = new Intent(OrderDetailActivity.this,
						CarWashDiscountDetailActivity.class);
			} else {
				intent = new Intent(OrderDetailActivity.this,
						DiscountDetailActivity.class);
			}
			Bundle bundle = new Bundle();
			bundle.putParcelable("discount", discount);
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}

	public void comment(View view) {
		progressDialog = new MyProgressDialog(this,
				R.style.CustomProgressDialog);
		progressDialog.setTitle("正在提交评价...");
		progressDialog.setCancelable(false);
		progressDialog.show();
		ThreadPoolUtils.execute(new Runnable() {

			@Override
			public void run() {
				try {
					httpComment();
				} catch (ConnectTimeoutException e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(TIME_OUT);
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
					// msg.what=LOGIN_ERROR;
				}
			}
		});
	}

	public void cancel(View view) {
		final View tmp = view;
		AlertDialog.Builder builder = new Builder(this);
		String message;
		if (order.getIntegral() != 0) {
			message = "下订单消耗了" + order.getIntegral() + "积分，取消订单积分不会返回！";
		} else {
			message = "正在等待商家确认中，确定要取消么？";
		}
		builder.setMessage(message);
		builder.setTitle("提示");
		builder.setPositiveButton("任性",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						progressDialog = new MyProgressDialog(
								OrderDetailActivity.this,
								R.style.CustomProgressDialog);
						progressDialog.setTitle("正在取消订单...");
						progressDialog.setCancelable(false);
						progressDialog.show();
						comment.setVisibility(View.GONE);
						tmp.setVisibility(View.GONE);
						for (int i = 0; i < stars.length; i++) {
							stars[i].setClickable(false);
						}
						ThreadPoolUtils.execute(new Runnable() {

							@Override
							public void run() {
								try {
									httpCancel();
								} catch (ConnectTimeoutException e) {
									e.printStackTrace();
									mHandler.sendEmptyMessage(TIME_OUT);
								} catch (NullPointerException e) {
									e.printStackTrace();
								} catch (JSONException e) {
									e.printStackTrace();
								} catch (Exception e) {
									e.printStackTrace();
									// msg.what=LOGIN_ERROR;
								}
							}
						});
						dialog.dismiss();
					}

				});
		builder.setNegativeButton("算了吧",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				});
		builder.create().show();
	}

	private void httpCancel() throws Exception {

		String url = Const.SERVER_BASE_PATH + Const.CANCEL_ORDER;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("order_id", "" + order.getOrder_id());
		JSONObject json = (JSONObject) request
				.execForJSONObject(OrderDetailActivity.this);
		Message msg = Message.obtain(mHandler, CANCEL_END, 0, 0, json);
		mHandler.sendMessage(msg);
	}

	private void httpComment() throws Exception {

		String url = Const.SERVER_BASE_PATH + Const.COMMENT_ORDER;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("order_id", "" + order.getOrder_id());
		request.setValueForKey("average_grade", "" + average_grade);
		JSONObject json = (JSONObject) request
				.execForJSONObject(OrderDetailActivity.this);
		Message msg = Message.obtain(mHandler, COMMENT_END, 0, 0, json);
		mHandler.sendMessage(msg);
	}

	private static class OrderDetailHandler extends Handler {
		WeakReference<OrderDetailActivity> mActivity;

		OrderDetailHandler(OrderDetailActivity activity) {
			mActivity = new WeakReference<OrderDetailActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			OrderDetailActivity theActivity = mActivity.get();
			if (theActivity.progressDialog != null) {
				theActivity.progressDialog.dismiss();
			}
			switch (msg.what) {

			case LOADING_FINISHED:
				// 更新显示的内容
				theActivity.isLoading = false;
				theActivity.refreshUI();
				break;
			case COMMENT_END:
				JSONObject jsonObj1 = (JSONObject) msg.obj;
				theActivity.order
						.setAverage_grade(jsonObj1.optInt("commented"));
				theActivity.order.setIs_graded(true);
				Toast.makeText(theActivity,
						(String) jsonObj1.optString("message"),
						Toast.LENGTH_SHORT).show();
				theActivity.refreshUI();
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
						theActivity.createOrder(jsonObj.optJSONObject("data"));
					} else {
						Toast.makeText(theActivity,
								jsonObj.optString("message"),
								Toast.LENGTH_SHORT).show();
					}

				} catch (Exception e) {
					e.printStackTrace();

				} finally {
					Message msgMessage = Message.obtain();
					msgMessage.what = LOADING_FINISHED;
					this.sendMessage(msgMessage);
				}

				break;
			case CANCEL_END:
				JSONObject jsonObj11 = (JSONObject) msg.obj;
				theActivity.order.setStatus(jsonObj11.optInt("canceled"));
				Toast.makeText(theActivity,
						(String) jsonObj11.optString("message"),
						Toast.LENGTH_SHORT).show();
				theActivity.refreshUI();

				break;

			default:
				break;
			}

			super.handleMessage(msg);

		}
	}

	public void createOrder(JSONObject item) throws JSONException {
		// TODO Auto-generated method stub
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
		order.setBook_time(item.optInt("book_time"));
		order.setIs_graded(item.optBoolean("is_graded"));
		order.setAverage_grade(item.optInt("average_grade"));
		order.setIs_car_wash(item.optBoolean("is_car_wash"));
	};

}
