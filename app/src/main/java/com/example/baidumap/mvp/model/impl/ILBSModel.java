package com.example.baidumap.mvp.model.impl;

import com.example.baidumap.bean.MarkerBean;
import com.example.baidumap.bean.StorageBean;

import java.util.Map;

import io.reactivex.Observable;

/**
 * Created by wangt on 2018/3/27.
 */
public interface ILBSModel {

    //search
    Observable<MarkerBean> getSearch(String ak, String geotable_id, String page_size, String radius, String location);

    //storage
    Observable<StorageBean> getStorage(Map<String, String> praise);
}
