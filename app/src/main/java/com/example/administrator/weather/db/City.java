package com.example.administrator.weather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/1/8.
 */

public class City extends DataSupport {
    private String id;
    private String cityCode;
    private String cityName;
    private String provinceId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(String provinceId) {
        this.provinceId = provinceId;
    }
}
