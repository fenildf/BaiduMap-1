package com.example.baidumap.mvp.presenter.impl;

/**
 * Created by wangt on 2018/3/27.
 */
public interface ILBSSearchPresenter {
    void getSearch(String ak, String geotable_id, String page_size, String radius, String location);
}
