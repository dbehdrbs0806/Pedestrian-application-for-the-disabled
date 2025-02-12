package com.example.connect_bridge;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.Manifest;                                          // Manifest에 선언한 권한을 사용하기 위한 import


public class AuthorityActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private int Disability_Type;                                  // 다음 activity로 넘기기 위한 Type;

    private Button confirm_button;                                // 확인 버튼
    private Intent next_intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority);                             // 권한 레이아웃 불러옴
        Intent before_intent = getIntent();
        Disability_Type = before_intent.getIntExtra("Disability_Type",  0);   // 이전 intent에서 장애의 종류(Disability_Type)를 불러옴

        confirm_button = findViewById(R.id.confirm_button);                       // 확인 버튼 findViewById로 가져옴
        confirm_button.setOnClickListener(new View.OnClickListener() {            // 확인 버튼의 OnClick 메소드
            @Override
            public void onClick(View view) {
                if (Disability_Type == 0) {
                    checkAndRequestPermissions(new String[]{                      // checkAndRequestPermissions 함수는 필요한 권한들 permissions 배열로 체크하는 메서드
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.RECORD_AUDIO
                    });
                }
                else {
                    checkAndRequestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    });
                }
            }
        });
    }

    private void checkAndRequestPermissions(String[] permissions) {     // checkAndRequestPermissions() 권한 설정을 위한 메소드
        List<String> PermissionsNeeded = new ArrayList<>();             // 권한이 없는 경우 목록을 저장하는 리스트
        for (String p : permissions) {                                  // 매개변수로 받은 권한들 permissions들 반복하며
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {   // ContextCompat 유틸리티의 checkSelfPermission() 사용해서 권한 설정함
                PermissionsNeeded.add(p);                                                               // 권한 허용 안됬을 경우 리스트에 추가
            }
        }

        // requestPermissions() 를 사용해 호출하여 권한 요청 // 위에서 선언한 CODE로 식별
        if (!PermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(AuthorityActivity.this, PermissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            if (Disability_Type == 0) {
                next_intent = new Intent(AuthorityActivity.this, VisionDisabilityActivity.class);
            } else {
                next_intent = new Intent(AuthorityActivity.this, MobilityDisabilityActivity.class);
            }
            startActivity(next_intent);
            finish();
        }
    }

    // requestPermission() 호출 시 사용
    // 사용자가 요청에 응답하면 호출됨 요청한 권한들의 결과를 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {    // requestCode: 권한 요청 시 사용한 식별 코드 permissions: 요청한 권한들의 배열 grantResults: 권한 요청 결과를 나타내는 정수 배열
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (!allPermissionsGranted) {
                Toast.makeText(this, "필수 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
