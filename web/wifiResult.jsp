  

  
<%@ page contentType="text/html; charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>와이파이 정보 구하기</title>
    <style>
        table {
            width: 100%;
            border-collapse: collapse;
        }

        table, th, td {
            border: 1px solid black;
        }

        th, td {
            padding: 10px;
            text-align: left;
        }
    </style>
</head>
<body>
<body>
<h1>와이파이 정보 구하기</h1>

<!-- 메뉴 항목 추가 -->
<div class="menu">
    <a href="/">홈</a>    |
    <a href="/LocationHistoryServlet">위치 히스토리 목록</a>   |
    <a href="/wifi">Open API 와이파이 정보 가져오기</a>   |
    <a href="/bookmarkdetail">즐겨찾기 보기</a>   |
    <a href="/bookMark.jsp">즐겨찾기 그룹 관리</a>
</div>
<br>
<br>
<table>
    <thead>
    <tr>
        <th>거리</th>
        <th>관리번호</th>
        <th>자치구</th>
        <th>와이파이명</th>
        <th>도로명주소</th>
        <th>상세주소</th>
        <th>설치위치</th>
        <th>설치유형</th>
        <th>설치기관</th>
        <th>서비스구분</th>
        <th>망종류</th>
        <th>설치년도</th>
        <th>실내외구분</th>
        <th>접속환경</th>
        <th>LAT</th>
        <th>LNT</th>
        <th>작업일자</th>
    </tr>
    </thead>
    <tbody>
    <%
        // Wifi Data 받아오기
        String wifiData = (String) request.getAttribute("wifiData");

        // JSON 데이터를 직접 파싱하는 방법
        String startMarker = "\"row\":[";  // "row" 배열의 시작
        String endMarker = "]";  // 배열의 끝

        // "row" 배열 부분을 추출
        int startIdx = wifiData.indexOf(startMarker) + startMarker.length();
        int endIdx = wifiData.indexOf(endMarker);
        String rowsData = wifiData.substring(startIdx, endIdx).trim();

        // 각 Wifi 항목을 ',' 기준으로 분리하여 처리
        String[] wifiEntries = rowsData.split("},\\{");

        for (String entry : wifiEntries) {
            // 각 항목에서 key-value 추출 (단순한 String 조작)
            String lat = getValue(entry, "LAT");
            String lnt = getValue(entry, "LNT");
            String distance = "근처";  // 거리 계산 필요
    %>
    <tr>
        <td><%= distance %>
        </td>
        <td><%= getValue(entry, "X_SWIFI_MGR_NO") %>
        </td>
        <td><%= getValue(entry, "X_SWIFI_WRDOFC") %>
        </td>
        <td><%= getValue(entry, "X_SWIFI_MAIN_NM") %>
        </td>
        <td><%= getValue(entry, "X_SWIFI_ADRES1") %>
        </td>
        <td><%= getValue(entry, "X_SWIFI_ADRES2") %>
        </td>
        <td><%= getValue(entry, "X_SWIFI_INSTL_FLOOR") %>
        </td>
        <td><%= getValue(entry, "X_SWIFI_INSTL_TY") %>
        </td>
        <td><%= getValue(entry, "X_SWIFI_INSTL_MBY") %>
        </td>
        <td><%= getValue(entry, "X_SWIFI_SVC_SE") %>
        </td>
        <td><%= getValue(entry, "X_SWIFI_CMCWR") %>
        </td>
        <td><%= getValue(entry, "X_SWIFI_CNSTC_YEAR") %>
        </td>
        <td><%= getValue(entry, "X_SWIFI_INOUT_DOOR") %>
        </td>
        <td><%= getValue(entry, "X_SWIFI_REMARS3") %>
        </td>
        <td><%= lat %>
        </td>
        <td><%= lnt %>
        </td>
        <td><%= getValue(entry, "WORK_DTTM") %>
        </td>
    </tr>
    <% } %>
    </tbody>
</table>

<script>
    // 자바스크립트로 내 위치를 가져오고 근처 와이파이 정보를 보여주는 기능을 구현할 수 있습니다.
</script>
</body>
</html>

<%!
    // "key" 값을 추출하는 메소드
    private String getValue(String entry, String key) {
        String keyWithQuotes = "\"" + key + "\":";
        int keyIdx = entry.indexOf(keyWithQuotes);
        if (keyIdx == -1) {
            return "";  // 값이 없으면 빈 문자열 반환
        }

        int valueStartIdx = keyIdx + keyWithQuotes.length();
        int valueEndIdx = entry.indexOf("\"", valueStartIdx + 1);

        return entry.substring(valueStartIdx + 1, valueEndIdx);
    }
%>
