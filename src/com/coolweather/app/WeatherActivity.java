package com.coolweather.app;

import com.gson.Forecast;
import com.gson.Weather;
import com.util.HttpCallbackListener;
import com.util.HttpUtil;
import com.util.Utility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherActivity extends Activity {

	ScrollView weatherLayout;
	TextView titieCity;
	TextView updateTime;
	
	TextView  degreeText;
	TextView weatherInfoText;
	
	LinearLayout forecastLayout;
	
	TextView aqiText;
	TextView pm25Text;
	
	TextView comfortText;
	TextView carWashText;
	TextView sportText;
	
	static String TAG = "WeatherActivity";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);
		
		weatherLayout = (ScrollView)findViewById(R.id.weather_layout);
		titieCity = (TextView)findViewById(R.id.title_city);
		updateTime = (TextView)findViewById(R.id.titie_update_time);
		
		degreeText = (TextView)findViewById(R.id.degree_text);
		weatherInfoText = (TextView)findViewById(R.id.weather_info_text);
		
		forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
		
		aqiText = (TextView)findViewById(R.id.aqi_text);
		pm25Text = (TextView)findViewById(R.id.pm25_text);
		
		comfortText = (TextView)findViewById(R.id.comfort_text);
		carWashText = (TextView)findViewById(R.id.car_wash_text);
		sportText = (TextView)findViewById(R.id.sport_text);
		
		
		//提取本地缓存数据
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherString = preferences.getString("weather", null);
		if (weatherString != null) {
			//有缓存直接解析天气数据
			Log.d("WeatherActivity",	 "从缓存中提取数据");
			Weather weather = Utility.handleWeatherResponse(weatherString);
				showWeatherInfo(weather);
				
				
		}else {
			//无缓存则从服务器上查询天气
			Log.d("WeatherActivity",	 "从服务器请求天气预报数据");
			String weatherId =getIntent().getStringExtra("weather_id");
			weatherLayout.setVisibility(View.INVISIBLE);
			
			requestWeather(weatherId);
		}
		
		

		
	}
	
	
	private void requestWeather(final String weatherId){
		String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" 
					+ weatherId +"&key=93ad8e7d270642438633abd687b877ef";
	
		HttpUtil.sendHttpRequest(weatherUrl, new HttpCallbackListener() {			
			@Override
			public void onFinish(String response) {

				final	Weather weather = Utility.handleWeatherResponse(response);
				if (weather != null && "ok".equals(weather.status)) {
					SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
					editor.putString("weather", response);
					editor.apply();
					
					runOnUiThread(new Runnable() {
						public void run() {
							Log.d(TAG, "开始刷新界面显示, 当请求完成后" );
							showWeatherInfo(weather);
						}
					});
				}else {
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(WeatherActivity.this, "加载失败", Toast.LENGTH_SHORT).show();	
						}
					});
				}
			}
	
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(WeatherActivity.this, "加载失败", Toast.LENGTH_SHORT).show();	
					}
				});
			}
		});
		
		
	}
	
	
	
	//处理并展示Weather实体类中的数据
	private void showWeatherInfo(Weather weather) {
		
		String cityName = weather.basic.cityName;
		String upTime = weather.basic.update.updateTime.split(" ")[1]; //提取出时分
		
		String degree = weather.now.remperataure + "℃";
		String weatherInfo = weather.now.more.info;
		
		Log.d(TAG, "观察Weather是否为空 : " +weather + 
				"\n cityName" + cityName +
				"\n updateTime:" + upTime +
				"\n degree: " + degree +
				"\n weatherInfo: " + weatherInfo);
		
		
		if (cityName != null) {
			Log.d(TAG, "cityName: " + cityName);
			titieCity.setText(cityName);
		}
		
		if (upTime != null) {
			Log.d(TAG, "updateTime: " + upTime);
			updateTime.setText(upTime);
		}
		
		if (degree != null) {
			Log.d(TAG, "degree: " + degree);
			degreeText.setText(degree);
		}

		if (weatherInfo != null) {
			Log.d(TAG, "weatherInfo: " + weatherInfo);
			weatherInfoText.setText(weatherInfo);
		}
		
		forecastLayout.removeAllViews();
		Log.d(TAG, "移除所有控件");
		
		
		
		if (weather.forecastList != null) {
			Log.d(TAG, "list存在:" + weather.forecastList);
			if (weather.forecastList.size() > 0) {
				for (Forecast forecast : weather.forecastList) {
					View view  = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
					
					TextView dateText = (TextView)view.findViewById(R.id.date_text);
					TextView infoText = (TextView)view.findViewById(R.id.info_text);
					TextView maxText = (TextView)view.findViewById(R.id.max_text);
					TextView minText = (TextView)view.findViewById(R.id.min_text);
					
					dateText.setText(forecast.date);
					infoText.setText(forecast.more.info);
					maxText.setText(forecast.temperature.max);
					minText.setText(forecast.temperature.min);
					forecastLayout.addView(view);
				}
			}
			
		}else {
			Log.d(TAG, "forecastList 队列为空");
		}
		
		
	
		if (weather.aqi != null) {
			aqiText.setText(weather.aqi.city.aqi);
			pm25Text.setTag(weather.aqi.city.pm25);
		}
		
		String comfort ="舒适度: " + weather.suggestion.comfort.info;
		String carWash = "洗车指数:  " + weather.suggestion.carWash.info;
		String sport = "运动建议:  " + weather.suggestion.sport.info;
		weatherLayout.setVisibility(View.VISIBLE);
	}
	
	
	
	
}
