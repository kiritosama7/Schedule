package com.dl.schedule.Motion;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.dl.schedule.DB.DBAdapter;
import com.dl.schedule.DB.Motion;
import com.dl.schedule.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

public class Mapp extends AppCompatActivity {

    private MapView mMapView = null;
    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();
    private BaiduMap mBaiduMap;
    private boolean isFirstLocate = true;
    private boolean isStart = true;
    double latitude;    //获取纬度信息
    double longitude;    //获取经度信息
    List<LatLng> points = new ArrayList<LatLng>();
    List<LatLng> targets = new ArrayList<LatLng>();
    ImageButton locate;
    BDLocation l;
    Double distance = 0.0;
    String todayDate;
    int id;
    Marker marker[] = new Marker[3];
    LatLng point[] = new LatLng[10];
    int showPoint[] = new int[3];
    int removeNum = 0;
    private Motion[] motions;
    private DBAdapter dbAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);
        setContentView(R.layout.activity_baidu_map);

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        List<String> permissionList = new ArrayList<>();

        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);

        mLocationClient = new LocationClient(getApplicationContext());


        if (ContextCompat.checkSelfPermission(Mapp.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(Mapp.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(Mapp.this, permissions, 1);
        } else {
            requestLocation();
        }

        initDate();
        motions =dbAdapter.queryDateDataMotion(String.valueOf(todayDate));
        if (motions!=null) {
            id = motions[0].getID();
            distance = motions[0].getDistance();
        }

        MyThread myThread = new MyThread();
        Thread thread = new Thread(myThread);


        FloatingActionButton start = (FloatingActionButton) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isStart) {
                    int count = 0;
                    HashSet<Integer> hash = new HashSet<Integer>();
                    while (count <= 2) {
                        int i = (int) (Math.random() * 7);
                        if (hash.add(i)) {
                            showPoint[count] = i;
                            BitmapDescriptor bitmap = BitmapDescriptorFactory
                                    .fromResource(R.drawable.location);
                            OverlayOptions optionTarget = new MarkerOptions()
                                    .position(targets.get(i))
                                    .icon(bitmap);
                            marker[count] = (Marker) mBaiduMap.addOverlay(optionTarget);
                            count++;
                        }
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(Mapp.this);
                    builder.setTitle("提示")
                            .setMessage("打卡点已生成！")
                            .setNegativeButton("知道了", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setCancelable(true)
                            .show();
                    thread.start();
                    start.setImageResource(R.drawable.stop);

                } else {

                    String i = String.valueOf(distance);
                    i = i.substring(0, i.indexOf("."));
                    AlertDialog.Builder builder = new AlertDialog.Builder(Mapp.this);
                    builder.setTitle("提示")
                            .setMessage("您今天已经跑了" + i + "米啦！")
                            .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Motion motion = new Motion();
                                    motion.setLocalDate(todayDate);
                                    motion.setDistance(distance);
                                    motion.setFlagDis("0");

                                    if (motions==null) {
                                        motion.setGoal("5000");
                                        dbAdapter.insertMotion(motion);
                                    } else {
                                        motion.setGoal(motions[0].getGoal());
                                        dbAdapter.updateOneDataMotion(id,motion);
                                    }

                                    //onDestroy();
                                    finish();
                                }
                            })
                            .setNegativeButton("继续跑", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setCancelable(true)
                            .show();
                }
                isStart = false;
            }
        });

        locate = findViewById(R.id.locate);
        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateTo(l, true);
            }
        });

        point[0] = new LatLng(30.234577, 120.045295);
        point[1] = new LatLng(30.232985, 120.043781);
        point[2] = new LatLng(30.230068, 120.044805);
        point[3] = new LatLng(30.235435, 120.050447);
        point[4] = new LatLng(30.234694, 120.047653);
        point[5] = new LatLng(30.230731, 120.042209);
        point[6] = new LatLng(30.227794, 120.038913);
        point[7] = new LatLng(30.236679, 120.050496);


//构建Marker图标
        for (int i = 0; i < 8; i++)
            targets.add(point[i]);

    }

    public void initDate() {
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
        todayDate = Today.toString();
    }

    public class MyThread implements Runnable {
        @Override
        public void run() {

            while (true) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LatLng p1 = new LatLng(latitude, longitude);
                points.add(p1);
                if (points.size() >= 2) {
                    OverlayOptions mOverlayOptions = new PolylineOptions()
                            .width(10)
                            .color(0xAAFF0000)
                            .points(points);
                    Overlay mPolyline = mBaiduMap.addOverlay(mOverlayOptions);
                    distance += DistanceUtil.getDistance(points.get(points.size() - 2), points.get(points.size() - 1));

                }
                navigateTo(l, true);
            }
        }
    }

    private void navigateTo(BDLocation location, boolean first) {
        if (isFirstLocate || first) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(update);

            update = MapStatusUpdateFactory.zoomTo(18f);
            mBaiduMap.animateMapStatus(update);

            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.
                Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        mBaiduMap.setMyLocationData(locationData);
    }

    private void requestLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setWifiCacheTimeOut(5 * 60 * 1000);
        option.setEnableSimulateGps(false);
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);

        mLocationClient.setLocOption(option);
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        mLocationClient.start();
    }


    @Override
    protected void onResume() {
        super.onResume();

        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mLocationClient.stopIndoorMode();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
            latitude = location.getLatitude();    //获取纬度信息
            longitude = location.getLongitude();    //获取经度信息
            LatLng p1 = new LatLng(latitude, longitude);
            Log.d("onReceiveLocation: ", "onReceiveLocation: " + showPoint[0] + "  " + marker[0]);
            if (marker[0] != null) {
                for (int i = 0; i < 3; i++) {
                    if (DistanceUtil.getDistance(point[showPoint[i]], p1) < 10) {
                        marker[i].remove();
                        removeNum++;
                    }
                }

                if (removeNum == 3) {
                    String i = String.valueOf(distance);
                    i = i.substring(0, i.indexOf("."));
                    AlertDialog.Builder builder = new AlertDialog.Builder(Mapp.this);
                    builder.setTitle("提示")
                            .setMessage("您今天已经跑了" + i + "米啦！\n 打卡成功了")
                            .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Motion motion = new Motion();
                                    motion.setLocalDate(todayDate);
                                    motion.setDistance(distance);
                                    motion.setFlagDis("0");

                                    if (motions==null) {
                                        motion.setGoal("5000");
                                        dbAdapter.insertMotion(motion);
                                    } else {
                                        motion.setGoal(motions[0].getGoal());
                                        dbAdapter.updateOneDataMotion(id,motion);
                                    }

                                    //onDestroy();
                                    finish();
                                }
                            })
                            .setNegativeButton("继续跑", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setCancelable(true)
                            .show();

                }
            }
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f
            String coorType = location.getCoorType();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

            int errorCode = location.getLocType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明

            String addr = location.getAddrStr();    //获取详细地址信息
            String country = location.getCountry();    //获取国家
            String province = location.getProvince();    //获取省份
            String city = location.getCity();    //获取城市
            String district = location.getDistrict();    //获取区县
            String street = location.getStreet();    //获取街道信息

            String locationDescribe = location.getLocationDescribe();    //获取位置描述信息
            List<Poi> poiList = location.getPoiList();

            if (location.getFloor() != null) {
                // 当前支持高精度室内定位
                String buildingID = location.getBuildingID();// 百度内部建筑物ID
                String buildingName = location.getBuildingName();// 百度内部建筑物缩写
                String floor = location.getFloor();// 室内定位的楼层信息，如 f1,f2,b1,b2
                mLocationClient.startIndoorMode();// 开启室内定位模式（重复调用也没问题），开启后，定位SDK会融合各种定位信息（GPS,WI-FI，蓝牙，传感器等）连续平滑的输出定位结果；
            }

            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            l = location;

            navigateTo(l, false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }


}
