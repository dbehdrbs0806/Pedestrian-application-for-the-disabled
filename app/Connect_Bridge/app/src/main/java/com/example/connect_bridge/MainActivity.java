package com.example.connect_bridge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.speech.tts.TextToSpeech;

import org.w3c.dom.Text;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ImageView Vision_Disability;                          // 시각장애인의 선택을 위한 ImageView
    private ImageView Mobility_Disability;                        // 지체장애인의 선택을 위한 ImageView

    private TextToSpeech tts;                                     // tts 사용을 위한 tts 객체

    private int Disability_Type;                                  // 다음 activity로 넘기기 위한 Type;

    private static final String text1 = "장애인을 위한 길안내 서비스 Connect_Bridge입니다.";
    private static final String text2 = "시각장애인은 앱 화면의 왼쪽을 터치하시고 지체 장애인은 앱의 오른쪽을 터치하십시오";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Vision_Disability = findViewById(R.id.Vision_Start);
        Mobility_Disability = findViewById(R.id.Mobility_start);

        // tts 객체 및 콜백 함수 생성
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {              // TTS가 초기화 완료 시 호출됨
                if (status == TextToSpeech.SUCCESS) {     // TTS가 성공적으로 초기화 되었을 시
                    tts.setLanguage(Locale.KOREAN);        // 알맞은 언어 설정
                    tts.setSpeechRate(1.0f);              // 기본속도 1.0 느리게 0.5
                    tts.speak(text1, TextToSpeech.QUEUE_FLUSH, null, "TEXT1");   // TextToSpeech.QUEUE_FLUSH 이전 음성 출력을 지우고 새 텍스트를 읽는 옵션

                    tts.playSilentUtterance(200, TextToSpeech.QUEUE_ADD, null);
                    tts.speak(text2, TextToSpeech.QUEUE_ADD, null, "TEXT2");
                }
            }
        });

        Intent intent= new Intent(MainActivity.this, AuthorityActivity.class);

        // ImageView Vision_Disability의 TouchListener
        Vision_Disability.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    Disability_Type = 0;
                    intent.putExtra("Disability_Type",Disability_Type);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });

        // ImageView Mobility_Disability의 TouchListener
        Mobility_Disability.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    Disability_Type = 1;
                    intent.putExtra("Disability_Type", Disability_Type);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }
}