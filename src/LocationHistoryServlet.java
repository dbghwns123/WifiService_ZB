import dto.LocationHistory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LocationHistoryServlet")
public class LocationHistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/location_history";
    private static final String DB_USER = "testuser1";
    private static final String DB_PASSWORD = "zerobase";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<LocationHistory> locationHistoryList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, x_coordinate, y_coordinate, query_date FROM location_history ORDER BY id DESC ")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                double xCoordinate = rs.getDouble("x_coordinate");
                double yCoordinate = rs.getDouble("y_coordinate");
                String queryDate = rs.getString("query_date");

                locationHistoryList.add(new LocationHistory(id, xCoordinate, yCoordinate, queryDate));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // JSP로 위치 데이터를 전달
        request.setAttribute("locationHistoryList", locationHistoryList);
        request.getRequestDispatcher("/locationHistory.jsp").forward(request, response);
    }

//    protected void doPost(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        String idParam = request.getParameter("id");
//
//        if (idParam != null) {
//            int id = Integer.parseInt(idParam);
//
//            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM location_history WHERE id = ?")) {
//
//                stmt.setInt(1, id);
//                stmt.executeUpdate();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");

        if (idParam != null) {
            int id = Integer.parseInt(idParam);

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // 1. 삭제 작업
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM location_history WHERE id = ?")) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }

                // 2. 테이블에 데이터가 없으면 AUTO_INCREMENT 값 초기화
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM location_history");
                    if (rs.next() && rs.getInt("count") == 0) {
                        stmt.execute("ALTER TABLE location_history AUTO_INCREMENT = 1");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 삭제 후 목록 페이지로 리다이렉트
        response.sendRedirect("LocationHistoryServlet");
    }
}

