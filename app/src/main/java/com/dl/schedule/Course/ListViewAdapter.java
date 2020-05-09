package com.dl.schedule.Course;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dl.schedule.DB.MySubject;
import com.dl.schedule.R;

public class ListViewAdapter extends ArrayAdapter {
    public ListViewAdapter(Context context, int resource, List<MySubject> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MySubject mySubject = (MySubject)getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.course_item, null);

        TextView course = (TextView)view.findViewById(R.id.course);
        TextView teacher = (TextView)view.findViewById(R.id.teacher);
        TextView times = (TextView)view.findViewById(R.id.times);
        TextView room = (TextView)view.findViewById(R.id.room);
        TextView week = (TextView)view.findViewById(R.id.week);

        int endTime=mySubject.getStart()+mySubject.getStep()-1;

        course.setText(mySubject.getName());
        teacher.setText(mySubject.getTeacher());
        times.setText(mySubject.getStart()+"-" +endTime+"节课");
        week.setText(mySubject.getWeekList().get(0)+"-" +mySubject.getWeekList().get(mySubject.getWeekList().size()-1)+"周");
        room.setText(mySubject.getRoom());

        return view;
    }

}
