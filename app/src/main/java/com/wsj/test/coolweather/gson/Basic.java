package com.wsj.test.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Basic 实体类<br>
 * Created by WSJ on 2017/2/10.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
