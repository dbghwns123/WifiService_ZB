
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

@WebServlet("/bookmark")
public class BookMarkServlet extends HttpServlet {

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
        String wifiMgrNo = request.getParameter("wifi_info_id");  // wifiDetails.mgrNo
        String bookmarkName = request.getParameter("bookmark_name");


        System.out.println(wifiMgrNo);
        System.out.println(bookmarkName);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 1. wifi_mgrNo를 통해 wifi_info_id를 찾아오기
            int wifiInfoId = getWifiInfoIdByMgrNo(conn, wifiMgrNo);

            if (wifiInfoId == -1) {
                response.getWriter().println("해당 Wi-Fi 정보를 찾을 수 없습니다.");
                return;
            }

            // 2. 북마크 추가 (있으면 기존 ID 가져오기)
            int bookmarkId = getOrCreateBookmarkId(conn, bookmarkName);

            // 3. 북마크와 Wi-Fi 매핑
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO bookmark_wifi (bookmark_id, wifi_info_id) VALUES (?, ?)")) {
                pstmt.setInt(1, bookmarkId);
                pstmt.setInt(2, wifiInfoId);
                pstmt.executeUpdate();
                response.getWriter().println("Wi-Fi가 북마크에 추가되었습니다.");
                System.out.println("Wi-Fi가 북마크에 추가되었습니다.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("북마크 추가 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String wifiInfoId = request.getParameter("wifi_info_id");
        String bookmarkId = request.getParameter("bookmark_id");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM bookmark_wifi WHERE bookmark_id = ? AND wifi_info_id = ?")) {
            pstmt.setInt(1, Integer.parseInt(bookmarkId));
            pstmt.setInt(2, Integer.parseInt(wifiInfoId));
            pstmt.executeUpdate();
            response.getWriter().println("북마크에서 Wi-Fi가 삭제되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("북마크 삭제 중 오류 발생: " + e.getMessage());
        }
    }

    // mgrNo를 사용하여 wifi_info_id 찾기
    private int getWifiInfoIdByMgrNo(Connection conn, String mgrNo) throws SQLException {
        int wifiInfoId = -1;

        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id FROM wifi_info WHERE wifi_mgr_no = ?")) {
            pstmt.setString(1, mgrNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    wifiInfoId = rs.getInt("id");
                }
            }
        }
        return wifiInfoId;
    }

    private int getOrCreateBookmarkId(Connection conn, String bookmarkName) throws SQLException {
        int bookmarkId = -1;

        // 기존 북마크 ID 조회
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id FROM bookmark WHERE bookmark_name = ?")) {
            pstmt.setString(1, bookmarkName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    bookmarkId = rs.getInt("id");
                }
            }
        }

        // 북마크 없으면 새로 생성
        if (bookmarkId == -1) {
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO bookmark (bookmark_name, created_at, updated_at) VALUES (?, NOW(), NOW())",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, bookmarkName);
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        bookmarkId = rs.getInt(1);
                    }
                }
            }
        }

        return bookmarkId;
    }
}
