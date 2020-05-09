package com.dl.schedule.Course;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dl.schedule.R;


public class DetailCourseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_course);
        setFinishOnTouchOutside(false);

        final com.zhuangfei.timetable.model.Schedule mySubject = (com.zhuangfei.timetable.model.Schedule) getIntent().getSerializableExtra("mySubject");

        final TextView inputCourseName = (TextView) findViewById(R.id.course_name);
        final TextView inputTeacher = (TextView) findViewById(R.id.teacher_name);
        final TextView inputClassRoom = (TextView) findViewById(R.id.class_room);
        final TextView startWeekSpinner = (TextView) findViewById(R.id.startWeek);
        final TextView endWeekSpinner = (TextView) findViewById(R.id.endWeek);
        final TextView startCourseSpinner = (TextView) findViewById(R.id.startCourse);
        final TextView endCourseSpinner = (TextView) findViewById(R.id.endCourse);
        final TextView daySpinner = (TextView) findViewById(R.id.day);

        startWeekSpinner.setText( mySubject.getWeekList().get(0).toString());
        endWeekSpinner.setText( mySubject.getWeekList().get(mySubject.getWeekList().size()-1).toString());
        startCourseSpinner.setText(""+(mySubject.getStart()));
        endCourseSpinner.setText(""+(mySubject.getStart()+mySubject.getStep()-1));
        daySpinner.setText(""+(mySubject.getDay()));
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

                   Intent intent = new Intent(DetailCourseActivity.this, UpdateCourseActivity.class);
                   intent.putExtra("detailSubject",mySubject);
                   startActivity(intent);
                   finish();
               }


        });
    }
}
