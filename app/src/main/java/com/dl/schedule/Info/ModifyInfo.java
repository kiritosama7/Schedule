package com.dl.schedule.Info;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.dl.schedule.DB.DBAdapter;
import com.dl.schedule.DB.Motion;
import com.dl.schedule.R;
import com.dl.schedule.Schedule.ScheduleAdapter;

import org.joda.time.LocalDateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ModifyInfo extends AppCompatActivity {
    EditText height;
    EditText weight;
    EditText age;
    EditText goal;
    public SharedPreferences.Editor editor;
    public SharedPreferences prefs;
    private int year, month, day;
    String chooseDate;
    private DBAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_info);
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        chooseDate = prefs.getString("chooseDate", null);
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();
        height = (EditText) findViewById(R.id.height);
        weight = (EditText) findViewById(R.id.weight);
        age = (EditText) findViewById(R.id.age);
        goal = (EditText) findViewById(R.id.goal);
        initInfo();

        FloatingActionButton fin = (FloatingActionButton) findViewById(R.id.Finsh);
        fin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor = PreferenceManager.
                        getDefaultSharedPreferences(ModifyInfo.this).edit();
                Motion[] motions;
                motions = dbAdapter.queryDateDataMotion(chooseDate);
                if (motions != null) {

                    Motion motion=new Motion();
                    motion.setGoal(goal.getText().toString());
                    motion.setDistance(motions[0].getDistance());
                    motion.setLocalDate(motions[0].getLocalDate());
                    motion.setFlagDis("0");
                    dbAdapter.updateOneDataMotion(motions[0].getID(),motion);
                }
                else {
                    Motion motion=new Motion();
                    motion.setGoal(goal.getText().toString());
                    motion.setFlagDis("0");
                    motion.setDistance(0.0);
                    motion.setLocalDate(chooseDate);
                    dbAdapter.insertMotion(motion);
                }
                editor.putString("height", height.getText().toString());
                editor.putString("weight", weight.getText().toString());
                editor.putString("age", age.getText().toString());
                editor.apply();
                finish();
            }
        });
    }

    private void initInfo() {
        Motion[] motions;
        motions = dbAdapter.queryDateDataMotion(chooseDate);
        if (motions != null)
            goal.setText(motions[0].getGoal());
        else
            goal.setText("5000");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        height.setText(prefs.getString("height", "0"));
        weight.setText(prefs.getString("weight", "0"));
        age.setText(prefs.getString("age", "0"));
    }
}
