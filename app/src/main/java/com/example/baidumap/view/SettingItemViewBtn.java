package com.example.baidumap.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.baidumap.R;

public class SettingItemViewBtn extends LinearLayout{

	TextView myLeftTextView;
	TextView myRightTextView;
	
	public SettingItemViewBtn(Context context, AttributeSet attrs) {
		super(context, attrs);
		initview();
	}

	private void initview() {
		LayoutInflater inflater=LayoutInflater.from(getContext());
		View view=inflater.inflate(R.layout.setting_view_btn, this);
		findView(view);
	}

	private void findView(View view) {
		myLeftTextView = (TextView) view.findViewById(R.id.setting_btn_LeftText);
		myRightTextView = (TextView) view.findViewById(R.id.setting_btn_RightText);
	}
    /**
     * 返回左边的控件
     * @return
     */
	public TextView getMyLeftTextView() {
		return myLeftTextView;
	}
	/**
	 * 返回右边的控件
	 * @return
	 */
	public TextView getMyRightTextView() {
		return myRightTextView;
	}
	
	
	/**
	 * 设置左边的文字
	 * @param txt
	 */
	public void setLeftText(String txt){
		myLeftTextView.setText(txt);
	}
	/**
	 * 设置右边的文字
	 * @param txt
	 */
	public void setRightText(String txt){
		myRightTextView.setText(txt);
	}
	/**
	 * 设置右边的图片
	 * @param resId
	 */
	public void setRightBitMap(int resId) {
		myRightTextView.setBackgroundResource(resId);
	}
}
