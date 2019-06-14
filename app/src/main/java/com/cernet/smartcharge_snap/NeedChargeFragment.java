package com.cernet.smartcharge_snap;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.yzq.zxinglibrary.android.CaptureActivity;

public class NeedChargeFragment extends Fragment {

    private ListView mapList;
    private final int REQUEST_CODE_SCAN = 5;
    private final int SCAN_REQUEST = 6;
    private Button startChargeBtn;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.need_charge_layout, null);
        mapList = view.findViewById(R.id.mapList);
        String[] strs = new String[]{"新校A座（25剩余）", "新校C座（0剩余）", "校本部（22剩余）"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_item, strs);
        mapList.setAdapter(adapter);
        startChargeBtn = view.findViewById(R.id.needChargeBtn);
        startChargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, SCAN_REQUEST);
                }else {
                    Intent intent = new Intent(getActivity(), CaptureActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SCAN);
                }
            }
        });
        return view;
    }
}
