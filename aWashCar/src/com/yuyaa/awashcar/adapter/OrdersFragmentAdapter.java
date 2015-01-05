package com.yuyaa.awashcar.adapter;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.entity.Order;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.StringUtils;

public class OrdersFragmentAdapter<T> extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<T> listContent;
	private int[] mLayoutResourceIds;
	private WeakReference<Context> mContext;

	public OrdersFragmentAdapter(Context context, int[] textViewResourceId,
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
		TextView name, newPrice, status;
		ImageView logo;
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
			viewHolder.newPrice = (TextView) convertView
					.findViewById(R.id.merchant_list_item_price);
			viewHolder.status = (TextView) convertView
					.findViewById(R.id.merchant_list_item_status);
			viewHolder.logo = (ImageView) convertView
					.findViewById(R.id.merchant_list_item_logo);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Order order = (Order) listContent.get(position);
		viewHolder.name.setText(order.getShop_name());
		if (StringUtils.isEmpty(order.getPrice())) {
			viewHolder.newPrice.setText("未设置");
		} else {
			viewHolder.newPrice.setText("￥" + order.getPrice());
		}
		String status;
		if (order.getStatus() == 0) {
			status = "未消费";
		} else if (order.getStatus() == 1) {
			status = "等待确认";
		} else if (order.getStatus() == 2) {
			status = "已消费";
		} else if (order.getStatus() == 3) {
			status = "已取消";
		} else {
			status = "已关闭";
		}
		viewHolder.status.setText(status);
		String image_path = order.getDiscount_photo();
		Ion.with(mContext.get()).load(Const.SERVER_BASE_PATH + image_path)
				.withBitmap().placeholder(R.drawable.unload_image)
				.error(R.drawable.outofmemory).animateLoad(null)
				.animateIn(null).intoImageView(viewHolder.logo);
		return convertView;
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

}
