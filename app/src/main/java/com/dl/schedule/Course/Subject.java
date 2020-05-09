package com.dl.schedule.Course;

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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dl.schedule.DB.MySubject;
import com.dl.schedule.MainActivity;
import com.dl.schedule.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.view.WeekView;

import org.litepal.LitePal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Subject extends AppCompatActivity {

    TimetableView mTimetableView;
    WeekView mWeekView;
    List<MySubject> mySubjects = new ArrayList<>();
    int target = -1;
    LinearLayout layout;
    TextView titleTextView;
    String chooseDate;
    String startDate;//----------------------------
    public SharedPreferences prefs;
    ImageView back_subject;
    ImageView more;
    int i = 1;
    int j = 1;

    MainActivity a =new MainActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        titleTextView = findViewById(R.id.id_title);
        layout = findViewById(R.id.id_layout);
        back_subject = findViewById(R.id.back_subject);
        back_subject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        more = findViewById(R.id.more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopmenu();
            }
        });
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWeekView.isShowing()) hideWeekView();
                else showWeekView();
            }
        });


        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        chooseDate = prefs.getString("chooseDate", null);
        startDate = prefs.getString("startDate", null);
        initTimetableView();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        mTimetableView.curWeek(startDate + " 00:00:00");
        if (chooseDate != null) {

            String str_date = chooseDate;
            long value = 0;
            try {
                value = sdf.parse(str_date).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long value2 = 0;
            try {
                value2 = sdf.parse(startDate).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }


            if (value < value2) {
                i = (int) (value2 / 3600000 - value / 3600000) / 24;

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示")
                        .setMessage("据开学还有" + i + "天")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int cur = mTimetableView.curWeek();
                                //更新切换后的日期，从当前周cur->切换的周week
                                mTimetableView.onDateBuildListener()
                                        .onUpdateDate(cur, 1);
                                mTimetableView.changeWeekOnly(1);

                            }
                        })
                        .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .show();
            } else {
                i = (int) (value / 3600000 - value2 / 3600000) / (24 * 7) + 1;

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                StringBuilder Today = new StringBuilder()
                        .append(year)
                        .append("-")
                        .append(month < 10 ? "0" + month : month)
                        .append("-")
                        .append(day < 10 ? "0" + day : day);
                String todayDate = Today.toString();
                long value3 = 0;
                try {
                    value3 = sdf.parse(todayDate).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (value3 > value2)
                    j = (int) (value3 / 3600000 - value2 / 3600000) / (24 * 7) + 1;

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示");
                if (i > 18) {
                    builder.setMessage("放假啦!查看新学期课表请重新导入");
                } else
                    builder.setMessage("你查看的第" + i + "周的课表");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int cur = mTimetableView.curWeek();
                        //更新切换后的日期，从当前周cur->切换的周week
                        mWeekView.curWeek(j).showView();
                        mTimetableView.onDateBuildListener()
                                .onUpdateDate(cur, i);
                        mTimetableView.changeWeekOnly(i);
                        if (i > 18) {
                            titleTextView.setText("放假啦");
                        }

                    }
                })
                        .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }
        show();


    }


    private void show() {
        mySubjects = LitePal.findAll(MySubject.class);
        Log.d("show", "show: " + mySubjects.size());
        if (i > 18) {
            titleTextView.setText("放假啦");
        }else {
            titleTextView.setText("第" + j + "周");
        }
        mWeekView.source(mySubjects).showView();
        mTimetableView.source(mySubjects).showView();


    }

    public void onResume() {
        super.onResume();
        show();
    }

    private void initTimetableView() {

        //获取控件
        mWeekView = findViewById(R.id.id_weekview);
        mTimetableView = findViewById(R.id.id_timetableView);

        mWeekView.hideLeftLayout();

        //设置周次选择属性
        mWeekView.curWeek(1)
                .callback(new IWeekView.OnWeekItemClickedListener() {
                    @Override
                    public void onWeekClicked(int week) {
                        int cur = mTimetableView.curWeek();
                        //更新切换后的日期，从当前周cur->切换的周week
                        mTimetableView.onDateBuildListener()
                                .onUpdateDate(cur, week);
                        mTimetableView.changeWeekOnly(week);

                    }
                })
                .itemCount(18)
                .isShow(false)//设置隐藏，默认显示
                .showView();


        mTimetableView.curWeek(1)
                .curTerm("大三下学期")
                .callback(new ISchedule.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, List<Schedule> scheduleList) {

                        Intent intent = new Intent(Subject.this, DetailCourseActivity.class);
                        intent.putExtra("mySubject", scheduleList.get(0));
                        startActivity(intent);
                    }
                })
                .callback(new ISchedule.OnItemLongClickListener() {
                    @Override
                    public void onLongClick(View v, int day, int start) {
                        mySubjects = LitePal.where("day = ? and start = ?", String.valueOf(day), String.valueOf(start)).find(MySubject.class);
                        AlertDialog.Builder builder = new AlertDialog.Builder(Subject.this);
                        builder.setTitle("提示")
                                .setMessage("确定删除" + mySubjects.get(0).getName() + "这门课吗")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        LitePal.delete(MySubject.class, mySubjects.get(0).getId());
                                        show();
                                        Toast.makeText(Subject.this, "删除成功", Toast.LENGTH_SHORT).show();
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

                })
                .callback(new ISchedule.OnWeekChangedListener() {
                    @Override
                    public void onWeekChanged(int curWeek) {
                        titleTextView.setText("第" + curWeek + "周");
                    }
                })
                //旗标布局点击监听
                .callback(new ISchedule.OnFlaglayoutClickListener() {
                    @Override
                    public void onFlaglayoutClick(int day, int start) {
                        mTimetableView.hideFlaglayout();
                        Intent intent = new Intent(Subject.this, AddCourseActivity.class);
                        intent.putExtra("day", day + 1);
                        intent.putExtra("start", start);
                        startActivity(intent);
                    }
                })
                .showView();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mTimetableView.onDateBuildListener()
                .onHighLight();
    }


    public void hideWeekView() {
        mWeekView.isShow(false);
        titleTextView.setTextColor(getResources().getColor(R.color.app_course_textcolor_blue));
        int cur = mTimetableView.curWeek();
        mTimetableView.onDateBuildListener()
                .onUpdateDate(cur, cur);
        mTimetableView.changeWeekOnly(cur);
    }

    public void showWeekView() {
        mWeekView.isShow(true);
        titleTextView.setTextColor(getResources().getColor(R.color.app_red));
    }

    public void showPopmenu() {
        PopupMenu popup = new PopupMenu(this, more);
        popup.getMenuInflater().inflate(R.menu.popmenu_slide, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.imports:
                        if (LitePal.findAll(MySubject.class).size() != 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Subject.this);
                            builder.setTitle("提示")
                                    .setMessage("导入会覆盖当前课程！确定导入吗？")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            if(MainActivity.isNetWorkOpen){
                                                Intent intent = new Intent(Subject.this, parseCourse.class);
                                                startActivity(intent);
                                            }else {
                                                Toast.makeText(Subject.this, "当前网络不可用，无法导入课程", Toast.LENGTH_SHORT).show();
                                            }


                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .setCancelable(true)
                                    .show();
                        } else {
                            if(MainActivity.isNetWorkOpen){
                                Intent intent = new Intent(Subject.this, parseCourse.class);
                                startActivity(intent);
                            }else {
                                Toast.makeText(Subject.this, "当前网络不可用，无法导入课程", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case R.id.deleAll:
                        LitePal.deleteAll(MySubject.class);
                        show();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        popup.show();
    }


}
