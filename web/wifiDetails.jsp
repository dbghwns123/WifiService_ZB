<%@ page import="dto.WifiDetails" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <h1>와이파이 정보 구하기</h1>

    <!-- 메뉴 항목 추가 -->
    <div class="menu">
        <a href="/">홈</a>   |
        <a href="/LocationHistoryServlet">위치 히스토리 목록</a>    |
        <a href="/wifi">Open API 와이파이 정보 가져오기</a>   |
        <a href="/bookmarkdetail">즐겨찾기 보기</a>   |
        <a href="/bookMark.jsp">즐겨찾기 그룹 관리</a>
    </div>

    <!-- 메뉴 항목 아래에 북마크 그룹 선택 버튼과 북마크 추가 버튼 추가 -->
    <div class="menu">
        <select id="bookmarkSelect">
            <option value="">-- 북마크 그룹을 선택하세요 --</option>
        </select>

        <!-- 북마크 추가 버튼 -->
        <button onclick="addBookmark()">북마크 추가하기</button>
    </div>

    <style>
        body {
            font-family: Arial, sans-serif;
        }

        .details-container {
            width: 80%;
            margin: 20px auto;
            padding: 20px;
            border: 1px solid #ccc;
            border-radius: 8px;
            background-color: #f9f9f9;
        }

        h2 {
            text-align: center;
        }

        .detail-item {
            margin-bottom: 10px;
        }

        .detail-item span {
            font-weight: bold;
        }

        .back-btn {
            margin-top: 20px;
            text-align: center;
        }

        .back-btn a {
            padding: 10px 20px;
            background-color: #007bff;
            color: #fff;
            text-decoration: none;
            border-radius: 4px;
        }

        .back-btn a:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>
<div class="details-container">
    <h2>와이파이 상세 정보</h2>


    <%
        WifiDetails wifiDetails = (WifiDetails) request.getAttribute("wifiDetails");
        if (wifiDetails != null) {
    %>
    <div class="detail-item"><span>와이파이명:</span> <%= wifiDetails.getWifiName() %></div>
    <div class="detail-item"><span>관리번호:</span> <%= wifiDetails.getMgrNo() %></div>
    <div class="detail-item"><span>주소:</span> <%= wifiDetails.getAddress() %></div>
    <div class="detail-item"><span>설치 유형:</span> <%= wifiDetails.getInstallationType() %></div>
    <div class="detail-item"><span>설치 연도:</span> <%= wifiDetails.getInstallationYear() %></div>
    <div class="detail-item"><span>설치 기관:</span> <%= wifiDetails.getInstallationAgency() %></div>
    <div class="detail-item"><span>위도:</span> <%= wifiDetails.getLatitude() %></div>
    <div class="detail-item"><span>경도:</span> <%= wifiDetails.getLongitude() %></div>
    <div class="detail-item"><span>층수:</span> <%= wifiDetails.getFloor() %></div>
    <div class="detail-item"><span>서비스 종류:</span> <%= wifiDetails.getServiceType() %></div>
    <%
        }
    %>

    <div class="back-btn">
        <a href="javascript:history.back()">뒤로 가기</a>
    </div>
</div>

<script>
    // 페이지 로드 시 북마크 그룹 목록을 불러오기
    window.onload = function() {
        loadBookmarkGroups();
    }

    // 서버에서 북마크 그룹 목록을 가져와서 드롭다운에 추가
    function loadBookmarkGroups() {
        const xhr = new XMLHttpRequest();
        xhr.open('GET', '/bookmarksave', true);  // 서버에서 북마크 그룹 목록을 가져오는 요청
        xhr.onload = function() {
            if (xhr.status === 200) {
                const bookmarkGroups = JSON.parse(xhr.responseText);
                const bookmarkSelect = document.getElementById('bookmarkSelect');

                // 기존 옵션 제거
                bookmarkSelect.innerHTML = '<option value="">-- 북마크 그룹을 선택하세요 --</option>';

                // 새로운 북마크 그룹 목록 추가
                bookmarkGroups.forEach(function(group) {
                    const option = document.createElement('option');
                    option.value = group.id;  // 그룹 ID를 값으로 설정
                    option.textContent = group.bookmark_name;  // 그룹 이름을 텍스트로 설정
                    bookmarkSelect.appendChild(option);
                });
            }
        };
        xhr.send();
    }

    function addBookmark() {
        const selectedGroupId = document.getElementById('bookmarkSelect').value;
        const bookmarkName = document.getElementById('bookmarkSelect').selectedOptions[0].textContent; // 선택된 그룹의 이름
        const wifiInfoId = '${wifiDetails.mgrNo}'; // JSP에서 전달된 와이파이 관리번호를 사용


        console.log(bookmarkName);
        console.log(wifiInfoId);

        if (!selectedGroupId) {
            alert('북마크 그룹을 선택해주세요.');
            return;
        }

        // POST 요청을 보내는 코드
        const xhr = new XMLHttpRequest();
        xhr.open('POST', '/bookmark', true);  // 북마크 추가를 위한 POST 요청
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');  // URL 인코딩 방식으로 데이터 전송

        xhr.onload = function() {
            if (xhr.status === 200) {
                alert('북마크가 추가되었습니다!');
                window.location.href = '/bookmarkdetail';
            } else {
                alert('북마크 추가에 실패했습니다.');
            }
        };

        // 서버로 URL 인코딩된 폼 데이터 전송
        const params = 'wifi_info_id=' + encodeURIComponent(wifiInfoId) + '&bookmark_name=' + encodeURIComponent(bookmarkName);
        xhr.send(params);
    }
</script>

</body>
</html>
