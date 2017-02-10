package com.wsj.test.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * 省份<br>
 * Created by WSJ on 2017/2/10.
 */

public class Province extends DataSupport {
    // province id
    private int id;
    // province name
    private String provinceName;
    // province code
    private int provinceCode;

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }
}
