package com.example.baidumap;

import com.baidu.location.Address;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import android.R.anim;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

public class UpDataLocationActivity extends Activity implements
		OnClickListener, OnMapLoadedCallback,OnGetGeoCoderResultListener {
	
	private static String mTAG = "UpDataLocationActivity";

	public MapView mMapView = null;
	public BaiduMap mBaiduMap = null;

	ImageView location_back;
	Button location_ok_Btn;
	ImageView mylocation_btn;
	
	
	public LatLng mCenterLatLng;
	private double myCentureLatitude;
	private double myCentureLongitude;
	private String myCentureAddress;
	
	private GeoCoder Search;
	private UiSettings mUiSettings;//地图控制

	// 定位相关
	public BDLocation currlocation = null; // 存储当前定位信息
	public String mAddress;                //存储当前地址
	private GeoCoder mySearch;
	public LocationClient mLocationClient = null;
	public MyLocationListener listener = new MyLocationListener();
	private boolean isFirstIn = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_updata_location);

		findView();
		initView();
		initLocation();
		//在屏幕中间画出图标
		CenterIcon centerIcon = new CenterIcon(this, mMapView);
		getWindow().addContentView(
				centerIcon,
				new LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT));
		
		// 百度地图状态改变监听函数
		mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {
			@Override
			public void onMapStatusChangeStart(MapStatus status) {
				// updateMapState(status);
			}

			@Override
			public void onMapStatusChangeFinish(MapStatus status) {
				updateMapState(status);
			}

			@Override
			public void onMapStatusChange(MapStatus status) {
				// updateMapState(status);
			}
        });  
	}
	
	/**
	 * 获取移动后屏幕中间经纬度
	 * @param status
	 */
    protected void updateMapState(MapStatus status) {
    	mCenterLatLng = status.target;
    	/** 获取经纬度 */  
        myCentureLatitude = mCenterLatLng.latitude;  
        myCentureLongitude = mCenterLatLng.longitude;
        LatLng ptCenter = new LatLng(myCentureLatitude, myCentureLongitude);
        
    	// 初始化搜索模块，注册事件监听
    	Search = GeoCoder.newInstance();
    	Search.setOnGetGeoCodeResultListener(this);
        Search.reverseGeoCode(new ReverseGeoCodeOption().location(ptCenter));
	}
    
    /**
	 * 反地理编码回调函数
	 */
	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {  
            return;  
        }else{
        	myCentureAddress = result.getAddress();
        	Toast.makeText(this, result.getAddress(),Toast.LENGTH_SHORT).show();
        	Log.e("aa", result.getAddress());
        }
        
	}
	/**
     * 找控件
     */
	private void findView() {
		location_back = (ImageView) findViewById(R.id.location_back);
		location_back.setOnClickListener(this);
		location_ok_Btn = (Button) findViewById(R.id.location_ok);
		location_ok_Btn.setOnClickListener(this);
		mylocation_btn = (ImageView) findViewById(R.id.id_mylocation_btn);
		mylocation_btn.setOnClickListener(this);
	}

	/**
	 * 初始化定位
	 */
	private void initLocation() {
		// 实例化百度地图定位核心类
		mLocationClient = new LocationClient(this);
		mLocationClient.registerLocationListener(listener);

		// mLocationMode = LocationMode.NORMAL;
		// // 地图状态按钮
		// img_myLocation = (ImageView) findViewById(R.id.id_mylocation_img);
		// img_myLocation.setImageResource(R.drawable.main_icon_location);

		// 设置地图参数
		LocationClientOption option = new LocationClientOption();
		// 设置坐标系
		option.setCoorType("bd09ll");
		// 返回位置信息
		option.setIsNeedAddress(true);
		// 启用高精度定位
		option.setOpenGps(true);
		// 设置刷新间隔1秒
		option.setScanSpan(2000);
		mLocationClient.setLocOption(option);

		// 启动地图定位
		if (!mLocationClient.isStarted()) {
			mLocationClient.stop();
		}
		mLocationClient.start();
	}

	/**
	 * 初始化地图
	 */
	private void initView() {
		mMapView = (MapView) findViewById(R.id.mapView);
		mBaiduMap = mMapView.getMap();
		mUiSettings = mBaiduMap.getUiSettings();
		mUiSettings.setRotateGesturesEnabled(false);//禁用旋转手势
		mUiSettings.setOverlookingGesturesEnabled(false);//禁用俯视
        mMapView.showScaleControl(false);// 不显示地图上比例尺  
        mMapView.showZoomControls(false);// 不显示地图缩放控件（按钮控制栏）  
		// 初始化比例尺到100米
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(18.0f);
		mBaiduMap.setMapStatus(msu);
	}

	/**
	 * 定位监听类
	 * 
	 * @author Administrator
	 * 
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}
			currlocation = location;
            mAddress = location.getAddrStr();
			// 封装数据
			MyLocationData data = new MyLocationData.Builder()//
					.accuracy(location.getRadius())//
					.latitude(location.getLatitude())//
					.longitude(location.getLongitude())//
					.build();
			// 绑定地图数据
			mBaiduMap.setMyLocationData(data);

			// 第一次打开
			if (isFirstIn) {
				// 获取坐标点
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(update);
				isFirstIn = false;
				myCentureLatitude = currlocation.getLatitude();
		        myCentureLongitude = currlocation.getLongitude();
		        myCentureAddress = mAddress;
				// Toast.makeText(context, location.getAddrStr(),
				// Toast.LENGTH_SHORT).show();
			}

		}

	}

	/**
	 * 地图加载完成后
	 */
	@Override
	public void onMapLoaded() {
		// BitmapDescriptor bitmap;
		// bitmap =
		// BitmapDescriptorFactory.fromResource(R.drawable.icon_marker);
		//
		// MarkerOptions options = new MarkerOptions().position(new LatLng(0,
		// 0))
		// .icon(bitmap).zIndex(9).anchor(0.5f, 0.5f);
		// marker = (Marker) (mBaiduMap.addOverlay(options));
		// marker.setFlat(true);
		
	}

	/**
	 * 在屏幕中间实现一个View
	 * 
	 * @author Administrator
	 * 
	 */
	class CenterIcon extends View {

		public int w;
		public int h;
		public Bitmap mBitmap;
		public MapView mMapView;

		public CenterIcon(Context context, MapView mMapView) {

			super(context);
			// 设置屏幕中心的图标
			mBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.icon_marker_location);
			this.mMapView = mMapView;
		}

		@Override
		protected void onDraw(Canvas canvas) {

			super.onDraw(canvas);
			// 获取屏幕中心的坐标
				
		    w = mMapView.getWidth() / 2 - mBitmap.getWidth() / 2;
			h = mMapView.getHeight() / 2 - mBitmap.getHeight();
			canvas.drawBitmap(mBitmap, w, h, null);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.location_back:
			Intent intent = new Intent(UpDataLocationActivity.this,
					UpDataActivity.class);
			setResult(RESULT_OK, intent);
			finish();
			break;
		case R.id.location_ok:
			PositionEntity.latitue = myCentureLatitude;
	        PositionEntity.longitude = myCentureLongitude;
	        PositionEntity.address = myCentureAddress;
	        Log.e(mTAG, PositionEntity.latitue+"-"+PositionEntity.longitude+"-"+PositionEntity.address);
	        Intent intent2 = new Intent(UpDataLocationActivity.this,
					UpDataActivity.class);
			setResult(RESULT_OK, intent2);
			finish();
			break;
		case R.id.id_mylocation_btn:
			LatLng ll = new LatLng(currlocation.getLatitude(), currlocation.getLongitude());
			MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
			mBaiduMap.animateMapStatus(update);
			break;
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
		mMapView = null;

	}

	protected void onResume() {
		super.onRestart();
		mMapView.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// 开启定位
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
	}

	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// 停止定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
		
	}

    /**
     * 地理编码回调函数
     */
	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		
	}
	

}
