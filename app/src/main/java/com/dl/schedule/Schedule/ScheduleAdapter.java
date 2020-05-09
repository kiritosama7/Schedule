package com.dl.schedule.Schedule;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dl.schedule.DB.Schedule;
import com.dl.schedule.R;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder>{


    private List<Schedule> schedulesList;
    private Context mContext;
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView time;
        TextView event;
        TextView date;
        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            time = (TextView) view.findViewById(R.id.time);
            event = (TextView) view.findViewById(R.id.event);
            date=(TextView) view.findViewById(R.id.date);
        }
    }

    public ScheduleAdapter(List<Schedule> schedules) {
        schedulesList = schedules;
    }
    public interface OnItemClickListener{
        void onItemClick(View view,int position,Context context);
    }
    public interface OnItemLongClickListener{
        void onItemLongClick(View view,int position);
    }

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }
    public void setOnItemLongClickListener(OnItemLongClickListener mOnItemLongClickListener) {
        this.mOnItemLongClickListener = mOnItemLongClickListener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (mContext == null) {
            mContext = viewGroup.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.schedule_item, viewGroup, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Schedule schedule=schedulesList.get(i);
        viewHolder.date.setText(schedule.getLocalDate());
        viewHolder.time.setText(schedule.getTime());
        viewHolder.event.setText(schedule.getEvent());

        if(mOnItemClickListener != null){
            //为ItemView设置监听器
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = viewHolder.getAdapterPosition();
                    mOnItemClickListener.onItemClick(viewHolder.cardView,position,mContext);
                }
            });
        }
        if(mOnItemLongClickListener != null){
            viewHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = viewHolder.getLayoutPosition();
                    mOnItemLongClickListener.onItemLongClick(viewHolder.cardView,position);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return schedulesList.size();
    }

}
