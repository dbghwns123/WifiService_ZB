
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/wifiNear")
public class WifiNearbyServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/location_history";
    private static final String DB_USER = "testuser1";
    private static final String DB_PASSWORD = "zerobase";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userLat = request.getParameter("lat");
        String userLng = request.getParameter("lng");

        if (userLat == null || userLng == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"위치 정보가 필요합니다.\"}");
            return;
        }

        double userLatDouble = Double.parseDouble(userLat);
        double userLngDouble = Double.parseDouble(userLng);

        System.out.println("User Latitude: " + userLat);
        System.out.println("User Longitude: " + userLng);

        List<JSONObject> wifiList = getNearbyWifi(userLatDouble, userLngDouble);

        JSONArray wifiArray = new JSONArray(wifiList);
        JSONObject result = new JSONObject();
        result.put("wifiData", wifiArray);

        System.out.println(result.toString());

        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(result.toString());
    }

    private List<JSONObject> getNearbyWifi(double userLat, double userLng) {
        List<JSONObject> wifiList = new ArrayList<>();
        String sql = "SELECT wifi_mgr_no, wrdofc, main_nm, adres1, adres2, instl_floor, instl_ty, instl_mby, svc_se, cmcwr, cnstc_year, inout_door, remars3, lat, lnt, work_dttm, " +
                "(6371 * acos(LEAST(1.0, COS(radians(37.5665)) * COS(radians(lat)) * COS(radians(lnt) - radians(126.978)) + SIN(radians(37.5665)) * SIN(radians(lat))))) AS distance " +
                "FROM wifi_info " +
                "WHERE (6371 * acos(LEAST(1.0, COS(radians(37.5665)) * COS(radians(lat)) * COS(radians(lnt) - radians(126.978)) + SIN(radians(37.5665)) * SIN(radians(lat))))) <= 10 " +
                "ORDER BY distance";

        try {
            // MySQL JDBC 드라이버를 명시적으로 로드
            Class.forName("org.mariadb.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                System.out.println(sql);


                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    JSONObject wifi = new JSONObject();
                    wifi.put("wifi_mgr_no", rs.getString("wifi_mgr_no"));
                    wifi.put("wrdofc", rs.getString("wrdofc"));
                    wifi.put("main_nm", rs.getString("main_nm"));
                    wifi.put("adres1", rs.getString("adres1"));
                    wifi.put("adres2", rs.getString("adres2"));
                    wifi.put("instl_floor", rs.getString("instl_floor"));
                    wifi.put("instl_ty", rs.getString("instl_ty"));
                    wifi.put("instl_mby", rs.getString("instl_mby"));
                    wifi.put("svc_se", rs.getString("svc_se"));
                    wifi.put("cmcwr", rs.getString("cmcwr"));
                    wifi.put("cnstc_year", rs.getString("cnstc_year"));
                    wifi.put("inout_door", rs.getString("inout_door"));
                    wifi.put("remars3", rs.getString("remars3"));
                    wifi.put("lat", rs.getDouble("lat"));
                    wifi.put("lnt", rs.getDouble("lnt"));
                    wifi.put("work_dttm", rs.getString("work_dttm"));
                    wifi.put("distance", rs.getDouble("distance"));
                    wifiList.add(wifi);
                }

                System.out.println("잘 호출되었습니다.");

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return wifiList;
    }
}
