import dto.WifiDetails;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.*;

@WebServlet("/wifiDetails")
public class WifiDetailsServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String wifiName = request.getParameter("wifiName");
        if (wifiName == null || wifiName.isEmpty()) {
            response.getWriter().println("와이파이 이름을 제공해야 합니다.");
            return;
        }

        // 데이터베이스 연결
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 1. 와이파이 상세 정보 조회
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT id, wifi_mgr_no, wrdofc, main_nm, adres1, adres2, instl_floor, instl_ty, instl_mby, svc_se, cmcwr, cnstc_year, inout_door, remars3, lat, lnt, work_dttm, distance "
                            +
                            "FROM wifi_info WHERE main_nm = ?")) {
                pstmt.setString(1, wifiName);  // 파라미터로 전달된 wifiName을 사용

                try (ResultSet rs = pstmt.executeQuery()) {
                    // 결과가 없으면 오류 처리
                    if (!rs.next()) {
                        response.getWriter().println("해당 와이파이 정보가 없습니다.");
                        return;
                    }

                    // 와이파이 정보 객체 생성
                    WifiDetails wifiDetails = new WifiDetails();
                    wifiDetails.setId(rs.getInt("id"));
                    wifiDetails.setMgrNo(rs.getString("wifi_mgr_no"));
                    wifiDetails.setWrdofc(rs.getString("wrdofc"));
                    wifiDetails.setWifiName(rs.getString("main_nm"));
                    wifiDetails.setAddress(rs.getString("adres1") + " " + rs.getString("adres2"));
                    wifiDetails.setFloor(rs.getString("instl_floor"));
                    wifiDetails.setInstallationType(rs.getString("instl_ty"));
                    wifiDetails.setInstallationAgency(rs.getString("instl_mby"));
                    wifiDetails.setServiceType(rs.getString("svc_se"));
                    wifiDetails.setCmcwr(rs.getString("cmcwr"));
                    wifiDetails.setInstallationYear(rs.getString("cnstc_year"));
                    wifiDetails.setInoutDoor(rs.getString("inout_door"));
                    wifiDetails.setRemarks(rs.getString("remars3"));
                    wifiDetails.setLatitude(rs.getString("lat"));
                    wifiDetails.setLongitude(rs.getString("lnt"));
                    wifiDetails.setWorkDttm(rs.getString("work_dttm"));
                    wifiDetails.setDistance(rs.getString("distance"));

                    // 데이터를 request 객체에 저장하여 JSP로 전달
                    request.setAttribute("wifiDetails", wifiDetails);

                    // wifiDetails.jsp로 포워딩
                    request.getRequestDispatcher("/wifiDetails.jsp").forward(request, response);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("와이파이 정보 조회 중 오류 발생: " + e.getMessage());
        }
    }
}
