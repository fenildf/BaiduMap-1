package com.example.baidumap.mvp.api;

import com.example.baidumap.bean.MarkerBean;
import com.example.baidumap.bean.StorageBean;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by wangt on 2018/3/27.
 */
public interface LBSApi {

    //search
    @GET("geosearch/v3/nearby?")
    Observable<MarkerBean> getSearch(@Query("ak") String ak, @Query("geotable_id") String geotable_id,
                                     @Query("page_size") String page_size, @Query("radius") String radius,
                                     @Query("location") String location);

    //storage
    @FormUrlEncoded
    @POST("geodata/v3/poi/create?")
    Observable<StorageBean> getStorage(@FieldMap Map<String, String> praise);
}
