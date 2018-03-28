package com.example.baidumap.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
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
import com.example.baidumap.AppConfig;
import com.example.baidumap.R;
import com.example.baidumap.bean.MarkerBean;
import com.example.baidumap.listener.MyOrientationListener;
import com.example.baidumap.listener.MyOrientationListener.OnOrientationListener;
import com.example.baidumap.mvp.model.LBSModel;
import com.example.baidumap.mvp.presenter.LBSSearchPresenter;
import com.example.baidumap.mvp.view.ILBSSearchView;
import com.example.baidumap.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity implements ILBSSearchView {

    public MapView mMapView = null;
    public BaiduMap mBaiduMap = null;

    // 上次按下返回键的系统时间
    private long lastBackTime = 0;
    // 当前按下返回键的系统时间
    private long currentBackTime = 0;

    private Context context;
    @BindView(R.id.id_mylocation)
    Button btn_myLocation;
    @BindView(R.id.id_overlays)
    Button btn_overlays;
    @BindView(R.id.id_upload_data)
    Button btn_uploaddata;
    @BindView(R.id.rl_marker_layout)
    RelativeLayout mMarkerLayout;
    @BindView(R.id.tv_addr)
    TextView tv_addr;

    // 定位相关
    public BDLocation currlocation = null; // 存储当前定位信息
    public LocationClient mLocationClient = null;
    public MyLocationListener listener = new MyLocationListener();
    private boolean isFirstIn = true;
    public double mLatitude;
    public double mLongitude;
    // 自定义定位图标
    private BitmapDescriptor mIconLocation;
    private MyOrientationListener myOrientationListener;
    private float mCurrentX;

    // 覆盖物相关
    private BitmapDescriptor bitmap;

    private LBSSearchPresenter searchPresenter;
    private List<MarkerBean.ContentsBean> dataMarker = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        this.context = this;

        searchPresenter = new LBSSearchPresenter(new LBSModel(), this);

        //初始化覆盖物图标
        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_marker);

        initView();
        initLocation();
    }

    /**
     * 初始化地图
     */
    private void initView() {
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();// 初始化百度地图 才能对地图操作
        mMapView.showScaleControl(true);// 不显示地图上比例尺
        mMapView.showZoomControls(false);// 不显示地图缩放控件（按钮控制栏）
        // 初始化比例尺到100米
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);
        mBaiduMap.setMapStatus(msu);

        //地图触摸事件
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent arg0) {
                mMarkerLayout.setVisibility(View.GONE);
                mBaiduMap.hideInfoWindow();
            }
        });

        //标记点击事件
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Bundle extraInfo = marker.getExtraInfo();
                MarkerBean.ContentsBean info;
                info = (MarkerBean.ContentsBean) extraInfo.getSerializable("info");

                // 将标记点移至中间
                LatLng latlng = new LatLng(info.getLocation().get(1), info.getLocation().get(0));
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlng);
                mBaiduMap.animateMapStatus(msu);

                tv_addr.setText(info.getAddress());

                InfoWindow infoWindow;
                TextView tv = new TextView(context);
                tv.setBackgroundResource(R.drawable.location_tips);
                tv.setPadding(30, 20, 30, 50);
                tv.setText("距离" + info.getDistance() + "米");
                tv.setTextColor(Color.parseColor("#fff5eb"));

                LatLng latLng = marker.getPosition();
                InfoWindow.OnInfoWindowClickListener listener;
                listener = new InfoWindow.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick() {
                        mBaiduMap.hideInfoWindow();
                    }
                };

                infoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(tv), latLng, -47, listener);
                mBaiduMap.showInfoWindow(infoWindow);
                mMarkerLayout.setVisibility(View.VISIBLE);
                return true;
            }
        });
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        // 实例化百度地图定位核心类
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(listener);

        // 设置地图参数
        LocationClientOption option = new LocationClientOption();
        // 设置坐标系
        option.setCoorType("bd09ll");
        // 返回位置信息
        option.setIsNeedAddress(true);
        // 启用高精度定位
        option.setOpenGps(true);
        // 设置刷新间隔1秒
        option.setScanSpan(5000);
        mLocationClient.setLocOption(option);

        // 启动地图定位
        if (!mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
        mLocationClient.start();

        // 初始化图标
        mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);

        myOrientationListener = new MyOrientationListener(context);
        myOrientationListener.setOnOrientationListener(new OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
    }

    //发起云检索
    private void ApiGetInfo() {
        String location = "";
        if (currlocation != null) {
            location = currlocation.getLongitude() + "," + currlocation.getLatitude();
        }
        searchPresenter.getSearch(AppConfig.AK, AppConfig.GEOTABLE_ID, "9999", AppConfig.RADIUS, location);
    }

    //添加覆盖物
    private void addOverlays(List<MarkerBean.ContentsBean> list) {
        mBaiduMap.clear();
        LatLng latlng;

        for (MarkerBean.ContentsBean info : list) {
            latlng = new LatLng(info.getLocation().get(1), info.getLocation().get(0));
            MarkerOptions options = new MarkerOptions().position(latlng).icon(bitmap).zIndex(9).draggable(false);

            options.animateType(MarkerAnimateType.grow);
            Marker marker = (Marker) (mBaiduMap.addOverlay(options));
            Bundle bundle = new Bundle();
            bundle.putSerializable("info", info);
            marker.setExtraInfo(bundle);
        }
    }


    @Override
    public void showSearch(MarkerBean bean) {
        if (bean.getStatus() == 0) {
            dataMarker = bean.getContents();
            addOverlays(dataMarker);
        } else {
            ToastUtils.showToast(this, "请求错误");
        }
    }

    /**
     * 定位监听类
     *
     * @author Administrator
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
            MyLocationConfiguration config = new MyLocationConfiguration(LocationMode.NORMAL, true, mIconLocation);
            mBaiduMap.setMyLocationConfigeration(config);

            // 更新经纬度
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();

            // 第一次打开
            if (isFirstIn) {
                // 获取坐标点
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(update);
                isFirstIn = false;

                ApiGetInfo();
            }

        }
    }

    @OnClick({R.id.id_mylocation, R.id.id_overlays, R.id.id_upload_data})
    public void onClick(View v) {
        switch (v.getId()) {
            // 我的位置按钮
            case R.id.id_mylocation:
                centerToMyLocation();
                break;
            case R.id.id_overlays:
                ApiGetInfo();
                break;
            case R.id.id_upload_data:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, AddMarkerActivity.class);
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

    /**
     * 双击返回键退出
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 捕获返回键按下的事件
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mMarkerLayout.getVisibility() == View.VISIBLE) {
                mMarkerLayout.setVisibility(View.GONE);
                mBaiduMap.hideInfoWindow();
            } else {
                // 获取当前系统时间的毫秒数
                currentBackTime = System.currentTimeMillis();
                // 比较上次按下返回键和当前按下返回键的时间差，如果大于2秒，则提示再按一次退出
                if (currentBackTime - lastBackTime > 2 * 1000) {
                    ToastUtils.showToast(this, "再按一次退出程序");
                    lastBackTime = currentBackTime;
                } else { // 如果两次按下的时间差小于2秒，则退出程序
                    finish();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    protected void onRestart() {
        super.onRestart();
        ApiGetInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
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
}