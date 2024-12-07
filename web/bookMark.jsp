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
            word-wrap: break-word;
            white-space: normal;
            max-width: 200px;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        th {
            background-color: #f2f2f2;
            font-weight: bold;
        }

        .centered-container {
            max-width: 1500px;
            margin: 0 auto;
            overflow-x: auto;
        }

        .menu a {
            margin-right: 10px;
        }

        .bookmark-container {
            margin-top: 30px;
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
    <a href="/bookmarkdetail">즐겨찾기 보기</a>    |
    <a href="/bookMark.jsp">즐겨찾기 그룹 관리</a>
</div>

<!-- 북마크 그룹 추가 버튼 및 테이블 -->
<div class="bookmark-container">
    <h2>북마크 그룹 관리</h2>
    <button type="button" onclick="showBookmarkInput()">북마크 그룹 이름 추가</button>

    <!-- 북마크 그룹 입력 폼 -->
    <div id="bookmarkInputForm" style="display: none;">
        <input type="text" id="bookmarkName" placeholder="북마크 그룹 이름 입력">
        <input type="text" id="bookOrderNum" placeholder="순서 입력">
        <button type="button" onclick="saveBookmarkGroup()">저장</button>
        <button type="button" onclick="cancelBookmarkInput()">취소</button>
    </div>

    <div class="centered-container">
        <table id="bookmarkTable">
            <thead>
            <tr>
                <th>ID</th>
                <th>북마크 이름</th>
                <th>순서</th>
                <th>등록일자</th>
                <th>수정일자</th>
                <th>비고</th>
            </tr>
            </thead>
            <tbody>
            <!-- 북마크 그룹 데이터가 여기에 추가될 예정 -->
            </tbody>
        </table>
    </div>
</div>

<script>
    // 북마크 그룹 이름과 순서 입력 폼 표시
    function showBookmarkInput() {
        document.getElementById('bookmarkInputForm').style.display = 'block';
    }

    // 취소 버튼 클릭 시 폼 숨기기
    function cancelBookmarkInput() {
        document.getElementById('bookmarkInputForm').style.display = 'none';
    }

    // 북마크 그룹 저장 함수
    function saveBookmarkGroup() {
        const bookmarkName = document.getElementById('bookmarkName').value;
        const bookOrderNum = document.getElementById('bookOrderNum').value;
        console.log(bookmarkName);

        if (!bookmarkName || !bookOrderNum) {
            alert('북마크 이름과 순서를 입력하세요.');
            return;
        }

        // AJAX 요청을 통해 서버에 북마크 그룹 저장
        const xhr = new XMLHttpRequest();
        xhr.open('POST', '/bookmarksave', true);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onload = function() {
            if (xhr.status === 200) {
                alert('북마크 그룹이 추가되었습니다.');
                // 새로 추가된 북마크 목록을 다시 불러오기
                loadBookmarkList();
                cancelBookmarkInput(); // 입력 폼 숨기기
            } else {
                alert('북마크 그룹 추가 실패');
            }
        };

        xhr.send('bookmark_name=' + encodeURIComponent(bookmarkName) + '&order_num=' + encodeURIComponent(bookOrderNum));
    }

    function loadBookmarkList() {
        const xhr = new XMLHttpRequest();
        xhr.open('GET', '/bookmarksave', true);
        xhr.onload = function() {
            if (xhr.status === 200) {
                console.log(xhr.responseText);  // 서버 응답 확인
                const bookmarkList = JSON.parse(xhr.responseText);
                console.log(bookmarkList);  // bookmarkList의 내용 확인

                const tableBody = document.getElementById('bookmarkTable').getElementsByTagName('tbody')[0];
                tableBody.innerHTML = ''; // 기존 목록 초기화

                // 북마크 그룹 목록을 테이블에 추가
                bookmarkList.forEach(function(bookmark) {
                    console.log(bookmark);  // 각 bookmark 객체 확인
                    const row = tableBody.insertRow();
                    row.insertCell(0).innerText = bookmark.id || 'ID 없음';
                    row.insertCell(1).innerText = bookmark.bookmark_name;

                    // 순서가 null인 경우 빈 문자열로 처리
                    row.insertCell(2).innerText = bookmark.order_num ? bookmark.order_num : '';

                    // created_at 및 updated_at이 null인 경우 빈 문자열로 처리
                    row.insertCell(3).innerText = bookmark.created_at && bookmark.created_at !== 'null' ? bookmark.created_at : '';
                    row.insertCell(4).innerText = (bookmark.updated_at !== 'null' && bookmark.updated_at !== null) ? bookmark.updated_at : '';

                    row.insertCell(5).innerHTML = '<button onclick="deleteBookmark(' + bookmark.id + ')">삭제</button>';
                });
            }
        };
        xhr.send();
    }

    // 북마크 삭제 함수
    function deleteBookmark(bookmarkId) {
        if (confirm('정말 삭제하시겠습니까?')) {
            const xhr = new XMLHttpRequest();
            console.log(bookmarkId);
            xhr.open('DELETE', '/bookmarksave?bookmark_id=' + bookmarkId, true); // DELETE 메서드 사용
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
            xhr.onload = function() {
                if (xhr.status === 200) {
                    alert('북마크 그룹이 삭제되었습니다.');
                    loadBookmarkList(); // 삭제 후 목록 갱신
                } else {
                    alert('삭제 실패');
                }
            };
            xhr.send();
        }
    }

    // 페이지 로드 시 북마크 목록을 자동으로 불러오기
    window.onload = loadBookmarkList;
</script>

</body>
</html>
