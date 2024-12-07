<%--
  Created by IntelliJ IDEA.
  User: yuhojun
  Date: 2024. 12. 6.
  Time: PM 11:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page import="dto.LocationHistory" %>
<%@ page import="java.util.List" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>위치 히스토리 목록</title>
  <style>
    body {
      font-family: Arial, sans-serif;
    }

    table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 20px;
    }

    table, th, td {
      border: 1px solid black;
    }

    th, td {
      padding: 10px;
      text-align: center;
    }

    th {
      background-color: #f2f2f2;
    }

    .delete-button {
      background-color: #ff4d4d;
      color: white;
      border: none;
      padding: 5px 10px;
      cursor: pointer;
      border-radius: 4px;
    }

    .delete-button:hover {
      background-color: #e60000;
    }
  </style>
</head>
<body>
<h1>위치 히스토리 목록</h1>
<div class="menu">
  <a href="/">홈</a>   |
  <a href="/LocationHistoryServlet">위치 히스토리 목록</a>   |
  <a href="/wifi">Open API 와이파이 정보 가져오기</a>   |
  <a href="/bookmarkdetail">즐겨찾기 보기</a>   |
  <a href="/bookMark.jsp">즐겨찾기 그룹 관리</a>
</div>
<table>
  <thead>
  <tr>
    <th>ID</th>
    <th>X 좌표</th>
    <th>Y 좌표</th>
    <th>조회 일자</th>
    <th>비고</th>
  </tr>
  </thead>
  <tbody>
  <!-- 데이터베이스로부터 위치 정보를 가져와 출력 -->
  <%
    List<LocationHistory> locationHistoryList =
            (List<LocationHistory>) request.getAttribute("locationHistoryList");

    if (locationHistoryList != null) {
      for (LocationHistory location : locationHistoryList) {
  %>
  <tr>
    <td><%= location.getId() %></td>
    <td><%= location.getXCoordinate() %></td>
    <td><%= location.getYCoordinate() %></td>
    <td><%= location.getQueryDate() %></td>
    <td>
      <form action="LocationHistoryServlet" method="post" style="margin: 0;">
        <input type="hidden" name="id" value="<%= location.getId() %>">
        <button type="submit" class="delete-button">삭제</button>
      </form>
    </td>
  </tr>
  <%
    }
  } else {
  %>
  <tr>
    <td colspan="5">저장된 위치 정보가 없습니다.</td>
  </tr>
  <%
    }
  %>
  </tbody>
</table>
</body>
</html>

