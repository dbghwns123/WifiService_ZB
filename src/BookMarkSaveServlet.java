
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/bookmarksave")
public class BookMarkSaveServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/location_history";
    private static final String DB_USER = "testuser1";
    private static final String DB_PASSWORD = "zerobase";

    // 드라이버 로드
    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");  // MySQL JDBC 드라이버 로드
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("MySQL JDBC Driver 로드 실패", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookmarkName = request.getParameter("bookmark_name");
        String bookOrderNum = request.getParameter("order_num");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 1. 북마크 추가
            int bookmarkId = createBookmark(conn, bookmarkName, bookOrderNum);

            response.getWriter().println("북마크가 성공적으로 생성되었습니다.");
            System.out.println("북마크가 성공적으로 생성되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("북마크 생성 중 오류 발생: " + e.getMessage());
        }
    }

    // 북마크 이름을 받아서 북마크를 생성하고 ID를 반환하는 메서드
    private int createBookmark(Connection conn, String bookmarkName, String bookOrderNum) throws SQLException {
        int bookmarkId = -1;

        // 북마크가 이미 존재하는지 확인 (이제 order_num도 체크)
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id FROM bookmark WHERE bookmark_name = ? AND order_num = ?")) {
            pstmt.setString(1, bookmarkName);
            pstmt.setString(2, bookOrderNum);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 이미 존재하는 북마크가 있으면 해당 ID 반환
                    bookmarkId = rs.getInt("id");
                }
            }
        }

        // 북마크가 없으면 새로 생성
        if (bookmarkId == -1) {
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO bookmark (bookmark_name, order_num) VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, bookmarkName);
                pstmt.setString(2, bookOrderNum);
                pstmt.executeUpdate();

                // 새로 생성된 북마크 ID 반환
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        bookmarkId = rs.getInt(1);
                    }
                }
            }
        }

        return bookmarkId;
    }

    // Delete
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookmarkId = request.getParameter("bookmark_id");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 1. 북마크와 관련된 모든 Wi-Fi 정보 삭제
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM bookmark_wifi WHERE bookmark_id = ?")) {
                pstmt.setInt(1, Integer.parseInt(bookmarkId));
                pstmt.executeUpdate();
            }

            // 2. 북마크 자체 삭제
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM bookmark WHERE id = ?")) {
                pstmt.setInt(1, Integer.parseInt(bookmarkId));
                pstmt.executeUpdate();
            }

            response.getWriter().println("북마크가 성공적으로 삭제되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("북마크 삭제 중 오류 발생: " + e.getMessage());
        }
    }

    // Get
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 1. 모든 북마크 정보 조회
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT id, bookmark_name, order_num, created_at, updated_at FROM bookmark")) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    // 결과 리스트를 JSON 형식으로 변환
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    StringBuilder jsonResponse = new StringBuilder("[");

                    while (rs.next()) {
                        if (jsonResponse.length() > 1) {
                            jsonResponse.append(",");
                        }
                        jsonResponse.append(String.format("{\"id\": %d, \"bookmark_name\": \"%s\", \"order_num\": \"%s\", \"created_at\": \"%s\", \"updated_at\": \"%s\"}",
                                rs.getInt("id"),
                                rs.getString("bookmark_name"),
                                rs.getString("order_num"),
                                rs.getString("created_at"),
                                rs.getString("updated_at")));
                    }
                    jsonResponse.append("]");

                    // JSON 데이터 반환
                    response.getWriter().write(jsonResponse.toString());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("북마크 조회 중 오류 발생: " + e.getMessage());
        }
    }
}
