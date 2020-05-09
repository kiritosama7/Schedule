package com.dl.schedule.Weather;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dl.schedule.Course.Subject;
import com.dl.schedule.R;
import com.dl.schedule.linechart.DoubleLineChartView;
import com.dl.schedule.linechart.TimeUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.search.Search;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.Lifestyle;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class WeatherActivity extends AppCompatActivity {
    private LinearLayout forecastLayout;
    private TextView city, wear, washcar, cool, rays, sport, comfort, tour, airpull;
    DoubleLineChartView doubleLineChartView;

    private List<String> listWeeks = new ArrayList<>();
    private List<String> listDayWeathers = new ArrayList<>();
    private List<Bitmap> listDayIcons = new ArrayList<>();
    private List<Integer> listDayTemperature = new ArrayList<>();
    private List<Integer> listNightTemperature = new ArrayList<>();
    private List<Bitmap> listNightIcons = new ArrayList<>();
    private List<String> listNightWeathers = new ArrayList<>();
    private List<String> listDate = new ArrayList<>();
    private List<String> listWind = new ArrayList<>();
    private List<String> listWindLevel = new ArrayList<>();

    private Button navButton,gp_bt;
    public SharedPreferences prefs;
    private String weatherId;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        doubleLineChartView = (DoubleLineChartView) findViewById(R.id.weather_view);
        city = (TextView) findViewById(R.id.city);
        wear = (TextView) findViewById(R.id.wear);
        washcar = (TextView) findViewById(R.id.washcar);
        cool = (TextView) findViewById(R.id.cool);
        rays = (TextView) findViewById(R.id.rays);
        sport = (TextView) findViewById(R.id.sport);
        comfort = (TextView) findViewById(R.id.comfort);
        tour = (TextView) findViewById(R.id.tour);
        airpull = (TextView) findViewById(R.id.airpull);
        //draweerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherActivity.this, ChooseActivity.class);
                startActivity(intent);
                finish();
            }
        });
        gp_bt = (Button) findViewById(R.id.gp_bt);
        gp_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("weatherId", "auto_ip");
                editor.apply();
                WeatherActivity.this.recreate();
            }
        });
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        weatherId = prefs.getString("weatherId", "auto_ip");
        requestWeather(weatherId);
        //requestWeather("CN101210101");
        requestLifeStyle(weatherId);

    }


    private void initData() {
        //添加星期几数据 星期六
        Date currentDate = new Date(System.currentTimeMillis());
        listWeeks.add("今天");
        listWeeks.add(TimeUtils.getWeekForDate(TimeUtils.addOneDayForDate(currentDate)));
        currentDate = TimeUtils.addOneDayForDate(currentDate);
        listWeeks.add(TimeUtils.getWeekForDate(TimeUtils.addOneDayForDate(currentDate)));
        currentDate = TimeUtils.addOneDayForDate(currentDate);
        listWeeks.add(TimeUtils.getWeekForDate(TimeUtils.addOneDayForDate(currentDate)));
        currentDate = TimeUtils.addOneDayForDate(currentDate);
        listWeeks.add(TimeUtils.getWeekForDate(TimeUtils.addOneDayForDate(currentDate)));

        //添加日期数据
        currentDate = new Date(System.currentTimeMillis());
        listDate.add(TimeUtils.formatDate("MM/dd", currentDate));
        currentDate = TimeUtils.addOneDayForDate(currentDate);
        listDate.add(TimeUtils.formatDate("MM/dd", currentDate));
        currentDate = TimeUtils.addOneDayForDate(currentDate);
        listDate.add(TimeUtils.formatDate("MM/dd", currentDate));
        currentDate = TimeUtils.addOneDayForDate(currentDate);
        listDate.add(TimeUtils.formatDate("MM/dd", currentDate));
        currentDate = TimeUtils.addOneDayForDate(currentDate);
        listDate.add(TimeUtils.formatDate("MM/dd", currentDate));


        doubleLineChartView.setWeeks(listWeeks);
        doubleLineChartView.setDayWeathers(listDayWeathers);
        doubleLineChartView.setDayWeatherIcons(listDayIcons);
        doubleLineChartView.setDayTemperatures(listDayTemperature);
        doubleLineChartView.setNightTemperatures(listNightTemperature);
        doubleLineChartView.setNightWeatherIcons(listNightIcons);
        doubleLineChartView.setNightWeathers(listNightWeathers);
        doubleLineChartView.setDates(listDate);
        doubleLineChartView.setWind(listWind);
        doubleLineChartView.setWindLevel(listWindLevel);
        doubleLineChartView.apply();
    }

    public void requestWeather(String weatherId) {
        HeWeather.getWeatherForecast(WeatherActivity.this, weatherId, Lang.CHINESE_SIMPLIFIED, Unit.METRIC, new HeWeather.OnResultWeatherForecastBeanListener() {
            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onSuccess(Forecast forecast) {
                if (Code.OK.getCode().equalsIgnoreCase(forecast.getStatus())) {
                    //此时返回数据
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //forecastBaseList.clear();
                            Log.d("requestWeather", "run: " + forecast.getBasic().getCid());
                            city.setText(forecast.getBasic().getLocation());
                            for (int i = 0; i < forecast.getDaily_forecast().size() - 2; i++) {
                                String weatype = forecast.getDaily_forecast().get(i).getCond_txt_d();
                                String weatype1 = forecast.getDaily_forecast().get(i).getCond_txt_n();
                                listDayWeathers.add(weatype);
                                listNightWeathers.add(weatype1);
                                listDayTemperature.add(Integer.valueOf(forecast.getDaily_forecast().get(i).getTmp_max()).intValue());
                                listNightTemperature.add(Integer.valueOf(forecast.getDaily_forecast().get(i).getTmp_min()).intValue());
                                if (weatype.contains("雨")) {
                                    listDayIcons.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_rain_small));
                                } else if (weatype.equals("晴")) {
                                    listDayIcons.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_sunny));
                                }
                                else if (weatype.equals("阴")) {
                                    listDayIcons.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cloud));
                                } else if (weatype.equals("多云")) {
                                    listDayIcons.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cloud));
                                }
                                if (weatype1.contains("雨")) {
                                    listNightIcons.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_rain_small));
                                } else if (weatype1.equals("晴")) {
                                    listNightIcons.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_sunny));
                                }
                                else if (weatype1.equals("阴")) {
                                    listNightIcons.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cloud));
                                }else if (weatype1.equals("多云")) {
                                    listNightIcons.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cloud));
                                }

                                //添加风向
                                if (forecast.getDaily_forecast().get(i).getWind_dir().length() >= 4)
                                    listWind.add("无风");
                                else
                                    listWind.add(forecast.getDaily_forecast().get(i).getWind_dir());

                                //添加风力等级
                                listWindLevel.add(forecast.getDaily_forecast().get(i).getWind_sc() + "级");
                            }
                            initData();
                        }
                    });

                    Log.d("requestWeather", "onSuccess: " + forecast.getDaily_forecast().get(0).getTmp_max() + forecast.getDaily_forecast().get(0).getTmp_min());
                } else {
                    //在此查看返回数据失败的原因
                    String status = forecast.getStatus();
                    Code code = Code.toEnum(status);
                    Log.i("requestWeather", "failed code: " + code);
                }
            }
        });
    }

    public void requestNowWeather() {
        HeWeather.getWeatherNow(WeatherActivity.this, "auto_ip", Lang.CHINESE_SIMPLIFIED, Unit.METRIC, new HeWeather.OnResultWeatherNowBeanListener() {

            @Override
            public void onError(Throwable throwable) {
                Log.i("requestNowWeather", "Weather Now onError: ", throwable);
            }

            @Override
            public void onSuccess(Now now) {
                Log.i("requestNowWeather", " Weather Now onSuccess: " + new Gson().toJson(now));
                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                if (Code.OK.getCode().equalsIgnoreCase(now.getStatus())) {
                    //此时返回数据

                } else {
                    //在此查看返回数据失败的原因
                    String status = now.getStatus();
                    Code code = Code.toEnum(status);
                    Log.i("requestNowWeather", "failed code: " + code);
                }
            }
        });
    }


    public void requestLifeStyle(String weatherId) {
        HeWeather.getWeatherLifeStyle(WeatherActivity.this, weatherId, Lang.CHINESE_SIMPLIFIED, Unit.METRIC, new HeWeather.OnResultWeatherLifeStyleBeanListener() {

            @Override
            public void onError(Throwable throwable) {
                Log.i("requestLifeStyle", "Weather Now onError: ", throwable);
            }

            @Override
            public void onSuccess(Lifestyle lifestyle) {
                Log.i("requestLifeStyle", " Weather Now onSuccess: " + new Gson().toJson(lifestyle.getLifestyle()));
                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                if (Code.OK.getCode().equalsIgnoreCase(lifestyle.getStatus())) {
                    //此时返回数据
                    wear.setText(lifestyle.getLifestyle().get(1).getBrf());
                    washcar.setText(lifestyle.getLifestyle().get(6).getBrf());
                    cool.setText(lifestyle.getLifestyle().get(2).getBrf());
                    rays.setText(lifestyle.getLifestyle().get(5).getBrf());
                    sport.setText(lifestyle.getLifestyle().get(3).getBrf());
                    comfort.setText(lifestyle.getLifestyle().get(0).getBrf());
                    tour.setText(lifestyle.getLifestyle().get(4).getBrf());
                    airpull.setText(lifestyle.getLifestyle().get(7).getBrf());


                } else {
                    //在此查看返回数据失败的原因
                    String status = lifestyle.getStatus();
                    Code code = Code.toEnum(status);
                    Log.i("lifestyle", "failed code: " + code);
                }
            }
        });
    }

}
