package com.example.baidumap.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by wangt on 2018/3/28.
 */
public class ToastUtils {

    /**
     * 重写吐司
     *
     * @param text
     */
    public static void showToast(Context context, String text) {
        Toast mtoast = null;
        if (mtoast == null) {
            mtoast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            mtoast.setText(text);
            mtoast.setDuration(Toast.LENGTH_SHORT);
        }
        mtoast.show();
//		mtoast.setGravity(Gravity.BOTTOM, 0, 0);  //窗口位置
    }
}
