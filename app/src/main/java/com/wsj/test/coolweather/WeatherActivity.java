package com.wsj.test.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wsj.test.coolweather.gson.Forecast;
import com.wsj.test.coolweather.gson.Weather;
import com.wsj.test.coolweather.service.AutoUpdateService;
import com.wsj.test.coolweather.util.HttpUtil;
import com.wsj.test.coolweather.util.LogUtils;
import com.wsj.test.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private static final String TAG = "WeatherActivity";

    private ScrollView sv_weather;
    private TextView tv_title_city;
    private TextView tv_title_update;
    private TextView tv_now_degree;
    private TextView tv_now_info;

    private LinearLayout ll_forecast;
    private TextView tv_aqi_txt;
    private TextView tv_pm25_txt;
    private TextView tv_suggestion_comfort;
    private TextView tv_suggestion_wash;
    private TextView tv_suggestion_sport;

    private ImageView bingPicImg;
    public SwipeRefreshLayout srl_refresh;

    public DrawerLayout dl_choose_area;
    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // 使用状态栏 5.0 以后可使用.
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        // 初始化控件
        initUI();


    }

    private void initUI() {
        sv_weather = (ScrollView) findViewById(R.id.sv_weather);
        tv_title_city = (TextView) findViewById(R.id.tv_title_city_name);
        tv_title_update = (TextView) findViewById(R.id.tv_title_update_time);
        tv_now_degree = (TextView) findViewById(R.id.tv_now_degree);
        tv_now_info = (TextView) findViewById(R.id.tv_now_info);

        ll_forecast = (LinearLayout) findViewById(R.id.ll_forecast);
        tv_aqi_txt = (TextView) findViewById(R.id.tv_aqi_txt);
        tv_pm25_txt = (TextView) findViewById(R.id.tv_pm25_txt);
        tv_suggestion_comfort = (TextView) findViewById(R.id.tv_suggestion_comfort);
        tv_suggestion_wash = (TextView) findViewById(R.id.tv_suggestion_wash);
        tv_suggestion_sport = (TextView) findViewById(R.id.tv_suggestion_sport);

        bingPicImg = (ImageView) findViewById(R.id.iv_background);

        srl_refresh = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);
        dl_choose_area = (DrawerLayout) findViewById(R.id.dl_choose_city);
        navButton = (Button) findViewById(R.id.btn_nav);




        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String bingPic = prefs.getString("bing_pic",null);

        final String weatherId ;

        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

        String weatherString = prefs.getString("weather",null);
        if (weatherString != null){
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            // 无缓存时去服务器查询
            weatherId = getIntent().getStringExtra("weather_id");
            sv_weather.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        srl_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dl_choose_area.openDrawer(GravityCompat.START);
            }
        });
    }

    private void loadBingPic() {
        final String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e(TAG,"加载图片失败");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"加载背景图片失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this)
                        .edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 根据城市 id 请求城市天气信息.
     */
    public void requestWeather(final String weatherId) {
        final String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId
                + "&key=2284432853fd4d1cba71a4090c35f8cb";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e(TAG,"请求失败");
                resquestFailedProcess();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    resquestFailedProcess();
                    return;
                }
                final String responseText = response.body().string();
                LogUtils.i(TAG,"请求天气响应 : " + responseText);
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        srl_refresh.setRefreshing(false);
                        if (weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor =
                                    PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                                            .edit();

                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            resquestFailedProcess();
                        }
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 请求失败,弹出Toast
     */
    private void resquestFailedProcess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                srl_refresh.setRefreshing(false);
                Toast.makeText(getApplicationContext(),"获取天气信息失败",Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据.
     */
    private void showWeatherInfo(Weather weather) {

        String citName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        tv_title_city.setText(citName);
        tv_title_update.setText(updateTime);
        tv_now_degree.setText(degree);
        tv_now_info.setText(weatherInfo);
        ll_forecast.removeAllViews();
        for (Forecast forecast : weather.forcastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item_layout,ll_forecast,false);
            TextView dateText = (TextView) view.findViewById(R.id.tv_forecast_date);
            TextView infoText = (TextView) view.findViewById(R.id.tv_forecast_info);
            TextView maxText = (TextView) view.findViewById(R.id.tv_forecast_max);
            TextView minText = (TextView) view.findViewById(R.id.tv_forecast_min);

            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);

            ll_forecast.addView(view);
        }

        if (weather.aqi != null){
            tv_aqi_txt.setText(weather.aqi.city.aqi);
            tv_pm25_txt.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度: " + weather.suggestion.comfort.info;
        String carWash = "洗车指数: " + weather.suggestion.cardWash.info;
        String sport = "运动建议: " + weather.suggestion.sport.info;

        tv_suggestion_comfort.setText(comfort);
        tv_suggestion_wash.setText(carWash);
        tv_suggestion_sport.setText(sport);

        sv_weather.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }


}
