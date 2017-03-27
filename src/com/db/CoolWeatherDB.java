package com.db;

import com.model.City;
import com.model.County;
import com.model.Province;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import com.db.CoolWeatherOpenHelper;

public class CoolWeatherDB {
	
	//数据库名
	public static final String DB_NAME = "cool_weather";
	
	//数据库版本
	public static final int VERSION = 1;		
	
	private static CoolWeatherDB coolWeatherDB;
	
	private SQLiteDatabase db;
	
	
	//将构造方法私有化
	private CoolWeatherDB(Context context) {
		CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
		
	}

	
	//单例方法  相当于OC中的		 +(xxx *)shareInstance;
	public synchronized static CoolWeatherDB getInstance(Context context){
		if (coolWeatherDB == null) {
			coolWeatherDB =  new CoolWeatherDB(context);
			}
		return coolWeatherDB;
	}
	
	//将Province实例存储到数据库
	public void saveProvince(Province province) {
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}
	}
	
	//从数据库读取全国的省份信息
	public List<Province> loadProvinces(){
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db.query("Province", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getInt(cursor.getColumnIndex("province_code")));
				list.add(province);
			} while (cursor.moveToNext());
		}
		
		return list;
	}
	
	//将City实例 存储到数据库
	public void saveCity(City city) {
		if (city != null) {			
			ContentValues values = new ContentValues();
			values.put("city_name",  city.getCityName());
			values.put("city_code",	city.getCityCode());
			values.put("province_id", city.getProvindeId());
			db.insert("City", null, values);
		}
		
	}
	
	//将数据库读取某省下所有的城市信息
	public List<City> loadCities(int provinceId) {
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null , "province_id = ?", new String[]{String.valueOf(provinceId)}, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getInt(cursor.getColumnIndex("city_code")));
				city.setProvindeId(provinceId);
				list.add(city);
			} while (cursor.moveToNext());
		}
		return list;
	}
	
	public void saveCounty(County county ) {
		if (county != null) {
			ContentValues values = new ContentValues();
			values.put("city_id", county.getCityId());
			values.put("county_name", county.getCountyName());
			values.put("weather_id", county.getWeatherId());
			db.insert("County", null, values);
			
		}
	}
	
	public List<County> loadCounties(int cityId) {
		List<County> list = new ArrayList<County>();
		Cursor cursor = db.query("County", null, "city_id = ?", new String[]{String.valueOf(cityId)}, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				County county = new County();
				county.setCityId(cityId);
				county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
				county.setWeatherId(cursor.getString(cursor.getColumnIndex("weather_id")));
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				list.add(county);
			} while (cursor.moveToNext());
		}
		
		return list;
	}
	
	
	
	

}
