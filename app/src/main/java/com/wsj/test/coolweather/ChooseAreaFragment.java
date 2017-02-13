package com.wsj.test.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wsj.test.coolweather.db.City;
import com.wsj.test.coolweather.db.Country;
import com.wsj.test.coolweather.db.Province;
import com.wsj.test.coolweather.util.HttpUtil;
import com.wsj.test.coolweather.util.LogUtils;
import com.wsj.test.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 城市选择界面<br>
 * Created by WSJ on 2017/2/10.
 */

public class ChooseAreaFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "ChooseAreaFragment";
    // 当前选择界别
    public static final int LEVEL_PROVINCE = 0x01;
    public static final int LEVEL_CITY     = 0x02;
    public static final int LEVEL_COUNTRY  = 0x03;
    // 查询类型
    public static final int TYPE_PROVINCE = 0x01;
    public static final int TYPE_CITY     = 0x02;
    public static final int TYPE_COUNTRY  = 0x03;

    public static final String WEATHER_API_PROVINCE = "http://guolin.tech/api/china";
    // 加载进度条
    private ProgressDialog mProgressDialog;
    private TextView tv_title_txt;
    private Button btn_title_back;
    private ListView lv_city_list;

    private ArrayAdapter<String> mAdapter;
    // 当前数据列表
    private List<String> mCurrentDataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> mProvinceList = new ArrayList<>();
    /**
     * 市列表
     */
    private List<City> mCityList = new ArrayList<>();
    /**
     * 县列表
     */
    private List<Country> mCountryList = new ArrayList<>();

    /**
     * 选中的省份
     */
    private Province mSelectedProvince;
    /**
     * 选中的城市
     */
    private City mSelectedCity;

    /**
     * 当前选中级别
     */
    private int mCurrentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtils.i(TAG,">>> onCreateView");
        // 加载布局
        View view = inflater.inflate(R.layout.choose_area_fragment,container,false);
        // 找到控件.
        tv_title_txt = (TextView) view.findViewById(R.id.tv_title_txt);
        btn_title_back = (Button) view.findViewById(R.id.btn_title_back);
        lv_city_list = (ListView) view.findViewById(R.id.lv_city_list);
        // 设置适配器
        mAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1,
                mCurrentDataList);

        lv_city_list.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtils.i(TAG,">>> onActivityCreated");
        // 设置ListView点击事件
        lv_city_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogUtils.i(TAG,"当前级别 : " + mCurrentLevel);
                if (mCurrentLevel == LEVEL_PROVINCE){
                    mSelectedProvince = mProvinceList.get(position);
                    // 加载该省份城市列表
                    queryCities();
                }else if (mCurrentLevel == LEVEL_CITY){
                    mSelectedCity = mCityList.get(position);
                    // 加载该城市的县城列表
                    queryCounties();
                }
                else if (mCurrentLevel == LEVEL_COUNTRY){
                    String weatherId = mCountryList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity){
                        // 加载详细信息
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.dl_choose_area.closeDrawers();
                        activity.srl_refresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }

                }
            }
        });
        btn_title_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentLevel == LEVEL_CITY){
                    // 省列表
                    querProvinces();
                }else if (mCurrentLevel == LEVEL_COUNTRY){
                    queryCities();
                }
            }
        });

        // 启动时显示省列表
        querProvinces();
    }

    /**
     * 加载省列表<br>
     *     先从数据库中获取.
     */
    private void querProvinces() {
        LogUtils.i(TAG,"获取省份列表");
        tv_title_txt.setText("中国");
        btn_title_back.setVisibility(View.GONE);
        // 数据库获取省列表
        mProvinceList = DataSupport.findAll(Province.class);
        LogUtils.i(TAG,"123");
        if (mProvinceList.size() > 0){
            mCurrentDataList.clear();
            for (Province province : mProvinceList) {
                mCurrentDataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            lv_city_list.setSelection(0);
            mCurrentLevel = LEVEL_PROVINCE;
        }else {
            // 网络获取
            queryFromServer(WEATHER_API_PROVINCE,TYPE_PROVINCE);
        }
    }
    /**
     * 加载该城市的县城列表.
     */
    private void queryCities() {
        LogUtils.i(TAG,"获取城市列表");
        tv_title_txt.setText(mSelectedProvince.getProvinceName());
        btn_title_back.setVisibility(View.VISIBLE);

        LogUtils.i(TAG,"城市 id : " + mSelectedProvince.getId());
        mCityList = DataSupport.where("provinceid = ?",String.valueOf(mSelectedProvince.getId()))
                .find(City.class);
        LogUtils.i(TAG,"数据库数据 : " + DataSupport.findAll(City.class));
        for (City city : DataSupport.findAll(City.class)) {
            LogUtils.i(TAG,"城市 : " + city.getCityName());
            LogUtils.i(TAG,"省份ID : " + city.getProvinceId());
        }
        if (mCityList.size() > 0){
            mCurrentDataList.clear();
            for (City city : mCityList) {
                mCurrentDataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            lv_city_list.setSelection(0);
            mCurrentLevel = LEVEL_CITY;
        }else {
            final int provinceCode = mSelectedProvince.getProvinceCode();
            final String cityUrl = WEATHER_API_PROVINCE + "/" + provinceCode;
            LogUtils.d(TAG,"City URL : " + cityUrl);
            queryFromServer(cityUrl,TYPE_CITY);
        }
    }

    /**
     * 加载城市列表.
     */
    private void queryCounties() {
        LogUtils.i(TAG,"获取县城列表 : queryCities");
        tv_title_txt.setText(mSelectedProvince.getProvinceName());
        btn_title_back.setVisibility(View.VISIBLE);

        mCountryList = DataSupport.where("cityid = ?",String.valueOf(mSelectedCity.getId()))
                .find(Country.class);
        LogUtils.i(TAG,"县城个数 : " + mCountryList.size());
        if (mCountryList.size() > 0){
            LogUtils.d(TAG,"数据库中获取县城");
            mCurrentDataList.clear();
            for (Country country : mCountryList) {
                mCurrentDataList.add(country.getCountryName());
            }
            mAdapter.notifyDataSetChanged();
            lv_city_list.setSelection(0);
            mCurrentLevel = LEVEL_COUNTRY;
        }else{
            LogUtils.d(TAG,"获取县城列表");
            final int proviceCode = mSelectedProvince.getProvinceCode();
            final int cityCode = mSelectedCity.getCityCode();
            final String address = WEATHER_API_PROVINCE + "/" + proviceCode + "/" + cityCode;
            queryFromServer(address,TYPE_COUNTRY);
        }
    }

    /**
     * 网络加载数据
     * @param address URL
     * @param type    请求类型
     */
    private void queryFromServer(final String address,final int type) {
        // 显示进度条
        showProgressDialog();

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e(TAG,"请求失败");
                requestFailed();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()){
                    LogUtils.e(TAG,"请求失败");
                    requestFailed();
                    return;
                }

                String responseText = response.body().string();
                LogUtils.i(TAG,"请求成功 : " + responseText);
                boolean result = false;
                if (type == TYPE_PROVINCE){
                    result = Utility.handleProvinceResponse(responseText);
                }else if (type == TYPE_COUNTRY){
                    result = Utility.handleCountryResponse(responseText,mSelectedCity.getId());
                }else if (type == TYPE_CITY){
                    result = Utility.handleCityResponse(responseText,mSelectedProvince.getId());
                }

                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (type){
                                case TYPE_PROVINCE:
                                    querProvinces();
                                    break;
                                case TYPE_CITY:
                                    queryCities();
                                    break;
                                case TYPE_COUNTRY:
                                    queryCounties();
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
            }
        });
    }

    private void requestFailed() {
        // 通过 runOnUiThread 回调到主线程中进行出力.
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeProgressDialog();
                Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 显示加载框
     */
    private void showProgressDialog() {
        if (mProgressDialog == null){
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }
    private void closeProgressDialog(){
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }
}





























