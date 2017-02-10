package com.wsj.test.coolweather.gson;

/**
 * aqi 实体类 <br>
 * Created by WSJ on 2017/2/10.
 */

public class AQI {

    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
