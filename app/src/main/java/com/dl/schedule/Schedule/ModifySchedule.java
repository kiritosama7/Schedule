package com.dl.schedule.Schedule;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

public class ModifySchedule extends AppCompatActivity implements  View.OnClickListener,  DatePicker.OnDateChangedListener{

    private DBAdapter dbAdapter;
    private Schedule[] schedules;
    TextView time;
    TextView tvDate;
    EditText TodayEvent;
    Long id;
    private int year, month, day;
    //在TextView上显示的字符
    private StringBuffer date;
    Switch notify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_schedule);

        dbAdapter=new DBAdapter(this);
        dbAdapter.open();
        Intent intent =getIntent();
        id= Long.valueOf(intent.getIntExtra("_id",-1));
        tvDate=(TextView)findViewById(R.id.tv_date);
        date = new StringBuffer();
        tvDate.setOnClickListener(this);
        notify = (Switch) findViewById(R.id.notify);
        schedules= dbAdapter.queryOneData(id);
        tvDate.setText(schedules[0].getLocalDate());



        time=(TextView)findViewById(R.id.tv_end_time);
        time.setText(schedules[0].getTime());
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(ModifySchedule.this, AlertDialog.THEME_HOLO_LIGHT,new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        time.setText(new StringBuilder()
                                .append(hourOfDay<10?"0"+hourOfDay:hourOfDay)
                                .append(":")
                                .append(minute<10?"0"+minute:minute));
                    }
                }, Integer.parseInt(schedules[0].getTime().substring(0,schedules[0].getTime().indexOf(':'))), Integer.parseInt(schedules[0].getTime().substring(schedules[0].getTime().indexOf(':')+1)), true).show();
            }
        });

        TodayEvent=(EditText) findViewById(R.id.TodayEvent);
        TodayEvent.setText(schedules[0].getEvent());
        FloatingActionButton modify = (FloatingActionButton) findViewById(R.id.modify);
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Schedule schedule=new Schedule();
                if(TextUtils.isEmpty(TodayEvent.getText().toString().trim())){
                    Toast.makeText(ModifySchedule.this,"请填写要添加的日程再提交",Toast.LENGTH_SHORT).show();
                }else {

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String str_date = tvDate.getText().toString()+" "+time.getText().toString();
                    long value = 0;
                    try {
                        value=sdf.parse(str_date).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long value2 = System.currentTimeMillis();
                    if(value<=value2){
                        Toast.makeText(getApplicationContext(), "选择时间不能小于当前系统时间", Toast.LENGTH_SHORT).show();
                        return;
                    }else{
                        schedule.setLocalDate(tvDate.getText().toString());
                        schedule.setTime(time.getText().toString());
                        schedule.setEvent(TodayEvent.getText().toString());
                        dbAdapter.updateOneData(id,schedule);
                        int delaytime = (int)(value - value2);
                        if (notify.isChecked())
                            MyService.addNotification(ModifySchedule.this, delaytime, tvDate.getText().toString(), time.getText().toString(), TodayEvent.getText().toString());
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
        initDateTime();

    }
    private void initDateTime() {
        year = Integer.parseInt(schedules[0].getLocalDate().substring(0,4));
        month = Integer.parseInt(schedules[0].getLocalDate().substring(5,7));
        day = Integer.parseInt(schedules[0].getLocalDate().substring(8,10));
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
                    String month2,day2;
                    if(month<10)
                        month2="0"+String.valueOf(month);
                    else
                        month2=String.valueOf(month);
                    if(day<10)
                        day2="0"+String.valueOf(day);
                    else
                        day2=String.valueOf(day);
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
