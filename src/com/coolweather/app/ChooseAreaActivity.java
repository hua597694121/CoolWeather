package com.coolweather.app;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R.id;
import com.coolweather.app.R.layout;
import com.db.CoolWeatherDB;
import com.model.City;
import com.model.County;
import com.model.Province;
import com.util.HttpCallbackListener;
import com.util.HttpUtil;
import com.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class ChooseAreaActivity  extends Activity{

	public static final int  LEVEL_PROVINCE = 0;
	public static final int  LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	public static final String TAG = "ChooseAreaActivity";
	
	private ProgressDialog progressDialog;
	private TextView titleView;
	private ListView listView;
	private Button backButton;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	
	private List<String> dataList = new ArrayList<>();
	
	private List<Province> provinceList; //省列表
	private List<City> cityList;		//市列表
	private List<County> countyList; //县列表
	
	private Province selectedProvince;
	private City selectedCity;
	private int  currentLevel; //当前选中的级别
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(layout.choose_area);
		
		titleView = (TextView)findViewById(id.titie_view);
		listView = (ListView)findViewById(id.list_view);
		backButton = (Button)findViewById(id.back_button);
		
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		
		queryProvinces();	//加载省级数据
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (currentLevel  == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(arg2);
					queryCities();
				}else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(arg2);
					queryCounties();
				}else if (currentLevel == LEVEL_COUNTY) {
					String weatherId = countyList.get(arg2).getWeatherId();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("weather_id", weatherId);
					startActivity(intent);
					
				}
			}
		});
		
	
		backButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (currentLevel == LEVEL_COUNTY) {
					queryCities();
				}else if (currentLevel == LEVEL_CITY) {
					queryProvinces();
				}
			}
		});
		
		
		
		
		
	}
	
	//查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
	private void queryProvinces() {
		//从数据库查询全国所有的省市
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			//添加前先清除数据源,避免重复 和越界
			dataList.clear();
			
			for (Province province : provinceList) {
				//数据源添加每个省市的名字并显示在表中
				dataList.add(province.getProvinceName());
			}
			//刷新表
			adapter.notifyDataSetChanged(); 
			listView.setSelection(0);
			titleView.setText("中国");
			backButton.setVisibility(View.GONE);//隐藏返回按钮
			currentLevel = LEVEL_PROVINCE;
		}else {
			//数据库中没, 则从网络服务器上查询
			String address =  "http://guolin.tech/api/china";
			queryFormServer(address, "province");
		}
		
	}
	
	
	
	//查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
	private void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleView.setText(selectedProvince.getProvinceName());
			backButton.setVisibility(View.VISIBLE);
			currentLevel = LEVEL_CITY;
		}else {
			int provinceCode = selectedProvince.getProvinceCode();
			String address = "http://guolin.tech/api/china/" + provinceCode;
			queryFormServer(address, "city");
		}
	}
	
	//查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
	private void queryCounties() {
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) {
			Log.d(TAG,	 "请求县城数据, 从数据库中");
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleView.setText(selectedCity.getCityName());
			backButton.setVisibility(View.VISIBLE);
			currentLevel = LEVEL_COUNTY;
		}else {
			Log.d(TAG,	 "请求县城数据, 从服务器中");
			int provinceCode = selectedProvince.getProvinceCode();
			int cityCode = selectedCity.getCityCode();
			String address  = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
			Log.d(TAG, address);
			queryFormServer(address, "county");
		}
		
	}
	
	private void queryFormServer( String address, final String type) {

		showProgressDialog();		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {			
			@Override
			public void onFinish(String response) {
				Boolean result = false;
				if ("province".equals(type)) {
					//数据格式解析
					result = Utility.handleProvinceWithJson(response, coolWeatherDB);
				}else if ("city".endsWith(type)) {
					result = Utility.handleCityWithJson(response, coolWeatherDB, selectedProvince.getId());
				}else if ("county".endsWith(type)) {
					result = Utility.handleCountyWithJson(response, coolWeatherDB, selectedCity.getId());
				}
				
				if (result) {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							}else if ("city".equals(type)) {
								queryCities();
							}else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
				
				
			}
	
			@Override
			public void onError(Exception e) {
				// 通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();	
					}
				});
			}
		});
	}
	
	
	//显示进度对话框
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	//关闭进度对话框
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	
	//捕获back按键, 根据当前级别来判断, 此时应该返回市列表, 省列表 还是直接退出 
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		}else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		}else {
			finish();
		}

	}
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}
	
}
