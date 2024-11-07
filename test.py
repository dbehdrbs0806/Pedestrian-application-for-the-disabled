import requests
import json

# 서버 URL
url = "http://127.0.0.1:5000/get_optimal_route"

# JSON 파일 경로
file_path = "test.json"

# JSON 파일 읽기
with open(file_path, 'r', encoding='utf-8') as file:
    json_data = json.load(file)

# 요청 보내기
headers = {"Content-Type": "application/json"}
response = requests.post(url, headers=headers, json=json_data)

# 응답 확인
if response.status_code == 200:
    print("성공:", response.json())
else:
    print("오류 발생:", response.status_code, response.text)
