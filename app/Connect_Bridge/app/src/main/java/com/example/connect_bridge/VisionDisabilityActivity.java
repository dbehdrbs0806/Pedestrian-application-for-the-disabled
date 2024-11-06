package com.example.connect_bridge;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class VisionDisabilityActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;

    private EditText Starting_Location;
    private EditText Departure_Location;

    private String Starting_Location_Text = "";               // stt로 입력받은 출발장소
    private String Destination_Location_Text = "";            // stt로 입력받은 출발장소
    private boolean isStartingLocation = true;              // 현재 입력 단계 추적

    private View background_view;

    private Button start_navigation_button1;

    static final String text1 = "지금부터 출발장소와 도착장소를 입력받습니다.";
    static final String text2 = "화면을 한 번 터치하고 출발장소 입력" +
            " 그리고 다시 한 번 터치하고 도착 장소를 입력 받습니다.";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visiondisability);

        Starting_Location = findViewById(R.id.Vision_Starting_Location);
        Departure_Location = findViewById(R.id.Vision_Departure_Location);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);                    // STT 사용을 위한 객체 생성
        initSpeechRecognizer();

        background_view = findViewById(R.id.touchable_area);
        start_navigation_button1 = findViewById(R.id.start_navigation_button1);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {              // TTS가 초기화 완료 시 호출됨
                if (status == TextToSpeech.SUCCESS) {     // TTS가 성공적으로 초기화 되었을 시
                    tts.setLanguage(Locale.KOREAN);       // 알맞은 언어 설정
                    tts.setSpeechRate(1.0f);              // 기본속도 1.0 느리게 0.5
                    tts.speak(text1, TextToSpeech.QUEUE_FLUSH, null, "TEXT1");   // TextToSpeech.QUEUE_FLUSH 이전 음성 출력을 지우고 새 텍스트를 읽는 옵션

                    tts.playSilentUtterance(200, TextToSpeech.QUEUE_ADD, null);
                    tts.speak(text2, TextToSpeech.QUEUE_ADD, null, "TEXT2");
                }
            }
        });

        background_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    startSpeechRecognition();
                }
                return false;
            }
        });

        start_navigation_button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent next_intent = new Intent(VisionDisabilityActivity.this, LoadingActivity.class);
                next_intent.putExtra("startingLocationText", Starting_Location_Text);
                next_intent.putExtra("destinationLocationText", Destination_Location_Text);
                startActivity(next_intent);
                finish();
            }
        });
    }

    // 음성 인식을 위한 SpeechRecognizer 초기화 및 리스너 설정 함수
    private void initSpeechRecognizer() {
        speechRecognizer.setRecognitionListener(new RecognitionListener(){           // 음성 인식 리스너 설정
            @Override
            public void onReadyForSpeech(Bundle params) {                            // 음성 인식 준비가 완료 되었을 때 호출
                Toast.makeText(VisionDisabilityActivity.this, "음성 인식을 시작합니다.", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onBeginningOfSpeech() { }                                    // 사용자가 말하기 시작할 때 호출

            @Override
            public void onRmsChanged(float rmsdB) { }                                // 음성의 입력의 볼륨을 감지하고 변경 시 호출

            @Override
            public void onBufferReceived(byte[] buffer) { }                          // 음성 데이터가 인식 중에 수신될 때 호출

            @Override
            public void onEndOfSpeech() { }                                          // 사용자가 말하기를 멈췄을 때 호출

            @Override
            public void onError(int error) {                                         // 오류가 발생했을 때 호출
                Toast.makeText(VisionDisabilityActivity.this, "음성 인식 에러: " , Toast.LENGTH_SHORT).show();
            }

            // 음성 인식 결과가 도출되었을 때 호출
            @Override
            public void onResults(Bundle results) {                                   // 인식된 결과를 문자열 리스트로 가져옴
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {                                                // 첫 번째 인식 결과
                    String resultText = matches.get(0);

                    // STT 결과를 여기서 처리합니다.
                    // 예를 들어, EditText에 결과를 표시하거나, 다른 동작을 수행할 수 있습니다.
                    
                    if (isStartingLocation) {
                        // 출발장소 입력 단계일 때
                        Starting_Location_Text = resultText;                            // 변수에 저장
                        Starting_Location.setText(resultText);                        // EditText에 표시
                        isStartingLocation = false;                                   // 다음 입력 단계로 전환
                    } else {
                        // 도착장소 입력 단계일 때
                        Destination_Location_Text = resultText;                          // 변수에 저장
                        Departure_Location.setText(resultText);                        // EditText에 표시
                        isStartingLocation = true;                                     // 다시 출발장소 입력 단계로 전환
                    }
                }
            }

            // 부분적인 인식 결과가 있을 때 호출 (실시간으로 부분 결과를 보여줄 때 사용)
            @Override
            public void onPartialResults(Bundle partialResults) { }

            // 기타 이벤트가 발생했을 때 호출 (거의 사용되지 않음)
            @Override
            public void onEvent(int eventType, Bundle params) { }
        });
    }

    // 음성 인식 시작 메서드
    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);              // 음성 인식을 요청하는 Intent 생성
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);    // 음성 모델을 자유로운 형식(FREE_FORM)으로 설정
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());             // 언어 설정 (기본 로케일 언어 사용)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "말씀해주세요...");              // 음성 인식 프롬프트 메시지 설정

        if (speechRecognizer != null) {                                                    // 음성 인식을 시작
            speechRecognizer.startListening(intent);
        }
    }

    // Activity 종료 시 호출되는 메서드 - SpeechRecognizer 자원을 해제
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

}
