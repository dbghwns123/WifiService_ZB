<%@ page import="dto.Bookmark" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>즐겨찾기 목록</title>
</head>

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

</style>

<body>

<h1>즐겨찾기 목록</h1>

<!-- 메뉴 항목 추가 -->
<div class="menu">
    <a href="/">홈</a>   |
    <a href="/LocationHistoryServlet">위치 히스토리 목록</a>   |
    <a href="/wifi">Open API 와이파이 정보 가져오기</a>   |
    <a href="/bookmarkdetail">즐겨찾기 보기</a>   |
    <a href="/bookMark.jsp">즐겨찾기 그룹 관리</a>
</div>

<!-- 북마크 테이블 -->
<table border="1">
    <thead>
    <tr>
        <th>ID</th>
        <th>북마크 이름</th>
        <th>와이파이명</th>
        <th>등록일자</th>
        <th>비고</th>
    </tr>
    </thead>
    <tbody>
    <%
        List<Bookmark> bookmarks = (List<Bookmark>) request.getAttribute("bookmarks");
        if (bookmarks != null) {
            for (Bookmark bookmark : bookmarks) {
    %>
    <tr>
        <td><%= bookmark.getId() %></td>
        <td><%= bookmark.getBookmarkName() %></td>
        <td><%= bookmark.getWifiName() %></td>
        <td><%= bookmark.getCreatedAt() != null ? bookmark.getCreatedAt().toString() : "" %></td>
        <td>
            <!-- 북마크 삭제 버튼 -->
            <button onclick="deleteBookMark(<%= bookmark.getId() %>, '<%= bookmark.getWifiName() %>')">삭제</button>
        </td>
    </tr>
    <%
            }
        }
    %>
    </tbody>
</table>

<script>
    function deleteBookMark(bookmarkId , wifiName) {
        if (confirm('정말 삭제하겠습니까?')) {
            const xhr = new XMLHttpRequest();
            console.log(bookmarkId);  // 삭제하려는 북마크의 ID 확인

            // DELETE 요청을 보낼 때 URL에 bookmarkId를 추가하여 서버에 전달
            xhr.open('DELETE', '/bookmarkdetail?bookmark_id=' + bookmarkId + '&main_nm=' + wifiName, true);
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
            xhr.onload = function() {
                if (xhr.status === 200) {
                    alert('북마크 그룹이 삭제되었습니다.');
                    location.reload();
                } else {
                    alert('삭제 실패');
                }
            };
            xhr.send();
        }
    }
</script>

</body>
</html>
