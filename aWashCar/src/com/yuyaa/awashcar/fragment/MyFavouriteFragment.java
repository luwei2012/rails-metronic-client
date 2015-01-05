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

public class MyFavouriteFragment extends BaseFragment {
	private View contentView;
	public ViewPager mViewPager;
	public FragmentManager mFragmentManager;
	private static MyFavouriteFragment myFavouriteFragment;
	private SegmentedRadioGroup typeGroup;
	private BaseFragment currentFragment;
	MyViewPagerAdapter mpa;
	public static final int VIEW_PAGER_INDEX_SHOPS = 0;
	public static final int VIEW_PAGER_INDEX_DISCOUNTS = 1;

	public static synchronized MyFavouriteFragment newInstance(int index) {
		if (myFavouriteFragment == null) {
			myFavouriteFragment = new MyFavouriteFragment();
			Bundle args = new Bundle();
			args.putInt("index", index);
			myFavouriteFragment.setArguments(args);
		}

		return myFavouriteFragment;
	}

	public static synchronized MyFavouriteFragment newInstance() {
		return newInstance(VIEW_PAGER_INDEX_SHOPS);
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
		if (currentFragment != null) {
			currentFragment.setUserVisibleHint(isVisibleToUser);
		}
	}

	public MyFavouriteFragment() {
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
		contentView = inflater.inflate(R.layout.fragment_my_favourite,
				container, false);
		findViews();
		init();
		return contentView;
	}

	private void findViews() {
		mViewPager = (ViewPager) contentView
				.findViewById(R.id.viewPager_home_fragment);
		mViewPager.setOffscreenPageLimit(2);
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
						&& typeGroup.getCheckedRadioButtonId() != R.id.rb_my_favourite_fragment_merchant) {
					typeGroup.check(R.id.rb_my_favourite_fragment_merchant);
				} else if (pageIndex == 1
						&& typeGroup.getCheckedRadioButtonId() != R.id.rb_my_favourite_fragment_service) {
					typeGroup.check(R.id.rb_my_favourite_fragment_service);
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
				if (checkedId == R.id.rb_my_favourite_fragment_merchant) {
					mViewPager.setCurrentItem(0);
				} else if (checkedId == R.id.rb_my_favourite_fragment_service) {
					mViewPager.setCurrentItem(1);
				}
			}
		});
		currentFragment = MyFavouriteShopsFragment.newInstance();
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
			case VIEW_PAGER_INDEX_SHOPS:
				fragment = MyFavouriteShopsFragment.newInstance();
				break;
			case VIEW_PAGER_INDEX_DISCOUNTS:
				fragment = MyFavouriteDiscountsFragment.newInstance();
				break;
			default:
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
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
