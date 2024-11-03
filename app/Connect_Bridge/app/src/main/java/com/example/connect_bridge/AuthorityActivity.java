package com.example.connect_bridge;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AuthorityActivity extends AppCompatActivity {


    private static final int CAMERA_REQUEST_CODE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority);

        Intent intent = getIntent();
        intent.getIntExtra();
    }
}
