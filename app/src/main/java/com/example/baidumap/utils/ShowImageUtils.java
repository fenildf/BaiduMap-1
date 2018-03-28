package com.example.baidumap.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

/**
 * Created by wangt on 2017/12/13.
 */

public class ShowImageUtils {

    /**
     * (1)
     * 显示图片Imageview
     *
     * @param context  上下文
     * @param errorimg 错误的资源图片
     * @param url      图片链接
     * @param imgeview 组件
     */
    public static void showImageView(Context context, int errorimg, String url, ImageView imgeview) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(errorimg)// 设置占位图
                .error(errorimg)// 设置错误图片
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.NONE);

        Glide.with(context)
                .load(url)// 加载图片
                .apply(options)
                .into(imgeview);

    }

    /**
     * （2）
     * 获取到Bitmap---不设置错误图片，错误图片不显示
     *
     * @param context
     * @param imageView
     * @param url
     */

    public static void showImageViewGone(final Context context, final ImageView imageView, String url) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.NONE);

        Glide.with(context).asBitmap().load(url)
                .apply(options)
                .into(new SimpleTarget<Bitmap>() {

                    @SuppressLint("NewApi")
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        imageView.setVisibility(View.VISIBLE);
                        BitmapDrawable bd = new BitmapDrawable(context.getResources(), resource);
                        imageView.setImageDrawable(bd);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageView.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * （3）
     * 设置RelativeLayout
     * <p>
     * 获取到Bitmap
     *
     * @param context
     * @param errorimg
     * @param url
     * @param bgLayout
     */

    public static void showImageView(final Context context, int errorimg, String url, final RelativeLayout bgLayout) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(errorimg)
                .priority(Priority.HIGH)
                .placeholder(errorimg)
                .diskCacheStrategy(DiskCacheStrategy.NONE);

        Glide.with(context).asBitmap().load(url)
                .apply(options)
                .into(new SimpleTarget<Bitmap>() {

                    @SuppressLint("NewApi")
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        BitmapDrawable bd = new BitmapDrawable(context.getResources(), resource);
                        bgLayout.setBackground(bd);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        bgLayout.setBackground(errorDrawable);
                    }
                });

    }

    /**
     * （4）
     * 设置LinearLayout
     * <p>
     * 获取到Bitmap
     *
     * @param context
     * @param errorimg
     * @param url
     * @param bgLayout
     */

    public static void showImageView(final Context context, int errorimg, String url, final LinearLayout bgLayout) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(errorimg)
                .priority(Priority.HIGH)
                .placeholder(errorimg)
                .diskCacheStrategy(DiskCacheStrategy.NONE);


        Glide.with(context).asBitmap().load(url)
                .apply(options)
                .into(new SimpleTarget<Bitmap>() {

                    @SuppressLint("NewApi")
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        BitmapDrawable bd = new BitmapDrawable(context.getResources(), resource);
                        bgLayout.setBackground(bd);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        bgLayout.setBackground(errorDrawable);
                    }
                });

    }

    /**
     * （5）
     * 设置FrameLayout
     * <p>
     * 获取到Bitmap
     *
     * @param context
     * @param errorimg
     * @param url
     * @param frameBg
     */

    public static void showImageView(final Context context, int errorimg, String url, final FrameLayout frameBg) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(errorimg)
                .priority(Priority.HIGH)
                .placeholder(errorimg)
                .diskCacheStrategy(DiskCacheStrategy.NONE);

        Glide.with(context).asBitmap().load(url)
                .apply(options)
                .into(new SimpleTarget<Bitmap>() {

                    @SuppressLint("NewApi")
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        BitmapDrawable bd = new BitmapDrawable(context.getResources(), resource);

                        frameBg.setBackground(bd);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        frameBg.setBackground(errorDrawable);
                    }
                });

    }
}
