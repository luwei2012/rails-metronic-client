package com.yuyaa.awashcar.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BaiduNaviManager;
import com.umeng.update.UmengUpdateAgent;
import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;
import com.yuyaa.awashcar.fragment.AboutUsFragment;
import com.yuyaa.awashcar.fragment.BaseFragment;
import com.yuyaa.awashcar.fragment.HomeFragment;
import com.yuyaa.awashcar.fragment.MyFavouriteFragment;
import com.yuyaa.awashcar.fragment.MyOrderFragment;
import com.yuyaa.awashcar.fragment.PersonalInfoFragment;
import com.yuyaa.awashcar.util.Const;
import com.yuyaa.awashcar.util.FileHelper;

public class MainActivity extends BaseActivity {

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (drawerListIcons != null) {
			drawerListIcons.recycle();
		}
	}

	private Context mContext;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private int last_position = 0;
	private String[] drawerListTitles;
	private TypedArray drawerListIcons;
	private BaseFragment currentFragment;
	private TextView title;
	private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
		public void engineInitSuccess() {
		}

		public void engineInitStart() {
		}

		public void engineInitFail() {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UmengUpdateAgent.update(this);
		setContentView(R.layout.activity_main);
		init();
		if (savedInstanceState == null) {
			selectItem(0);
		}
		BaiduNaviManager.getInstance().initEngine(this,
				new FileHelper().getStockPATH(), mNaviEngineInitListener,
				new LBSAuthManagerListener() {
					@Override
					public void onAuthResult(int status, String msg) {

					}
				});
	}

	private void findViews() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
		mDrawerListView = (ListView) findViewById(R.id.main_left_drawer_list);
		title = (TextView) findViewById(R.id.title);
	}

	private void init() {
		ShareSDK.initSDK(this);
		mContext = this;
		findViews();
		currentFragment = null;
		drawerListTitles = getResources().getStringArray(
				R.array.main_drawer_list_titles);
		drawerListIcons = getResources().obtainTypedArray(
				R.array.main_drawer_list_icons);
		mDrawerListView.setAdapter(new DrawerListAdapter(mContext));
		mDrawerListView.setOnItemClickListener(new DrawerItemClickListener());
	}

	private final class DrawerItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		// Create a new fragment and specify the planet to show based on
		// position
		BaseFragment fragment = null;
		switch (position) {
		case 0:
			fragment = HomeFragment.newInstance();
			// 主页
			last_position = 0;
			break;
		case 1:
			fragment = PersonalInfoFragment.newInstance();
			// 个人信息
			last_position = 1;

			break;
		case 2:
			if (MyApplication.getInstance()
					.getStringGlobalData("session", null) != null) {
				fragment = MyOrderFragment.newInstance();
				// 我的订单
				last_position = 2;
			} else {
				Toast toast = Toast.makeText(MainActivity.this, MyApplication
						.getInstance().getString(R.string.please_log),
						Toast.LENGTH_SHORT);
				toast.setMargin(0f, 0.05f);
				toast.show();
				Intent intent = new Intent(MainActivity.this,
						LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			break;
		case 3:
			if (MyApplication.getInstance()
					.getStringGlobalData("session", null) != null) {
				// 最爱
				fragment = MyFavouriteFragment.newInstance();
				last_position = 3;
			} else {
				Toast toast = Toast.makeText(MainActivity.this, MyApplication
						.getInstance().getString(R.string.please_log),
						Toast.LENGTH_SHORT);
				toast.setMargin(0f, 0.05f);
				toast.show();
				Intent intent = new Intent(MainActivity.this,
						LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			break;
		case 4:
			// 分享
			showShare();
			break;
		case 5:
			fragment = AboutUsFragment.newInstance();
			// 关于我们
			last_position = 5;
			break;
		default:
			fragment = HomeFragment.newInstance();
			last_position = 0;
			break;
		}
		mDrawerListView.setItemChecked(last_position, true);
		if (fragment != null) {
			switchContent(fragment);
			title.setText(drawerListTitles[last_position]);
		}
		mDrawerLayout.closeDrawer(mDrawerListView);
	}

	public void switchContent(BaseFragment to) {
		BaseFragment from = currentFragment;
		if (currentFragment != to) {
			currentFragment = to;
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction transaction = fragmentManager
					.beginTransaction().setCustomAnimations(
							android.R.anim.fade_in, android.R.anim.fade_out);
			if (!to.isAdded()) { // 先判断是否被add过
				if (from == null) {
					transaction.add(R.id.main_content_frame, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
				} else {
					from.setMenuVisibility(false);
					from.setUserVisibleHint(false);
					transaction.hide(from).add(R.id.main_content_frame, to)
							.commit(); // 隐藏当前的fragment，add下一个到Activity中
				}

			} else {
				if (from == null) {
					transaction.show(to).commit(); // 隐藏当前的fragment，显示下一个
				} else {
					from.setMenuVisibility(false);
					from.setUserVisibleHint(false);
					transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
				}

			}
			to.setMenuVisibility(true);
			to.setUserVisibleHint(true);
		}
	}

	public void clickCallback(View view) {
		switch (view.getId()) {
		case R.id.btn_drawer_switch: {
			if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			} else {
				mDrawerLayout.openDrawer(Gravity.LEFT);
			}
			break;
		}
		default:
			currentFragment.xmlClickMethod(view);
			break;
		}
	}

	private final class DrawerListAdapter extends BaseAdapter {
		private Context mContext;

		public DrawerListAdapter(Context c) {
			mContext = c;
		}

		@Override
		public int getCount() {
			return drawerListTitles.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.view_item_drawer_list, null);
				holder = new ViewHolder();
				holder.itemName = (TextView) convertView
						.findViewById(R.id.text_drawer_list_item_name);
				holder.itemIcon = (ImageView) convertView
						.findViewById(R.id.img_drawer_list_item_icon);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.itemName.setText(drawerListTitles[position]);
			holder.itemIcon.setBackgroundDrawable(drawerListIcons
					.getDrawable(position));

			return convertView;
		}

		private final class ViewHolder {
			TextView itemName;
			ImageView itemIcon;
		}

	}

	// 分享接口
	private void showShare() {
		OnekeyShare oks = new OnekeyShare();
		// 关闭sso授权
		oks.disableSSOWhenAuthorize();

		// 分享时Notification的图标和文字
		oks.setNotification(R.drawable.ic_launcher,
				getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(getString(R.string.share));
		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		// oks.setTitleUrl("http://sharesdk.cn");
		// text是分享文本，所有平台都需要这个字段
		oks.setText("客户端下载地址：" + Const.SERVER_BASE_PATH + Const.DOWNLOAD);
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl(Const.SERVER_BASE_PATH + Const.DOWNLOAD);
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		// oks.setComment("我是测试评论文本");
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl(Const.SERVER_BASE_PATH + Const.DOWNLOAD);
		// url仅在微信（包括好友和朋友圈）中使用
		// oks.setTheme(OnekeyShareTheme.SKYBLUE);
		// 启动分享GUI
		// 令编辑页面显示为Dialog模式
		oks.setDialogMode();
		oks.show(this);
	}

	// 再按一次推出程序，只在MainActivity中有此功能
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (currentFragment instanceof HomeFragment) {
				long currentTime = System.currentTimeMillis();
				if ((currentTime - Const.touchTime) >= Const.doubleBackInteralTime) {
					Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
					Const.touchTime = currentTime;
				} else {
					MyApplication.getInstance().exit();
				}
			} else {
				// 退回到主页
				selectItem(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
