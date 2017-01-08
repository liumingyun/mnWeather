package com.example.administrator.weather.util;

import android.text.TextUtils;

import com.example.administrator.weather.db.City;
import com.example.administrator.weather.db.County;
import com.example.administrator.weather.db.Province;
import com.example.administrator.weather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/1/8.
 */

public class Utility {

    /**
     * 解析服务器返回来省的数据
     */
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){

            try{
                JSONArray alleProvinces=new JSONArray(response);
                for (int i=0;i<alleProvinces.length();i++){
                    JSONObject provinceObject=alleProvinces.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getString("id"));
                    province.save();//保存数据库
                }

                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 解析服务器返回来市的数据
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){

            try{
                JSONArray alleCitys=new JSONArray(response);
                for (int i=0;i<alleCitys.length();i++){
                    JSONObject cityObject=alleCitys.getJSONObject(i);
                    City city=new City();
                    city.setCityCode(cityObject.getString("id"));
                    city.setCityName(cityObject.getString("name"));
                    city.setProvinceId(provinceId+"");
                    city.save();//保存数据库
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析服务器返回来县的数据
     */
    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){

            try{
                JSONArray alleCountys=new JSONArray(response);
                for (int i=0;i<alleCountys.length();i++){
                    JSONObject countyObject=alleCountys.getJSONObject(i);
                    County county=new County();
                    county.setCityId(cityId+"");
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCountyName(countyObject.getString("name"));
                //   county.setCountyCode(countyObject.getString("id"));
                    county.save();//保存数据库
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 解析成实体类
     */


    public static Weather handleWeatherResponse(String response){

        try{
            JSONObject object=new JSONObject(response);
            JSONArray jsonArray=object.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return  new Gson().fromJson(weatherContent,Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }




}
