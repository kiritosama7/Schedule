package com.dl.schedule.Schedule;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.dl.schedule.MainActivity;
import com.dl.schedule.R;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    static Timer timer = null;
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void addNotification(Context context, int delayTime,String date, String time, String event){

        Intent intent=new Intent(context,MyService.class);
        intent.putExtra("delayTime", delayTime);
        intent.putExtra("date",date);
        intent.putExtra("time",time);
        intent.putExtra("event",event);
        context.startService(intent);
    }

    public int onStartCommand(final Intent intent, int flags, int startId){
        long period = 24 * 60 * 60 * 1000; // 24小时一个周期
        int delay = intent.getIntExtra("delayTime", 0);
        if (null == timer) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                NotificationManager mn = (NotificationManager) MyService.this
                        .getSystemService(NOTIFICATION_SERVICE);
                Notification.Builder builder = new Notification.Builder(
                        MyService.this);
                Intent notificationIntent = new Intent(MyService.this,
                        MainActivity.class);// 点击跳转位置
                PendingIntent contentIntent = PendingIntent.getActivity(
                        MyService.this, 0, notificationIntent, 0);
                builder.setContentIntent(contentIntent);
                builder.setSmallIcon(R.drawable.ic_launcher_foreground);
                builder.setContentText(intent.getStringExtra("event"));
                builder.setContentTitle(intent.getStringExtra("date")+"  "+intent.getStringExtra("time")+"的日程提醒");// 下拉通知栏标题
                builder.setAutoCancel(true);// 点击弹出的通知后,让通知将自动取消
                builder.setVibrate(new long[] { 0, 2000, 1000, 4000 });
                builder.setDefaults(Notification.DEFAULT_ALL);
                Notification notification = builder.build();
                notification.flags = notification.FLAG_INSISTENT;
                mn.notify((int) System.currentTimeMillis(), notification);
            }
        }, delay, period);
        return super.onStartCommand(intent, flags, startId);
    }
}
