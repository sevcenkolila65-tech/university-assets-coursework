package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBUtil {

    private DBUtil() {}

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found", e);
        }

        String url = System.getenv().getOrDefault(
                "ASSETS_DB_URL",
                "jdbc:mysql://localhost:3306/university_assets?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC"
        );

        String user = System.getenv().getOrDefault("ASSETS_DB_USER", "root");
        String password = System.getenv().getOrDefault("ASSETS_DB_PASSWORD", "");

        return DriverManager.getConnection(url, user, password);
    }
}