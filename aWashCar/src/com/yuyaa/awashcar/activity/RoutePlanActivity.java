package com.yuyaa.awashcar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMyLocationClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.yuyaa.awashcar.MyApplication;
import com.yuyaa.awashcar.R;

public class RoutePlanActivity extends BaseActivity implements
		OnGetGeoCoderResultListener, OnGetRoutePlanResultListener {
	private BaiduMap mBaiduMap;
	// 定位：
	private LocationClient mLocationClient;
	private BDLocationListener mBdLocationListener;
	private LocationMode mCurrentMode = LocationMode.NORMAL; // 普通、罗盘、跟随
	MapView mMapView;
	private Double endLongitude;
	private Double endLatitude;
	private Double mLongitude;
	private Double mLatitude;
	private String start_address;
	private String end_address;
	private BitmapDescriptor bd;
	private BitmapDescriptor bd_focus;
	private Marker position_marker;
	private Boolean isFirstinBoolean = true;
	OverlayManager routeOverlay = null;
	RoutePlanSearch mRouteSearch = null; // 搜索模块，也可去掉地图模块独立使用

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		setContentView(R.layout.activity_map_route);
		endLatitude = intent.getDoubleExtra("latitude", 0.0);
		endLongitude = intent.getDoubleExtra("longitude", 0.0);
		end_address = intent.getStringExtra("address");
		init();
		// 初始化搜索模块，注册事件监听
		mRouteSearch = RoutePlanSearch.newInstance();
		mRouteSearch.setOnGetRoutePlanResultListener(this);
	}

	private void findViews() {
		bd = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_mark);
		bd_focus = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_openmap_focuse_mark);
		mMapView = (MapView) findViewById(R.id.mapview_map_search_activity);
		mBaiduMap = mMapView.getMap();
		// 隐藏缩放控件
		int childCount = mMapView.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = mMapView.getChildAt(i);
			if (child instanceof ImageView || child instanceof ZoomControls) {
				child.setVisibility(View.GONE);
			}

		}
		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onMapClick(LatLng arg0) {
				// TODO Auto-generated method stub
				OverlayOptions ooA = new MarkerOptions().position(arg0)
						.icon(bd).zIndex(0).draggable(true);
				if (position_marker != null) {
					position_marker.remove();
					mMapView.invalidate();
				}
				mBaiduMap.hideInfoWindow();
				position_marker = (Marker) (mBaiduMap.addOverlay(ooA));
				mLatitude = arg0.latitude;
				mLongitude = arg0.longitude;
				startCalcRoute();
			}
		});
	}

	private void init() {
		findViews();
		// 定位初始化
		mLocationClient = new LocationClient(this);
		mBdLocationListener = new MyLocationListenner();
		mLocationClient.registerLocationListener(mBdLocationListener);
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				mCurrentMode, true, null)); // 设置定位marker
		mBaiduMap.setOnMyLocationClickListener(new OnMyLocationClickListener() {

			@Override
			public boolean onMyLocationClick() {
				// TODO Auto-generated method stub
				if (null != position_marker) {
					position_marker.remove();
					mMapView.invalidate();
					position_marker = null;
				}
				mLatitude = MyApplication.getInstance().user.mCurrentLantitude;
				mLongitude = MyApplication.getInstance().user.mCurrentLongitude;
				startCalcRoute();
				return true;
			}
		});
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
		mBaiduMap.setMapStatus(msu);
		mBaiduMap.setMyLocationEnabled(true);
		LocationClientOption option = new LocationClientOption();
		option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度，默认值gcj02，如果后面要在百度地图上用此定位，使用"bd09ll"更精确
		option.setOpenGps(true); // 打开gps
		option.setScanSpan(1000); // 设置发起定位请求的间隔时间为1000ms
		option.setIsNeedAddress(true);
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();

	}

	@Override
	protected void onStart() {
		// 开启图层定位
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		// 关闭图层定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mLocationClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		bd.recycle();
		bd_focus.recycle();
		mRouteSearch.destroy();
		super.onDestroy();
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360，这里随便写100，通过location.getDirection()得不到？
					.direction(location.getDirection())
					.latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			MyApplication.getInstance().user.mCurrentLantitude = location
					.getLatitude();
			MyApplication.getInstance().user.mCurrentLongitude = location
					.getLongitude();
			mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
					mCurrentMode, true, null)); // 设置定位marker

			if (isFirstinBoolean) {
				isFirstinBoolean = false;
				mLatitude = location.getLatitude();
				mLongitude = location.getLongitude();
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
				startCalcRoute();
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	public OnClickListener routingButtonOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// 获取输入的起终点
			startCalcRoute();
		}
	};

	private void startCalcRoute() {
		if (routeOverlay != null) {
			routeOverlay.removeFromMap();
			mMapView.invalidate();
		}
		// 重置浏览节点的路线数据
		// 设置起终点信息，对于tranist search 来说，城市名无意义
		PlanNode stNode = PlanNode.withLocation(new LatLng(mLatitude,
				mLongitude));
		PlanNode enNode = PlanNode.withLocation(new LatLng(endLatitude,
				endLongitude));

		// 实际使用中请对起点终点城市进行正确的设定
		mRouteSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode)
				.to(enNode));
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			return;
		}
		mLatitude = result.getLocation().latitude;
		mLongitude = result.getLocation().longitude;

	}

	public void clickCallback(View view) {
		switch (view.getId()) {
		case R.id.btn_brand_detail_activity_back:
			onBackPressed();
			break;

		default:
			break;
		}
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			return;
		}
		mLatitude = result.getLocation().latitude;
		mLongitude = result.getLocation().longitude;
		start_address = result.getAddress();
	}

	public void startNavi(View view) {
		// 这里给出一个起终点示例，实际应用中可以通过POI检索、外部POI来源等方式获取起终点坐标
		BNaviPoint startPoint = new BNaviPoint(mLongitude, mLatitude,
				start_address, BNaviPoint.CoordinateType.BD09_MC);
		BNaviPoint endPoint = new BNaviPoint(endLongitude, endLatitude,
				end_address, BNaviPoint.CoordinateType.BD09_MC);
		BaiduNaviManager.getInstance().launchNavigator(this, startPoint, // 起点（可指定坐标系）
				endPoint, // 终点（可指定坐标系）
				NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME, // 算路方式
				true, // 真实导航
				BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, // 在离线策略
				new OnStartNavigationListener() { // 跳转监听

					@Override
					public void onJumpToNavigator(Bundle configParams) {
						Intent intent = new Intent(RoutePlanActivity.this,
								BNavigatorActivity.class);
						intent.putExtras(configParams);
						startActivity(intent);
					}

					@Override
					public void onJumpToDownloader() {
					}
				});
	}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
			routeOverlay = overlay;
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}

	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult result) {

		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaiduMap);
			routeOverlay = overlay;
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}
	}

	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
			routeOverlay = overlay;
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}
	}

	// 定制RouteOverly
	private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

		public MyDrivingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {

			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {

			return null;
		}
	}

	private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

		public MyWalkingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {

			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {

			return null;
		}
	}

	private class MyTransitRouteOverlay extends TransitRouteOverlay {

		public MyTransitRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {

			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {

			return null;
		}
	}
}
