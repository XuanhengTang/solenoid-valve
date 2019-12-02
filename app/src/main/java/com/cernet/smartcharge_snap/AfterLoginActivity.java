package com.cernet.smartcharge_snap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.yzq.zxinglibrary.android.CaptureActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AfterLoginActivity extends FragmentActivity {

    private LineChart chargingChart;
    private TextView currentTv, voltageTv;
    private ChargeEntity chargeEntity;
    private TextView capacityTv;
    private ProgressBar chargeLvl;
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

    private Button YV258;
    private Button YV263;
    private Button YV257;

    private TextView curMoni;

    private final int REQUEST_CODE_SCAN = 5;
    private final int SCAN_REQUEST = 6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);
        creatMap();
        capacityTv = findViewById(R.id.capacityTv);
        chargeLvl = findViewById(R.id.chargeLvl);
        YV258 = findViewById(R.id.YV258);
        YV263 = findViewById(R.id.YV263);
        YV257 = findViewById(R.id.YV257);
        curMoni = findViewById(R.id.curmoni);
        creatTabHost();
        initChargingChart();
        initChargeEntity();
        Intent intent = getIntent();
        account = findViewById(R.id.account);
        mail = findViewById(R.id.mail);
        account.setText(intent.getStringExtra("account"));
        mail.setText(intent.getStringExtra("mail"));
        capacityIni = intent.getDoubleExtra("rul", 10.0);


    }

    private void creatTabHost() {
        tabHost = findViewById(R.id.mainTab);
        tabHost.setup();
        tab1 = tabHost.newTabSpec("tab1").setIndicator("制动系统").setContent(R.id.tab1);
        tab2 = tabHost.newTabSpec("tab2").setIndicator("电磁阀监测").setContent(R.id.tab2);
        tab3 = tabHost.newTabSpec("tab3").setIndicator("管理员账户").setContent(R.id.tab3);
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
        String[] strs = new String[]{"001DF3 244C95   (铁道学院电子楼)"};
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
        x.setAxisMaximum(25.0f);
        x.setTextColor(Color.BLACK);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setTextSize(14);

        YAxis y = chargingChart.getAxisLeft();
        y.setLabelCount(10, false);
        y.setTextColor(Color.BLACK);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setDrawGridLines(false);
        y.setAxisLineColor(Color.BLACK);
        y.setAxisMaximum(700.0f);
        y.setAxisMinimum(-5.0f);
        y.setTextSize(14);


        chargingChart.getAxisRight().setEnabled(false);

        chargingChart.getLegend().setEnabled(true);

        chargingChart.animateXY(2000, 2000);


    }

    public void startMoni258(View v){
        YV258.setEnabled(false);
        YV257.setEnabled(true);
        YV263.setEnabled(true);
        curMoni.setText(R.string.YV258);
        ChargeEntity entity = new ChargeEntity();
        entity.reflactTime = 3.31;
        entity.stableCurrent = 642.19;
        entity.rul = 45742100.0;
        bindUI(entity);
        try {
            initDataSet(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startMoni263(View v){
        YV258.setEnabled(true);
        YV257.setEnabled(true);
        YV263.setEnabled(false);
        curMoni.setText(R.string.YV263);
        ChargeEntity entity = new ChargeEntity();
        entity.reflactTime = 4.19;
        entity.stableCurrent = 498.08;
        entity.rul = 23493800.0;
        bindUI(entity);
        try {
            initDataSet(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startMoni257(View v){
        YV258.setEnabled(true);
        YV257.setEnabled(false);
        YV263.setEnabled(true);
        curMoni.setText(R.string.YV257);
        ChargeEntity entity = new ChargeEntity();
        entity.reflactTime = 4.49;
        entity.stableCurrent = 494.76;
        entity.rul = 21364900.0;
        bindUI(entity);
        try {
            initDataSet(2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void bindUI(ChargeEntity entity){
        DecimalFormat format = new DecimalFormat("#.00");
        entity.rate = entity.rul / ChargeEntity.MAX_RUL * 100;
        currentTv.setText(entity.reflactTime + " ms");
        voltageTv.setText(entity.stableCurrent + " mA");
        capacityTv.setText(format.format(entity.rate) + "% (" + (int)entity.rul + " / " + (int)ChargeEntity.MAX_RUL + ")");
        chargeLvl.setProgress((int) Math.round(entity.rate * 100));
    }

    private void initChargeEntity(){
        chargeEntity = new ChargeEntity();
        System.out.println("初始化数据容器成功");
    }

    /**
     * 初始化数据集合
     */
    private void initDataSet(int flag) throws IOException {
        InputStream in = null;
        switch (flag){
            case 0:
                in = getResources().openRawResource(R.raw.yv258);
                break;
            case 1:
                in = getResources().openRawResource(R.raw.yv263);
                break;
            case 2:
                in = getResources().openRawResource(R.raw.yv257);
                break;
        }
        drwaData(in);

    }

    private void drwaData(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = "";
        List<Entry> capacityEntrys = new ArrayList<>();
        while ((line = reader.readLine()) != null){
            String[] res = line.split("\t");
            int x = Integer.parseInt(res[0]);
            double y = Double.parseDouble(res[1]);
            Entry point = new Entry((float) ((x - 1) * 0.01), (float) (y * 1000));
            capacityEntrys.add(point);
        }
        xTime = new ArrayList<>();
        capacityLineDataSet = new LineDataSet(capacityEntrys, "电磁阀驱动电流曲线");
        capacityLineDataSet.setDrawValues(false);
        capacityLineDataSet.setDrawCircles(false);
        capacityLineDataSet.setLineWidth(3.0f);
        ArrayList<LineDataSet> sets2 = new ArrayList<>();
        sets2.add(capacityLineDataSet);

        chart2LineData = new LineData(capacityLineDataSet);
        chargingChart.setData(chart2LineData);
        chargingChart.invalidate();
    }

    /**
     * 点击开始充电启动扫码
     * @param view
     */
    public void startCharge(View view) {
        Toast.makeText(this, "开启监控成功", Toast.LENGTH_LONG).show();
        isCharging = findViewById(R.id.isCharging);
        isCharging.setText("正在监控");
        isCharging.setTextColor(Color.parseColor("#008577"));

        needChargeBtn = findViewById(R.id.refreshMapBtn);
        needChargeBtn.setText(R.string.charging);
        needChargeBtn.setEnabled(false);
        tabHost.setCurrentTab(1);
        startMoni258(view);
    }

    /**
     * 退出登录
     * @param view
     */
    public void loginOut(View view){
        System.out.println("logout");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("rul", chargeEntity.rul);
        startActivity(intent);
    }
}
