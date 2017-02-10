package com.wsj.test.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 天气实体类<br>
 * Created by WSJ on 2017/2/10.
 */

public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;


    @SerializedName("daily_forecast")
    public List<Forecast> forcastList;
}
