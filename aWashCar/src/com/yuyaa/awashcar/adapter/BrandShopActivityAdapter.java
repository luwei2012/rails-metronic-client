package com.yuyaa.awashcar.adapter;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.entity.Shop;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.StringUtils;

public class BrandShopActivityAdapter<T> extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<T> listContent;
	private int[] mLayoutResourceIds;
	private WeakReference<Context> mContext;

	public BrandShopActivityAdapter(Context context, int[] textViewResourceId,
			List<T> objects, ListView listView) {
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
		TextView name, distance, address;
		ImageView logo;
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
			viewHolder.distance = (TextView) convertView
					.findViewById(R.id.merchant_list_item_distance);
			viewHolder.address = (TextView) convertView
					.findViewById(R.id.merchant_list_item_address);
			viewHolder.logo = (ImageView) convertView
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
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Shop shop = (Shop) listContent.get(position);
		viewHolder.name.setText(shop.getName());
		viewHolder.address.setText(shop.getAddress());
		viewHolder.distance.setText(StringUtils.friendly_distance(shop
				.getDistance()));
		int average_grade = shop.getAverage_grade();
		for (int i = 0; i < viewHolder.stars.length; i++) {
			viewHolder.stars[i]
					.setBackgroundResource(R.drawable.img_star_dark_big);
		}
		for (int i = 0; i < average_grade; i++) {
			viewHolder.stars[i]
					.setBackgroundResource(R.drawable.img_star_light_big);
		}
		String image_path = shop.getShop_photo();
		viewHolder.logo.setTag(image_path + position);
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
