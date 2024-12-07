
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@WebServlet("/wifi")
public class WifiServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/location_history";
    private static final String DB_USER = "testuser1";
    private static final String DB_PASSWORD = "zerobase";


    @Override
    public void init() throws ServletException {
        // 서블릿 초기화 시 한 번만 TRUNCATE 실행
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                conn.createStatement().executeUpdate("TRUNCATE TABLE wifi_info");
                conn.createStatement().executeUpdate("ALTER TABLE wifi_info ADD CONSTRAINT unique_wifi_name UNIQUE (main_nm);");

                System.out.println("wifi_info 테이블 초기화 완료!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 인코딩
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String apiKey = "6b494b70436a6f6d313330577752636e";
        StringBuilder result = new StringBuilder();
        int startIndex = 1;  // 시작 인덱스
        int limit = 1000;  // 한 번에 요청할 데이터 개수
        boolean moreData = true;
        int totalSaved = 0;

        try {
            while (moreData) {
                StringBuilder urlBuilder = new StringBuilder("http://openapi.seoul.go.kr:8088");
                urlBuilder.append("/").append(URLEncoder.encode(apiKey, "UTF-8"));
                urlBuilder.append("/").append("json");
                urlBuilder.append("/").append("TbPublicWifiInfo");
                urlBuilder.append("/").append(startIndex);  // 시작 인덱스
                urlBuilder.append("/").append(startIndex + limit - 1);  // 끝 인덱스 (startIndex + 999)

                URL url = new URL(urlBuilder.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");

                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 299
                                ? conn.getInputStream() : conn.getErrorStream()));

                result.setLength(0);  // 이전 결과를 지우고 새로 읽음
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
                conn.disconnect();

                int start = result.indexOf("\"row\":[");
                if (start == -1) {
                    response.getWriter().println("데이터가 없습니다.");
                    break;  // 더 이상 데이터가 없으면 종료
                }
                int end = result.indexOf("]", start);
                String rowArrayString = result.substring(start + 7, end);
                String[] wifiArray = rowArrayString.split("},\\{");

                // DB에 저장
                saveWifiDataToDB(wifiArray, response);

                totalSaved += wifiArray.length;

                // 다음 요청을 위한 인덱스 증가
                startIndex += limit;

                // 1000개 미만의 데이터가 반환되면 종료
                if (wifiArray.length < limit) {
                    moreData = false;
                }
            }

            response.getWriter().println("<html><body>");
            response.getWriter().println("<h3>" + totalSaved + "개의 WIFI 정보를 정상적으로 저장하였습니다.</h3>");
            response.getWriter().println("<a href='index.jsp'><button>홈으로 돌아가기</button></a>");
            response.getWriter().println("</body></html>");

            System.out.println("모든 데이터 수집 완료");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("서버에서 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void saveWifiDataToDB(String[] wifiArray, HttpServletResponse response) {
        String insertSql =
                "INSERT INTO wifi_info (wifi_mgr_no, wrdofc, main_nm, adres1, adres2, instl_floor, instl_ty, instl_mby, " +
                        "svc_se, cmcwr, cnstc_year, inout_door, remars3, lat, lnt, work_dttm, distance) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {

            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {



                for (String wifiData : wifiArray) {


                    pstmt.setString(1, extractField(wifiData, "X_SWIFI_MGR_NO"));
                    pstmt.setString(2, extractField(wifiData, "X_SWIFI_WRDOFC"));
                    pstmt.setString(3, extractField(wifiData, "X_SWIFI_MAIN_NM"));
                    pstmt.setString(4, extractField(wifiData, "X_SWIFI_ADRES1"));
                    pstmt.setString(5, extractField(wifiData, "X_SWIFI_ADRES2"));
                    pstmt.setString(6, extractField(wifiData, "X_SWIFI_INSTL_FLOOR"));
                    pstmt.setString(7, extractField(wifiData, "X_SWIFI_INSTL_TY"));
                    pstmt.setString(8, extractField(wifiData, "X_SWIFI_INSTL_MBY"));
                    pstmt.setString(9, extractField(wifiData, "X_SWIFI_SVC_SE"));
                    pstmt.setString(10, extractField(wifiData, "X_SWIFI_CMCWR"));
                    pstmt.setString(11, extractField(wifiData, "X_SWIFI_CNSTC_YEAR"));
                    pstmt.setString(12, extractField(wifiData, "X_SWIFI_INOUT_DOOR"));
                    pstmt.setString(13, extractField(wifiData, "X_SWIFI_REMARS3"));
                    pstmt.setDouble(14, parseDouble(extractField(wifiData, "LAT")));
                    pstmt.setDouble(15, parseDouble(extractField(wifiData, "LNT")));
                    pstmt.setString(16, extractField(wifiData, "WORK_DTTM"));
                    pstmt.setDouble(17, 0.0);

                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                System.out.println("DB 저장 완료!");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            try {
                response.getWriter().println("MySQL 드라이버 오류: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private String extractField(String data, String fieldName) {
        String key = "\"" + fieldName + "\":\"";
        int startIndex = data.indexOf(key);
        if (startIndex == -1) return "";
        startIndex += key.length();
        int endIndex = data.indexOf("\"", startIndex);
        return endIndex == -1 ? "" : data.substring(startIndex, endIndex).replace("\\", "");
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
