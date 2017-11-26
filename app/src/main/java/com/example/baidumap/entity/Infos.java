package com.example.baidumap.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Infos implements Serializable{

	private static final long serialVersionUID = 8084087341100393207L;
	private double latitude;
	private double longitude;
	private String imgurl;
	private String name;
	private String addr;
	private String distance;
	private int zan;
	
	private String returnid;
	
	private double mLatitude;
	private double mLongitude;
	
	public static List<Infos> infos = new ArrayList<Infos>();
	
	public static List<Infos> returnInfos = new ArrayList<Infos>();
	
//	static{
//		infos.add(new Infos(34.242652, 108.971171, R.drawable.a01, "水果店","距离500米", 1000));
//	}
	
//	public Infos(double latitude, double longitude, int imgId, String name,
//			String distance, int zan) {
//		super();
//		this.latitude = latitude;
//		this.longitude = longitude;
//		this.imgId = imgId;
//		this.name = name;
//		this.distance = distance;
//		this.zan = zan;
//	}
	
	

	public static List<Infos> getInfos() {
		return infos;
	}
	public static void setInfos(List<Infos> infos) {
		Infos.infos = infos;
	}
	
	//我的位置坐标
	public void setmLatitude(double mLatitude) {
		this.mLatitude = mLatitude;
	}
	public void setmLongitude(double mLongitude) {
		this.mLongitude = mLongitude;
	}
	public double getmLatitude() {
		return mLatitude;
	}
	public double getmLongitude() {
		return mLongitude;
	}
	
	
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getImgurl() {
		return imgurl;
	}
	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}
	public int getZan() {
		return zan;
	}
	public void setZan(int zan) {
		this.zan = zan;
	}
	
	
	//云存储返回数据
	public String getReturnid() {
		return returnid;
	}
	public void setReturnid(String returnid) {
		this.returnid = returnid;
	}
	
	public static List<Infos> getReturnInfos() {
		return returnInfos;
	}
	public static void setReturnInfos(List<Infos> returnInfos) {
		Infos.returnInfos = returnInfos;
	}
	
	
	
	
}
