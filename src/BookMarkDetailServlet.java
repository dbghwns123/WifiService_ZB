import dto.Bookmark;

import javax.servlet.RequestDispatcher;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/bookmarkdetail")
public class BookMarkDetailServlet extends HttpServlet {

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT b.id, b.bookmark_name, w.main_nm, bw.created_at " +
                    "FROM bookmark_wifi bw " +
                    "JOIN bookmark b ON bw.bookmark_id = b.id " +
                    "JOIN wifi_info w ON bw.wifi_info_id = w.id";

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                List<Bookmark> bookmarks = new ArrayList<>();

                while (rs.next()) {
                    Bookmark bookmark = new Bookmark();
                    bookmark.setId(rs.getInt("id"));
                    bookmark.setBookmarkName(rs.getString("bookmark_name"));
                    bookmark.setWifiName(rs.getString("main_nm"));
                    bookmark.setCreatedAt(rs.getTimestamp("created_at"));
                    bookmarks.add(bookmark);
                }

                request.setAttribute("bookmarks", bookmarks);
                System.out.println(bookmarks);
                RequestDispatcher dispatcher = request.getRequestDispatcher("/bookMarkDetail.jsp");
                dispatcher.forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("데이터베이스 오류: " + e.getMessage());
        }
    }


    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookmarkIdParam = request.getParameter("bookmark_id");
        String wifiNameParam = request.getParameter("main_nm");

        if (bookmarkIdParam != null && !bookmarkIdParam.isEmpty() && wifiNameParam != null && !wifiNameParam.isEmpty()) {
            int bookmarkId = Integer.parseInt(bookmarkIdParam);

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // wifiName으로 wifi_info_id를 찾는 SQL 쿼리
                String getWifiInfoIdSql = "SELECT id FROM wifi_info WHERE main_nm = ?";

                String wifiInfoId = null;
                try (PreparedStatement pstmt = conn.prepareStatement(getWifiInfoIdSql)) {
                    pstmt.setString(1, wifiNameParam);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            wifiInfoId = rs.getString("id"); // wifi_info_id 가져오기
                        }
                    }
                }

                if (wifiInfoId != null) {
                    // bookmark_id와 wifi_info_id가 일치하는 경우 삭제 쿼리 실행
                    String deleteSql = "DELETE FROM bookmark_wifi WHERE bookmark_id = ? AND wifi_info_id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                        pstmt.setInt(1, bookmarkId);
                        pstmt.setString(2, wifiInfoId);

                        int rowsAffected = pstmt.executeUpdate();
                        if (rowsAffected > 0) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().println("북마크가 삭제되었습니다.");
                        } else {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            response.getWriter().println("해당 북마크를 찾을 수 없습니다.");
                        }
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().println("Wi-Fi 정보가 존재하지 않습니다.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("데이터베이스 오류: " + e.getMessage());
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("유효하지 않은 요청입니다. bookmark_id와 main_nm을 확인하세요.");
        }
    }
}
