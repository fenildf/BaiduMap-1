package com.example.baidumap.mvp.presenter;


import com.example.baidumap.bean.MarkerBean;
import com.example.baidumap.mvp.model.impl.ILBSModel;
import com.example.baidumap.mvp.presenter.impl.ILBSSearchPresenter;
import com.example.baidumap.mvp.view.ILBSSearchView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by wangt on 2018/3/27.
 */
public class LBSSearchPresenter implements ILBSSearchPresenter {
    private ILBSModel model;
    private ILBSSearchView view;

    public LBSSearchPresenter(ILBSModel model, ILBSSearchView view) {
        this.model = model;
        this.view = view;
    }

    @Override
    public void getSearch(String ak, String geotable_id, String page_size, String radius, String location) {
        model.getSearch(ak, geotable_id, page_size, radius, location)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<MarkerBean>() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(MarkerBean s) {
                        view.showSearch(s);
                    }

                });
    }
}
