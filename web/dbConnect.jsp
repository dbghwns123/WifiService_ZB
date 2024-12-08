
<%@ page import="java.sql.Connection, java.sql.DriverManager, java.sql.SQLException" %>
<%
    // Database credentials
    String jdbcUrl = "jdbc:mariadb://localhost:3306/location_history";
    String username = "testuser1";
    String password = "zerobase";

    Connection connection = null;
    try {
        Class.forName("org.mariadb.jdbc.Driver");

        connection = DriverManager.getConnection(jdbcUrl, username, password);

        if (connection != null) {
            out.println("Database connection successful!");
        } else {
            out.println("Failed to connect to the database.");
        }
    } catch (ClassNotFoundException e) {
        out.println("JDBC Driver not found: " + e.getMessage());
    } catch (SQLException e) {
        out.println("Database connection error: " + e.getMessage());
    } finally {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                out.println("Error closing the connection: " + e.getMessage());
            }
        }
    }
%>
