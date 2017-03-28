package com.service;

import java.io.IOException;

import com.gson.Weather;
import com.util.HttpUtil;
import com.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;import android.text.style.UpdateAppearance;
import android.util.Log;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService  extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("autoUpdateService", "启用了定时服务");
		updateWeather();
		updateBingPic();
		
		AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
		int anHour = 8*60*60 *1000; //8小时更新一次
		long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
		Intent intent2 = new Intent(this, AutoUpdateService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent2, 0);
		manager.cancel(pendingIntent);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	
	
	
	
	private void updateWeather(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
		String weatherString = preferences.getString("weather", null);
		if (weatherString != null) {
			//有缓存时直接解析天气数据
			Weather weather = Utility.handleWeatherResponse(weatherString);
			String weatherId = weather.basic.weatherId;
			String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" 
					+ weatherId +"&key=93ad8e7d270642438633abd687b877ef";
			
			HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
				@Override
				public void onResponse(Call arg0, Response arg1) throws IOException {
					String responseText = arg1.body().string();
					Weather weather = Utility.handleWeatherResponse(responseText);
					if (weather != null && "ok".equals(weather.status)) {
						
						SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
						editor.putString("weather", responseText);
						editor.apply();
						
					}
				}
				
				@Override
				public void onFailure(Call arg0, IOException arg1) {
					arg1.printStackTrace();
				}
			});
			
			
		}
		
		
	}
	
	
	
	
	//更新必应每日一图
	private void updateBingPic() {
	
		
		String requestBingPic = "http://guolin.tech/api/bing_pic";
		HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
			
			@Override
			public void onResponse(Call arg0, Response arg1) throws IOException {
				String resonseText = arg1.body().string();
				
				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
				editor.putString("bing_pic", resonseText);
				editor.apply();
				
			}
			
			@Override
			public void onFailure(Call arg0, IOException arg1) {
				arg1.printStackTrace();
			}
		});
		
		
		
	}
	
	
	
	
}
