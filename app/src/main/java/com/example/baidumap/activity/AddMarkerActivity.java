package com.example.baidumap.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.baidumap.AppConfig;
import com.example.baidumap.R;
import com.example.baidumap.bean.StorageBean;
import com.example.baidumap.mvp.model.LBSModel;
import com.example.baidumap.mvp.presenter.LBSStoragePresenter;
import com.example.baidumap.mvp.view.ILBSStorageView;
import com.example.baidumap.utils.ToastUtils;
import com.example.baidumap.widght.CenterIconView;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddMarkerActivity extends Activity implements OnMapLoadedCallback, OnGetGeoCoderResultListener, ILBSStorageView {
    public MapView mMapView = null;
    public BaiduMap mBaiduMap = null;

    public LatLng mCenterLatLng;
    private double myCentureLatitude;
    private double myCentureLongitude;
    private String myCentureAddress;

    // 定位相关
    public BDLocation currlocation = null; // 存储当前定位信息
    public String mAddress;                //存储当前地址
    public LocationClient mLocationClient = null;
    public MyLocationListener listener = new MyLocationListener();
    private boolean isFirstIn = true;

    private LBSStoragePresenter storagePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updata_location);
        ButterKnife.bind(this);

        storagePresenter = new LBSStoragePresenter(new LBSModel(), this);

        initView();
        initLocation();
        //在屏幕中间画出图标
        CenterIconView centerIconView = new CenterIconView(this, mMapView);
        getWindow().addContentView(centerIconView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    /**
     * 反地理编码回调函数
     */
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            return;
        } else {
            myCentureAddress = result.getAddress();
            ToastUtils.showToast(this, result.getAddress());
            Log.e("aa", result.getAddress());
        }
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
    }

    /**
     * 初始化地图
     */
    private void initView() {
        mMapView = (MapView) findViewById(R.id.mapView);
        mBaiduMap = mMapView.getMap();
        UiSettings mUiSettings = mBaiduMap.getUiSettings();
        mUiSettings.setRotateGesturesEnabled(false);//禁用旋转手势
        mUiSettings.setOverlookingGesturesEnabled(false);//禁用俯视
        mMapView.showScaleControl(false);// 不显示地图上比例尺
        mMapView.showZoomControls(false);// 不显示地图缩放控件（按钮控制栏）
        // 初始化比例尺到100米
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(18.0f);
        mBaiduMap.setMapStatus(msu);

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

    //获取移动后屏幕中间经纬度
    protected void updateMapState(MapStatus status) {
        mCenterLatLng = status.target;
        //获取经纬度
        myCentureLatitude = mCenterLatLng.latitude;
        myCentureLongitude = mCenterLatLng.longitude;
        LatLng ptCenter = new LatLng(myCentureLatitude, myCentureLongitude);

        // 初始化搜索模块，注册事件监听
        GeoCoder search = GeoCoder.newInstance();
        search.setOnGetGeoCodeResultListener(this);
        search.reverseGeoCode(new ReverseGeoCodeOption().location(ptCenter));
    }

    //提交点
    private void ApiPostInfo() {
        Map<String, String> map = new HashMap<>();
        map.put("ak", AppConfig.AK);
        map.put("geotable_id", AppConfig.GEOTABLE_ID);
        map.put("coord_type", AppConfig.COORD_TYPE);
        map.put("latitude", myCentureLatitude + "");
        map.put("longitude", myCentureLongitude + "");
        map.put("address", myCentureAddress);
        storagePresenter.getStorage(map);
    }

    @Override
    public void showStorage(StorageBean bean) {
        if (bean.getStatus() == 0) {
            ToastUtils.showToast(this, "提交成功");
            finish();
        } else {
            ToastUtils.showToast(this, "提交失败");
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
            }
        }
    }

    /**
     * 地图加载完成后
     */
    @Override
    public void onMapLoaded() {
    }

    @OnClick({R.id.location_back, R.id.location_ok, R.id.id_mylocation_btn})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.location_back:
                finish();
                break;
            case R.id.location_ok:
                ApiPostInfo();
                break;
            case R.id.id_mylocation_btn:
                LatLng ll = new LatLng(currlocation.getLatitude(), currlocation.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(update);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mMapView = null;
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
    }

    @Override
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
