package com.cernet.smartcharge_snap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityGroup;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.yzq.zxinglibrary.android.CaptureActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AfterLoginActivity extends FragmentActivity {

    private LineChart chargingChart;
    private LineChart currentVotageChart;
    private TextView currentTv, voltageTv;
    private ChargeEntity chargeEntity;
    private TextView capacityTv;
    private ProgressBar chargeLvl;
    private LineDataSet currentLineDataSet;
    private LineDataSet voltageLineDataSet;
    private LineDataSet capacityLineDataSet;
    private int times = 0;
    private LineData chart1LineData;
    private LineData chart2LineData;
    private ArrayList<String> xTime;
    private TabHost tabHost;
    private ListView mapView;
    private TabHost.TabSpec tab1;
    private TabHost.TabSpec tab2;
    private TabHost.TabSpec tab3;
    private TextView account;
    private TextView mail;
    private TextView balance;
    private double capacityIni;
    private TextView isCharging;
    private Button needChargeBtn;

    private final int REQUEST_CODE_SCAN = 5;
    private final int SCAN_REQUEST = 6;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1){
                times += 2;
                if (times >= 120){
                    XAxis xAxis = chargingChart.getXAxis();
                    xAxis.setAxisMinimum(times);
                    xAxis.setAxisMaximum(times + 120);

                    xAxis = currentVotageChart.getXAxis();
                    xAxis.setAxisMinimum(times);
                    xAxis.setAxisMaximum(times + 120);
                }
                ChargeEntity entity = (ChargeEntity) msg.obj;
                DecimalFormat format = new DecimalFormat("#.00");
                currentTv.setText(format.format(entity.current) + " A");
                voltageTv.setText(format.format(entity.voltage) + " V");
                capacityTv.setText(format.format(entity.capacity) + "%");
                chargeLvl.setProgress((int) Math.round(entity.capacity * 100));

//                currentLineDataSet.addEntry(new Entry(times, (float) entity.current));
//                voltageLineDataSet.addEntry(new Entry(times, (float) entity.voltage));
                LineData chargingLineData = chargingChart.getLineData();
                chargingLineData.addEntry(new Entry(times, (float) entity.capacity), 0);
                LineData currentLineData = currentVotageChart.getLineData();
                currentLineData.addEntry(new Entry(times, (float) entity.current), 0);
                System.out.println(times);
                currentVotageChart.notifyDataSetChanged();
                chargingChart.notifyDataSetChanged();
                currentVotageChart.invalidate();
                chargingChart.invalidate();
            }
        }
    };

    private final Thread genDataThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (true){
                    Thread.sleep(2000);
                    Message msg = new Message();
                    double currentValue = Math.random() * 0.1 + 2.5;
                    double voltageValue = Math.random() * 5 + 217;
                    double capacityValue = Math.random() * 0.1;
                    chargeEntity.capacity += capacityValue;
                    chargeEntity.current = currentValue;
                    chargeEntity.voltage = voltageValue;
                    msg.obj = chargeEntity;
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);
        creatMap();
        capacityTv = findViewById(R.id.capacityTv);
        chargeLvl = findViewById(R.id.chargeLvl);
        creatTabHost();
        initChargingChart();
        initCurrentVoltageChart();
        initChargeEntity();
        initDataSet();
        Intent intent = getIntent();
        account = findViewById(R.id.account);
        mail = findViewById(R.id.mail);
        balance = findViewById(R.id.balance);
        account.setText(intent.getStringExtra("account"));
        mail.setText(intent.getStringExtra("mail"));
        capacityIni = intent.getDoubleExtra("capacity", 10.0);
        balance.setText("12.0");


    }

    private void creatTabHost() {
        tabHost = findViewById(R.id.mainTab);
        tabHost.setup();
        tab1 = tabHost.newTabSpec("tab1").setIndicator("我要充电").setContent(R.id.tab1);
        tab2 = tabHost.newTabSpec("tab2").setIndicator("充电监控").setContent(R.id.tab2);
        tab3 = tabHost.newTabSpec("tab3").setIndicator("我的账户").setContent(R.id.tab3);
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void creatMap(){
        mapView = findViewById(R.id.mapList);
        String[] strs = new String[]{"新校A座（25剩余）", "新校C座（0剩余）", "校本部（22剩余）"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, strs);
        mapView.setAdapter(adapter);
    }

    /**
     * 初始化充电图表
     */
    private void initChargingChart(){
        currentTv = findViewById(R.id.currentTv);
        voltageTv = findViewById(R.id.voltageTv);

        chargingChart = findViewById(R.id.chart1);
        chargingChart.setBackgroundColor(Color.rgb(255, 255, 255));

        // no description text
        chargingChart.getDescription().setEnabled(false);

        // enable touch gestures
        chargingChart.setTouchEnabled(true);

        // enable scaling and dragging
        chargingChart.setDragEnabled(true);
        chargingChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chargingChart.setPinchZoom(false);

        chargingChart.setDrawGridBackground(false);
        chargingChart.setMaxHighlightDistance(300);
        chargingChart.setDrawMarkers(false);


        XAxis x = chargingChart.getXAxis();
        x.setEnabled(true);
        x.setAxisMinimum(0.0f);
        x.setAxisMaximum(120.0f);
        x.setTextColor(Color.BLACK);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setTextSize(16);

        YAxis y = chargingChart.getAxisLeft();
        y.setLabelCount(10, false);
        y.setTextColor(Color.BLACK);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setDrawGridLines(false);
        y.setAxisLineColor(Color.BLACK);
        y.setAxisMaximum(100.0f);
        y.setAxisMinimum(0.0f);
        y.setTextSize(16);


        chargingChart.getAxisRight().setEnabled(false);

        chargingChart.getLegend().setEnabled(true);

        chargingChart.animateXY(2000, 2000);

        // don't forget to refresh the drawing

        System.out.println("创建第1个图成功");

    }

    /**
     * 初始化电压电流图表
     */
    private void initCurrentVoltageChart(){
        currentVotageChart = findViewById(R.id.chart2);
        // enable touch gestures
        currentVotageChart.setTouchEnabled(true);

        // enable scaling and dragging
        currentVotageChart.setDragEnabled(true);
        currentVotageChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        currentVotageChart.setPinchZoom(false);

        currentVotageChart.setDrawGridBackground(false);
        currentVotageChart.setMaxHighlightDistance(300);

        XAxis x = currentVotageChart.getXAxis();
        x.setEnabled(true);
        x.setAxisMinimum(0.0f);
        x.setAxisMaximum(120.0f);
        x.setTextColor(Color.BLACK);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setTextSize(16);

        YAxis y = currentVotageChart.getAxisLeft();
        y.setLabelCount(10, false);
        y.setTextColor(Color.RED);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setDrawGridLines(false);
        y.setAxisLineColor(Color.RED);
        y.setAxisMaximum(3.5f);
        y.setAxisMinimum(0.0f);
        y.setTextSize(16);

        YAxis y2 = currentVotageChart.getAxisRight();
        y2.setLabelCount(10, false);
        y2.setTextSize(16);
        y2.setDrawGridLines(false);
        y2.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        y2.setAxisMaximum(250.0f);
        y2.setAxisMinimum(200.0f);
        y2.setTextColor(Color.BLUE);
        y2.setAxisLineColor(Color.BLUE);


        currentVotageChart.getAxisRight().setEnabled(false);

        currentVotageChart.getLegend().setEnabled(true);

        currentVotageChart.animateXY(2000, 2000);

        // don't forget to refresh the drawing

        System.out.println("创建第2个图成功");
    }

    private void initChargeEntity(){
        chargeEntity = new ChargeEntity(0.0, 0.0, capacityIni);
        System.out.println("初始化数据容器成功");
    }

    /**
     * 初始化数据集合
     */
    private void initDataSet(){
        xTime = new ArrayList<>();
        List<Entry> currentEntrys = new ArrayList<>();
        List<Entry> voltageEntrys = new ArrayList<>();
        List<Entry> capacityEntrys = new ArrayList<>();
        currentLineDataSet = new LineDataSet(currentEntrys, "充电电流");
        currentLineDataSet.setDrawValues(false);
        currentLineDataSet.setDrawCircles(false);
        currentLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        voltageLineDataSet = new LineDataSet(voltageEntrys, "充电电压");
        voltageLineDataSet.setDrawValues(false);
        voltageLineDataSet.setDrawCircles(false);
        voltageLineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        capacityLineDataSet = new LineDataSet(capacityEntrys, "电池电量");
        capacityLineDataSet.setDrawValues(false);
        capacityLineDataSet.setDrawCircles(false);

        capacityEntrys.add(new Entry(0.0f, (float) capacityIni));
        ArrayList<LineDataSet> sets1 = new ArrayList<>();
        sets1.add(currentLineDataSet);
        sets1.add(voltageLineDataSet);
        ArrayList<LineDataSet> sets2 = new ArrayList<>();
        sets2.add(capacityLineDataSet);

        chart1LineData = new LineData(currentLineDataSet);
        chart1LineData.setDrawValues(false);

        chart2LineData = new LineData(capacityLineDataSet);

        currentVotageChart.setData(chart1LineData);
        chargingChart.setData(chart2LineData);
        System.out.println("初始化数据集合成功");
        chargingChart.invalidate();
        currentVotageChart.invalidate();
    }

    /**
     * 点击开始充电启动扫码
     * @param view
     */
    public void startCharge(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, SCAN_REQUEST);
        }else {
            Intent intent = new Intent(AfterLoginActivity.this, CaptureActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SCAN);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case SCAN_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // 权限申请成功，扫一扫
                    Intent intent = new Intent(AfterLoginActivity.this,
                            CaptureActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SCAN);
                }else{
                    Toast.makeText(this, "无相机调用权限，扫一扫功能无法使用，", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_CODE_SCAN:
                if (resultCode == RESULT_OK){
                    Toast.makeText(this, "开启充电成功", Toast.LENGTH_LONG).show();
                    isCharging = findViewById(R.id.isCharging);
                    isCharging.setText("正在充电");
                    isCharging.setTextColor(Color.parseColor("#008577"));

                    needChargeBtn = findViewById(R.id.needChargeBtn);
                    needChargeBtn.setText(R.string.charging);
                    needChargeBtn.setEnabled(false);
                    genDataThread.start();
                }
                break;
        }
    }

    /**
     * 退出登录
     * @param view
     */
    public void loginOut(View view){
        System.out.println("logout");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("capacity", chargeEntity.capacity);
        startActivity(intent);
    }
}
