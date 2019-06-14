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

public class MainActivity extends FragmentActivity {

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
    private EditText account;
    private EditText password;
    private TabHost.TabSpec tab1;
    private TabHost.TabSpec tab2;
    private TabHost.TabSpec tab3;
    private double capacityIni;



    private final int REQUEST_CODE_SCAN = 5;
    private final int SCAN_REQUEST = 6;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1){
                times += 2;
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
        setContentView(R.layout.activity_main);
        creatMap();
        capacityTv = findViewById(R.id.capacityTv);
        chargeLvl = findViewById(R.id.chargeLvl);
        capacityTv.setText("16.00 %");
        chargeLvl.setProgress(1600);
        creatTabHost();
        initChargingChart();
        initCurrentVoltageChart();
        initChargeEntity();
        initDataSet();
        //genDataThread.start();
        Intent intent = getIntent();
        capacityIni = intent.getDoubleExtra("capacity", 16.00);
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
        y.setTextColor(Color.BLACK);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setDrawGridLines(false);
        y.setAxisLineColor(Color.BLACK);
        y.setAxisMaximum(3.5f);
        y.setAxisMinimum(0.0f);
        y.setTextSize(16);


        currentVotageChart.getAxisRight().setEnabled(false);

        currentVotageChart.getLegend().setEnabled(false);

        currentVotageChart.animateXY(2000, 2000);

        // don't forget to refresh the drawing

        System.out.println("创建第2个图成功");
    }

    private void initChargeEntity(){
        chargeEntity = new ChargeEntity(0.0, 0.0, 16);
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
        voltageLineDataSet = new LineDataSet(voltageEntrys, "充电电压");
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
        tabHost.setCurrentTab(2);
        Toast.makeText(this, "请先登录", Toast.LENGTH_LONG).show();
    }


    public void loginHandler(View view) {
        account = findViewById(R.id.account);
        password = findViewById(R.id.password);
        if (account.getText().toString().equals("cernet") && password.getText().toString().equals("cernet")){
            Intent intent = new Intent(this, AfterLoginActivity.class);
            intent.putExtra("account", "cernet");
            intent.putExtra("mail", "854300805@qq.com");
            intent.putExtra("capacity", chargeEntity.capacity);
            startActivity(intent);
        }else {
            Toast.makeText(this, "账号或密码错误", Toast.LENGTH_LONG).show();
            account.setText("");
            password.setText("");
        }
    }

    public void registHandler(View view) {
        Intent intent = new Intent(this, RegistActivity.class);
        intent.putExtra("capacity", chargeEntity.capacity);
        startActivity(intent);
    }
}
