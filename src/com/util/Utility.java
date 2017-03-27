package com.util;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.db.CoolWeatherDB;
import com.google.gson.Gson;
import com.gson.Weather;
import com.model.City;
import com.model.County;
import com.model.Province;

import android.R.string;
import android.text.TextUtils;
import android.util.Log;

public class Utility {

	//解析和处理服务器返回的省级数据
	public synchronized static boolean handleProvincesResponse (CoolWeatherDB coolWeatherDB, String response){
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null  && allProvinces.length > 0) {
				for (String pString : allProvinces) {
					String[] array = pString.split("\\|");
					Province province = new Province();
//					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					
					//将解析出来的数据存储到Province表
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	//解析和处理服务器返回的市级数据
	public static Boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId ) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String cString : allCities) {
					String[] array = cString.split("\\|");
					City city = new City();
//					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvindeId(provinceId);
					
					//将解析出来的数据存储到City表
					coolWeatherDB.saveCity(city);
				}
				
				
			return true;	
			}
		}
		
	return 	 false;
	}
	
	 //解析和处理服务器返回的县级信息
	public static Boolean  handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String cString : allCounties) {
					String[] array = cString.split("\\|");
					County county = new County();
//					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					
					coolWeatherDB.saveCounty(county);
				}
			}
			
			
		}
		
		return false;
	}
	
	
	/**
	 * 
	 * 
	 * @param response
	 * @param coolWeatherDB
	 * @return
	 */
	public static Boolean  handleProvinceWithJson(String response, CoolWeatherDB coolWeatherDB) {
		if (!TextUtils.isEmpty(response)) {
			try {
				JSONArray allProvinces = new JSONArray(response);
				for (int i = 0; i < allProvinces.length(); i++) {
					JSONObject provinceObject = allProvinces.getJSONObject(i);
					Province province = new Province();
					province.setProvinceName(provinceObject.getString("name"));
					province.setProvinceCode(provinceObject.getInt("id"));

					coolWeatherDB.saveProvince(province);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
/**
 * 
 * *
 * @param response
 * @param coolWeatherDB
 * @param provinceId
 * @return
 */
	public static Boolean  handleCityWithJson(String response, CoolWeatherDB coolWeatherDB, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			try {
				JSONArray allProvinces = new JSONArray(response);
				for (int i = 0; i < allProvinces.length(); i++) {
					JSONObject cityObject = allProvinces.getJSONObject(i);
					City city = new City();
					city.setCityName(cityObject.getString("name"));
					city.setCityCode(cityObject.getInt("id"));
					city.setProvindeId(provinceId);
					
					coolWeatherDB.saveCity(city);
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * 
	 * *
	 * @param response
	 * @param coolWeatherDB
	 * @param cityId
	 * @return
	 */
	public static Boolean  handleCountyWithJson(String response, CoolWeatherDB coolWeatherDB, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			try {
				JSONArray allCounties = new JSONArray(response);
				for (int i = 0; i < allCounties.length(); i++) {
					JSONObject countyObject = allCounties.getJSONObject(i);
					County county = new County();
					county.setCountyName(countyObject.getString("name"));
					county.setWeatherId(countyObject.getString("weather_id"));
					county.setCityId(cityId);
					
					coolWeatherDB.saveCounty(county);
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	
	
	
	
	//将返回的json数据解析成 Weather实体类
	public static  Weather 	 handleWeatherResponse(String response) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
			String weatherContent = jsonArray.getJSONObject(0).toString();
			
			Log.d("json数据解析", weatherContent);
	
			return new Gson().fromJson(weatherContent, Weather.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
