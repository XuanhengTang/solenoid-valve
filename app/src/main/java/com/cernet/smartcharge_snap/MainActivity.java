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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        creatMap();
        creatTabHost();
        //genDataThread.start();
        Intent intent = getIntent();
        capacityIni = intent.getDoubleExtra("rul", 16.00);
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
            intent.putExtra("mail", "列车长");
            startActivity(intent);
        }else {
            Toast.makeText(this, "账号或密码错误", Toast.LENGTH_LONG).show();
            account.setText("");
            password.setText("");
        }
    }

    public void registHandler(View view) {
        Intent intent = new Intent(this, RegistActivity.class);
        startActivity(intent);
    }
}
