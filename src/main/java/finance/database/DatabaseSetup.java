package finance.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    public static void initialise() {
        try (Connection conn = Database.getConnection(); Statement statement = conn.createStatement()) {
            statement.executeUpdate("""
                                        CREATE TABLE IF NOT EXISTS users (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                        username TEXT NOT NULL UNIQUE, hash TEXT NOT NULL,
                                        cash NUMERIC NOT NULL DEFAULT 10000.00
                    )""");
            statement.executeUpdate("""
                                        CREATE TABLE IF NOT EXISTS transactions (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                                            user_id INTEGER NOT NULL,
                                            symbol TEXT NOT NULL,
                                            company_name TEXT NOT NULL,
                                            shares INTEGER NOT NULL CHECK(shares > 0),
                                            price NUMERIC NOT NULL CHECK(price >= 0),
                                            transaction_type TEXT NOT NULL CHECK(transaction_type IN ('buy', 'sell')),
                                            timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                                            FOREIGN KEY (user_id) REFERENCES users(id)
                    )""");

            statement.executeUpdate("""
                                       CREATE TABLE IF NOT EXISTS holdings (
                                           user_id INTEGER NOT NULL,
                                           symbol TEXT NOT NULL,
                                           company_name TEXT NOT NULL,
                                           shares INTEGER NOT NULL CHECK(shares >= 0),
                                           PRIMARY KEY (user_id, symbol),
                                           FOREIGN KEY (user_id) REFERENCES users(id)
                    )""");

            System.out.println("Database schema initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
