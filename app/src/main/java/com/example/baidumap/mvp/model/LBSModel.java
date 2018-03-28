package com.example.baidumap.mvp.model;

import com.example.baidumap.bean.MarkerBean;
import com.example.baidumap.bean.StorageBean;
import com.example.baidumap.utils.ApiUtil;
import com.example.baidumap.mvp.api.LBSApi;
import com.example.baidumap.mvp.model.impl.ILBSModel;

import java.util.Map;

import io.reactivex.Observable;

/**
 * Created by wangt on 2018/3/27.
 */
public class LBSModel implements ILBSModel {
    private LBSApi api;

    public LBSApi ApiInstance() {
        if (api != null) {
            return api;
        } else {
            return ApiUtil.getInstance().createRetrofitApi(LBSApi.class);
        }
    }

    @Override
    public Observable<MarkerBean> getSearch(String ak, String geotable_id, String page_size, String radius, String location) {
        api = ApiInstance();
        return api.getSearch(ak, geotable_id, page_size, radius, location);
    }

    @Override
    public Observable<StorageBean> getStorage(Map<String, String> praise) {
        api = ApiInstance();
        return api.getStorage(praise);
    }
}
