package com.example.administrator.weather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/1/8.
 */

public class Province extends DataSupport {

    private int id;
    private String provinceName;

    private String provinceCode;

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
