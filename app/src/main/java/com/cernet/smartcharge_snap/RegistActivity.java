package com.cernet.smartcharge_snap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegistActivity extends Activity {
    private Intent intent = null;
    private double capacity;
    private EditText account;
    private EditText password;
    private EditText mail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regist_layout);
        intent = getIntent();
        capacity = intent.getDoubleExtra("capacity", 0.0);
    }


    public void regist(View view) {
        Toast.makeText(this, "已注册", Toast.LENGTH_SHORT);
        Intent intent = new Intent(RegistActivity.this, AfterLoginActivity.class);
        account = findViewById(R.id.account);
        password = findViewById(R.id.password);
        mail = findViewById(R.id.mail);
        intent.putExtra("account", account.getText().toString());
        intent.putExtra("mail", mail.getText().toString());
        intent.putExtra("capacity", capacity);
        startActivity(intent);
    }

    public void backToLoginHandler(View view) {

    }
}
