package com.example.connect_bridge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class LoadingActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private String Starting_Location_Text;
    private String Destination_Location_Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        Intent location_intent = getIntent();
        Starting_Location_Text = location_intent.getStringExtra("Starting_Location_Text");
        Destination_Location_Text = location_intent.getStringExtra("Destination_Location_Text");


        Data inputData = new Data.Builder()
                .putString("Starting_Location_Text", Starting_Location_Text)
                .putString("Destination_Location_Text", Destination_Location_Text)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ProgressWorker.class)
                .setInputData(inputData)
                .build();
        WorkManager.getInstance(this).enqueue(workRequest);
    }
}
