package com.dl.schedule.Schedule;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dl.schedule.DB.DBAdapter;
import com.dl.schedule.DB.Schedule;
import com.dl.schedule.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class AddSchedule extends AppCompatActivity implements View.OnClickListener, DatePicker.OnDateChangedListener {
    private DBAdapter dbAdapter;
    TextView tvDate;
    TextView time;
    EditText TodayEvent;
    String chooseDate;
    private int year, month, day;
    //在TextView上显示的字符
    private StringBuffer date;
    Switch notify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        Intent intent = getIntent();
        chooseDate = intent.getStringExtra("chooseDate");
        tvDate = (TextView) findViewById(R.id.tv_date);
        tvDate.setText(chooseDate);
        date = new StringBuffer();
        tvDate.setOnClickListener(this);
        notify = (Switch) findViewById(R.id.notify);

        initDateTime();

        Time times = new Time();
        times.setToNow();//获取系统当前时间

        time = (TextView) findViewById(R.id.tv_end_time);
        time.setText((times.hour< 10 ? "0" + times.hour : times.hour)+":"+(times.minute< 10 ? "0" + times.minute : times.minute));
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(AddSchedule.this, AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        time.setText(new StringBuilder()
                                .append(hourOfDay < 10 ? "0" + hourOfDay : hourOfDay)
                                .append(":")
                                .append(minute < 10 ? "0" + minute : minute));
                    }
                }, times.hour, times.minute, true).show();
            }
        });

        TodayEvent = (EditText) findViewById(R.id.TodayEvent);
        FloatingActionButton fin = (FloatingActionButton) findViewById(R.id.Finsh);
        fin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(TodayEvent.getText().toString().trim())) {
                    Toast.makeText(AddSchedule.this, "请填写要添加的日程再提交", Toast.LENGTH_SHORT).show();
                } else {

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String str_date = tvDate.getText().toString() + " " + time.getText().toString();
                    long value = 0;
                    try {
                        value = sdf.parse(str_date).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long value2 = System.currentTimeMillis();
                    if (value <= value2) {
                        Toast.makeText(getApplicationContext(), "选择时间不能小于当前系统时间", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        Schedule schedule = new Schedule();
                        schedule.setLocalDate(tvDate.getText().toString());
                        schedule.setTime(time.getText().toString());
                        schedule.setEvent(TodayEvent.getText().toString());
                        dbAdapter.insert(schedule);
                        int delaytime = (int) (value - value2);

                        if (notify.isChecked())
                            MyService.addNotification(AddSchedule.this, delaytime, tvDate.getText().toString(), time.getText().toString(), TodayEvent.getText().toString());
                        finish();
                    }

                }
            }
        });

        notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notify.isChecked())
                notify.setChecked(true);
                else notify.setChecked(false);
            }
        });
    }

    private void initDateTime() {
        Calendar calendar = Calendar.getInstance();
        year = Integer.parseInt(chooseDate.substring(0, 4));
        month = Integer.parseInt(chooseDate.substring(5, 7));
        day = Integer.parseInt(chooseDate.substring(8, 10));
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.month = monthOfYear + 1;
        this.day = dayOfMonth;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.tv_date) {
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
                    tvDate.setText(date.append(String.valueOf(year)).append("-").append(String.valueOf(month2)).append("-").append(day2));
                    dialog.dismiss();
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

        }

    }
}
