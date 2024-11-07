package com.example.connect_bridge;


import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class LoadingActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView progressText;
    private String Starting_Location_Text;
    private String Destination_Location_Text;

    private Intent location_intent;
    private Data inputData;

    private Data outputData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        location_intent = getIntent();                                          // 그전 activity로부터 intent 받음
        Starting_Location_Text = location_intent.getStringExtra("Starting_Location_Text");      // 출발지
        Destination_Location_Text = location_intent.getStringExtra("Destination_Location_Text");// 도착지

        // ProgressWorker에서 사용을 위해 데이터 보냄
        inputData = new Data.Builder()
                .putString("Starting_Location_Text", Starting_Location_Text)
                .putString("Destination_Location_Text", Destination_Location_Text)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ProgressWorker.class).setInputData(inputData).build();   // WorkerManager 작업 생성
        WorkManager.getInstance(this).enqueue(workRequest);                                                               // WorkerManager 작업 큐에 추가

        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(workRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null) {                                       // 진행 중 상태일 때
                            if (workInfo.getState() == WorkInfo.State.RUNNING) {      // 진행 상태 수신
                                Data progressData = workInfo.getProgress();
                                int progress = progressData.getInt("PROGRESS", 0);   // 프로그래스 현 상황과 값을 받기 위한 부분

                                progressBar.setProgress(progress);                    // 프로그래스 바 업데이트
                                progressText.setText(progress + "%");
                            }
                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {     // 완료되면 결과를 받음
                                outputData = workInfo.getOutputData();                 // Data 객체가 받은 결과


                            }
                        }
                    }
                });
    }
}

