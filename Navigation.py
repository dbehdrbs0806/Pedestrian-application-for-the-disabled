import os
import json
import requests
import pymysql
import numpy as np
from flask import Flask, jsonify, request

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
        return float(x), float(y)  # 좌표를 실수로 반환
    else:
        raise Exception(f"주소 '{address}'를 찾을 수 없습니다.")

# 좌표를 이차원 배열에 저장하는 함수
def store_coordinates(start_location, destination_location):
    # 출발지와 도착지 좌표 얻기
    start_coords = get_coordinates(start_location)
    destination_coords = get_coordinates(destination_location)
    
    # 위도와 경도 값을 이차원 배열에 저장
    position = np.array([
        [start_coords[1], start_coords[0]],  # 출발지 좌표 (위도, 경도)
        [destination_coords[1], destination_coords[0]]  # 도착지 좌표 (위도, 경도)
    ])
    
    return position

# 데이터베이스에서 장애물 정보를 가져오는 함수
def get_obstacles_from_db(start_coords, destination_coords):
    connection = pymysql.connect(
        host='127.0.0.1',
        user='root',
        password='3413',
        database='obstacle_db',
        charset='utf8mb4'
    )
    cursor = connection.cursor()
    
    # 범위 내 장애물 정보 쿼리
    query = """
    SELECT obstacle_type, latitude, longitude
    FROM obstacles
    WHERE (latitude BETWEEN %s AND %s) AND (longitude BETWEEN %s AND %s)
    """
    
    # 좌표 범위 계산
    min_x = min(float(start_coords[0]), float(destination_coords[0]))
    max_x = max(float(start_coords[0]), float(destination_coords[0]))
    min_y = min(float(start_coords[1]), float(destination_coords[1]))
    max_y = max(float(start_coords[1]), float(destination_coords[1]))

    cursor.execute(query, (min_x, max_x, min_y, max_y))
    rows = cursor.fetchall()

    # 범위 내 장애물 데이터를 목록으로 정리
    obstacle_data = [{"type": row[0], "location": (row[1], row[2])} for row in rows]

    connection.close()
    return obstacle_data

# 카카오내비 API를 사용하여 최적 경로를 설정하는 함수
def get_optimized_route_with_kakao_navi(start_coords, destination_coords, obstacles):
    url = "https://apis-navi.kakaomobility.com/v1/directions"
    headers = {"Authorization": f"KakaoAK {API_KEY}"}

    # 장애물을 피해가기 위해 경유지(waypoints)를 설정
    waypoints = "|".join(f"{obstacle['location'][1]},{obstacle['location'][0]}" for obstacle in obstacles)

    params = {
        "origin": f"{start_coords[1]},{start_coords[0]}",            # 출발지 경도, 위도
        "destination": f"{destination_coords[1]},{destination_coords[0]}",  # 도착지 경도, 위도
        "waypoints": waypoints,                                      # 장애물 위치를 경유지로 설정
        "priority": "RECOMMEND"                                      # 추천 경로 우선
    }

    # 카카오내비 API 요청
    response = requests.get(url, headers=headers, params=params)
    if response.status_code == 200:
        return response.json()  # 최적 경로 정보 반환
    else:
        raise Exception(f"카카오내비 API 오류: {response.json().get('msg', '경로를 찾을 수 없습니다.')}")

# 최적 경로를 계산하여 반환하는 엔드포인트
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
        # 이차원 배열에 좌표 저장
        position_array = store_coordinates(start_location, destination_location)
        
        # 장애물 정보를 데이터베이스에서 가져오기
        start_coords = (position_array[0][0], position_array[0][1])  # (위도, 경도) 형식으로 맞춤
        destination_coords = (position_array[1][0], position_array[1][1])  # (위도, 경도) 형식으로 맞춤
        obstacle_data = get_obstacles_from_db(start_coords, destination_coords)
        
        # 카카오내비 API를 사용하여 최적 경로 가져오기
        optimized_route = get_optimized_route_with_kakao_navi(start_coords, destination_coords, obstacle_data)

        # 카카오맵 URL 스킴 생성
        kakao_map_url = f"https://map.kakao.com/link/to/{destination_location},{destination_coords[1]},{destination_coords[0]}"
        
        # 결과 출력
        result = {
            "StartingLocation": start_location,
            "DepartureLocation": destination_location,
            "CoordinatesArray": position_array.tolist(),  # numpy 배열을 리스트로 변환하여 JSON 응답
            "Obstacles": obstacle_data,                   # 장애물 정보 추가
            "OptimizedRoute": optimized_route,            # 최적 경로 정보 추가
            "KakaoMapUrl": kakao_map_url                  # 카카오맵 URL 스킴 추가
        }
        return jsonify(result)

    except Exception as e:
        return jsonify({"error": str(e)}), 500

# Flask 서버 실행
if __name__ == '__main__':
    app.run(debug=True)
