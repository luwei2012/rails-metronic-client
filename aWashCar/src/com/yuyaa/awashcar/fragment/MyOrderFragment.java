package com.yuyaa.awashcar.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.widget.SegmentedRadioGroup;

public class MyOrderFragment extends BaseFragment {
	private View contentView;
	public ViewPager mViewPager;
	public FragmentManager mFragmentManager;
	private static MyOrderFragment myOrderFragment;
	private SegmentedRadioGroup typeGroup;
	private BaseFragment currentFragment;
	private MyViewPagerAdapter mpa;
	public static final int VIEW_PAGER_INDEX_DISCOUNTS_ALL = 0;
	public static final int VIEW_PAGER_INDEX_DISCOUNTS_WAIT = 1;
	public static final int VIEW_PAGER_INDEX_DISCOUNTS_END = 2;

	public static synchronized MyOrderFragment newInstance(int index) {
		if (myOrderFragment == null) {
			myOrderFragment = new MyOrderFragment();
			Bundle args = new Bundle();
			args.putInt("index", index);
			myOrderFragment.setArguments(args);
		}

		return myOrderFragment;
	}

	public static synchronized MyOrderFragment newInstance() {
		return newInstance(VIEW_PAGER_INDEX_DISCOUNTS_ALL);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
		if (currentFragment != null) {
			currentFragment.setUserVisibleHint(isVisibleToUser);
		}
	}

	public MyOrderFragment() {
		// Required empty public constructor
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
		contentView = inflater.inflate(R.layout.fragment_my_order, container,
				false);
		findViews();
		init();
		return contentView;
	}

	private void findViews() {
		mViewPager = (ViewPager) contentView
				.findViewById(R.id.viewPager_home_fragment);
		mViewPager.setOffscreenPageLimit(3);
		typeGroup = (SegmentedRadioGroup) contentView
				.findViewById(R.id.radiogroup_my_favourite_fragment);
	}

	private void init() {
		mFragmentManager = getChildFragmentManager();
		mpa = new MyViewPagerAdapter(mFragmentManager);
		mViewPager.setAdapter(mpa);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int pageIndex) {
				currentFragment = (BaseFragment) mpa.getItem(pageIndex);
				if (pageIndex == 0
						&& typeGroup.getCheckedRadioButtonId() != R.id.rb_my_order_fragment_all) {
					typeGroup.check(R.id.rb_my_order_fragment_all);
				} else if (pageIndex == 1
						&& typeGroup.getCheckedRadioButtonId() != R.id.rb_my_order_fragment_wait_confirm) {
					typeGroup.check(R.id.rb_my_order_fragment_wait_confirm);
				} else if (pageIndex == 2
						&& typeGroup.getCheckedRadioButtonId() != R.id.rb_my_order_fragment_did_consume) {
					typeGroup.check(R.id.rb_my_order_fragment_did_consume);
				}

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		mViewPager.setCurrentItem(getShownIndex());
		typeGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if (checkedId == R.id.rb_my_order_fragment_all) {
					mViewPager.setCurrentItem(0);
				} else if (checkedId == R.id.rb_my_order_fragment_wait_confirm) {
					mViewPager.setCurrentItem(1);
				} else if (checkedId == R.id.rb_my_order_fragment_did_consume) {
					mViewPager.setCurrentItem(2);
				}
			}
		});
		currentFragment = MyOrderDiscountsAllFragment.newInstance();
	}

	private class MyViewPagerAdapter extends FragmentPagerAdapter {

		public MyViewPagerAdapter(android.support.v4.app.FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// TODO Auto-generated method stub
			BaseFragment fragment = null;
			switch (position) {
			case VIEW_PAGER_INDEX_DISCOUNTS_ALL:
				fragment = MyOrderDiscountsAllFragment.newInstance();
				break;
			case VIEW_PAGER_INDEX_DISCOUNTS_WAIT:
				fragment = MyOrderDiscountsWaitFragment.newInstance();
				break;
			case VIEW_PAGER_INDEX_DISCOUNTS_END:
				fragment = MyOrderDiscountsEndFragment.newInstance();
				break;
			default:
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 3;
		}
	}

	@Override
	public void xmlClickMethod(View v) {
		// TODO Auto-generated method stub
		if (null != currentFragment) {
			currentFragment.xmlClickMethod(v);
		}
	}
}
