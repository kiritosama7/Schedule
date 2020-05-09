package com.dl.schedule;


import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.dl.schedule.Course.ListViewAdapter;
import com.dl.schedule.Course.Subject;
import com.dl.schedule.DB.DBAdapter;
import com.dl.schedule.DB.Motion;
import com.dl.schedule.Info.Perinfor;
import com.dl.schedule.Motion.Mapp;
import com.dl.schedule.DB.MySubject;
import com.dl.schedule.DB.Schedule;
import com.dl.schedule.Schedule.AddSchedule;
import com.dl.schedule.Schedule.ModifySchedule;
import com.dl.schedule.Schedule.ScheduleAdapter;
import com.dl.schedule.Weather.WeatherActivity;
import com.google.gson.Gson;
import com.necer.MyLog;
import com.necer.calendar.Miui9Calendar;
import com.necer.entity.NDate;
import com.necer.listener.OnCalendarChangedListener;
import com.necer.painter.InnerPainter;


import org.litepal.LitePal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, DatePicker.OnDateChangedListener {

    public SharedPreferences.Editor editor;
    private Miui9Calendar miui9Calendar;
    List<MySubject> mySubjects = new ArrayList<>();
    private TextView tv_month;
    private TextView tv_year;
    private TextView tv_lunar;
    private ListView listView;
    List<MySubject> list = new ArrayList<>();
    private DBAdapter dbAdapter;
    String chooseDate;
    private List<Schedule> schedulesList = new ArrayList<>();
    private Schedule[] schedules;
    private ScheduleAdapter adapter;
    private String startDate;
    private LinearLayout forecastLayout;
    private LinearLayout fabLayout;
    private int year, month, day;
    private int chooseYear, chooseMonth, chooseDay;
    boolean isContained = false;
    //在TextView上显示的字符
    private StringBuffer date;
    TextView tvDate;

    private LinearLayout rlAddBill;
    private int[] llId = new int[]{R.id.ll01, R.id.ll02, R.id.ll03};
    private LinearLayout[] ll = new LinearLayout[llId.length];
    private int[] fabId = new int[]{R.id.fab, R.id.start_date};
    private FloatingActionButton[] fab2 = new FloatingActionButton[fabId.length];
    private FloatingActionButton fabb;
    private AnimatorSet addBillTranslate1;
    private AnimatorSet addBillTranslate2;
    private AnimatorSet addBillTranslate3;
    private boolean isAdd = false;
    private int month3, year3, w;
    public static boolean isNetWorkOpen;
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;

    private TextView NowTmp;
    private TextView NowWeather;
    private ListViewAdapter adapterCourse;
    private android.support.v4.widget.NestedScrollView scroll;

    private TextView city;
    private TextView nowWeek;
    private TextView subjectNone;
    private TextView nowrun, aimrun;
    List<String> pointList = new ArrayList<String>();
    SharedPreferences prefs;
    private Motion[] motions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        initNetwork();



        scroll = findViewById(R.id.scroll);
        city = (TextView) findViewById(R.id.city);
        nowWeek = findViewById(R.id.nowWeek);
        listView = (ListView) findViewById(R.id.show);
        fabLayout = findViewById(R.id.fabLayout);
        adapterCourse = new ListViewAdapter(MainActivity.this, R.layout.course_item, list);
        list.clear();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        initDateTime();
        tvDate = (TextView) findViewById(R.id.tv_date);
        tvDate.setText(year + "-" + month + "-" + day);
        date = new StringBuffer();
        tvDate.setOnClickListener(this);

        NowTmp = (TextView) findViewById(R.id.NowTmp);
        NowWeather = (TextView) findViewById(R.id.NowWeather);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        subjectNone = findViewById(R.id.subjectNone);

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        StringBuilder Today = new StringBuilder()
                .append(year)
                .append("-")
                .append(month < 10 ? "0" + month : month)
                .append("-")
                .append(day < 10 ? "0" + day : day);
        chooseDate = Today.toString();

        miui9Calendar = findViewById(R.id.miui9Calendar);
        tv_month = findViewById(R.id.tv_month);
        tv_year = findViewById(R.id.tv_year);
        tv_lunar = findViewById(R.id.tv_lunar);


        miui9Calendar.setOnCalendarChangedListener(new OnCalendarChangedListener() {
            @Override
            public void onCalendarDateChanged(NDate date, boolean isClick) {
                chooseDate = date.localDate.toString();
                tv_month.setText(date.localDate.getMonthOfYear() + "月");
                tv_year.setText(date.localDate.getYear() + "年");
                tv_lunar.setText(date.lunar.lunarYearStr + date.lunar.lunarMonthStr + date.lunar.lunarDayStr);

                if (chooseDate.equals(Today.toString())) {
                    requestNowWeather();
                } else {
                    NowWeather.setText("查看");
                    NowTmp.setText("天气");
                }
                initSchedule();
                initSubject();
                adapter.notifyDataSetChanged();
                adapterCourse.notifyDataSetChanged();
                fabLayout.setVisibility(View.VISIBLE);
                todayDistance();
            }

            @Override
            public void onCalendarStateChanged(boolean isMonthSate) {
                MyLog.d("OnCalendarChangedListener:::" + isMonthSate);
                if (!isMonthSate) {
                    hideFABMenu();
                    fabLayout.setVisibility(View.GONE);
                } else fabLayout.setVisibility(View.VISIBLE);
            }
        });


        scroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (fabLayout.getVisibility() == View.GONE)
                            fabLayout.setVisibility(View.VISIBLE);
                        else {
                            hideFABMenu();
                            fabLayout.setVisibility(View.GONE);
                        }
                        break;

                }

                return false;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (scrollY == 0) scroll.setNestedScrollingEnabled(true);
                    else scroll.setNestedScrollingEnabled(false);

                    if (scrollY < oldScrollY) {
                        fabLayout.setVisibility(View.VISIBLE);
                    } else {
                        hideFABMenu();
                        fabLayout.setVisibility(View.GONE);
                    }
                }
            });
        }


        tv_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                miui9Calendar.toToday();
                requestNowWeather();
            }
        });


        initSchedule();
        initFAB();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        adapter = new ScheduleAdapter(schedulesList);
        recyclerView.setAdapter(adapter);


        adapter.setOnItemClickListener(new ScheduleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Context context) {
                Schedule schedule = schedulesList.get(position);
                Intent intent2 = new Intent(context, ModifySchedule.class);
                intent2.putExtra("_id", schedule.getID());
                context.startActivity(intent2);
            }

        });
        adapter.setOnItemLongClickListener(new ScheduleAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                Schedule schedule = schedulesList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示")
                        .setMessage("确定删除这个日程提醒吗?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dbAdapter.deleteOneData(schedule.getID());
                                initSchedule();
                                adapter.notifyDataSetChanged();
                                Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setCancelable(true)
                        .show();
            }
        });

        LinearLayout subjectText = (LinearLayout) findViewById(R.id.subjectButton);
        subjectText.setOnClickListener(this);

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                        Intent intent = new Intent(MainActivity.this, Subject.class);
                        editor = PreferenceManager.
                                getDefaultSharedPreferences(MainActivity.this).edit();
                        editor.putString("chooseDate", chooseDate);
                        editor.putString("startDate", tvDate.getText().toString());
                        editor.apply();
                        startActivity(intent);
                    }
                }
        );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        aimrun = findViewById(R.id.aimrun);


        nowrun = findViewById(R.id.nowrun);


        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        HeConfig.init("HE1905292245131082", "893028cb24914f59b607bced0db0545e");
        HeConfig.switchToFreeServerNode();
        requestNowWeather();

        NowTmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NowTmp.getText().equals("天气")) {
                    miui9Calendar.toToday();
                    requestNowWeather();
                } else {
                    editor = PreferenceManager.
                            getDefaultSharedPreferences(MainActivity.this).edit();
                    editor.putString("weatherId", "auto_ip");
                    editor.apply();
                    Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
                    startActivity(intent);
                }
            }
        });
        NowWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NowWeather.getText().equals("查看")) {
                    miui9Calendar.toToday();
                    requestNowWeather();
                } else {
                    editor = PreferenceManager.
                            getDefaultSharedPreferences(MainActivity.this).edit();
                    editor.putString("weatherId", "auto_ip");
                    editor.apply();
                    Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    public void onResume() {
        super.onResume();
        initSchedule();
        initSubject();

        motions = dbAdapter.queryFlagDataMotion("1");
        if (motions != null) {
            for (int i = 0; i < motions.length; i++) {
                pointList.add(motions[i].getLocalDate());
            }
        }
        InnerPainter innerPainter = (InnerPainter) miui9Calendar.getCalendarPainter();
        innerPainter.setPointList(pointList);
        pointList.clear();
        todayDistance();

    }

    private void initSchedule() {
        schedules = dbAdapter.queryDateData(chooseDate);
        if (schedules == null) {
            schedulesList.clear();
        } else {
            schedulesList.clear();
            for (int i = 0; i < schedules.length; i++)
                schedulesList.add(schedules[i]);
        }
    }

    private void todayDistance() {
        motions = dbAdapter.queryDateDataMotion(chooseDate);
        if (motions != null) {
            Log.d("1111111",chooseDate );
            aimrun.setText(motions[0].getGoal()+"M");
            String i = String.valueOf(motions[0].getDistance());
            i = i.substring(0, i.indexOf("."));
            int id = motions[0].getID();
            nowrun.setText(i);
            if (Integer.parseInt(i) >= Integer.parseInt(motions[0].getGoal())) {
                Motion motion = motions[0];
                motion.setGoal(motions[0].getGoal());
                motion.setLocalDate(chooseDate);
                motion.setFlagDis("1");
                motion.setDistance(motions[0].getDistance());
                dbAdapter.updateOneDataMotion(id, motion);
            }
        } else {
            nowrun.setText("0");
            aimrun.setText("5000M");
        }


        adapter.notifyDataSetChanged();
        adapterCourse.notifyDataSetChanged();
    }

    private void initSubject() {
        startDate = prefs.getString("startDate", "2019-02-25");
        chooseYear = Integer.parseInt(chooseDate.substring(0, 4));
        chooseMonth = Integer.parseInt(chooseDate.substring(5, 7));
        chooseDay = Integer.parseInt(chooseDate.substring(8, 10));

        if (chooseMonth == 1 || chooseMonth == 2) {
            chooseMonth = chooseMonth + 12;
            chooseYear = chooseYear - 1;
        }
        int c = chooseYear / 100;
        int y = chooseYear - c * 100;

        w = ((c / 4) - 2 * c + (y + y / 4) + (13 * (chooseMonth + 1) / 5) + chooseDay - 1) % 7;
        if (w == 0)
            w = 7;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String str_date = chooseDate;
        long value = 0;
        try {
            value = sdf.parse(str_date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long value2 = 0;
        try {
            value2 = sdf.parse(startDate).getTime();//------------------------------------------
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (value >= value2) {
            int week = (int) (value / 3600000 - value2 / 3600000) / (24 * 7) + 1;
            if (week > 18) nowWeek.setText("放假啦");
            else nowWeek.setText("第" + week + "周");
            mySubjects = LitePal.where("day =? ", String.valueOf(w)).order("start").find(MySubject.class);
            for (int i = mySubjects.size() - 1; i >= 0; i--) {
                for (int j = 0; j < mySubjects.get(i).getWeekList().size(); j++) {
                    if (mySubjects.get(i).getWeekList().get(j) == week) {
                        isContained = true;
                        break;
                    }
                }
                if (!isContained) {
                    mySubjects.remove(i);
                }
                isContained = false;
            }
        } else {
            mySubjects.clear();
            nowWeek.setText("没开学");
        }
        list.clear();
        for (int i = 0; i < mySubjects.size(); i++) {
            list.add(mySubjects.get(i));
        }
        listView.setAdapter(adapterCourse);
        setListViewHeightBasedOnChildren(listView);

        if (list.size() == 0) {
            subjectNone.setVisibility(View.VISIBLE);
        } else {
            subjectNone.setVisibility(View.GONE);
        }
    }

    public void requestNowWeather() {
        HeWeather.getWeatherNow(MainActivity.this, "auto_ip", Lang.CHINESE_SIMPLIFIED, Unit.METRIC, new HeWeather.OnResultWeatherNowBeanListener() {

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
                    NowTmp.setText(now.getNow().getTmp() + "℃");
                    NowWeather.setText(now.getNow().getCond_txt());


                } else {
                    //在此查看返回数据失败的原因
                    String status = now.getStatus();
                    Code code = Code.toEnum(status);
                    Log.i("requestNowWeather", "failed code: " + code);
                }
            }
        });
        FloatingActionButton linechart = (FloatingActionButton) findViewById(R.id.linechart);
        linechart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Perinfor.class);
                editor = PreferenceManager.
                        getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putString("chooseDate",chooseDate);
                editor.apply();
                startActivity(intent);
            }
        });


        LinearLayout mapp = (LinearLayout) findViewById(R.id.mapp);
        mapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(MainActivity.this, Mapp.class);
                startActivity(intent1);
            }
        });
    }


    private void initDateTime() {
        Calendar calendar = Calendar.getInstance();
        if (startDate != null && startDate.length() == 10) {
            year = Integer.parseInt(startDate.substring(0, 4));
            month = Integer.parseInt(startDate.substring(5, 7));
            day = Integer.parseInt(startDate.substring(8, 10));
        } else {
            year = 2019;
            month = 02;
            day = 25;
            editor = PreferenceManager.
                    getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("startDate", "2019-02-25");
            editor.apply();
        }

    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.month = monthOfYear + 1;
        this.day = dayOfMonth;
    }


    private void initFAB() {
        fabb = (FloatingActionButton) findViewById(R.id.fab2);
        rlAddBill = (LinearLayout) findViewById(R.id.rlAddBill);
        for (int i = 0; i < llId.length; i++) {
            ll[i] = (LinearLayout) findViewById(llId[i]);
        }
        for (int i = 0; i < fabId.length; i++) {
            fab2[i] = (FloatingActionButton) findViewById(fabId[i]);
        }

        addBillTranslate1 = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.add_bill_anim);
        addBillTranslate2 = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.add_bill_anim);
        addBillTranslate3 = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.add_bill_anim);

        fabb.setOnClickListener(this);
        for (int i = 0; i < fabId.length; i++) {
            fab2[i].setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab2:
                isAdd = !isAdd;
                rlAddBill.setVisibility(isAdd ? View.VISIBLE : View.GONE);
                if (isAdd) {
                    addBillTranslate1.setTarget(ll[0]);
                    addBillTranslate1.start();
                    addBillTranslate2.setTarget(ll[1]);
                    addBillTranslate2.setStartDelay(150);
                    addBillTranslate2.start();
                    addBillTranslate3.setTarget(ll[2]);
                    addBillTranslate3.setStartDelay(300);
                    addBillTranslate3.start();
                }
                break;
            case R.id.fab:
                hideFABMenu();
                Intent intent = new Intent(MainActivity.this, AddSchedule.class);
                intent.putExtra("chooseDate", chooseDate);
                startActivity(intent);
                break;
            case R.id.start_date:
                hideFABMenu();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (date.length() > 0) { //清除上次记录的日期
                            date.delete(0, date.length());
                        }
                        String month2, day2;
                        if (month < 10)
                            month2 = "0" + String.valueOf(month);
                        else
                            month2 = String.valueOf(month);
                        if (day < 10)
                            day2 = "0" + String.valueOf(day);
                        else
                            day2 = String.valueOf(day);

                        if (month == 1 || month == 2) {
                            month3 = month + 12;
                            year3 = year - 1;
                        } else {
                            month3 = month;
                            year3 = year ;
                        }
                        int c = year3 / 100;
                        int y = year3 - c * 100;

                        w = ((c / 4) - 2 * c + (y + y / 4) + (13 * (month3 + 1) / 5) + day - 1) % 7;
                        if (w == 1) {

                            tvDate.setText(date.append(String.valueOf(year)).append("-").append(String.valueOf(month2)).append("-").append(day2));
                            Toast.makeText(MainActivity.this, "已将开学日期设置在"+tvDate.getText().toString(),
                                    Toast.LENGTH_SHORT).show();
                            editor = PreferenceManager.
                                    getDefaultSharedPreferences(MainActivity.this).edit();
                            editor.putString("startDate", tvDate.getText().toString());
                            editor.apply();
                            initSubject();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this, "请将开学日期设置在周一",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }


                    }

                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                final AlertDialog dialog = builder.create();
                View dialogView = View.inflate(this, R.layout.dialog_date, null);
                final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.datePicker);
                dialog.setTitle("设置日期");
                dialog.setView(dialogView);
                dialog.show();
                //初始化日期监听事件
                datePicker.init(year, month - 1, day, this);
                break;
            case R.id.subjectButton:
                Intent intent1 = new Intent(MainActivity.this, Subject.class);
                editor = PreferenceManager.
                        getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putString("chooseDate", chooseDate);
                editor.putString("startDate", tvDate.getText().toString());
                editor.apply();
                startActivity(intent1);
                break;
            default:
                break;
        }
    }

    private void hideFABMenu() {
        rlAddBill.setVisibility(View.GONE);
        isAdd = false;
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    void initNetwork() {
        intentFilter = new IntentFilter();
        //当网络状态发生变化时，系统发出值为android.net.conn.CONNECTIVITY_CHANGE广播
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //动态注册的广播接收器一定要取消注册才行
        unregisterReceiver(networkChangeReceiver);
    }


    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                isNetWorkOpen = true;
                requestNowWeather();
                Toast.makeText(context, "网络已连接,天气数据已刷新", Toast.LENGTH_SHORT).show();
            } else {
                isNetWorkOpen = false;
                Toast.makeText(context, "当前网络不可用，天气以及导入课程可能无法正常工作", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
