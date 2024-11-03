package com.example.connect_bridge;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.speech.tts.TextToSpeech;
import android.widget.EditText;

import org.w3c.dom.Text;

public class VisionDisabilityActivity extends AppCompatActivity {

    TextToSpeech tts;
    EditText Starting_Location;
    EditText Departure_Location;

    static final String text1 = "지금부터 출발장소와 도착장소를 입력받습니다.";
    static final String text2 = "화면을 한 번 터치하고 출발장소 입력" +
            " 그리고 다시 한 번 터치하고 도착 장소를 입력 받습니다.";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visiondisability);

        Starting_Location = findViewById(R.id.Vision_Starting_Location);
        Departure_Location = findViewById(R.id.Vision_Departure_Location);



        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

            }
        })
    }
}
