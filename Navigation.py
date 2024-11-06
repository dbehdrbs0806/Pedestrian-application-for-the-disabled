import os
from flask import Flask, jsonify, request
import json
import requests
import pymysql

app = Flask(__name__)

# 카카오 API 키 설정
API_KEY = 'c541cd06f03f4fed23adbceb47d703bc'

# JSON 데이터를 파일에 저장하는 함수
def json_to_file(json_data):
    data = {
        'Id': json_data.get('Id', 'ID 정보 없음'),
        'StartingLocation': json_data.get('StartingLocation', '출발지 정보 없음'),
        'DepartureLocation': json_data.get('DepartureLocation', '목적지 정보 없음'),
        'Timestamp': json_data.get('Timstamp', '타임스탬프 정보 없음'),
        'Disability': json_data.get('Disability', '장애 정보 없음')
    }
    with open('output_data.json', 'w', encoding='utf-8') as file:
        json.dump(data, file, ensure_ascii=False, indent=4)
    print("JSON 데이터가 파일에 저장되었습니다.")

# JSON 파일에서 데이터를 읽는 함수
def read_from_file(file_name):
    with open(file_name, 'r', encoding='utf-8') as file:
        data = json.load(file)
    return data

# 주소를 좌표로 변환하는 함수 (카카오 API 사용)
def get_coordinates(address):
    url = "https://dapi.kakao.com/v2/local/search/address.json"
    headers = {"Authorization": f"KakaoAK {API_KEY}"}
    params = {"query": address}
    response = requests.get(url, headers=headers, params=params)
    result = response.json()
    if result['documents']:
        x = result['documents'][0]['x']
        y = result['documents'][0]['y']
        return x, y
    else:
        raise Exception(f"주소 '{address}'를 찾을 수 없습니다.")

# 데이터베이스에서 장애물 정보를 가져오는 함수 (예제)
def get_obstacles_from_db(start_coords, destination_coords):
    connection = pymysql.connect(
        host='your_host',
        user='your_user',
        password='your_password',
        database='your_database',
        charset='utf8mb4'
    )
    cursor = connection.cursor()

    # 장애물 정보 쿼리
    query = """
    SELECT type, x, y
    FROM obstacles
    WHERE (x BETWEEN %s AND %s) AND (y BETWEEN %s AND %s)
    """
    
    # 좌표 범위 계산
    min_x = min(float(start_coords[0]), float(destination_coords[0]))
    max_x = max(float(start_coords[0]), float(destination_coords[0]))
    min_y = min(float(start_coords[1]), float(destination_coords[1]))
    max_y = max(float(start_coords[1]), float(destination_coords[1]))

    cursor.execute(query, (min_x, max_x, min_y, max_y))
    rows = cursor.fetchall()

    # 장애물 데이터를 목록으로 정리
    obstacle_data = [{"type": row[0], "location": (row[1], row[2])} for row in rows]

    connection.close()
    return obstacle_data

# 장애물을 피해서 경로를 계산하는 함수 (예제)
def calculate_route_avoiding_obstacles(start_coords, destination_coords, obstacle_data):
    # 장애물을 피하는 경로 최적화 로직 구현
    route = {"start": start_coords, "destination": destination_coords, "obstacles": obstacle_data}
    return route

# 좌표를 출력하는 엔드포인트
@app.route('/get_optimal_route', methods=['POST'])
def get_optimal_route_route():
    json_data = request.get_json()
    if not json_data:
        return jsonify({"error": "JSON 데이터를 전달해주세요."}), 400

    start_location = json_data.get('StartingLocation')
    destination_location = json_data.get('DepartureLocation')

    if not start_location or not destination_location:
        return jsonify({"error": "출발지와 도착지 정보를 모두 입력해주세요."}), 400

    try:
        # 출발지와 도착지의 좌표 얻기
        start_coords = get_coordinates(start_location)
        destination_coords = get_coordinates(destination_location)
        
        # 데이터베이스에서 장애물 정보 얻기
        #obstacle_data = get_obstacles_from_db(start_coords, destination_coords)
        
        # 장애물을 회피하는 경로 계산
        #optimal_route = calculate_route_avoiding_obstacles(start_coords, destination_coords, obstacle_data)
        
        # 결과 출력
        result = {
            "StartingLocation": start_location,
            "StartingCoordinates": {"latitude": start_coords[1], "longitude": start_coords[0]},
            "DepartureLocation": destination_location,
            "DepartureCoordinates": {"latitude": destination_coords[1], "longitude": destination_coords[0]},
            #"OptimalRoute": optimal_route
        }
        return jsonify(result)

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)
