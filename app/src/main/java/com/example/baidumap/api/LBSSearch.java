package com.example.baidumap.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.R.string;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 百度云检索使用类  GET请求
 * 
 * @author Lu.Jian
 * 
 */
public class LBSSearch {
	private static String mTAG = "LBSSearch";
	// 百度云检索API URI
	private static final String SEARCH_URI_LOCAL = "http://api.map.baidu.com/geosearch/v3/local?";
	// 云检索公钥
	private static String ak = "W9aWyT6yAaILUOur0j8BjOgl";
	//每页显示数量
	private static String page_size = "50";
	private static String geotable_id = "133284";

	private static int retry = 1;
	private static boolean IsBusy = false;
	
	/**
	 * 云检索访问
	 * 
	 * @param filterParams
	 *            访问参数.
	 * @param handler
	 *            数据回调Handler
	 * @return
	 */
	public static boolean request(final HashMap<String, String> filterParams,
			final Handler handler) {

		if (IsBusy || filterParams == null)
			return false;
		IsBusy = true;

		new Thread() {
			public void run() {
				int count = retry;
				while (count > 0) {
					try {
						String requestURL = "";
						requestURL = SEARCH_URI_LOCAL;
						requestURL = requestURL + "&" + "ak=" + ak
								+ "&geotable_id=" + geotable_id 
								+ "&page_size=" + page_size;

						Iterator iter = filterParams.entrySet().iterator();

						while (iter.hasNext()) {
							Map.Entry entry = (Map.Entry) iter.next();
							String key = entry.getKey().toString();
							String value = entry.getValue().toString();

							requestURL = requestURL + "&" + key + "=" + value;
						}
						Log.d(mTAG, "request url:" + requestURL);

						URL requestUrl = new URL(requestURL);
						HttpURLConnection connection = (HttpURLConnection) requestUrl
								.openConnection();
						// 建立实际的连接
						connection.connect();
						
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(
										connection.getInputStream(), "utf-8"));
						
						StringBuilder entityStringBuilder=new StringBuilder();
						String result = "";
						while ((result = reader.readLine()) != null) {
							entityStringBuilder.append(result+"/n");
							Log.d(mTAG, result);
						}
						Message msg = handler.obtainMessage();
						msg.what = 0;
						msg.obj = entityStringBuilder.toString();
						msg.sendToTarget();
						
						reader.close();  
				        connection.disconnect();

					} catch (Exception e) {
						Log.e(mTAG, "GET请求错误！");
						e.printStackTrace();
					}
					count--;
				}
				IsBusy = false;
			}
		}.start();
		return true;
	}
}
