package com.dl.schedule.Course;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.dl.schedule.DB.MySubject;
import com.dl.schedule.R;

import java.util.ArrayList;
import java.util.List;


public class UpdateCourseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_course);
        setFinishOnTouchOutside(false);

        final com.zhuangfei.timetable.model.Schedule mySubject = (com.zhuangfei.timetable.model.Schedule) getIntent().getSerializableExtra("detailSubject");

        final EditText inputCourseName = (EditText) findViewById(R.id.course_name);
        final EditText inputTeacher = (EditText) findViewById(R.id.teacher_name);
        final EditText inputClassRoom = (EditText) findViewById(R.id.class_room);
        final Spinner startWeekSpinner = (Spinner) findViewById(R.id.startWeek);
        final Spinner endWeekSpinner = (Spinner) findViewById(R.id.endWeek);
        final Spinner startCourseSpinner = (Spinner) findViewById(R.id.startCourse);
        final Spinner endCourseSpinner = (Spinner) findViewById(R.id.endCourse);
        final Spinner daySpinner = (Spinner) findViewById(R.id.day);

        startWeekSpinner.setSelection(mySubject.getWeekList().get(0) - 1, true);
        endWeekSpinner.setSelection(mySubject.getWeekList().get(mySubject.getWeekList().size() - 1) - 1, true);
        startCourseSpinner.setSelection(mySubject.getStart() - 1, true);
        endCourseSpinner.setSelection(mySubject.getStart() + mySubject.getStep() - 2, true);
        daySpinner.setSelection(mySubject.getDay() - 1, true);
        inputCourseName.setText(mySubject.getName());
        inputTeacher.setText(mySubject.getTeacher());
        inputClassRoom.setText(mySubject.getRoom());

        Button backButton = (Button) findViewById(R.id.back_add);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button okButton = (Button) findViewById(R.id.button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String courseName = inputCourseName.getText().toString();
                String teacher = inputTeacher.getText().toString();
                String classRoom = inputClassRoom.getText().toString();
                int startWeek = Integer.parseInt(startWeekSpinner.getSelectedItem().toString());
                int endWeek = Integer.parseInt(endWeekSpinner.getSelectedItem().toString());
                List<Integer> weekList = new ArrayList<Integer>();
                for (int i = startWeek; i <= endWeek; i++) {
                    weekList.add(i);
                }
                int startCourse = Integer.parseInt(startCourseSpinner.getSelectedItem().toString());
                int endCourse = Integer.parseInt(endCourseSpinner.getSelectedItem().toString());
                int step = endCourse - startCourse + 1;
                int day = Integer.parseInt(daySpinner.getSelectedItem().toString());
                if (courseName.equals("") || teacher.equals("") || classRoom.equals("")) {
                    Toast.makeText(UpdateCourseActivity.this, "基本课程信息未填写", Toast.LENGTH_SHORT).show();
                } else if (endCourse < startCourse) {
                    Toast.makeText(UpdateCourseActivity.this, "课程结束时间不能小于开始时间", Toast.LENGTH_SHORT).show();
                } else if (endWeek < startWeek) {
                    Toast.makeText(UpdateCourseActivity.this, "课程结束周不能小于开始周", Toast.LENGTH_SHORT).show();
                } else {
                    MySubject mysubject = new MySubject("2018-2019学年秋", courseName, classRoom, teacher, weekList, startCourse, step, day, 0, "");
                    mysubject.update(Long.parseLong(mySubject.getExtras().get("extras_id").toString()));
                    finish();
                }

            }
        });
    }
}
