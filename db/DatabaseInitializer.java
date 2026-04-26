package db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {

        String createTable = "CREATE TABLE IF NOT EXISTS appliances (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "appliance_name TEXT NOT NULL," +
                "power_watts REAL NOT NULL," +
                "hours_used REAL NOT NULL," +
                "rate_per_unit REAL NOT NULL," +
                "duration TEXT NOT NULL," +
                "solar_wattage REAL," +
                "sunlight_hours REAL," +
                "created_at TEXT DEFAULT (datetime('now'))" +
                ");";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTable);
            System.out.println("Table ready.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}