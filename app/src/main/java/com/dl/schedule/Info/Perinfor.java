package com.dl.schedule.Info;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dl.schedule.DB.DBAdapter;
import com.dl.schedule.DB.Motion;
import com.dl.schedule.DB.Schedule;
import com.dl.schedule.R;
import com.dl.schedule.Schedule.ScheduleAdapter;
import com.dl.schedule.linechart.LineChartView;

import org.litepal.LitePal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Perinfor extends AppCompatActivity {
    LineChartView lineChartView;
    LineChartView lineChartView2;
    LinearLayout gexing;
    LinearLayout HWAG;
    TextView height;
    TextView weight;
    TextView age;
    TextView goal;
    private Schedule[] schedules;
    private DBAdapter dbAdapter;
    public SharedPreferences prefs;
    private Motion[] motions;
    private int year, month, day;
    String chooseDate;
    private ScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perinfor);
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();
        lineChartView = (LineChartView) findViewById(R.id.line_chart);
        lineChartView2 = (LineChartView) findViewById(R.id.line_chart2);

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        gexing = (LinearLayout) findViewById(R.id.gexing);
        HWAG = (LinearLayout) findViewById(R.id.ll_HWAG);

        height = (TextView) findViewById(R.id.height);
        weight = (TextView) findViewById(R.id.weight);
        age = (TextView) findViewById(R.id.age);
        goal = (TextView) findViewById(R.id.goal);

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        chooseDate = prefs.getString("chooseDate", null);


        gexing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Perinfor.this, ModifyInfo.class);
                startActivity(intent);
            }
        });
        String da[] = new String[7];
        int num[] = new int[7];
        for (int i = 0; i < 7; i++) {
            da[i] = getOldDate(i).substring(getOldDate(i).indexOf('-') + 1);
        }

        for (int i = 0; i < 7; i++) {
            schedules = dbAdapter.queryDateData(getOldDate(i));
            if (schedules != null) {
                num[i] = schedules.length;
            } else
                num[i] = 0;
        }
        int[] dataArr = new int[]{num[0], num[1], num[2], num[3], num[4], num[5], num[6]};
        String[] lable = {da[0], da[1], da[2], da[3], da[4], da[5], da[6]};


        String a = "日期";
        String b = "每日日程数";
        lineChartView.setData(dataArr);
        lineChartView.setLables(lable);
        lineChartView.setxTitle(a);
        lineChartView.setyTitle(b);
        lineChartView.setDataFactor(1);

        int dis[] = new int[7];
        String daDis[] = new String[7];
        for (int i = 0; i < 7; i++) {
            daDis[i] = getOldDate(-i).substring(getOldDate(-i).indexOf('-') + 1);
            Log.d("111111111111", "onCreate: " + daDis[i]);
        }

        for (int j = 0; j < 7; j++) {
            motions = dbAdapter.queryDateDataMotion(String.valueOf(String.valueOf(getOldDate(-j))));
            if (motions != null) {
                String i = String.valueOf(motions[0].getDistance());
                i = i.substring(0, i.indexOf("."));

                dis[j] = Integer.parseInt(i);
            } else
                dis[j] = 0;
        }
        int[] dataArr2 = new int[]{dis[6], dis[5], dis[4], dis[3], dis[2], dis[1], dis[0]};
        String[] lable2 = {daDis[6], daDis[5], daDis[4], daDis[3], daDis[2], daDis[1], daDis[0]};
        String b2 = "跑步距离";
        lineChartView2.setData(dataArr2);
        lineChartView2.setLables(lable2);
        lineChartView2.setxTitle(a);
        lineChartView2.setyTitle(b2);
        lineChartView2.setDataFactor(500);
    }

    public static String getOldDate(int distanceDay) {
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
        Date beginDate = new Date();
        Calendar date = Calendar.getInstance();
        date.setTime(beginDate);
        date.set(Calendar.DATE, date.get(Calendar.DATE) + distanceDay);
        Date endDate = null;
        try {
            endDate = dft.parse(dft.format(date.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dft.format(endDate);
    }

    protected void onResume() {
        Motion[] motions;
        motions = dbAdapter.queryDateDataMotion(chooseDate);
        if (motions != null) {
            goal.setText(motions[0].getGoal() + "M");
            Log.d("111", motions[0].getGoal());

        } else
            goal.setText("5000M");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        height.setText(prefs.getString("height", "0") + "M");
        weight.setText(prefs.getString("weight", "0") + "Kg");
        age.setText(prefs.getString("age", "0") + "岁");

        super.onResume();
    }
}
