/**
 * <p>Project：venus</p>
 * <p>Package：	com.tp.venus.module.home.api</p>
 * <p>File：ApiUtil.java</p>
 * <p>Version： 4.0.0</p>
 * <p>Date： 2016/1/14/11:52.</p>
 * Copyright © 2016 www.qbt365.com Corporation Inc. All rights reserved.
 */
package com.example.baidumap.utils;


import android.util.Log;

import com.example.baidumap.base.AppConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * <p>Class：com.tp.venus.util.ApiUtil</p>
 * <p>Description：</p>
 * <pre>
 *      API工具类
 * </pre>
 *
 * @version 1.0.0
 * @date 2016/1/14/11:52
 */

public class ApiUtil {

    private static ApiUtil instance = new ApiUtil();

    public ApiUtil() {
    }

    public static ApiUtil getInstance() {
        return instance;
    }

    public OkHttpClient InterceptClient() {
        HttpLoggingInterceptor HTTP_LOGGING_INTERCEPTOR =
                new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String message) {
                        Log.e("-------------------Http:", message);
                    }
                }).setLevel(HttpLoggingInterceptor.Level.BODY);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(HTTP_LOGGING_INTERCEPTOR)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        return mOkHttpClient;
    }

    public <T> T createRetrofitApi(Class<T> service) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.WEB_ROOT)
//                .addConverterFactory(FastJsonConvertFactory.create())
                .addConverterFactory(GsonConverterFactory.create())//Retrofit访问服务器时候返回参数通过Gson解析
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(this.InterceptClient())
                .build();
        return retrofit.create(service);
    }
}