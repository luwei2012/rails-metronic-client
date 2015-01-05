package com.yuyaa.awashcar.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yuyaa.awashcar.R;

public class HomeFragment extends BaseFragment {

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
		if (currentFragment != null) {
			currentFragment.setUserVisibleHint(isVisibleToUser);
			if (!(currentFragment instanceof MainFragment)) {
				if (mViewPager != null) {
					mViewPager.setCurrentItem(VIEW_PAGER_INDEX_MAIN);
				}
			}
		}
	}

	private static HomeFragment mHomeFragment;
	public ViewPager mViewPager;
	public FragmentManager mFragmentManager;
	private View contentView;

	public static final int VIEW_PAGER_INDEX_BRAND_SERVICE = 0;
	public static final int VIEW_PAGER_INDEX_MAIN = 1;
	public static final int VIEW_PAGER_INDEX_WASH_CAR = 2;
	private BaseFragment currentFragment;

	public static synchronized HomeFragment newInstance() {
		return newInstance(VIEW_PAGER_INDEX_MAIN);
	}

	public static synchronized HomeFragment newInstance(int index) {
		if (mHomeFragment == null) {
			mHomeFragment = new HomeFragment();
			Bundle args = new Bundle();
			args.putInt("index", index);
			mHomeFragment.setArguments(args);
		}

		return mHomeFragment;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mHomeFragment = null;
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	public HomeFragment() {
		// Required empty public constructor
		super();
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		currentFragment = null;
		contentView = inflater
				.inflate(R.layout.fragment_home, container, false);
		findViews();
		init();
		return contentView;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void init() {
		mFragmentManager = getChildFragmentManager();
		final MyViewPagerAdapter mpa = new MyViewPagerAdapter(mFragmentManager);
		mViewPager.setAdapter(mpa);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int pageIndex) {
				currentFragment = (BaseFragment) mpa.getItem(pageIndex);

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		mViewPager.setCurrentItem(getShownIndex());
	}

	private void findViews() {
		mViewPager = (ViewPager) contentView
				.findViewById(R.id.viewPager_home_fragment);
		mViewPager.setOffscreenPageLimit(3);
	}

	private final class MyViewPagerAdapter extends FragmentPagerAdapter {

		public MyViewPagerAdapter(android.support.v4.app.FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// TODO Auto-generated method stub
			BaseFragment fragment = null;
			switch (position) {
			case VIEW_PAGER_INDEX_BRAND_SERVICE:
				fragment = BrandServiceFragment.newInstance();
				break;
			case VIEW_PAGER_INDEX_MAIN:
				fragment = MainFragment.newInstance(mHomeFragment);
				break;
			case VIEW_PAGER_INDEX_WASH_CAR:
				fragment = WashCarListFragment.newInstance();
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
