package com.example.baidumap.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangt on 2018/3/27.
 */
public class MarkerBean implements Serializable {

    private int status;
    private int total;
    private int size;
    private List<ContentsBean> contents;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<ContentsBean> getContents() {
        return contents;
    }

    public void setContents(List<ContentsBean> contents) {
        this.contents = contents;
    }

    public static class ContentsBean implements Serializable {
        /**
         * uid : 1611996505
         * province : 浙江省
         * GEOTABLE_ID : 133284
         * modify_time : 1455794278
         * district : 椒江区
         * create_time : 1454743302
         * city : 台州市
         * location : [121.398248,28.663937]
         * address : 浙江省台州市椒江区市府大道
         * title : 33
         * coord_type : 3
         * type : 0
         * distance : 0
         * weight : 0
         */

        private String uid;
        private String province;
        private String geotable_id;
        private String modify_time;
        private String district;
        private String create_time;
        private String city;
        private String address;
        private String title;
        private int coord_type;
        private int type;
        private int distance;
        private int weight;
        private List<Double> location;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getGeotable_id() {
            return geotable_id;
        }

        public void setGeotable_id(String geotable_id) {
            this.geotable_id = geotable_id;
        }

        public String getModify_time() {
            return modify_time;
        }

        public void setModify_time(String modify_time) {
            this.modify_time = modify_time;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public String getCreate_time() {
            return create_time;
        }

        public void setCreate_time(String create_time) {
            this.create_time = create_time;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getCoord_type() {
            return coord_type;
        }

        public void setCoord_type(int coord_type) {
            this.coord_type = coord_type;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getDistance() {
            return distance;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public List<Double> getLocation() {
            return location;
        }

        public void setLocation(List<Double> location) {
            this.location = location;
        }
    }
}
