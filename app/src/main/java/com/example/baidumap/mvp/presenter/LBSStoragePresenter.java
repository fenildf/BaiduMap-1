package com.example.baidumap.mvp.presenter;

import com.example.baidumap.bean.StorageBean;
import com.example.baidumap.mvp.model.impl.ILBSModel;
import com.example.baidumap.mvp.presenter.impl.ILBSStoragePresenter;
import com.example.baidumap.mvp.view.ILBSStorageView;

import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by wangt on 2018/3/27.
 */
public class LBSStoragePresenter implements ILBSStoragePresenter {
    private ILBSModel model;
    private ILBSStorageView view;

    public LBSStoragePresenter(ILBSModel model, ILBSStorageView view) {
        this.model = model;
        this.view = view;
    }

    @Override
    public void getStorage(Map<String, String> params) {
        model.getStorage(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<StorageBean>() {
                    @Override
                    public void onNext(StorageBean s) {
                        view.showStorage(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
