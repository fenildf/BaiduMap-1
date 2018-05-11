package com.example.baidumap.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.baidumap.AppConfig;
import com.example.baidumap.R;
import com.example.baidumap.bean.MarkerBean;
import com.example.baidumap.listener.MyOrientationListener;
import com.example.baidumap.listener.MyOrientationListener.OnOrientationListener;
import com.example.baidumap.mvp.model.LBSModel;
import com.example.baidumap.mvp.presenter.LBSSearchPresenter;
import com.example.baidumap.mvp.view.ILBSSearchView;
import com.example.baidumap.utils.OverlayManager;
import com.example.baidumap.utils.ToastUtils;
import com.example.baidumap.utils.WalkingRouteOverlay;
import com.example.baidumap.widght.CenterIconView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity implements ILBSSearchView {
    private Context context;
    // 上次按下返回键的系统时间
    private long lastBackTime = 0;
    // 当前按下返回键的系统时间
    private long currentBackTime = 0;


    @BindView(R.id.bmapView)
    MapView mMapView;
    @BindView(R.id.id_mylocation)
    Button btn_myLocation;
    @BindView(R.id.id_upload_data)
    Button btn_uploaddata;
    @BindView(R.id.rl_marker_layout)
    RelativeLayout mMarkerLayout;
    @BindView(R.id.tv_addr)
    TextView tv_addr;

    private boolean isFirstIn = true;
    // 覆盖物相关
    private BitmapDescriptor bitmap;

    public BaiduMap mBaiduMap;
    // 定位相关
    public LocationClient mLocationClient;
    private BDAbstractLocationListener myListener = new MyLocationListener();

    // 存储当前我的定位信息
    public BDLocation currlocation;

    //当前选择的位置坐标
    public double mLatitude;
    public double mLongitude;
    // 自定义定位图标
    private MyOrientationListener myOrientationListener;
    private float mCurrentX;

    //步行路径规划
    private RoutePlanSearch mSearch = RoutePlanSearch.newInstance();
    private OverlayManager routeOverlay;
    boolean useDefaultIcon = false;//是否使用默认图标

    private LBSSearchPresenter searchPresenter;
    private List<MarkerBean.ContentsBean> dataMarker = new ArrayList<>();

    //在屏幕中间画出图标
    private CenterIconView centerIconView;
    //展示路径规划
    private boolean isShowRoutePlan = false;

    //规划点
    private WalkingRouteOverlay overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        this.context = this;

        searchPresenter = new LBSSearchPresenter(new LBSModel(), this);
        //初始化覆盖物图标
        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_bike_nearby);
        mSearch.setOnGetRoutePlanResultListener(routePlanlistener);

        initView();
        initLocation();


        //在屏幕中间画出图标
        centerIconView = new CenterIconView(this, mMapView);
        getWindow().addContentView(centerIconView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * 初始化地图
     */
    private void initView() {
        mBaiduMap = mMapView.getMap();// 初始化百度地图 才能对地图操作
        mMapView.showScaleControl(true);// 不显示地图上比例尺
        mMapView.showZoomControls(false);// 不显示地图缩放控件（按钮控制栏）
        // 初始化比例尺到100米
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);
        mBaiduMap.setMapStatus(msu);
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        //地图触摸事件
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent arg0) {
                if (isShowRoutePlan) {
                    isShowRoutePlan = false;
                    mMarkerLayout.setVisibility(View.GONE);
                    mBaiduMap.hideInfoWindow();
                    if (!centerIconView.isShown()) {
                        centerIconView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });


        // 百度地图状态改变监听函数
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {
                // updateMapState(status);
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus status) {
                if (!isShowRoutePlan) {
                    updateMapState(status);
                    /*mMarkerLayout.setVisibility(View.GONE);
                    mBaiduMap.hideInfoWindow();
                    if (!centerIconView.isShown()) {
                        centerIconView.setVisibility(View.VISIBLE);
                    }*/
                }
            }

            @Override
            public void onMapStatusChange(MapStatus status) {
                // updateMapState(status);
            }
        });

        //标记点击事件
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Bundle extraInfo = marker.getExtraInfo();
                MarkerBean.ContentsBean info;
                info = (MarkerBean.ContentsBean) extraInfo.getSerializable("info");

                if (info != null) {
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

                    //开始规划路线
                    LatLng startLatLng = new LatLng(mLatitude, mLongitude);
                    setPlanResult(startLatLng, latLng);
                }

                return true;
            }
        });
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        mLocationClient = new LocationClient(getApplicationContext());
        // 设置地图参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 设置坐标系
        option.setCoorType("bd09ll");
        int span = 5000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.setLocOption(option);

        mLocationClient.registerLocationListener(myListener);
        mLocationClient.start();


        myOrientationListener = new MyOrientationListener(context);
        myOrientationListener.setOnOrientationListener(new OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
    }

    //发起云检索
    private void ApiGetInfo(Double Longitude, Double Latitude) {
        dataMarker.clear();
        String location = "";
        if (currlocation != null) {
            location = Longitude + "," + Latitude;
        }
        searchPresenter.getSearch(AppConfig.AK, AppConfig.GEOTABLE_ID, "9999", AppConfig.RADIUS, location);
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

    //添加覆盖物
    private void addOverlays(List<MarkerBean.ContentsBean> list) {
        mBaiduMap.clear();
        LatLng latlng;

        for (MarkerBean.ContentsBean info : list) {
            latlng = new LatLng(info.getLocation().get(1), info.getLocation().get(0));
            MarkerOptions options = new MarkerOptions().position(latlng).icon(bitmap).zIndex(5).draggable(false);

            options.animateType(MarkerAnimateType.grow);
            Marker marker = (Marker) (mBaiduMap.addOverlay(options));
            Bundle bundle = new Bundle();
            bundle.putSerializable("info", info);
            marker.setExtraInfo(bundle);
        }
    }


    //获取移动后屏幕中间经纬度
    protected void updateMapState(MapStatus status) {
        LatLng mCenterLatLng = status.target;
        //获取经纬度
        mLatitude = mCenterLatLng.latitude;
        mLongitude = mCenterLatLng.longitude;

        ApiGetInfo(mLongitude, mLatitude);
    }

    //规划步行路线
    private void setPlanResult(LatLng stLatLng, LatLng enLatLng) {
        PlanNode stNode = PlanNode.withLocation(stLatLng);
        PlanNode enNode = PlanNode.withLocation(enLatLng);
        mSearch.walkingSearch((new WalkingRoutePlanOption())
                .from(stNode)
                .to(enNode));
    }

    /**
     * 定位监听类
     *
     * @author Administrator
     */
    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                return;
            }
            currlocation = location;

            // 封装数据
            MyLocationData data = new MyLocationData.Builder()
                    .accuracy(0)
                    .direction(mCurrentX)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            // 绑定地图数据
            mBaiduMap.setMyLocationData(data);

            // 设置自定义图标
            MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
            BitmapDescriptor mCurrentMarker = null;
            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, false, mCurrentMarker);
            mBaiduMap.setMyLocationConfiguration(config);

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

                ApiGetInfo(currlocation.getLongitude(), currlocation.getLatitude());
            }
        }
    }

    /**
     * 步行路线规划监听
     */
    private OnGetRoutePlanResultListener routePlanlistener = new OnGetRoutePlanResultListener() {

        public void onGetWalkingRouteResult(WalkingRouteResult result) {
            //获取步行线路规划结果
            if (result.getRouteLines().size() == 1 && !isShowRoutePlan) {
                centerIconView.setVisibility(View.GONE);
                isShowRoutePlan = true;

                List<WalkingRouteLine> routeLines = result.getRouteLines();
                int allDistances = 0;//总距离/米
                int allDurations = 0;//总时长/秒
                if (routeLines != null) {
                    for (WalkingRouteLine drivingRouteLine : routeLines) {
                        allDistances = allDistances + drivingRouteLine.getDistance();
                        allDurations = allDurations + drivingRouteLine.getDuration();
                    }
                }

                WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));//设置路线数据
                overlay.addToMap();//将所有overlay添加到地图中
                overlay.zoomToSpan();//缩放地图
            }
        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

        }

        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

        }

        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

        }
    };

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (!useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_marker_location);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (!useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_bike_nearby);
            }
            return null;
        }
    }

    @OnClick({R.id.id_mylocation, R.id.id_upload_data})
    public void onClick(View v) {
        switch (v.getId()) {
            // 我的位置按钮
            case R.id.id_mylocation:
                centerToMyLocation();
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
        LatLng ll = new LatLng(currlocation.getLatitude(), currlocation.getLongitude());
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
            if (mMarkerLayout.isShown()) {
                mMarkerLayout.setVisibility(View.GONE);
                mBaiduMap.hideInfoWindow();
//                mSearch.destroy();
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
        ApiGetInfo(currlocation.getLongitude(), currlocation.getLatitude());
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