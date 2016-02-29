package com.example.baidumap;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.baidumap.MyOrientationListener.OnOrientationListener;
import com.example.baidumap.api.LBSSearch;

public class MainActivity extends Activity implements OnClickListener {

	public MapView mMapView = null;
	public BaiduMap mBaiduMap = null;

	private Bitmap mBitmap;
	private Infos info;
	private ImageView image;
	private TextView zan;
	private TextView name;
	private TextView distance;

	private Context context;
	private Button btn_myLocation;
	private Button btn_overlays;
	private Button btn_uploaddata;
	private ImageView img_myLocation;

	// 定位相关
	public BDLocation currlocation = null; // 存储当前定位信息
	public LocationClient mLocationClient = null;
	public MyLocationListener listener = new MyLocationListener();
	private boolean isFirstIn = true;
	public double mLatitude;
	public double mLongitude;
	// 模式切换
	private LocationMode mLocationMode;

	// 自定义定位图标
	private BitmapDescriptor mIconLocation;
	private MyOrientationListener myOrientationListener;
	private float mCurrentX;

	// 覆盖物相关
	private BitmapDescriptor bitmap;
	private Marker marker;
	private RelativeLayout mMarkerLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);

		this.context = this;
		findViewById();

		initView();
		initLocation();
		initMarker();

		search();

		/**
		 * 标记点击事件
		 */
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				Bundle extraInfo = marker.getExtraInfo();
				info = (Infos) extraInfo.getSerializable("info");
				image = (ImageView) mMarkerLayout
						.findViewById(R.id.id_info_image);
				distance = (TextView) mMarkerLayout
						.findViewById(R.id.id_info_distance);
				zan = (TextView) mMarkerLayout.findViewById(R.id.id_info_zan);
				name = (TextView) mMarkerLayout.findViewById(R.id.id_info_name);

				String string = info.getImgurl();
				Log.e("main", string);
				if (string.length() < 5) {
					image.setImageResource(R.drawable.a01);
				} else {
					new Thread(runnable).start();
				}
				distance.setText(info.getDistance());
				zan.setText(info.getZan() + "");
				name.setText(info.getName());

				InfoWindow infoWindow;
				TextView tv = new TextView(context);
				tv.setBackgroundResource(R.drawable.location_tips);
				tv.setPadding(30, 20, 30, 50);
				tv.setText(info.getName());
				tv.setTextColor(Color.parseColor("#fff5eb"));

				LatLng latLng = marker.getPosition();
				OnInfoWindowClickListener listener = null;
				listener = new OnInfoWindowClickListener() {
					@Override
					public void onInfoWindowClick() {
						mBaiduMap.hideInfoWindow();
					}
				};

				infoWindow = new InfoWindow(BitmapDescriptorFactory
						.fromView(tv), latLng, -47, listener);
				mBaiduMap.showInfoWindow(infoWindow);
				mMarkerLayout.setVisibility(View.VISIBLE);

				return true;
			}
		});

		/**
		 * 地图点击事件
		 */
		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				return false;
			}

			@Override
			public void onMapClick(LatLng arg0) {
				mMarkerLayout.setVisibility(View.GONE);
				mBaiduMap.hideInfoWindow();
			}
		});

	}

	/**
	 * 找控件
	 */
	private void findViewById() {
		btn_overlays = (Button) findViewById(R.id.id_overlays);
		btn_overlays.setOnClickListener(this);

		btn_myLocation = (Button) findViewById(R.id.id_mylocation);
		btn_myLocation.setOnClickListener(this);

		btn_uploaddata = (Button) findViewById(R.id.id_upload_data);
		btn_uploaddata.setOnClickListener(this);
		
		mMarkerLayout = (RelativeLayout) findViewById(R.id.id_marker_layout);
	}

	/**
	 * 读取网络图片
	 * 
	 * @param imgurl
	 * @return
	 */
	protected Bitmap getBitmapFromUrl(String imgurl) {
		URL url;
		Bitmap bitmap = null;
		try {
			url = new URL(imgurl);
			InputStream is = url.openConnection().getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			bitmap = BitmapFactory.decodeStream(bis);
			bis.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	// 线程处理网络图片下载
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			Message msg = new Message();
			msg.what = 1;
			mBitmap = getBitmapFromUrl(info.getImgurl());
			mHandler.sendMessage(msg);
		}
	};

	/**
	 * 返回数据
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (msg.obj == null) {
					Log.e("bb", "接收数据为空");
				} else {
					String result = msg.obj.toString();
					try {
						JSONObject json = new JSONObject(result);
						parser(json);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				break;
			case 1:
				image.setImageBitmap(mBitmap);
				break;
			}
		}
	};

	/**
	 * 解析返回数据
	 * 
	 * @param json
	 */
	protected void parser(JSONObject json) {
		Infos infos = new Infos();
		List<Infos> list = infos.getInfos();
		try {
			JSONArray jsonArray = json.getJSONArray("contents");
			if (jsonArray != null && jsonArray.length() <= 0) {
				Toast.makeText(this, "没有符合要求的数据", Toast.LENGTH_SHORT).show();
			} else {
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject2 = (JSONObject) jsonArray.opt(i);
					Infos info = new Infos();

					info.setName(jsonObject2.getString("title"));
					info.setAddr(jsonObject2.getString("address"));
					info.setDistance("距离" + jsonObject2.getString("distance")
							+ "米");

					JSONArray locArray = jsonObject2.getJSONArray("location");
					double longitude = locArray.getDouble(0);
					double latitude = locArray.getDouble(1);
					info.setLatitude(latitude);
					info.setLongitude(longitude);

					float results[] = new float[1];
					if (currlocation != null) {
						Location.distanceBetween(currlocation.getLatitude(),
								currlocation.getLongitude(), latitude,
								longitude, results);
					}
					info.setDistance("距离" + (int) results[0] + "米");

					info.setImgurl(jsonObject2.getString("image"));
					info.setZan(jsonObject2.getInt("zan"));

					list.add(info);
				}
			}
		} catch (Exception e) {
			Log.e("mainactivity", "parser错误！");
		}
	}

	/**
	 * 发起云检索
	 */
	private void search() {
		Infos infos = new Infos();
		infos.getInfos().clear();
		LBSSearch.request(getRequestParams(), mHandler);
	}

	/**
	 * 设定云检索参数
	 * 
	 * @return
	 */
	private HashMap<String, String> getRequestParams() {
		HashMap<String, String> map = new HashMap<String, String>();

		try {
			map.put("radius", "2000");
			if (currlocation != null) {
				map.put("location", currlocation.getLongitude() + ","
						+ currlocation.getLatitude());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * 初始化覆盖物
	 */
	private void initMarker() {
		bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_marker);
	}

	/**
	 * 添加覆盖物
	 * 
	 * @param infos
	 */
	private void addOverlays(List<Infos> infos) {
		mBaiduMap.clear();
		LatLng latlng = null;

		for (Infos info : infos) {
			latlng = new LatLng(info.getLatitude(), info.getLongitude());

			MarkerOptions options = new MarkerOptions().position(latlng)
					.icon(bitmap).zIndex(9).draggable(true);

			options.animateType(MarkerAnimateType.grow);
			marker = (Marker) (mBaiduMap.addOverlay(options));
			Bundle arg0 = new Bundle();
			arg0.putSerializable("info", info);
			marker.setExtraInfo(arg0);
		}
//		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlng);
//		mBaiduMap.animateMapStatus(msu);

	}

	/**
	 * 初始化地图
	 */
	private void initView() {
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();// 初始化百度地图 才能对地图操作
		mMapView.showScaleControl(false);// 不显示地图上比例尺  
        mMapView.showZoomControls(false);// 不显示地图缩放控件（按钮控制栏）
		// 初始化比例尺到100米
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);
		mBaiduMap.setMapStatus(msu);

	}

	/**
	 * 初始化定位
	 */
	private void initLocation() {

		// 实例化百度地图定位核心类
		mLocationClient = new LocationClient(this);
		mLocationClient.registerLocationListener(listener);

		mLocationMode = LocationMode.NORMAL;
		// 地图状态按钮
		img_myLocation = (ImageView) findViewById(R.id.id_mylocation_img);
		img_myLocation.setImageResource(R.drawable.main_icon_location);

		// 设置地图参数
		LocationClientOption option = new LocationClientOption();
		// 设置坐标系
		option.setCoorType("bd09ll");
		// 返回位置信息
		option.setIsNeedAddress(true);
		// 启用高精度定位
		option.setOpenGps(true);
		// 设置刷新间隔1秒
		option.setScanSpan(1000);
		mLocationClient.setLocOption(option);

		// 启动地图定位
		if (!mLocationClient.isStarted()) {
			mLocationClient.stop();
		}
		mLocationClient.start();

		// 初始化图标
		mIconLocation = BitmapDescriptorFactory
				.fromResource(R.drawable.navi_map_gps_locked);

		myOrientationListener = new MyOrientationListener(context);
		myOrientationListener
				.setOnOrientationListener(new OnOrientationListener() {
					@Override
					public void onOrientationChanged(float x) {
						mCurrentX = x;
					}
				});

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

			// 封装数据
			MyLocationData data = new MyLocationData.Builder()//
					.accuracy(location.getRadius())//
					.direction(mCurrentX)//
					.latitude(location.getLatitude())//
					.longitude(location.getLongitude())//
					.build();
			// 绑定地图数据
			mBaiduMap.setMyLocationData(data);

			// 设置自定义图标
			MyLocationConfiguration config = new MyLocationConfiguration(
					mLocationMode, true, mIconLocation);
			mBaiduMap.setMyLocationConfigeration(config);

			// 更新经纬度
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();

			// 第一次打开
			if (isFirstIn) {
				// 获取坐标点
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(update);
				isFirstIn = false;

				// Toast.makeText(context, location.getAddrStr(),
				// Toast.LENGTH_SHORT).show();
			}

		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 我的位置按钮
		case R.id.id_mylocation:
			switch (mLocationMode) {
			case NORMAL:
				mLocationMode = LocationMode.FOLLOWING;
				img_myLocation.setImageResource(R.drawable.main_icon_follow);
				break;
			case FOLLOWING:
				mLocationMode = LocationMode.COMPASS;
				img_myLocation.setImageResource(R.drawable.main_icon_compass);
				mIconLocation = BitmapDescriptorFactory
						.fromResource(R.drawable.navi_map_gps_locked_compass);
				break;
			case COMPASS:
				mLocationMode = LocationMode.NORMAL;
				img_myLocation.setImageResource(R.drawable.main_icon_location);
				mIconLocation = BitmapDescriptorFactory
						.fromResource(R.drawable.navi_map_gps_locked);
				break;
			}
			break;
		// 覆盖物按钮
		case R.id.id_overlays:
			addOverlays(Infos.infos);
			search();
			break;
		case R.id.id_upload_data:
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, UpDataActivity.class);
			startActivity(intent);
			break;
		}
	}

	/**
	 * 定位到我的位置
	 */
	private void centerToMyLocation() {
		LatLng ll = new LatLng(mLatitude, mLongitude);
		MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(update);
	}

	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
		mMapView = null;
		// 停止定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
		// 停止方向传感器
		myOrientationListener.stop();
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
		// 开启方向传感器
		myOrientationListener.start();

	}

	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
}
