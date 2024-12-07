import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject; // JSON 파싱 라이브러리

@WebServlet("/saveLocation")
public class SaveLocationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // JDBC URL, 사용자명, 비밀번호 설정
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/location_history";
    private static final String DB_USER = "testuser1";
    private static final String DB_PASSWORD = "zerobase";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 요청 본문에서 JSON 데이터 읽기
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            sb.append(line);
        }

        try {
            // JSON 객체로 파싱
            JSONObject json = new JSONObject(sb.toString());
            double xCoordinate = json.getDouble("x_coordinate");
            double yCoordinate = json.getDouble("y_coordinate");

            // 데이터베이스에 위치 정보 저장
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO location_history (x_coordinate, y_coordinate) VALUES (?, ?)")) {
                stmt.setDouble(1, xCoordinate);
                stmt.setDouble(2, yCoordinate);
                stmt.executeUpdate();
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}


