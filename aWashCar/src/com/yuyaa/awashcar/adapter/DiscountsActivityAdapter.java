package com.yuyaa.awashcar.adapter;

import java.lang.ref.WeakReference;
import java.util.List;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.baidu.navisdk.util.common.StringUtils;
import com.koushikdutta.ion.Ion;
import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.activity.LoginActivity;
import com.yuyaa.awashcar.entity.Discount;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.GOVHttp;
import com.yuyaa.awashcar.util.ThreadPoolUtils;

public class DiscountsActivityAdapter<T> extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<T> listContent;
	private int[] mLayoutResourceIds;
	private WeakReference<Context> mContext;
	public static final int TIME_OUT = 0;
	public static final int FOLLOW_END = 1;
	public static final int ZAN_END = 2;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case FOLLOW_END:
				int position = msg.arg1;
				try {
					Discount discount = (Discount) listContent.get(position);
					JSONObject jsonObject = (JSONObject) msg.obj;
					if (jsonObject.optInt("status") == 1) {
						discount.setFollowed(jsonObject.optBoolean("followed"));
						discount.setFollow_count(jsonObject
								.optInt("follow_count"));
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					notifyDataSetChanged();
				}
				break;

			case ZAN_END:
				int position1 = msg.arg1;
				try {
					Discount discount1 = (Discount) listContent.get(position1);
					JSONObject jsonObject1 = (JSONObject) msg.obj;
					if (jsonObject1.optInt("status") == 1) {
						discount1.setPraised(jsonObject1.optBoolean("praised"));
						discount1.setPraise_count(jsonObject1
								.optInt("praise_count"));
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					notifyDataSetChanged();
				}

				break;
			case TIME_OUT:
			default:
				Toast.makeText(
						mContext.get(),
						MyApplication.getInstance().getString(
								R.string.net_connect_time_out),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

	};

	private void httpFollow(Boolean flag, int position) {
		String url = Const.SERVER_BASE_PATH + Const.FOLLOW_DISCOUNT;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("flag", "" + flag);
		Discount discount = (Discount) listContent.get(position);
		request.setValueForKey("discount_id", "" + discount.getDiscount_id());
		JSONObject json = (JSONObject) request
				.execForJSONObject(mContext.get());
		Message msg = Message.obtain(mHandler, FOLLOW_END, position, 0, json);
		mHandler.sendMessage(msg);
	}

	private void httpPraise(Boolean flag, int position) {
		String url = Const.SERVER_BASE_PATH + Const.PRAISE_DIACOUNT;
		GOVHttp request = GOVHttp.requestWithURL(url, "POST");
		request.setValueForKey("flag", "" + flag);
		Discount discount = (Discount) listContent.get(position);
		request.setValueForKey("discount_id", "" + discount.getDiscount_id());
		JSONObject json = (JSONObject) request
				.execForJSONObject(mContext.get());
		Message msg = Message.obtain(mHandler, ZAN_END, position, 0, json);
		mHandler.sendMessage(msg);
	}

	public DiscountsActivityAdapter(Context context, int[] textViewResourceId,
			List<T> objects) {
		super();
		mContext = new WeakReference<Context>(context);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayoutResourceIds = textViewResourceId;
		listContent = objects;

	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub0
		return 0;

	}

	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();

	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return mLayoutResourceIds == null ? 1 : mLayoutResourceIds.length;
	}

	class ViewHolder {
		TextView name, text_detail_body, zan_text, follow_text;
		LinearLayout type_container;
		RelativeLayout logo_container;
		View follow_layout, zan_layout, share_layout;
		ImageView zan, follow, shop_photo;
		ImageView[] stars = new ImageView[5];
	}

	@SuppressWarnings("unchecked")
	public View getView(int position, View convertView, ViewGroup parent) {
		int flag = getItemViewType(position);
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(mLayoutResourceIds[flag], null);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView
					.findViewById(R.id.merchant_list_item_name);
			viewHolder.type_container = (LinearLayout) convertView
					.findViewById(R.id.type_container);
			viewHolder.logo_container = (RelativeLayout) convertView
					.findViewById(R.id.merchant_list_item_logo);
			viewHolder.stars[0] = (ImageView) convertView
					.findViewById(R.id.merchant_list_item_star0);
			viewHolder.stars[1] = (ImageView) convertView
					.findViewById(R.id.merchant_list_item_star1);
			viewHolder.stars[2] = (ImageView) convertView
					.findViewById(R.id.merchant_list_item_star2);
			viewHolder.stars[3] = (ImageView) convertView
					.findViewById(R.id.merchant_list_item_star3);
			viewHolder.stars[4] = (ImageView) convertView
					.findViewById(R.id.merchant_list_item_star4);
			viewHolder.text_detail_body = (TextView) convertView
					.findViewById(R.id.text_detail_body);
			viewHolder.logo_container = (RelativeLayout) convertView
					.findViewById(R.id.merchant_list_item_logo);
			viewHolder.follow_layout = convertView
					.findViewById(R.id.follow_layout);
			viewHolder.zan_layout = convertView.findViewById(R.id.zan_layout);
			viewHolder.share_layout = convertView
					.findViewById(R.id.share_layout);
			viewHolder.zan = (ImageView) convertView.findViewById(R.id.zan);
			viewHolder.zan_text = (TextView) convertView
					.findViewById(R.id.zan_text);
			viewHolder.follow = (ImageView) convertView
					.findViewById(R.id.follow);
			viewHolder.follow_text = (TextView) convertView
					.findViewById(R.id.follow_text);
			viewHolder.shop_photo = (ImageView) convertView
					.findViewById(R.id.shop_photo);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Discount discount = (Discount) listContent.get(position);
		viewHolder.name.setText(discount.getShop_name());
		Ion.with(mContext.get())
				.load(Const.SERVER_BASE_PATH + discount.getShop_photo())
				.withBitmap().placeholder(R.drawable.unload_image)
				.error(R.drawable.outofmemory).animateLoad(null)
				.animateIn(null).intoImageView(viewHolder.shop_photo);
		if (StringUtils.isEmpty(discount.getContent())) {
			viewHolder.text_detail_body.setVisibility(View.GONE);
		} else {
			viewHolder.text_detail_body.setText(discount.getContent());
			viewHolder.text_detail_body.setVisibility(View.VISIBLE);
		}
		updateLinearLayout(viewHolder.type_container, discount);
		updateRelativeLayout(viewHolder.logo_container, discount);
		int average_grade = discount.getAverage_grade();
		for (int i = 0; i < viewHolder.stars.length; i++) {
			viewHolder.stars[i]
					.setBackgroundResource(R.drawable.img_star_dark_big);
		}
		for (int i = 0; i < average_grade; i++) {
			viewHolder.stars[i]
					.setBackgroundResource(R.drawable.img_star_light_big);
		}
		viewHolder.zan_layout
				.setOnClickListener(new ZanClickListener(position));
		viewHolder.share_layout.setOnClickListener(new ShareClickListener(
				position));
		viewHolder.follow_layout.setOnClickListener(new FollowClickListener(
				position));
		if (discount.getPraised()) {
			viewHolder.zan.setImageResource(R.drawable.market_icon_liked);
			viewHolder.zan_text.setTextColor(Color.rgb(255, 0, 0));
			viewHolder.zan_text.setText("" + discount.getPraise_count());
		} else {
			viewHolder.zan.setImageResource(R.drawable.market_icon_dislike);
			viewHolder.zan_text.setTextColor(Color.rgb(200, 200, 200));
			viewHolder.zan_text
					.setText(discount.getPraise_count() > 0 ? discount
							.getPraise_count() + "" : "赞");
		}
		if (discount.getFollowed()) {
			viewHolder.follow
					.setImageResource(R.drawable.img_drawer_list_item_favourite_followed);
			viewHolder.follow_text.setTextColor(Color.rgb(255, 0, 0));
			viewHolder.follow_text.setText("" + discount.getFollow_count());
		} else {
			viewHolder.follow
					.setImageResource(R.drawable.img_drawer_list_item_favourite_normal);
			viewHolder.follow_text.setTextColor(Color.rgb(200, 200, 200));
			viewHolder.follow_text.setText("收藏");
		}
		return convertView;
	}

	@SuppressLint("InflateParams")
	void updateLinearLayout(LinearLayout type_selected_container,
			Discount discount) {
		type_selected_container.removeAllViews();
		int horizontalSpacing = mContext.get().getResources()
				.getDimensionPixelSize(R.dimen.padding_white_strip);
		List<String> typeList = discount.getDiscount_types();
		for (int i = 0; i < typeList.size(); i++) {
			TextView view = (TextView) mInflater.inflate(
					R.layout.service_type_select_item, null);
			view.setText(typeList.get(i));
			android.widget.LinearLayout.LayoutParams layoutParams = new android.widget.LinearLayout.LayoutParams(
					android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
					android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(0, horizontalSpacing, horizontalSpacing,
					horizontalSpacing);
			view.setLayoutParams(layoutParams);
			type_selected_container.addView(view);
		}

	}

	void updateRelativeLayout(RelativeLayout container, Discount discount) {
		container.removeAllViews();
		List<String> imagesList = discount.getDiscount_photo();
		if (imagesList == null || imagesList.size() == 0) {
			imagesList.add(discount.getShop_photo());
		}
		int size = imagesList.size();
		int horizontalSpacing = mContext.get().getResources()
				.getDimensionPixelSize(R.dimen.padding_white_strip);
		int image_width = MyApplication.getInstance().screenWidth;
		int image_height = MyApplication.getInstance().screenHeight;
		if (size == 1) {
			image_width = (int) ((image_width - horizontalSpacing * 4) * 0.5);
			image_height = (int) (image_width * 0.75);
		} else {
			image_width = (int) ((image_width - horizontalSpacing * 6) * 0.25);
			image_height = (int) (image_width * 0.75);
		}
		int count = 0, viewId = 0x7F24FFF0;
		for (String urlString : imagesList) {
			ImageView imageView = new ImageView(mContext.get());
			imageView.setId(++viewId);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			imageView.setBackgroundResource(R.drawable.image_border);
			container.addView(imageView);
			RelativeLayout.LayoutParams layoutParams = (LayoutParams) imageView
					.getLayoutParams();
			layoutParams.width = image_width;
			layoutParams.height = image_height;
			layoutParams.setMargins(0, horizontalSpacing, horizontalSpacing,
					horizontalSpacing);
			int column = count % 3;
			int row = count / 3;
			if (row > 0) {
				layoutParams.addRule(RelativeLayout.BELOW, viewId - 3);
			}
			if (column > 0) {
				layoutParams.addRule(RelativeLayout.RIGHT_OF, viewId - 1);
			}
			Ion.with(mContext.get()).load(Const.SERVER_BASE_PATH + urlString)
					.withBitmap().placeholder(R.drawable.unload_image)
					.error(R.drawable.outofmemory).animateLoad(null)
					.animateIn(null).intoImageView(imageView);
			count++;
		}

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listContent != null ? listContent.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (listContent != null && listContent.size() > 0) {
			return listContent.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	public class ZanClickListener implements OnClickListener {

		private int position;
		private Boolean isLoading = false;

		/**
		 * @param position
		 */
		public ZanClickListener(int position) {
			super();
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// 发送Post请求
			if (!isLoading) {
				isLoading = true;
				if (MyApplication.getInstance().getStringGlobalData("session",
						null) != null) {
					Discount discount = (Discount) listContent.get(position);
					final ImageView imageView = (ImageView) v
							.findViewById(R.id.zan);
					final TextView zanTextView = (TextView) v
							.findViewById(R.id.zan_text);
					final Boolean praisedBoolean = !discount.getPraised();
					discount.setPraised(praisedBoolean);
					discount.setPraise_count(discount.getPraise_count()
							+ (praisedBoolean ? 1 : -1));
					Animation scaleAnimation = AnimationUtils.loadAnimation(
							mContext.get(), R.anim.scale);
					final String result = discount.getPraise_count() > 0 ? discount
							.getPraise_count() + ""
							: "赞";
					scaleAnimation
							.setAnimationListener(new AnimationListener() {
								@Override
								public void onAnimationStart(Animation animation) {
									// TODO Auto-generated method stub
								}

								@Override
								public void onAnimationRepeat(
										Animation animation) {
									// TODO Auto-generated method stub

								}

								@Override
								public void onAnimationEnd(Animation animation) {
									// TODO Auto-generated method stub
									if (praisedBoolean) {
										zanTextView.setTextColor(Color.rgb(255,
												0, 0));
									} else {
										zanTextView.setTextColor(Color.rgb(200,
												200, 200));
									}
									zanTextView.setText(result);
									imageView
											.setImageResource(praisedBoolean ? R.drawable.market_icon_liked
													: R.drawable.market_icon_dislike);
									Animation shrinkAnimation = AnimationUtils
											.loadAnimation(mContext.get(),
													R.anim.shrink);
									shrinkAnimation
											.setAnimationListener(new AnimationListener() {

												@Override
												public void onAnimationStart(
														Animation animation) {
													// TODO Auto-generated
													// method stub
												}

												@Override
												public void onAnimationRepeat(
														Animation animation) {
													// TODO Auto-generated
													// method stub

												}

												@Override
												public void onAnimationEnd(
														Animation animation) {
													// TODO Auto-generated
													// method stub
													isLoading = false;
												}
											});
									ThreadPoolUtils.execute(new Runnable() {
										public void run() {
											httpPraise(praisedBoolean, position);
										}
									});
									imageView.startAnimation(shrinkAnimation);
								}
							});
					imageView.startAnimation(scaleAnimation);
				} else {
					Toast toast = Toast.makeText(mContext.get(), MyApplication
							.getInstance().getString(R.string.please_log),
							Toast.LENGTH_SHORT);
					toast.setMargin(0f, 0.05f);
					toast.show();
					Intent intent = new Intent(mContext.get(),
							LoginActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					mContext.get().startActivity(intent);
				}
			}

		}
	}

	public class FollowClickListener implements OnClickListener {

		private int position;
		private Boolean isLoading = false;

		/**
		 * @param position
		 */
		public FollowClickListener(int position) {
			super();
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// 发送Post请求
			if (!isLoading) {
				isLoading = true;
				if (MyApplication.getInstance().getStringGlobalData("session",
						null) != null) {
					Discount discount = (Discount) listContent.get(position);
					final ImageView imageView = (ImageView) v
							.findViewById(R.id.follow);
					final TextView zanTextView = (TextView) v
							.findViewById(R.id.follow_text);
					final Boolean followedBoolean = !discount.getFollowed();
					discount.setFollowed(followedBoolean);
					discount.setFollow_count(discount.getFollow_count()
							+ (followedBoolean ? 1 : -1));
					Animation scaleAnimation = AnimationUtils.loadAnimation(
							mContext.get(), R.anim.scale);
					final String result = followedBoolean ? ""
							+ discount.getPraise_count() : "收藏";
					scaleAnimation
							.setAnimationListener(new AnimationListener() {
								@Override
								public void onAnimationStart(Animation animation) {
									// TODO Auto-generated method stub
								}

								@Override
								public void onAnimationRepeat(
										Animation animation) {
									// TODO Auto-generated method stub

								}

								@Override
								public void onAnimationEnd(Animation animation) {
									// TODO Auto-generated method stub
									if (followedBoolean) {
										zanTextView.setTextColor(Color.rgb(255,
												0, 0));
									} else {
										zanTextView.setTextColor(Color.rgb(200,
												200, 200));
									}
									zanTextView.setText(result);
									imageView
											.setImageResource(followedBoolean ? R.drawable.img_drawer_list_item_favourite_followed
													: R.drawable.img_drawer_list_item_favourite_normal);
									Animation shrinkAnimation = AnimationUtils
											.loadAnimation(mContext.get(),
													R.anim.shrink);
									shrinkAnimation
											.setAnimationListener(new AnimationListener() {

												@Override
												public void onAnimationStart(
														Animation animation) {
													// TODO Auto-generated
													// method stub

												}

												@Override
												public void onAnimationRepeat(
														Animation animation) {
													// TODO Auto-generated
													// method stub
												}

												@Override
												public void onAnimationEnd(
														Animation animation) {
													// TODO Auto-generated
													// method stub
													isLoading = false;
												}
											});
									ThreadPoolUtils.execute(new Runnable() {
										public void run() {
											httpFollow(followedBoolean,
													position);
										}
									});
									imageView.startAnimation(shrinkAnimation);
								}
							});
					imageView.startAnimation(scaleAnimation);
				} else {
					Toast toast = Toast.makeText(mContext.get(), MyApplication
							.getInstance().getString(R.string.please_log),
							Toast.LENGTH_SHORT);
					toast.setMargin(0f, 0.05f);
					toast.show();
					Intent intent = new Intent(mContext.get(),
							LoginActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					mContext.get().startActivity(intent);
				}
			}

		}

	}

	public class ShareClickListener implements OnClickListener {

		private int position;

		/**
		 * @param position
		 */
		public ShareClickListener(int position) {
			super();
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (MyApplication.getInstance()
					.getStringGlobalData("session", null) != null) {
				Discount discount = (Discount) listContent.get(position);
				ShareSDK.initSDK(mContext.get());
				OnekeyShare oks = new OnekeyShare();
				// 关闭sso授权
				oks.disableSSOWhenAuthorize();

				// 分享时Notification的图标和文字
				oks.setNotification(R.drawable.ic_launcher, mContext.get()
						.getString(R.string.app_name));
				// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
				oks.setTitle(discount.getTitle());
				// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
				// oks.setTitleUrl("http://sharesdk.cn");
				// text是分享文本，所有平台都需要这个字段
				oks.setText(discount.getContent());
				// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
				oks.setImageUrl(Const.SERVER_BASE_PATH
						+ discount.getShop_photo());
				// url仅在微信（包括好友和朋友圈）中使用
				// oks.setUrl("http://sharesdk.cn");
				// comment是我对这条分享的评论，仅在人人网和QQ空间使用
				// oks.setComment("我是测试评论文本");
				// site是分享此内容的网站名称，仅在QQ空间使用
				oks.setSite(mContext.get().getString(R.string.app_name));
				// siteUrl是分享此内容的网站地址，仅在QQ空间使用
				// oks.setSiteUrl(Const.SERVER_BASE_PATH);
				// 启动分享GUI
				oks.show(mContext.get());
			} else {
				Toast toast = Toast.makeText(mContext.get(), MyApplication
						.getInstance().getString(R.string.please_log),
						Toast.LENGTH_SHORT);
				toast.setMargin(0f, 0.05f);
				toast.show();
				Intent intent = new Intent(mContext.get(), LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mContext.get().startActivity(intent);
			}

		}

	}

}
