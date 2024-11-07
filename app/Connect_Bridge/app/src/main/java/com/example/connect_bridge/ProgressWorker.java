package com.example.connect_bridge;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ProgressWorker extends Worker {
    private static RequestQueue requestQueue;                             // volley 사용을 위한 rest api 요청을 담고 뱉을 요청 큐
    private String startingLocation;                                      // 서버로 보내기 위한 출발 장소
    private String destinationLocation;                                   // 서버로 보내기 위한 도착 장소
    private Location location;                                            // json 형태로 보내기위한 Gson 으로 보낼 객체파일

    private String jsonfile;                                              // 보낼 json 파일

    static final String url = "http://127.0.0.1:5000";                                   // url 내용

    public ProgressWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        if (requestQueue == null) {                                        // requestQueue 사용을 위해 Queue를 초기화
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
    }

    @NonNull
    @Override
    public Result doWork() {                                                            // 백그라운드 처리될 내용의 함수
        try {                                                                           // try-catch를 통해 base64를 통해 이미지 인코딩
            setProgressAsync(new Data.Builder().putInt("PROGRESS", 10).build());        // 인코딩 10%로 progress 업데이트

            startingLocation = getInputData().getString("Starting_Location_Text");  // activity에서 넘어온 데이터로 출발장소, 도착장소를 받음
            destinationLocation = getInputData().getString("Destination_Location_Text");
            Thread.sleep(1000);                                                    // 1초 대기

            if (MainActivity.Disability_Type == 0) {                                    // 시각장애
                location = new Location(startingLocation, destinationLocation, 0);
                jsonfile = saveTojson(location);
            }
            else {                                                                      // 지체장애
                location = new Location(startingLocation, destinationLocation, 1);
                jsonfile = saveTojson(location);
            }
            setProgressAsync(new Data.Builder().putInt("PROGRESS", 30).build());        // 인코딩 30%로 progress 업데이트


            // doWork 메소드의 종료를 막기위해 CounterDownLatch 사용
            // CountDownLatch latch = new CountDownLatch(1);

            handleResult();                                                        // handleResult() 로 서버로부터 응답 받음
            // setProgressAsync(new Data.Builder().putInt("PROGRESS", 50).build());        // 인코딩 50%로 progress 업데이트

          /*  if (!latch.await(15, TimeUnit.SECONDS)) {  // 응답이 10초 이내에 오지 않으면 실패
                return Result.failure();
            }*/

            // 성공 결과 반환
            // activity 로 보낼 outputData
            Data outputData = new Data.Builder().build();                                        // 여기다가 받은 데이터 받아서 다시 Activity로 보냄
            return Result.success(outputData);                                                   // 비동기 progressworker가 success 되어 받음
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
    private String saveTojson(Location location) {
        String resultfile;
        Gson gson = new Gson();
        resultfile = gson.toJson(location);
        return resultfile;
    }
    private void handleResult() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,    // Post 형식으로 rest api 보낼 내용 작성
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {                        // 서버로부터의 응답(response)을 JSON 형태로 처리
                        try {
                            JSONObject jsonResponse = new JSONObject(response);      // jsonResponse 가 받은 json, 데이터 파일

                            // 서버에서 전달된 데이터를 추출

                            // 응답 완료 후 진행률 100% 업데이트
                            setProgressAsync(new Data.Builder().putInt("PROGRESS", 100).build());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("JSONError", "JSON Parsing error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {                                       // 오류 발생 시 실행 될 부분
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", "Error: " + error.getMessage()); // 오류 처리
                        Toast.makeText(getApplicationContext(), "Error: " +error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonfile.getBytes();                                         // JSON 데이터를 서버로 보냄
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";                            // Content-Type 설정
            }
        };

        requestQueue.add(stringRequest);                                             // 요청 큐에 추가하여 비동기 요청 수행
    }
}

