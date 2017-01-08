package com.example.administrator.weather;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administrator.weather.gson.Forecast;
import com.example.administrator.weather.gson.Weather;
import com.example.administrator.weather.service.AutoUpdateService;
import com.example.administrator.weather.util.HttpUtil;
import com.example.administrator.weather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/1/8.
 */

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private  ImageView bgimage;
    public SwipeRefreshLayout  sw;
    private Button buttonHome;
    public DrawerLayout drawLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        buttonHome= (Button) findViewById(R.id.nav_button);
        drawLayout= (DrawerLayout) findViewById(R.id.draw_layout);
        weatherLayout= (ScrollView) findViewById(R.id.weather_layout);
        titleCity= (TextView) findViewById(R.id.title_city);
        titleUpdateTime= (TextView) findViewById(R.id.title_update_time);
        degreeText= (TextView) findViewById(R.id.degree_text);
        weatherInfoText=(TextView) findViewById(R.id.weather_info_text);
        forecastLayout= (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText=(TextView) findViewById(R.id.aqi_text);
        pm25Text=(TextView) findViewById(R.id.pm25_text);
        comfortText=(TextView) findViewById(R.id.comfort_text);
        carWashText=(TextView) findViewById(R.id.car_wash_text);
        sportText=(TextView) findViewById(R.id.sport_text);
        bgimage= (ImageView) findViewById(R.id.bg_pic_image);

        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawLayout.openDrawer(GravityCompat.START);
            }
        });


        sw= (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        sw.setColorSchemeResources(R.color.colorPrimary);


        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        final  String weatherId;
        if (weatherString!=null){
            //有缓存直接用
            Weather weather= Utility.handleWeatherResponse(weatherString);
            weatherId=weather.basic.weatherId;
            ShowWeatherInfo(weather);
        }else{
            //无缓存查询
            weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.VISIBLE);
            requestWeather(weatherId);
        }


        sw.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        String bigPic=prefs.getString("bing_pic",null);
        if (bigPic!=null){
            Glide.with(this).load(bigPic).into(bgimage);
        }else{
            loadImage();
        }

    }

    public void loadImage() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bgimage);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });

    }


    /**
     * 获取数据
     * @param weatherId
     */
    public void requestWeather(String weatherId) {

        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=c8d825be68b14dda96fd8de268238f30";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取信息失败",Toast.LENGTH_LONG).show();
                        sw.setRefreshing(false);
                    }

                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather w=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (w!=null&&"ok".equals(w.status)){
                             SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            ShowWeatherInfo(w);
                            sw.setRefreshing(false);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取信息失败",Toast.LENGTH_LONG).show();
                            sw.setRefreshing(false);
                        }

                    }
                });
            }
        });
        loadImage();

    }

    private void ShowWeatherInfo(Weather weather) {
        if (weather!=null&&"ok".equals(weather.status)){
            String cityName=weather.basic.cityName;
            String updateTime=weather.basic.update.updateTime.split(" ")[1];
            String degree=weather.now.temperature+"℃";
            String weatherInfo=weather.now.more.info;
            titleCity.setText(cityName);
            titleUpdateTime.setText(updateTime);
            degreeText.setText(degree);
            weatherInfoText.setText(weatherInfo);
            forecastLayout.removeAllViews();
            for (Forecast f:weather.forecastList){
                View v= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
                TextView dateText= (TextView) v.findViewById(R.id.date_text);
                TextView infoText= (TextView) v.findViewById(R.id.info_text);
                TextView maxText= (TextView) v.findViewById(R.id.max_text);
                TextView minText= (TextView) v.findViewById(R.id.min_text);
                dateText.setText(f.date);
                infoText.setText(f.more.info);
                maxText.setText(f.temperature.max);
                minText.setText(f.temperature.min);
                forecastLayout.addView(v);
            }
            if (weather.aqi!=null){
                aqiText.setText(weather.aqi.city.aqi);
                pm25Text.setText(weather.aqi.city.pm25);
            }
            String comfort="舒适度:"+weather.suggestion.comfort.info;
            String carWash="洗车指数:"+weather.suggestion.carWash.info;
            String sport="运动建议:"+weather.suggestion.sport.info;

            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);
            weatherLayout.setVisibility(View.VISIBLE);

            Intent intent=new Intent(this, AutoUpdateService.class);
            startService(intent);
        }else {
            Toast.makeText(WeatherActivity.this,"获取信息失败",Toast.LENGTH_LONG).show();
        }


    }


}
