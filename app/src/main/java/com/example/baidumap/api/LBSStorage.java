package com.example.baidumap.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 百度云存储使用类 POST请求
 * 
 * @author Lu.Jian
 * 
 */
public class LBSStorage {
	private static String mTAG = "LBSStorage";
	// 百度云检索API URI
	private static final String SEARCH_URI = "http://api.map.baidu.com/geodata/v3/poi/create";
	// 云检索公钥
	private static String ak = "W9aWyT6yAaILUOur0j8BjOgl";
	private static String geotable_id = "133284";
	private static String coord_type = "3";

	private static int retry = 1;
	private static boolean IsBusy = false;

	/**
	 * 云存储访问
	 * 
	 * @param filterParams
	 * @param handler
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
						// String requestURL = "";
						// requestURL = SEARCH_URI;
						// requestURL = requestURL + "&" + "ak=" + ak
						// + "&geotable_id=" + geotable_id;
						//
						// Iterator iter = filterParams.entrySet().iterator();
						//
						// while (iter.hasNext()) {
						// Map.Entry entry = (Map.Entry) iter.next();
						// String key = entry.getKey().toString();
						// String value = entry.getValue().toString();
						//
						// requestURL = requestURL + "&" + key + "=" + value;
						// }
						// Log.d(mTAG, "request url:" + requestURL);

						URL requestUrl = new URL(SEARCH_URI);
						HttpURLConnection connection = (HttpURLConnection) requestUrl
								.openConnection();
						connection.setDoOutput(true);
						connection.setDoInput(true);
						connection.setRequestMethod("POST");
						// Post 请求不能使用缓存
						connection.setUseCaches(false);
						connection.setInstanceFollowRedirects(true);
						connection.setRequestProperty("Content-Type",
								"application/x-www-form-urlencoded");

						// 建立实际的连接
						connection.connect();

						DataOutputStream out = new DataOutputStream(
								connection.getOutputStream());
						
						String content = "";
						content = "&" + "ak=" + ak +
								"&geotable_id=" + geotable_id + 
								"&coord_type=" + coord_type;
						Iterator iter = filterParams.entrySet().iterator();
						while (iter.hasNext()) {
							Map.Entry entry = (Map.Entry) iter.next();
							String key = entry.getKey().toString();
							String value = entry.getValue().toString();
							String valueString = URLEncoder.encode(value, "UTF-8");

							content = content + "&" + key + "=" + valueString;
						}
						Log.e(mTAG, content);
						
						out.writeBytes(content);
						out.flush();
						out.close();
						
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(
										connection.getInputStream(), "utf-8"));

						StringBuilder entityStringBuilder = new StringBuilder();
						String result = "";
						while ((result = reader.readLine()) != null) {
							entityStringBuilder.append(result + "/n");
							Log.e(mTAG, result);
						}
						Message msg = handler.obtainMessage();
						msg.what = 3;
						msg.obj = entityStringBuilder.toString();
						msg.sendToTarget();
						
						reader.close();  
				        connection.disconnect();

					} catch (Exception e) {
						Log.e(mTAG, "POST请求错误！");
						e.printStackTrace();
					}
					count--;
				}
				IsBusy = false;
			};
		}.start();
		return true;

	}
}
