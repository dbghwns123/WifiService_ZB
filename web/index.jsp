<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>와이파이 정보 구하기</title>
    <style>

        body {
            font-family: Arial, sans-serif;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            font-size: 14px;
        }

        table, th, td {
            border: 1px solid black;
        }

        th, td {
            padding: 8px 12px;
            text-align: left;
            word-wrap: break-word; /* 긴 단어는 줄바꿈 */
            white-space: normal; /* 텍스트가 자동으로 줄 바꿈되게 */
            max-width: 200px; /* 모든 td의 최대 너비 설정 */
            overflow: hidden; /* 넘치는 내용은 숨김 처리 */
            text-overflow: ellipsis; /* 넘치면 ... 으로 표시 */
        }

        th {
            background-color: #f2f2f2;
            font-weight: bold;
        }

        td.work-dttm {
            max-width: 300px; /* work_dttm 컬럼 너비를 더 넓게 설정 */
            white-space: nowrap; /* 텍스트가 줄 바꿈되지 않게 설정 */
            overflow: hidden;
            text-overflow: ellipsis; /* 넘치는 텍스트를 "..."으로 표시 */
        }

        #wifiTableContainer {
            max-width: 1500px;
            margin: 0 auto;
            overflow-x: auto; /* 테이블 내용이 화면을 넘지 않도록 스크롤 추가 */
        }
    </style>

</head>
<body>
<h1>와이파이 정보 구하기</h1>

<!-- 메뉴 항목 추가 -->
<div class="menu">
    <a href="/">홈</a>   |
    <a href="/LocationHistoryServlet">위치 히스토리 목록</a>   |
    <a href="/wifi">Open API 와이파이 정보 가져오기</a>   |
    <a href="/bookmarkdetail">즐겨찾기 보기</a>   |
    <a href="/bookMark.jsp">즐겨찾기 그룹 관리</a>
</div>


<!-- 내 위치 및 근처 Wi-Fi 정보 보기 -->
<div class="location-input">
    <h3>내 위치 정보</h3>
    <label for="lat">LAT : </label>
    <input type="text" id="lat" name="lat" readonly>
    <label for="lng">LNT : </label>
    <input type="text" id="lng" name="lng" readonly>

    <button type="button" onclick="getLocation()">내 위치 가져오기</button>
    <button type="button" onclick="getNearbyWifi()">근처 와이파이 정보 보기</button>

    <!-- 와이파이 테이블 -->
    <div id="wifiTableContainer">
        <!-- 테이블 헤더만 미리 표시 -->
        <table>
            <thead>
            <tr>
                <th>거리 (km)</th>
                <th>관리번호</th>
                <th>자치구</th>
                <th>와이파이명</th>
                <th>도로명주소</th>
                <th>상세주소</th>
                <th>설치유형</th>
                <th>설치기관</th>
                <th>서비스구분</th>
                <th>망종류</th>
                <th>설치년도</th>
                <th>실내외구분</th>
                <th>LAT</th>
                <th>LNT</th>
                <th>작업일자</th>
            </tr>
            </thead>
            <tbody>
            <!-- AJAX로 데이터가 들어갈 부분 -->
            </tbody>
        </table>
    </div>
    <script>
        // 내 위치를 가져오는 함수
        function getLocation() {
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                    function (position) { // 성공 콜백
                        var lat = position.coords.latitude;
                        var lng = position.coords.longitude;

                        // 위도, 경도 값을 입력 필드에 표시
                        document.getElementById('lat').value = lat;
                        document.getElementById('lng').value = lng;

                        console.log(`Location set: lat = ${lat}, lnt = ${lng}`);

                        // 위치 데이터를 서버로 전송
                        saveLocationToDatabase(lat, lng);
                    },
                    function (error) { // 실패 콜백
                        switch (error.code) {
                            case error.PERMISSION_DENIED:
                                alert("위치 정보 접근이 거부되었습니다.");
                                break;
                            case error.POSITION_UNAVAILABLE:
                                alert("위치 정보를 사용할 수 없습니다.");
                                break;
                            case error.TIMEOUT:
                                alert("위치 정보를 가져오는 데 시간이 초과되었습니다.");
                                break;
                            default:
                                alert("알 수 없는 오류가 발생했습니다.");
                        }
                    }
                );
            } else {
                alert("브라우저가 Geolocation을 지원하지 않습니다.");
            }
        }

        // 위치 데이터를 서버로 전송하는 함수
        function saveLocationToDatabase(lat, lng) {
            var xhr = new XMLHttpRequest();
            xhr.open("POST", "/saveLocation", true); // 서버의 서블릿 경로 지정
            xhr.setRequestHeader("Content-Type", "application/json");

            var data = JSON.stringify({
                x_coordinate: lat,
                y_coordinate: lng
            });

            xhr.onload = function () {
                if (xhr.status === 200) {
                    console.log("Location saved to database successfully.");
                } else {
                    alert("위치 정보를 저장하는 데 실패했습니다.");
                }
            };

            xhr.send(data);
        }

        // 근처 와이파이 정보를 가져오는 함수
        function getNearbyWifi() {
            var lat = document.getElementById('lat').value;
            var lng = document.getElementById('lng').value;

            // lat, lng 값이 비어 있으면 오류 처리
            if (!lat || !lng) {
                alert("위치 정보를 먼저 가져와 주세요.");
                return;
            }
            console.log(lat + " " + lng);
            console.log(`lat: ${lat}, lng: ${lng}`);  // 확인용 로그

            // AJAX 요청을 보내서 서버에서 근처 와이파이 정보 받기
            var xhr = new XMLHttpRequest();
            xhr.open("GET", "/wifiNear?lat=" + lat + "&lng=" + lng, true);
            xhr.setRequestHeader("Content-Type", "application/json");

            xhr.onload = function () {
                if (xhr.status === 200) {
                    var response = JSON.parse(xhr.responseText); // 응답 데이터 파싱
                    var wifiData = response.wifiData; // 서버 응답에서 wifiData 가져오기
                    displayWifiData(wifiData); // 데이터를 테이블로 표시
                } else {
                    alert("와이파이 정보를 가져오는 데 실패했습니다.");
                }
            };

            xhr.send();
        }

        // 와이파이 데이터를 테이블 형식으로 표시
        function displayWifiData(wifiData) {
            var tableHtml = "";

            wifiData.forEach(function (wifi) {
                tableHtml += "<tr>" +
                    "<td>" + wifi.distance.toFixed(2) + " km</td>" +
                    "<td>" + wifi.wifi_mgr_no + "</td>" +
                    "<td>" + wifi.wrdofc + "</td>" +
                    "<td><a href='/wifiDetails?wifiName=" + wifi.main_nm + "'>" + wifi.main_nm + "</a></td>" +
                    "<td>" + wifi.adres1 + "</td>" +
                    "<td>" + wifi.adres2 + "</td>" +
                    "<td>" + wifi.instl_ty + "</td>" +
                    "<td>" + wifi.instl_mby + "</td>" +
                    "<td>" + wifi.svc_se + "</td>" +
                    "<td>" + wifi.cmcwr + "</td>" +
                    "<td>" + wifi.cnstc_year + "</td>" +
                    "<td>" + wifi.inout_door + "</td>" +
                    "<td>" + wifi.lat + "</td>" +
                    "<td>" + wifi.lnt + "</td>" +
                    "<td>" + wifi.work_dttm + "</td>" +
                    "</tr>";
            });

            // 테이블 본문에 추가
            document.querySelector("#wifiTableContainer tbody").innerHTML = tableHtml;
        }
    </script>
</body>
</html>
