package com.wsj.test.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 网络工具类<br>
 * Created by WSJ on 2017/2/10.
 */

public class HttpUtil {
    private static final String TAG = "HttpUtil";
    public static void sendOkHttpRequest(final String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
