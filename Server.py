import os
from flask import Flask, jsonify, request
from google.cloud import speech, texttospeech
import io
import json
import requests
import pyaudio

app = Flask(__name__)

'''
STT (Speech-to-Text) 함수
SpeechClient() : 서비스 이용을 위한 클라이언트 객체 생성 음성 파일을 서버로 전송할 때 사용
RecognitionAudio() : 음성 데이터를 구글 API가 사용할 수 있는 형태로 전환
'''
def speech_to_text(audio_content):
    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "eminent-tape-437815-g0-7b658cb5f6ed.json" # STT 인증 키
    client = speech.SpeechClient()
    audio = speech.RecognitionAudio(content=audio_content)
    config = speech.RecognitionConfig(
        encoding=speech.RecognitionConfig.AudioEncoding.LINEAR16,
        sample_rate_hertz=16000,
        language_code="ko-KR",
    )

    # 음성을 텍스트로 변환
    response = client.recognize(config=config, audio=audio)
    transcript_list = [result.alternatives[0].transcript for result in response.results]
    transcript = "\n".join(transcript_list)
    return transcript.strip()

# TTS (Text-to-Speech) 함수
def text_to_speech(text_content):
    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "eminent-tape-437815-g0-ada330fc7bc2.json" # TTS 인증 키
    client = texttospeech.TextToSpeechClient()
    input_text = texttospeech.SynthesisInput(text=text_content)
    voice = texttospeech.VoiceSelectionParams(
        language_code="ko-KR",
        ssml_gender=texttospeech.SsmlVoiceGender.NEUTRAL,
    )
    audio_config = texttospeech.AudioConfig(audio_encoding=texttospeech.AudioEncoding.MP3)

    # 텍스트를 음성으로 변환
    response = client.synthesize_speech(input=input_text, voice=voice, audio_config=audio_config)
    return response.audio_content

# 실시간 음성 입력을 처리하는 함수
def live_speech_to_text():
    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "eminent-tape-437815-g0-7b658cb5f6ed.json" # STT 인증 키
    client = speech.SpeechClient()
    
    # PyAudio 설정
    RATE = 16000
    CHUNK = int(RATE / 10)  # 100ms
    
    p = pyaudio.PyAudio()
    stream = p.open(format=pyaudio.paInt16,
                    channels=1,
                    rate=RATE,
                    input=True,
                    frames_per_buffer=CHUNK)
    
    print("Listening...")
    audio_content = b""  # 오디오 데이터 초기화
    try:
        while True:
            data = stream.read(CHUNK)
            audio_content += data
            
            # 음성 데이터를 일정 크기만큼 수집 후 처리
            if len(audio_content) > RATE * 10:  # 약 10초간 데이터 수집 후 변환 시도
                audio = speech.RecognitionAudio(content=audio_content)
                config = speech.RecognitionConfig(
                    encoding=speech.RecognitionConfig.AudioEncoding.LINEAR16,
                    sample_rate_hertz=RATE,
                    language_code="ko-KR",
                )
                response = client.recognize(config=config, audio=audio)
                transcript_list = [result.alternatives[0].transcript for result in response.results]
                transcript = "\n".join(transcript_list)
                
                # 변경된 부분: 변환된 텍스트를 client.txt 파일에 저장
                with open("client.txt", "w", encoding="utf-8") as text_file:
                    text_file.write(transcript)
                
                # 변경된 부분: TTS를 통해 텍스트를 음성으로 변환 및 출력
                if transcript.strip():
                    audio_response = text_to_speech(transcript)
                    with open("response.mp3", "wb") as output_file:
                        output_file.write(audio_response)
                    print("Transcript:", transcript.strip())
                
                audio_content = b""  # 오디오 데이터 초기화
    except KeyboardInterrupt:
        print("Stopping...")
    finally:
        stream.stop_stream()
        stream.close()
        p.terminate()

#앱에서 해당 엔드포인트를 호출하면 녹음이 시작된다.
@app.route('/start_live_speech', methods=['GET'])
def start_live_speech():
    # 실시간 음성 입력 처리 시작
    live_speech_to_text()
    return jsonify({"message": "Live speech recognition started"}), 200


if __name__ == '__main__':
    live_speech_to_text()


"""해당 코드는 오디오 파일을 서버에 직접 업로드 할 때 사용 그럼 이거 쓰면 되요
# 오디오 데이터 처리 라우트 (서버 측)
@app.route('/process_audio', methods=['POST'])
def process_audio():
    if 'audio' not in request.files:
        return jsonify({"error": "No audio file provided"}), 400

    # 음성 파일을 읽음
    audio_file = request.files['audio']
    audio_content = audio_file.read()

    # STT를 사용하여 텍스트 변환
    transcript = speech_to_text(audio_content)

    # 텍스트를 JSON으로 저장 (예: 데이터베이스로 전송하기 전에)
    data = {"transcript": transcript}
    try:
        with open("client.txt", "w", encoding="utf-8") as text_file:
            text_file.write(transcript)
    except IOError as e:
        return jsonify({"error": f"Failed to write to TXT file: {str(e)}"}), 500

    # TTS를 사용하여 텍스트를 음성으로 변환
    audio_response = text_to_speech(transcript)

    # 음성 파일 반환
    return (audio_response, 200, {
        'Content-Type': 'audio/mpeg',
        'Content-Disposition': 'inline; filename="response.mp3"'
    })
    # 클라이언트 -> 서버 파일 업로드
def upload_audio_file():
    url = "http://192.168.0.63:5000/process_audio"
    audio_file_path = "C:\\Users\\alvin\\OneDrive\\대학\\학술제\\길안내\\tts\\컴공.mp3"



    with open(audio_file_path, "rb") as f:
        files = {"audio": f}
        response = requests.post(url, files=files)

    if response.status_code == 200:
        with open("response.mp3", "wb") as output_file:
            output_file.write(response.content)
    else:
        print("Error:", response.json())
"""

#입력받은 데이터를 DB로 전달 앱(json파일) -> 서버(받은 데이터를 쪼개기 그걸 이제 db로 보낸다 그럼 출발지 목적지를 보내면 -> db에서 그걸로 경로를 서버에 보내겠죠? 그걸 앱으로 다시 보내면 끝 ez 오늘 그럼 db쪼개는거 다 작성할께요 
# 형
# 장난 아니에요 한번 해볼래요? 이거 절대 못깨 ) ->  
#